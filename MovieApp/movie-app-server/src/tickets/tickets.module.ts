import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Ticket } from './entities/ticket.entity';
import { TicketsService } from './tickets.service';
import { TicketsController } from './tickets.controller';
import { Booking } from '../bookings/entities/booking.entity';

@Module({
  imports: [TypeOrmModule.forFeature([Ticket, Booking])],
  providers: [TicketsService],
  controllers: [TicketsController],
  exports: [TicketsService, TypeOrmModule],
})
export class TicketsModule {}
