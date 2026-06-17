import { Entity, Column, PrimaryGeneratedColumn, ManyToOne, JoinColumn } from 'typeorm';
import { Movie } from '../../movies/entities/movie.entity';
import { Theater } from '../../theaters/entities/theater.entity';

@Entity('showtimes')
export class Showtime {
  @PrimaryGeneratedColumn()
  id: number;

  @Column({ name: 'show_date' })
  showDate: string;

  @Column()
  time: string;

  @Column()
  hall: string;

  @Column({ name: 'available_seats' })
  availableSeats: number;

  @ManyToOne(() => Movie, (movie) => movie.showtimes, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'movie_id' })
  movie: Movie;

  @ManyToOne(() => Theater, (theater) => theater.showtimes, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'theater_id' })
  theater: Theater;
}
