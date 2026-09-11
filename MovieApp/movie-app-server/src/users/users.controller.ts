import { Body, Controller, Get, Patch, Req, UseGuards } from '@nestjs/common';
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
  getMe(@Req() req: any) {
    return this.usersService.findByUid(req.user.uid);
  }

  @UseGuards(FirebaseAuthGuard)
  @Patch('me')
  updateMe(@Req() req: any, @Body() dto: UpdateUserDto) {
    return this.usersService.updateByUid(req.user.uid, dto);
  }
}
