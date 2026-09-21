import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { User } from './entities/user.entity';
import { UpdateUserDto } from './dto/update-user.dto';
import * as fs from 'fs';
import * as path from 'path';
import * as crypto from 'crypto';

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

  async updateByUid(uid: string, dto: UpdateUserDto): Promise<User> {
    const user = await this.findByUid(uid);
    if (dto.name !== undefined) user.name = dto.name;
    if (dto.email !== undefined) user.email = dto.email;
    if (dto.phone !== undefined) user.phone = dto.phone;
    if (dto.pushNotification !== undefined) user.pushNotification = dto.pushNotification;
    return this.usersRepo.save(user);
  }

  async saveProfileImage(uid: string, fileBuffer: Buffer, mimeType: string): Promise<User> {
    const user = await this.findByUid(uid);

    if (user.profileImageId) {
      const oldPath = this.resolveImagePath(user.profileImageId);
      if (fs.existsSync(oldPath)) fs.unlinkSync(oldPath);
    }

    const ext = mimeType === 'image/png' ? 'png' : mimeType === 'image/webp' ? 'webp' : 'jpg';
    const imageId = `${crypto.randomUUID()}.${ext}`;
    const uploadDir = path.resolve(process.cwd(), 'uploads', 'profile-images');
    if (!fs.existsSync(uploadDir)) fs.mkdirSync(uploadDir, { recursive: true });

    fs.writeFileSync(path.join(uploadDir, imageId), fileBuffer);
    user.profileImageId = imageId;
    return this.usersRepo.save(user);
  }

  async deleteProfileImage(uid: string): Promise<User> {
    const user = await this.findByUid(uid);
    if (user.profileImageId) {
      const imagePath = this.resolveImagePath(user.profileImageId);
      if (fs.existsSync(imagePath)) fs.unlinkSync(imagePath);
      user.profileImageId = null;
      return this.usersRepo.save(user);
    }
    return user;
  }

  resolveImagePath(imageId: string): string {
    return path.resolve(process.cwd(), 'uploads', 'profile-images', imageId);
  }
}
