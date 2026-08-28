import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { User } from './entities/user.entity';

@Injectable()
export class UsersService {
  constructor(
    @InjectRepository(User)
    private readonly usersRepo: Repository<User>,
  ) {}

  async getProfile(): Promise<User> {
    const [user] = await this.usersRepo.find({ take: 1 });
    return user ?? null;
  }

  async findOrCreate(uid: string, email: string, name: string): Promise<User> {
    const existing = await this.usersRepo.findOne({ where: { firebaseUid: uid } });
    if (existing) return existing;

    const user = this.usersRepo.create({
      firebaseUid: uid,
      email: email ?? null,
      name: name ?? email ?? uid,
      isGuest: false,
    });
    return this.usersRepo.save(user);
  }

  async findByUid(uid: string): Promise<User> {
    const user = await this.usersRepo.findOne({ where: { firebaseUid: uid } });
    if (!user) throw new NotFoundException(`User with uid ${uid} not found`);
    return user;
  }
}
