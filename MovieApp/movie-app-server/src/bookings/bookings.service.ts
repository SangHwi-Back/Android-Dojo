import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Between, In, Repository } from 'typeorm';
import { Booking } from './entities/booking.entity';
import { Seat } from '../seats/entities/seat.entity';

export interface CreateBookingDto {
  movie: { id: number };
  theater: { id: number };
  date: string;
  time: string;
  seatIds: number[];
  isUpcoming?: boolean;
}

@Injectable()
export class BookingsService {
  constructor(
      @InjectRepository(Booking)
      private readonly bookingsRepo: Repository<Booking>,
      @InjectRepository(Seat)
      private readonly seatsRepo: Repository<Seat>,
  ) {}

  findAll(): Promise<Booking[]> {
    return this.bookingsRepo.find({ relations: { seats: true } });
  }

  findUpcoming(): Promise<Booking[]> {
    return this.bookingsRepo.find({ where: { isUpcoming: true }, relations: { seats: true } });
  }

  findPast(): Promise<Booking[]> {
    return this.bookingsRepo.find({ where: { isUpcoming: false }, relations: { seats: true } });
  }

  findSchedules(movieId: string, startDate: string, endDate: string): Promise<Booking[]> {
    return this.bookingsRepo.find({
      where: {
        movie: { id: parseInt(movieId) },
        ...(startDate && endDate ? { date: Between(startDate, endDate) } : {}),
      },
      relations: { seats: true },
    });
  }

  async saveBooking(dto: CreateBookingDto): Promise<Booking> {
    const seats = await this.seatsRepo.find({ where: { id: In(dto.seatIds) } });
    const booking = this.bookingsRepo.create({
      movie: { id: dto.movie.id },
      theater: { id: dto.theater.id },
      date: dto.date,
      time: dto.time,
      seats,
      isUpcoming: dto.isUpcoming ?? true,
    });
    return this.bookingsRepo.save(booking);
  }
}
