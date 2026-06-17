import { Entity, Column, PrimaryColumn, OneToMany } from 'typeorm';
import { Showtime } from '../../showtimes/entities/showtime.entity';
import { Booking } from '../../bookings/entities/booking.entity';

@Entity('movies')
export class Movie {
  @PrimaryColumn()
  id: number;

  @Column()
  title: string;

  @Column()
  duration: string;

  @Column('decimal', { precision: 3, scale: 1 })
  rating: number;

  @Column({ name: 'release_date' })
  releaseDate: string;

  @Column('text', { array: true })
  genres: string[];

  @Column('text')
  description: string;

  @Column({ name: 'poster_url', nullable: true })
  posterUrl: string;

  @Column({ name: 'backdrop_url', nullable: true })
  backdropUrl: string;

  @Column({ name: 'is_now_playing', default: false })
  isNowPlaying: boolean;

  @Column({ name: 'is_coming_soon', default: false })
  isComingSoon: boolean;

  @Column({ name: 'is_featured', default: false })
  isFeatured: boolean;

  @OneToMany(() => Showtime, (showtime) => showtime.movie)
  showtimes: Showtime[];

  @OneToMany(() => Booking, (booking) => booking.movie)
  bookings: Booking[];
}
