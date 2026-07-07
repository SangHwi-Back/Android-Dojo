import { Entity, Column, PrimaryGeneratedColumn, ManyToOne, JoinColumn } from 'typeorm';
import { Movie } from '../../movies/entities/movie.entity';
import { Theater } from '../../theaters/entities/theater.entity';

@Entity('bookings')
export class Booking {
  @PrimaryGeneratedColumn()
  id: number;

  @ManyToOne(() => Movie, (movie) => movie.bookings, { eager: true })
  @JoinColumn({ name: 'movie_id' })
  movie: Movie;

  @ManyToOne(() => Theater, (theater) => theater.bookings, { eager: true })
  @JoinColumn({ name: 'theater_id' })
  theater: Theater;

  @Column()
  date: string;

  @Column()
  time: string;

  @Column('text', { array: true })
  seats: string[];

  @Column({ name: 'is_upcoming', default: true })
  isUpcoming: boolean;
}
