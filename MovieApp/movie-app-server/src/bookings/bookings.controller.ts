import {BadRequestException, Controller, Get, Query} from '@nestjs/common';
import { BookingsService } from './bookings.service';

@Controller('bookings')
export class BookingsController {
  constructor(private readonly bookingsService: BookingsService) {}

  @Get()
  findAll() {
    return this.bookingsService.findAll();
  }

  @Get('upcoming')
  findUpcoming() {
    return this.bookingsService.findUpcoming();
  }

  @Get('past')
  findPast() {
    return this.bookingsService.findPast();
  }

  @Get('schedules/date')
  findSchedule(
    @Query('movie_id') movieId?: string,
    @Query('start_date') startDate?: string,
    @Query('end_date') endDate?: string,
  ) {
    if (movieId === null)   throw new BadRequestException("movie_id null");
    if (startDate === null) throw new BadRequestException("start_date null");
    if (endDate === null)   throw new BadRequestException("end_date null");
    return this.bookingsService.findSchedules(movieId, startDate, endDate);
  }

  // TODO: 하나의 Booking 에 다수의 Theater 가 연결되도록 DB 구조가 다시 구축되어야 함
  @Get('theater')
  findTheater(
      @Query('movie_id') movieId?: string
  ) {
    if (movieId === null) throw new BadRequestException("movie_id null");
    return this.bookingsService.findTheater(movieId)
  }

  @Get('schedules')
  findAllSchedules(@Query('movie_id') movieId?: string) {
    return this.bookingsService.findSchedules(movieId, undefined, undefined);
  }
}
