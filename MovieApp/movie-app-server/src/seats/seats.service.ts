import {
  BadRequestException,
  ConflictException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Seat, SeatStatus } from './entities/seat.entity';

const DEFAULT_HOLD_TTL_MINUTES = 10;

@Injectable()
export class SeatsService {
  constructor(
    @InjectRepository(Seat)
    private readonly seatsRepo: Repository<Seat>,
  ) {}

  async findAll(theaterId: number, hall: string): Promise<Seat[]> {
    // 조회 시점에 만료된 HELD 좌석을 AVAILABLE 로 정리 (별도 스케줄러 없이 lazy expiry)
    await this.seatsRepo
      .createQueryBuilder()
      .update(Seat)
      .set({ status: SeatStatus.AVAILABLE, heldByUserId: null, heldUntil: null })
      .where('theater_id = :theaterId', { theaterId })
      .andWhere('hall = :hall', { hall })
      .andWhere('status = :held', { held: SeatStatus.HELD })
      .andWhere('held_until < :now', { now: new Date() })
      .execute();

    return this.seatsRepo.find({
      where: { theater: { id: theaterId }, hall },
      order: { rowIndex: 'ASC', columnIndex: 'ASC' },
    });
  }

  // 직접 상태를 지정하는 관리용 API. HELD 는 소유자(userId)+TTL이 필수라 여기선 허용하지 않음
  async updateStatus(id: number, status: SeatStatus): Promise<Seat> {
    if (status === SeatStatus.HELD) {
      throw new BadRequestException(
        'HELD 상태는 POST /seats/:id/hold 로만 설정할 수 있습니다 (userId, ttlMinutes 필요)',
      );
    }
    const seat = await this.seatsRepo.findOne({ where: { id } });
    if (!seat) throw new NotFoundException(`Seat #${id} not found`);
    seat.status = status;
    seat.heldByUserId = null;
    seat.heldUntil = null;
    return this.seatsRepo.save(seat);
  }

  // 좌석을 userId 명의로 TTL 동안 임시 잠금. 동시 요청 경쟁을 막기 위해 트랜잭션 + row lock 사용
  async holdSeat(
    id: number,
    userId: string,
    ttlMinutes: number = DEFAULT_HOLD_TTL_MINUTES,
  ): Promise<Seat> {
    return this.seatsRepo.manager.transaction(async (manager) => {
      const seat = await manager.findOne(Seat, {
        where: { id },
        lock: { mode: 'pessimistic_write' },
      });
      if (!seat) throw new NotFoundException(`Seat #${id} not found`);

      this.expireIfNeeded(seat);

      if (seat.status === SeatStatus.OCCUPIED) {
        throw new ConflictException(`Seat #${id} is already booked`);
      }
      if (seat.status === SeatStatus.DISABLED) {
        throw new BadRequestException(`Seat #${id} is disabled`);
      }
      if (seat.status === SeatStatus.HELD && seat.heldByUserId !== userId) {
        throw new ConflictException(`Seat #${id} is currently held by another user`);
      }

      seat.status = SeatStatus.HELD;
      seat.heldByUserId = userId;
      seat.heldUntil = new Date(Date.now() + ttlMinutes * 60 * 1000);
      return manager.save(seat);
    });
  }

  // 사용자가 선택을 취소할 때 — 자신이 잡은 홀드만 풀 수 있음
  async releaseSeat(id: number, userId: string): Promise<Seat> {
    return this.seatsRepo.manager.transaction(async (manager) => {
      const seat = await manager.findOne(Seat, {
        where: { id },
        lock: { mode: 'pessimistic_write' },
      });
      if (!seat) throw new NotFoundException(`Seat #${id} not found`);

      this.expireIfNeeded(seat);

      if (seat.status !== SeatStatus.HELD) {
        return seat; // 이미 풀려있으면 그대로 반환 (idempotent)
      }
      if (seat.heldByUserId !== userId) {
        throw new ForbiddenException(`Seat #${id} is held by another user`);
      }

      seat.status = SeatStatus.AVAILABLE;
      seat.heldByUserId = null;
      seat.heldUntil = null;
      return manager.save(seat);
    });
  }

  // 결제 확정 시 호출 — 본인이 홀드 중이고 만료 전이어야 확정 가능
  async confirmSeat(id: number, userId: string): Promise<Seat> {
    return this.seatsRepo.manager.transaction(async (manager) => {
      const seat = await manager.findOne(Seat, {
        where: { id },
        lock: { mode: 'pessimistic_write' },
      });
      if (!seat) throw new NotFoundException(`Seat #${id} not found`);

      this.expireIfNeeded(seat);

      if (seat.status !== SeatStatus.HELD || seat.heldByUserId !== userId) {
        throw new BadRequestException(
          `Seat #${id} has no active hold for this user — hold it again before confirming`,
        );
      }

      seat.status = SeatStatus.OCCUPIED;
      seat.heldByUserId = null;
      seat.heldUntil = null;
      return manager.save(seat);
    });
  }

  // 트랜잭션 내에서 읽어온 seat 객체를 그 자리에서 만료 처리(아직 save 하지 않은 상태)
  private expireIfNeeded(seat: Seat): void {
    if (
      seat.status === SeatStatus.HELD &&
      seat.heldUntil !== null &&
      seat.heldUntil < new Date()
    ) {
      seat.status = SeatStatus.AVAILABLE;
      seat.heldByUserId = null;
      seat.heldUntil = null;
    }
  }
}
