import { Injectable, OnModuleInit, Logger } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Movie } from '../movies/entities/movie.entity';
import { Theater } from '../theaters/entities/theater.entity';
import { Showtime } from '../showtimes/entities/showtime.entity';
import { Booking } from '../bookings/entities/booking.entity';
import { User } from '../users/entities/user.entity';
import { Seat, SeatStatus, SeatType } from '../seats/entities/seat.entity';

interface HallLayout {
  hall: string;
  floor: number;
  rows: string[]; // 행 라벨 순서 (렌더링 순서 그대로)
  columnsPerRow: number;
  aisleAfterColumns: number[]; // 이 열 번호(1-based) 뒤에 통로
  aisleAfterRows: string[]; // 이 행 라벨 다음에 통로
  vipRows: string[]; // VIP 좌석으로 지정할 행 라벨
}

@Injectable()
export class SeederService implements OnModuleInit {
  private readonly logger = new Logger(SeederService.name);

  // 모든 극장이 공유하는 3개 홀의 좌석 배치 (Showtime.hall 문자열과 이름을 맞춤)
  private readonly hallLayouts: HallLayout[] = [
    {
      hall: 'IMAX Hall 1',
      floor: 2,
      rows: ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'],
      columnsPerRow: 12,
      aisleAfterColumns: [3, 9], // 3+6+3 좌우 통로
      aisleAfterRows: ['D'], // 앞 4줄 / 뒤 4줄 사이 통로
      vipRows: ['G', 'H'],
    },
    {
      hall: 'Hall 2',
      floor: 1,
      rows: ['A', 'B', 'C', 'D', 'E', 'F'],
      columnsPerRow: 10,
      aisleAfterColumns: [5],
      aisleAfterRows: [],
      vipRows: [],
    },
    {
      hall: 'Hall 3',
      floor: 1,
      rows: ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'],
      columnsPerRow: 14,
      aisleAfterColumns: [4, 10],
      aisleAfterRows: ['E'],
      vipRows: ['I', 'J'],
    },
  ];

  constructor(
    @InjectRepository(Movie) private readonly moviesRepo: Repository<Movie>,
    @InjectRepository(Theater) private readonly theatersRepo: Repository<Theater>,
    @InjectRepository(Showtime) private readonly showtimesRepo: Repository<Showtime>,
    @InjectRepository(Booking) private readonly bookingsRepo: Repository<Booking>,
    @InjectRepository(User) private readonly usersRepo: Repository<User>,
    @InjectRepository(Seat) private readonly seatsRepo: Repository<Seat>,
  ) {}

  async onModuleInit() {
    const count = await this.moviesRepo.count();
    if (count > 0) {
      this.logger.log('Database already seeded — skipping');
      return;
    }
    await this.seed();
    this.logger.log('Database seeded successfully');
  }

