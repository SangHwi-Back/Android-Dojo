import { Entity, Column, PrimaryGeneratedColumn } from 'typeorm';

@Entity('users')
export class User {
  @PrimaryGeneratedColumn()
  id: number;

  @Column()
  name: string;

  @Column({ name: 'is_guest', default: true })
  isGuest: boolean;

  @Column({ name: 'movies_count', default: 0 })
  moviesCount: number;

  @Column({ default: '0' })
  points: string;

  @Column({ default: 0 })
  saved: number;
}
