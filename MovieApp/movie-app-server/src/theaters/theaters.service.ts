import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Theater } from './entities/theater.entity';

@Injectable()
export class TheatersService {
  constructor(
    @InjectRepository(Theater)
    private readonly theatersRepo: Repository<Theater>,
  ) {}

  findAll(): Promise<Theater[]> {
    return this.theatersRepo.find({ order: { distanceKm: 'ASC' } });
  }

  async findOne(id: number): Promise<Theater> {
    const theater = await this.theatersRepo.findOne({ where: { id } });
    if (!theater) throw new NotFoundException(`Theater #${id} not found`);
    return theater;
  }
}
