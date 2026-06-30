import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Between, Repository } from 'typeorm';
import { Booking } from './entities/booking.entity';

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
}
