import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Booking } from './entities/booking.entity';
import { Seat } from '../seats/entities/seat.entity';
import { Ticket } from '../tickets/entities/ticket.entity';
import { BookingsService } from './bookings.service';
import { BookingsController } from './bookings.controller';

@Module({
  imports: [TypeOrmModule.forFeature([Booking, Seat, Ticket])],
  controllers: [BookingsController],
  providers: [BookingsService],
})
export class BookingsModule {}
