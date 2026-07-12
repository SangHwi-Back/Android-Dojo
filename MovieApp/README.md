# CineBook — MovieApp

Android 영화 예매 앱(Kotlin) + NestJS 백엔드로 구성된 개인 학습/포트폴리오 프로젝트입니다. 영화 검색부터 극장·상영시간·좌석 선택까지 이어지는 예매 플로우를 구현합니다.

---

## 프로젝트 구성

```
MovieApp/
├── app/                 # Android 클라이언트 (Kotlin, MVVM + Hilt)
└── movie-app-server/    # 백엔드 서버 (NestJS + TypeORM + PostgreSQL)
```

두 모듈은 독립적으로 실행되며, 안드로이드 앱이 HTTPS로 로컬 서버에 접속하는 구조입니다.

---

## 기술 스택

### Android (`app/`)

| 영역 | 사용 기술 |
|---|---|
| 언어 | Kotlin |
| 아키텍처 | MVVM + Repository 패턴 |
| DI | Hilt |
| 네트워킹 | Retrofit2 + OkHttp (HTTPS, mkcert 인증서) |
| 네비게이션 | Navigation Component + SafeArgs |
| 바인딩 | ViewBinding / DataBinding |
| 이미지 로딩 | Coil |
| 비동기 | Kotlin Coroutines + StateFlow |
| 최소/타깃 SDK | minSdk 24 / targetSdk 36 |

### 서버 (`movie-app-server/`)

| 영역 | 사용 기술 |
|---|---|
| 프레임워크 | NestJS 10 (TypeScript) |
| ORM | TypeORM |
| DB | PostgreSQL 16 (Docker) |
| 인증서 | mkcert 기반 로컬 HTTPS |
| 패키지 매니저 | pnpm |

---

## 주요 화면 / 기능

- **Search 탭**: 현재 상영작 검색, 장르별 브라우징, 최근 검색어, 영화 상세 바텀시트
- **Book 탭**: 영화 선택 → 극장 선택 → 상영일/시간 선택 → 좌석 선택까지 이어지는 예매 플로우
  - 상단 영화 ViewPager2(여러 영화 배열을 스와이프)와 하단 단계별 ViewPager2(극장/시간/좌석, 스와이프 불가·버튼으로만 진행)로 구성된 `BookChooseInfoFragment` 단일 화면 구조
- **My Info 탭**: 사용자 프로필, 예매 내역, 통계 요약(Mock 데이터 기반)

---

## 서버 도메인 모델

```
Movie ──< Showtime >── Theater
  │                        │
  └──< Booking >───────────┘
                            │
                       Theater ──< Seat (좌석 배치 + 현재 상태)
```

- **Movie**: 영화 정보 (제목, 장르, 상영시간, 평점 등)
- **Theater**: 극장 정보 (이름, 주소, 거리)
- **Showtime**: 영화 × 극장 × 홀 × 날짜 × 시간의 상영 회차. 한 극장에 여러 홀(`IMAX Hall 1`, `Hall 2`, `Hall 3`)이 존재
- **Seat**: 극장×홀 단위의 실제 좌석 배치(행/열/층/통로 위치/좌석 등급)와 현재 상태(`AVAILABLE`/`HELD`/`OCCUPIED`/`DISABLED`)를 함께 담는 엔티티. `HELD` 상태는 소유자(`heldByUserId`)와 만료 시각(`heldUntil`)을 가지며, 트랜잭션 + row lock으로 동시 예약 충돌을 방지
- **Booking**: 확정된 예매 1건(영화, 극장, 날짜, 시간, 좌석 목록)

앱 최초 구동 시 Seeder가 영화 6편, 극장 4곳, 극장×홀 조합별 좌석 배치(약 1,184석), 상영 회차, 샘플 예매 내역을 자동으로 채워 넣습니다.

---

## API 엔드포인트

