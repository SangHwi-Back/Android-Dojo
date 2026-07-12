import {
  BadRequestException,
  Body,
  Controller,
  Get,
  Param,
  ParseIntPipe,
  Patch,
  Post,
  Query,
} from '@nestjs/common';
import { SeatsService } from './seats.service';
import { SeatStatus } from './entities/seat.entity';

@Controller('seats')
export class SeatsController {
  constructor(private readonly seatsService: SeatsService) {}

  // GET /api/seats?theaterId=1&hall=IMAX%20Hall%201
  @Get()
  findAll(
    @Query('theaterId') theaterId?: string,
    @Query('hall') hall?: string,
  ) {
    if (!theaterId) throw new BadRequestException('theaterId is required');
    if (!hall) throw new BadRequestException('hall is required');
    return this.seatsService.findAll(parseInt(theaterId), hall);
  }

  // PATCH /api/seats/1/status  { "status": "OCCUPIED" }
  @Patch(':id/status')
  updateStatus(
    @Param('id', ParseIntPipe) id: number,
    @Body('status') status?: SeatStatus,
  ) {
    if (!status || !Object.values(SeatStatus).includes(status)) {
      throw new BadRequestException(
        `status must be one of: ${Object.values(SeatStatus).join(', ')}`,
      );
    }
    return this.seatsService.updateStatus(id, status);
  }

  // POST /api/seats/1/hold  { "userId": "device-abc", "ttlMinutes": 10 }
  @Post(':id/hold')
  hold(
    @Param('id', ParseIntPipe) id: number,
    @Body('userId') userId?: string,
    @Body('ttlMinutes') ttlMinutes?: number,
  ) {
    if (!userId) throw new BadRequestException('userId is required');
    return this.seatsService.holdSeat(id, userId, ttlMinutes);
  }

  // POST /api/seats/1/release  { "userId": "device-abc" }
  @Post(':id/release')
  release(
    @Param('id', ParseIntPipe) id: number,
    @Body('userId') userId?: string,
  ) {
    if (!userId) throw new BadRequestException('userId is required');
    return this.seatsService.releaseSeat(id, userId);
  }

  // POST /api/seats/1/confirm  { "userId": "device-abc" }
  @Post(':id/confirm')
  confirm(
    @Param('id', ParseIntPipe) id: number,
    @Body('userId') userId?: string,
  ) {
    if (!userId) throw new BadRequestException('userId is required');
    return this.seatsService.confirmSeat(id, userId);
  }
}
