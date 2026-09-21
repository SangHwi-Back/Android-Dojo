import {
  Body, Controller, Delete, Get, NotFoundException,
  Param, Patch, Post, Req, Res, UploadedFile,
  UseGuards, UseInterceptors,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { Response } from 'express';
import * as fs from 'fs';
import { UsersService } from './users.service';
import { FirebaseAuthGuard } from '../auth/firebase-auth.guard';
import { UpdateUserDto } from './dto/update-user.dto';

@Controller('users')
export class UsersController {
  constructor(private readonly usersService: UsersService) {}

  @Get('profile')
  getProfile() {
    return this.usersService.getProfile();
  }

  @UseGuards(FirebaseAuthGuard)
  @Get('me')
  async getMe(@Req() req: any) {
    const { uid, email, name } = req.user;
    const user = await this.usersService.findOrCreate(uid, email, name ?? email ?? uid);
    return {
      ...user,
      profileImageUrl: user.profileImageId
        ? `/api/users/profile-images/${user.profileImageId}`
        : null,
    };
  }

  @UseGuards(FirebaseAuthGuard)
  @Patch('me')
  async updateMe(@Req() req: any, @Body() dto: UpdateUserDto) {
    const user = await this.usersService.updateByUid(req.user.uid, dto);
    return {
      ...user,
      profileImageUrl: user.profileImageId
        ? `/api/users/profile-images/${user.profileImageId}`
        : null,
    };
  }

  @UseGuards(FirebaseAuthGuard)
  @Post('me/profile-image')
  @UseInterceptors(FileInterceptor('image', { limits: { fileSize: 5 * 1024 * 1024 } }))
  async uploadProfileImage(@Req() req: any, @UploadedFile() file: Express.Multer.File) {
    const user = await this.usersService.saveProfileImage(req.user.uid, file.buffer, file.mimetype);
    return {
      ...user,
      profileImageUrl: `/api/users/profile-images/${user.profileImageId}`,
    };
  }

  @UseGuards(FirebaseAuthGuard)
  @Delete('me/profile-image')
  async deleteProfileImage(@Req() req: any) {
    const user = await this.usersService.deleteProfileImage(req.user.uid);
    return { ...user, profileImageUrl: null };
  }

  @UseGuards(FirebaseAuthGuard)
  @Get('profile-images/:imageId')
  serveProfileImage(@Param('imageId') imageId: string, @Res() res: Response) {
    const imagePath = this.usersService.resolveImagePath(imageId);
    if (!fs.existsSync(imagePath)) throw new NotFoundException('Image not found');
    res.sendFile(imagePath);
  }
}
