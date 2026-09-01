import { Injectable, NotFoundException, ConflictException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Ticket } from './entities/ticket.entity';
import { Booking } from '../bookings/entities/booking.entity';
import * as crypto from 'crypto';

@Injectable()
export class TicketsService {
  constructor(
    @InjectRepository(Ticket)
    private readonly ticketRepository: Repository<Ticket>,
  ) {}

  async createTicketByBookingId(bookingId: number): Promise<Ticket> {
    // Check if booking exists
    const booking = await this.ticketRepository.manager.getRepository(Booking).findOne({
      where: { id: bookingId }
    });
    if (!booking) {
      throw new NotFoundException(`Booking with ID ${bookingId} not found`);
    }

    // Check if ticket already exists for this booking
    const existingTicket = await this.ticketRepository.findOne({
      where: { booking: { id: bookingId } }
    });

    if (existingTicket) {
      throw new ConflictException(`Ticket already exists for booking ID ${bookingId}`);
    }

    // Create new ticket
    const ticket = new Ticket();
    ticket.booking = booking;
    ticket.qrCode = crypto.randomUUID();

    return this.ticketRepository.save(ticket);
  }
}