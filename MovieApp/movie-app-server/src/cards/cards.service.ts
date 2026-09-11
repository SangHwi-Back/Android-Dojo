import { ForbiddenException, Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { PaymentMethod, PaymentMethodType } from './entities/card.entity';

export interface CreatePaymentMethodDto {
  type: PaymentMethodType;
  billingKey?: string;
  maskedNumber?: string;
  cardCompany?: string;
  expiryDate?: string;
  cardholderName?: string;
  pgCustomerUid?: string;
  isDefault?: boolean;
  nickname?: string;
}

export interface UpdatePaymentMethodDto {
  billingKey?: string;
  maskedNumber?: string;
  cardCompany?: string;
  expiryDate?: string;
  cardholderName?: string;
  pgCustomerUid?: string;
  isDefault?: boolean;
  isActive?: boolean;
  nickname?: string;
}

@Injectable()
export class CardsService {
  constructor(
    @InjectRepository(PaymentMethod)
    private readonly paymentMethodRepo: Repository<PaymentMethod>,
  ) {}

  findAll(uid: string): Promise<PaymentMethod[]> {
    return this.paymentMethodRepo.find({ where: { userUid: uid, isActive: true } });
  }

  async create(uid: string, dto: CreatePaymentMethodDto): Promise<PaymentMethod> {
    if (dto.isDefault) {
      await this.paymentMethodRepo.update({ userUid: uid }, { isDefault: false });
    }
    const paymentMethod = this.paymentMethodRepo.create({ ...dto, userUid: uid });
    return this.paymentMethodRepo.save(paymentMethod);
  }

  async update(id: number, uid: string, dto: UpdatePaymentMethodDto): Promise<PaymentMethod> {
    const paymentMethod = await this.paymentMethodRepo.findOne({ where: { id } });
    if (!paymentMethod) throw new NotFoundException(`PaymentMethod #${id} not found`);
    if (paymentMethod.userUid !== uid) throw new ForbiddenException('You do not own this payment method');
    if (dto.isDefault) {
      await this.paymentMethodRepo.update({ userUid: uid }, { isDefault: false });
    }
    Object.assign(paymentMethod, dto);
    return this.paymentMethodRepo.save(paymentMethod);
  }

  async remove(id: number, uid: string): Promise<void> {
    const paymentMethod = await this.paymentMethodRepo.findOne({ where: { id } });
    if (!paymentMethod) throw new NotFoundException(`PaymentMethod #${id} not found`);
    if (paymentMethod.userUid !== uid) throw new ForbiddenException('You do not own this payment method');
    paymentMethod.isActive = false;
    await this.paymentMethodRepo.save(paymentMethod);
  }
}
