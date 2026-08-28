import { ForbiddenException, Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Card } from './entities/card.entity';

export interface CreateCardDto {
  cardNumber: string;
  expiryDate: string;
  cvv: string;
  nickname: string;
}

export interface UpdateCardDto {
  cardNumber?: string;
  expiryDate?: string;
  cvv?: string;
  nickname?: string;
}

@Injectable()
export class CardsService {
  constructor(
    @InjectRepository(Card)
    private readonly cardsRepo: Repository<Card>,
  ) {}

  findAll(uid: string): Promise<Card[]> {
    return this.cardsRepo.find({ where: { userUid: uid } });
  }

  create(uid: string, dto: CreateCardDto): Promise<Card> {
    const card = this.cardsRepo.create({ ...dto, userUid: uid });
    return this.cardsRepo.save(card);
  }

  async update(id: number, uid: string, dto: UpdateCardDto): Promise<Card> {
    const card = await this.cardsRepo.findOne({ where: { id } });
    if (!card) throw new NotFoundException(`Card #${id} not found`);
    if (card.userUid !== uid) throw new ForbiddenException('You do not own this card');
    Object.assign(card, dto);
    return this.cardsRepo.save(card);
  }

  async remove(id: number, uid: string): Promise<void> {
    const card = await this.cardsRepo.findOne({ where: { id } });
    if (!card) throw new NotFoundException(`Card #${id} not found`);
    if (card.userUid !== uid) throw new ForbiddenException('You do not own this card');
    await this.cardsRepo.remove(card);
  }
}
