import { Controller, Get, Query } from '@nestjs/common';
import { ShowtimesService } from './showtimes.service';

@Controller('showtimes')
export class ShowtimesController {
  constructor(private readonly showtimesService: ShowtimesService) {}

  // GET /api/showtimes/dates?movieId=1
  @Get('dates')
  findUniqueDates(@Query('movieId') movieId: string) {
    return this.showtimesService.findUniqueDates(parseInt(movieId));
  }

  // GET /api/showtimes?date=2026-05-15&movieId=1&theaterId=1
  @Get()
  findAll(
    @Query('date') date?: string,
    @Query('movieId') movieId?: string,
    @Query('theaterId') theaterId?: string,
  ) {
    return this.showtimesService.findAll(
      date,
      movieId ? parseInt(movieId) : undefined,
      theaterId ? parseInt(theaterId) : undefined,
    );
  }
}