| Method | URL | 설명 |
|---|---|---|
| GET | `/api/movies` | 전체 영화 목록 |
| GET | `/api/movies/now-playing` | 현재 상영작 |
| GET | `/api/movies/coming-soon` | 개봉 예정작 |
| GET | `/api/movies/featured` | 피처드 배너용 |
| GET | `/api/movies/:id` | 영화 상세 |
| GET | `/api/theaters` | 극장 목록 (거리순) |
| GET | `/api/theaters/:id` | 극장 상세 |
| GET | `/api/showtimes?movieId=&theaterId=&date=` | 조건별 상영 회차(시간 포함) |
| GET | `/api/showtimes/dates?movieId=` | 특정 영화의 상영 날짜 목록 |
| GET | `/api/showtimes/theaters?movieId=` | 특정 영화를 상영하는 극장 목록 |
| GET | `/api/seats?theaterId=&hall=` | 특정 극장·홀의 좌석 배치 + 현재 상태 |
| PATCH | `/api/seats/:id/status` | 좌석 상태 직접 지정(`AVAILABLE`/`OCCUPIED`/`DISABLED`) |
| POST | `/api/seats/:id/hold` | 좌석 임시 잠금 (`{ userId, ttlMinutes? }`) |
| POST | `/api/seats/:id/release` | 본인이 잡은 좌석 잠금 해제 (`{ userId }`) |
| POST | `/api/seats/:id/confirm` | 홀드 중인 좌석을 예약 확정으로 전환 (`{ userId }`) |
| GET | `/api/bookings` | 전체 예매 내역 |
| GET | `/api/bookings/upcoming` | 다가오는 예매 |
| GET | `/api/bookings/past` | 지난 예매 |
| GET | `/api/bookings/schedules?movie_id=` | 영화별 예매 일정 |
| GET | `/api/bookings/schedules/date?movie_id=&start_date=&end_date=` | 기간 조건 포함 조회 |
| POST | `/api/bookings` | 새 예매 생성 (id는 서버가 자동 채번) |
| GET | `/api/users/profile` | 유저 프로필 |

---

## 시작하기

### 1. 서버 실행

사전 준비: Docker Desktop, Node.js 18+, pnpm, [mkcert](https://github.com/FiloSottile/mkcert)

```bash
cd movie-app-server

# 의존성 설치
pnpm install

# mkcert 인증서 발급 (에뮬레이터용 10.0.2.2 포함)
mkcert -install
mkcert localhost 127.0.0.1 10.0.2.2

# PostgreSQL 컨테이너 시작
docker compose up -d postgres

# 서버 실행 (개발 모드)
pnpm start:dev
```

서버가 `https://localhost:3009/api`에서 기동되며, 최초 실행 시 목업 데이터가 자동으로 시딩됩니다. 자세한 실행 옵션은 [`movie-app-server/README.md`](movie-app-server/README.md) 참고.

### 2. 안드로이드 앱 실행

1. `local.properties`에 서버 접속 정보 설정 (에뮬레이터 기준):
   ```properties
   network.ip.local=10.0.2.2
   network.port.local=3009
   ```
2. mkcert 루트 CA를 앱 리소스에 등록 (최초 1회):
   ```bash
   cp "$(mkcert -CAROOT)/rootCA.pem" app/src/main/res/raw/mkcert_ca.pem
   ```
3. Android Studio에서 프로젝트를 열고 에뮬레이터로 실행

실기기로 테스트할 경우 `network.ip.local`을 PC의 로컬 IP(`ipconfig getifaddr en0`)로, mkcert 인증서도 해당 IP를 포함해 재발급해야 합니다.

---

## 알려진 제약 / 진행 상황

- 예매 확정 이후(좌석 선택 → 결제/완료 화면) 플로우는 아직 설계가 확정되지 않았습니다.
- 좌석 홀드는 단일 Postgres 인스턴스의 row lock에 의존하며, 실제 로그인/인증 시스템은 없습니다(사용자 식별자는 클라이언트가 생성한 임의 문자열).
- 그 외 구조 분석, 리팩터링 이력, 설계 결정 배경은 [`PROJECT_REVIEW.md`](PROJECT_REVIEW.md)에 상세히 기록되어 있습니다.
