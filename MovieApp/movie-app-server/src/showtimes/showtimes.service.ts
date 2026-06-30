import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Showtime } from './entities/showtime.entity';

@Injectable()
export class ShowtimesService {
  constructor(
    @InjectRepository(Showtime)
    private readonly showtimesRepo: Repository<Showtime>,
  ) {}

  findAll(date?: string, movieId?: number, theaterId?: number): Promise<Showtime[]> {
    const where: any = {};
    if (date) where.showDate = date;
    if (movieId) where.movie = { id: movieId };
    if (theaterId) where.theater = { id: theaterId };

    return this.showtimesRepo.find({
      where,
      relations: ['movie', 'theater'],
      order: { showDate: 'ASC', time: 'ASC' },
    });
  }

  async findUniqueDates(movieId: number): Promise<string[]> {
    const rows = await this.showtimesRepo
      .createQueryBuilder('showtime')
      .select('DISTINCT showtime.show_date', 'showDate')
      .where('showtime.movie_id = :movieId', { movieId })
      .orderBy('showtime.show_date', 'ASC')
      .getRawMany();
    return rows.map((r) => r.showDate);
  }
}
