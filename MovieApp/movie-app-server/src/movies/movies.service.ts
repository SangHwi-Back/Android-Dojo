import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Movie } from './entities/movie.entity';

@Injectable()
export class MoviesService {
  constructor(
    @InjectRepository(Movie)
    private readonly moviesRepo: Repository<Movie>,
  ) {}

  findAll(): Promise<Movie[]> {
    return this.moviesRepo.find();
  }

  findNowPlaying(): Promise<Movie[]> {
    return this.moviesRepo.find({ where: { isNowPlaying: true } });
  }

  findComingSoon(): Promise<Movie[]> {
    return this.moviesRepo.find({ where: { isComingSoon: true } });
  }

  findFeatured(): Promise<Movie[]> {
    return this.moviesRepo.find({ where: { isFeatured: true } });
  }

  async findOne(id: number): Promise<Movie> {
    const movie = await this.moviesRepo.findOne({ where: { id } });
    if (!movie) throw new NotFoundException(`Movie #${id} not found`);
    return movie;
  }
}
