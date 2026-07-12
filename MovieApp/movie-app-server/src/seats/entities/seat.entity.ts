import { Entity, Column, PrimaryGeneratedColumn, ManyToOne, JoinColumn } from 'typeorm';
import { Theater } from '../../theaters/entities/theater.entity';

export enum SeatType {
  NORMAL = 'NORMAL',
  VIP = 'VIP',
}

export enum SeatStatus {
  AVAILABLE = 'AVAILABLE', // 예약 가능
  HELD = 'HELD',           // 누군가 결제 진행 중이라 TTL 동안 임시로 잠김
  OCCUPIED = 'OCCUPIED',   // 이미 확정된 예매로 채워짐
  DISABLED = 'DISABLED',   // 물리적으로 앉을 수 없는 자리 (고장, 임시 폐쇄 등)
}

@Entity('seats')
export class Seat {
  @PrimaryGeneratedColumn()
  id: number;

  @ManyToOne(() => Theater, (theater) => theater.seats, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'theater_id' })
  theater: Theater;

  @Column()
  hall: string; // Showtime.hall 과 동일한 문자열로 매칭 (예: "IMAX Hall 1")

  @Column()
  floor: number;

  @Column({ name: 'row_label' })
  rowLabel: string; // "A", "B", "C" ...

  @Column({ name: 'row_index' })
  rowIndex: number; // 0, 1, 2 ... 행 정렬/렌더링 순서

  @Column({ name: 'column_index' })
  columnIndex: number; // 1, 2, 3 ... 행 내 좌석 순서(가로 위치)

  @Column({ type: 'enum', enum: SeatType, default: SeatType.NORMAL, name: 'seat_type' })
  seatType: SeatType;

  @Column({ name: 'has_aisle_after_column', default: false })
  hasAisleAfterColumn: boolean; // 이 좌석 오른쪽에 통로

  @Column({ name: 'has_aisle_after_row', default: false })
  hasAisleAfterRow: boolean; // 이 행 다음에 통로

  @Column({ type: 'enum', enum: SeatStatus, default: SeatStatus.AVAILABLE })
  status: SeatStatus;

  // HELD 상태일 때만 의미 있는 필드 — 누가, 언제까지 잠갔는지
  @Column({ name: 'held_by_user_id', nullable: true })
  heldByUserId: string | null;

  @Column({ name: 'held_until', type: 'timestamp', nullable: true })
  heldUntil: Date | null;
}
