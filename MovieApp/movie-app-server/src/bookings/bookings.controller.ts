import { BadRequestException, Body, Controller, Get, Post, Query, Req, UseGuards } from '@nestjs/common';
import { BookingsService, CreateBookingDto } from './bookings.service';
import { FirebaseAuthGuard } from '../auth/firebase-auth.guard';
import { AdminAuthGuard } from '../auth/admin-auth.guard';

@UseGuards(FirebaseAuthGuard)
@Controller('bookings')
export class BookingsController {
  constructor(private readonly bookingsService: BookingsService) {}

  @Get()
  findAll(@Req() req: any) {
    return this.bookingsService.findAll(req.user.uid);
  }

  @Get('upcoming')
  findUpcoming(@Req() req: any) {
    return this.bookingsService.findUpcoming(req.user.uid);
  }

  @Get('past')
  findPast(@Req() req: any) {
    return this.bookingsService.findPast(req.user.uid);
  }

  @Get('schedules/date')
  findSchedule(
    @Query('movie_id') movieId?: string,
    @Query('start_date') startDate?: string,
    @Query('end_date') endDate?: string,
  ) {
    if (movieId === null)   throw new BadRequestException('movie_id null');
    if (startDate === null) throw new BadRequestException('start_date null');
    if (endDate === null)   throw new BadRequestException('end_date null');
    return this.bookingsService.findSchedules(movieId, startDate, endDate);
  }

  @Get('schedules')
  findAllSchedules(@Query('movie_id') movieId?: string) {
    return this.bookingsService.findSchedules(movieId, undefined, undefined);
  }

  @Get('my')
  findMy(@Req() req: any) {
    return this.bookingsService.findByUserUid(req.user.uid);
  }

  @Post()
  bookMovie(@Body() dto: CreateBookingDto) {
    return this.bookingsService.saveBooking(dto);
  }

  // 관리자 전용 엔드포인트
  @UseGuards(AdminAuthGuard)
  @Get('admin/all')
  findAllAdmin() {
    return this.bookingsService.findAllAdmin();
  }

  @UseGuards(AdminAuthGuard)
  @Get('admin/upcoming')
  findUpcomingAdmin() {
    return this.bookingsService.findUpcomingAdmin();
  }

  @UseGuards(AdminAuthGuard)
  @Get('admin/past')
  findPastAdmin() {
    return this.bookingsService.findPastAdmin();
  }
}
