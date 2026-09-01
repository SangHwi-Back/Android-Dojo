import { Controller, Post, Param, ParseIntPipe } from '@nestjs/common';
import { TicketsService } from './tickets.service';
@Controller('tickets')
export class TicketsController {
  constructor(private readonly ticketsService: TicketsService) {}

  @Post(':bookingId')
  async createTicketByBookingId(@Param('bookingId', ParseIntPipe) bookingId: number) {
    return this.ticketsService.createTicketByBookingId(bookingId);
  }
}
