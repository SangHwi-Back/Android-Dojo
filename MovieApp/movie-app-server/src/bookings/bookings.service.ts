import {Injectable} from '@nestjs/common';
import {InjectRepository} from '@nestjs/typeorm';
import {Between, Repository} from 'typeorm';
import {Booking} from './entities/booking.entity';

@Injectable()
export class BookingsService {
  constructor(
      @InjectRepository(Booking)
      private readonly bookingsRepo: Repository<Booking>,
  ) {}

  findAll(): Promise<Booking[]> {
    return this.bookingsRepo.find();
  }

  findUpcoming(): Promise<Booking[]> {
    return this.bookingsRepo.find({ where: { isUpcoming: true } });
  }

  findPast(): Promise<Booking[]> {
    return this.bookingsRepo.find({ where: { isUpcoming: false } });
  }

  findSchedules(movieId: string, startDate: string, endDate: string): Promise<Booking[]> {
    return this.bookingsRepo.find({ where: {
        movie: { id: parseInt(movieId) },
        date: Between(startDate, endDate)
      }})
  }

  async saveBooking(booking: Booking): Promise<Booking> {
    // id는 서버가 발급한다 — 클라이언트가 보낸 값이 있어도 무시하고 새 레코드로 저장
    return this.bookingsRepo.save({ ...booking, id: undefined })
  }
}
