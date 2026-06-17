import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Movie } from './movies/entities/movie.entity';
import { Theater } from './theaters/entities/theater.entity';
import { Showtime } from './showtimes/entities/showtime.entity';
import { Booking } from './bookings/entities/booking.entity';
import { User } from './users/entities/user.entity';
import { MoviesModule } from './movies/movies.module';
import { TheatersModule } from './theaters/theaters.module';
import { ShowtimesModule } from './showtimes/showtimes.module';
import { BookingsModule } from './bookings/bookings.module';
import { UsersModule } from './users/users.module';
import { DatabaseModule } from './database/database.module';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true }),
    TypeOrmModule.forRoot({
      type: 'postgres',
      host: process.env.DB_HOST ?? 'localhost',
      port: parseInt(process.env.DB_PORT ?? '5433'),
      username: process.env.DB_USER ?? 'movieapp',
      password: process.env.DB_PASSWORD ?? 'movieapp123',
      database: process.env.DB_NAME ?? 'movieappdb',
      entities: [Movie, Theater, Showtime, Booking, User],
      synchronize: true,
    }),
    DatabaseModule,
    MoviesModule,
    TheatersModule,
    ShowtimesModule,
    BookingsModule,
    UsersModule,
  ],
})
export class AppModule {}
