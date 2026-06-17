import { Controller, Get, Param, ParseIntPipe } from '@nestjs/common';
import { MoviesService } from './movies.service';

@Controller('movies')
export class MoviesController {
  constructor(private readonly moviesService: MoviesService) {}

  @Get()
  findAll() {
    return this.moviesService.findAll();
  }

  @Get('now-playing')
  findNowPlaying() {
    return this.moviesService.findNowPlaying();
  }

  @Get('coming-soon')
  findComingSoon() {
    return this.moviesService.findComingSoon();
  }

  @Get('featured')
  findFeatured() {
    return this.moviesService.findFeatured();
  }

  @Get(':id')
  findOne(@Param('id', ParseIntPipe) id: number) {
    return this.moviesService.findOne(id);
  }
}
