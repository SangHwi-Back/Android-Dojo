import {
  Column,
  CreateDateColumn,
  Entity,
  JoinColumn,
  OneToOne,
  PrimaryGeneratedColumn,
} from 'typeorm';
import { Booking } from '../../bookings/entities/booking.entity';

@Entity('tickets')
export class Ticket {
  @PrimaryGeneratedColumn()
  id: number;

  @OneToOne(() => Booking, { eager: true })
  @JoinColumn({ name: 'booking_id' })
  booking: Booking;

  @Column({ name: 'qr_code' })
  qrCode: string;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;
}