  private async seed() {
    // ── Movies ────────────────────────────────────────────────────
    const movies = await this.moviesRepo.save([
      {
        id: 1,
        title: 'Dune: Part Three',
        duration: '2h 45m',
        rating: 9.2,
        releaseDate: '2026-03-15',
        genres: ['Sci-Fi', 'Adventure'],
        description:
          'The epic conclusion to the Dune saga follows Paul Atreides as he unites with the Fremen people of the desert planet Arrakis.',
        posterUrl: null,
        backdropUrl: null,
        isNowPlaying: true,
        isComingSoon: false,
        isFeatured: true,
      },
      {
        id: 2,
        title: "Ocean's Eleven: Legacy",
        duration: '2h 12m',
        rating: 8.1,
        releaseDate: '2026-02-20',
        genres: ['Thriller', 'Comedy'],
        description:
          'A new crew of master thieves attempts the most daring casino heist in Las Vegas history.',
        posterUrl: null,
        backdropUrl: null,
        isNowPlaying: true,
        isComingSoon: false,
        isFeatured: false,
      },
      {
        id: 3,
        title: 'Neon Knights',
        duration: '1h 58m',
        rating: 7.6,
        releaseDate: '2026-04-10',
        genres: ['Action', 'Sci-Fi'],
        description:
          'In a neon-lit dystopian city, a rogue detective uncovers a conspiracy that threatens the last free zone on Earth.',
        posterUrl: null,
        backdropUrl: null,
        isNowPlaying: true,
        isComingSoon: false,
        isFeatured: false,
      },
      {
        id: 4,
        title: 'Starfall Chronicles',
        duration: '2h 30m',
        rating: 8.5,
        releaseDate: '2026-05-01',
        genres: ['Adventure', 'Drama'],
        description:
          'An astronaut stranded on a dying moon must choose between survival and saving an alien civilization.',
        posterUrl: null,
        backdropUrl: null,
        isNowPlaying: true,
        isComingSoon: false,
        isFeatured: true,
      },
      {
        id: 5,
        title: 'Midnight Sonata',
        duration: '1h 45m',
        rating: 7.9,
        releaseDate: '2026-01-30',
        genres: ['Drama', 'Romance'],
        description:
          'A blind pianist rediscovers passion when a mysterious composer leaves unfinished scores on his doorstep.',
        posterUrl: null,
        backdropUrl: null,
        isNowPlaying: false,
        isComingSoon: true,
        isFeatured: false,
      },
      {
        id: 6,
        title: 'Iron Veil: Rising',
        duration: '2h 20m',
        rating: 8.3,
        releaseDate: '2026-06-12',
        genres: ['Action', 'Adventure'],
        description:
          "The world's last supersoldier comes out of hiding when a shadow organization threatens global infrastructure.",
        posterUrl: null,
        backdropUrl: null,
        isNowPlaying: false,
        isComingSoon: true,
        isFeatured: true,
      },
    ]);

    // ── Theaters ──────────────────────────────────────────────────
    const theaters = await this.theatersRepo.save([
      { id: 1, name: 'IMAX Cineplex Downtown', address: '123 Main Street, Downtown', distanceKm: 1.2 },
      { id: 2, name: 'Galaxy Multiplex',       address: '456 Oak Avenue, Midtown',   distanceKm: 2.8 },
      { id: 3, name: 'Starlight Cinema',        address: '789 Park Boulevard, Uptown', distanceKm: 4.5 },
      { id: 4, name: 'Grand Screen Arena',      address: '321 River Road, Eastside',  distanceKm: 6.1 },
    ]);

    // ── Showtimes (MockData.kt ShowtimeMock 기준) ─────────────────
    const showtimesByDate = [
      {
        date: '2026-05-14',
        slots: [
          { time: '12:00 PM', hall: 'IMAX Hall 1', availableSeats: 42 },
          { time: '3:30 PM',  hall: 'Hall 2',       availableSeats: 28 },
          { time: '6:45 PM',  hall: 'IMAX Hall 1', availableSeats: 15 },
          { time: '9:30 PM',  hall: 'Hall 3',       availableSeats: 60 },
        ],
      },
      {
        date: '2026-05-15',
        slots: [
          { time: '11:00 AM', hall: 'Hall 2',       availableSeats: 50 },
          { time: '2:15 PM',  hall: 'IMAX Hall 1', availableSeats: 33 },
          { time: '6:45 PM',  hall: 'IMAX Hall 1', availableSeats: 8  },
          { time: '9:00 PM',  hall: 'Hall 3',       availableSeats: 45 },
        ],
      },
      {
        date: '2026-05-16',
        slots: [
          { time: '1:00 PM',  hall: 'Hall 2',       availableSeats: 55 },
          { time: '4:30 PM',  hall: 'IMAX Hall 1', availableSeats: 20 },
          { time: '7:45 PM',  hall: 'IMAX Hall 1', availableSeats: 12 },
        ],
      },
      {
        date: '2026-05-17',
        slots: [
          { time: '10:30 AM', hall: 'Hall 3',       availableSeats: 70 },
          { time: '1:45 PM',  hall: 'IMAX Hall 1', availableSeats: 40 },
          { time: '5:00 PM',  hall: 'Hall 2',       availableSeats: 25 },
          { time: '8:15 PM',  hall: 'IMAX Hall 1', availableSeats: 18 },
        ],
      },
    ];

    const showtimesToSave: Partial<Showtime>[] = [];
    for (const movie of movies) {
      for (const theater of theaters) {
        for (const day of showtimesByDate) {
          for (const slot of day.slots) {
            showtimesToSave.push({
              showDate: day.date,
              time: slot.time,
              hall: slot.hall,
              availableSeats: slot.availableSeats,
              movie,
              theater,
            });
          }
        }
      }
    }
    await this.showtimesRepo.save(showtimesToSave);

    // ── Seats (모든 극장 × 모든 홀 조합에 좌석 배치 생성) ─────────
    const seatsToSave: Partial<Seat>[] = [];
    for (const theater of theaters) {
      for (const layout of this.hallLayouts) {
        seatsToSave.push(...this.generateHallSeats(theater, layout));
      }
    }
    await this.seatsRepo.save(seatsToSave);

    // ── Bookings (id는 자동 채번 컬럼이라 지정하지 않음) ──────────
    await this.bookingsRepo.save([
      {
        movie: movies[0],    // Dune: Part Three
        theater: theaters[0], // IMAX Cineplex Downtown
        date: 'May 15, 2026',
        time: '6:45 PM',
        seats: ['D4', 'D5'],
        isUpcoming: true,
      },
      {
        movie: movies[2],    // Neon Knights
        theater: theaters[1], // Galaxy Multiplex
        date: 'May 20, 2026',
        time: '9:30 PM',
        seats: ['E2', 'E3', 'E4'],
        isUpcoming: true,
      },
      {
        movie: movies[1],    // Ocean's Eleven: Legacy
        theater: theaters[2], // Starlight Cinema
        date: 'May 28, 2026',
        time: '7:00 PM',
        seats: ['B1', 'B2'],
        isUpcoming: true,
      },
      {
        movie: movies[3],    // Starfall Chronicles
        theater: theaters[0], // IMAX Cineplex Downtown
        date: 'April 5, 2026',
        time: '3:00 PM',
        seats: ['F5', 'F6', 'F7'],
        isUpcoming: false,
      },
    ]);

    // ── User ──────────────────────────────────────────────────────
    await this.usersRepo.save({
      name: 'Guest User',
      isGuest: true,
      moviesCount: 12,
      points: '1.2K',
      saved: 89,
    });
  }

  private generateHallSeats(theater: Theater, layout: HallLayout): Partial<Seat>[] {
    const seats: Partial<Seat>[] = [];
    layout.rows.forEach((rowLabel, rowIndex) => {
      for (let column = 1; column <= layout.columnsPerRow; column++) {
        seats.push({
          theater,
          hall: layout.hall,
          floor: layout.floor,
          rowLabel,
          rowIndex,
          columnIndex: column,
          seatType: layout.vipRows.includes(rowLabel) ? SeatType.VIP : SeatType.NORMAL,
          hasAisleAfterColumn: layout.aisleAfterColumns.includes(column),
          hasAisleAfterRow: layout.aisleAfterRows.includes(rowLabel),
          status: SeatStatus.AVAILABLE,
        });
      }
    });
    return seats;
  }
}
