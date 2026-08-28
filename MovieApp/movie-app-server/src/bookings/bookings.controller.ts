import {BadRequestException, Body, Controller, Get, Post, Query, Req, UseGuards} from '@nestjs/common';
import { BookingsService, CreateBookingDto } from './bookings.service';
import { FirebaseAuthGuard } from '../auth/firebase-auth.guard';

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

  @Get('schedules')
  findAllSchedules(@Query('movie_id') movieId?: string) {
    return this.bookingsService.findSchedules(movieId, undefined, undefined);
  }

  @UseGuards(FirebaseAuthGuard)
  @Get('my')
  findMy(@Req() req: any) {
    return this.bookingsService.findByUserUid(req.user.uid);
  }

  @Post()
  bookMovie(@Body() dto: CreateBookingDto) {
    return this.bookingsService.saveBooking(dto)
  }
}
