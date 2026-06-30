import {BadRequestException, Controller, Get, HttpException, HttpStatus, Query} from '@nestjs/common';
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

  @Get('schedule')
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
}
