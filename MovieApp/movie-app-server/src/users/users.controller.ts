import { Controller, Get, Req, UseGuards } from '@nestjs/common';
import { UsersService } from './users.service';
import { FirebaseAuthGuard } from '../auth/firebase-auth.guard';

@Controller('users')
export class UsersController {
  constructor(private readonly usersService: UsersService) {}

  @Get('profile')
  getProfile() {
    return this.usersService.getProfile();
  }

  @UseGuards(FirebaseAuthGuard)
  @Get('me')
  getMe(@Req() req: any) {
    return this.usersService.findByUid(req.user.uid);
  }
}
