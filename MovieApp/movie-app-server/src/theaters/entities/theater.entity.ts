import { Entity, Column, PrimaryColumn, OneToMany } from 'typeorm';
import { Showtime } from '../../showtimes/entities/showtime.entity';
import { Booking } from '../../bookings/entities/booking.entity';

@Entity('theaters')
export class Theater {
  @PrimaryColumn()
  id: number;

  @Column()
  name: string;

  @Column()
  address: string;

  @Column('decimal', { precision: 4, scale: 1, name: 'distance_km' })
  distanceKm: number;

  @OneToMany(() => Showtime, (showtime) => showtime.theater)
  showtimes: Showtime[];

  @OneToMany(() => Booking, (booking) => booking.theater)
  bookings: Booking[];
}
