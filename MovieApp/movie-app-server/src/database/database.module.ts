import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Movie } from '../movies/entities/movie.entity';
import { Theater } from '../theaters/entities/theater.entity';
import { Showtime } from '../showtimes/entities/showtime.entity';
import { Booking } from '../bookings/entities/booking.entity';
import { User } from '../users/entities/user.entity';
import { Seat } from '../seats/entities/seat.entity';
import { SeederService } from './seeder.service';

@Module({
  imports: [TypeOrmModule.forFeature([Movie, Theater, Showtime, Booking, User, Seat])],
  providers: [SeederService],
})
export class DatabaseModule {}
