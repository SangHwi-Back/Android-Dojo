import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  ParseIntPipe,
  Patch,
  Post,
  Req,
  UseGuards,
} from '@nestjs/common';
import { CardsService, CreateCardDto, UpdateCardDto } from './cards.service';
import { FirebaseAuthGuard } from '../auth/firebase-auth.guard';

@UseGuards(FirebaseAuthGuard)
@Controller('cards')
export class CardsController {
  constructor(private readonly cardsService: CardsService) {}

  @Get()
  findAll(@Req() req: any) {
    return this.cardsService.findAll(req.user.uid);
  }

  @Post()
  create(@Req() req: any, @Body() dto: CreateCardDto) {
    return this.cardsService.create(req.user.uid, dto);
  }

  @Patch(':id')
  update(
    @Param('id', ParseIntPipe) id: number,
    @Req() req: any,
    @Body() dto: UpdateCardDto,
  ) {
    return this.cardsService.update(id, req.user.uid, dto);
  }

  @Delete(':id')
  remove(@Param('id', ParseIntPipe) id: number, @Req() req: any) {
    return this.cardsService.remove(id, req.user.uid);
  }
}
