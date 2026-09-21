import { Entity, Column, PrimaryGeneratedColumn } from 'typeorm';

@Entity('users')
export class User {
  @PrimaryGeneratedColumn()
  id: number;

  @Column()
  name: string;

  @Column({ name: 'firebase_uid', unique: true, nullable: true })
  firebaseUid: string | null;

  @Column({ nullable: true })
  email: string | null;

  @Column({ name: 'is_guest', default: true })
  isGuest: boolean;

  @Column({ name: 'movies_count', default: 0 })
  moviesCount: number;

  @Column({ default: '0' })
  points: string;

  @Column({ default: 0 })
  saved: number;

  @Column({ nullable: true })
  phone: string | null;

  @Column({ name: 'push_notification', default: true })
  pushNotification: boolean;

  @Column({ name: 'profile_image_id', nullable: true })
  profileImageId: string | null;
}
