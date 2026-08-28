import { Column, CreateDateColumn, Entity, PrimaryGeneratedColumn } from 'typeorm';

@Entity('cards')
export class Card {
  @PrimaryGeneratedColumn()
  id: number;

  @Column({ name: 'user_uid' })
  userUid: string;

  @Column({ name: 'card_number' })
  cardNumber: string;

  @Column({ name: 'expiry_date' })
  expiryDate: string;

  @Column()
  cvv: string;

  @Column()
  nickname: string;

  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;
}
