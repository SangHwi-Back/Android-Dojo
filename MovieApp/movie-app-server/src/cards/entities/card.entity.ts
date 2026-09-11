import { Column, CreateDateColumn, Entity, PrimaryGeneratedColumn } from 'typeorm';

export enum PaymentMethodType {
  CREDIT_CARD = 'CREDIT_CARD',
  DEBIT_CARD = 'DEBIT_CARD',
  KAKAO_PAY = 'KAKAO_PAY',
  NAVER_PAY = 'NAVER_PAY',
  TOSS = 'TOSS',
}

@Entity('payment_methods')
export class PaymentMethod {
  @PrimaryGeneratedColumn()
  id: number;

  @Column({ name: 'user_uid' })
  userUid: string;

  @Column({ type: 'enum', enum: PaymentMethodType })
  type: PaymentMethodType;

  // PG사가 발급한 빌링키 (카드 원문 대신 저장)
  @Column({ name: 'billing_key', nullable: true })
  billingKey: string | null;

  // 표시용 마스킹 번호: "1234-****-****-5678"
  @Column({ name: 'masked_number', nullable: true })
  maskedNumber: string | null;

  @Column({ name: 'card_company', nullable: true })
  cardCompany: string | null;

  // 표시용 만료일: "MM/YY"
  @Column({ name: 'expiry_date', nullable: true })
  expiryDate: string | null;

  @Column({ name: 'cardholder_name', nullable: true })
  cardholderName: string | null;

  // 카카오페이 등 PG 결제수단의 고객 식별자
  @Column({ name: 'pg_customer_uid', nullable: true })
  pgCustomerUid: string | null;

  @Column({ name: 'is_default', default: false })
  isDefault: boolean;

  @Column({ name: 'is_active', default: true })
  isActive: boolean;

  @Column({ nullable: true })
  nickname: string | null;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;
}
