# Overview

간단한 영화예매 앱의 서버 애플리케이션

AI 에 전반적으로 작업을 진행 지시

## 필요 사전 설치:

- Docker Desktop
- Node.js (v18+)
- pnpm (npm install -g pnpm)

## 실행 순서:

1. 프로젝트 디렉토리로 이동

```cd movie-app-server```

2. 의존성 설치

```pnpm install```

3. PostgreSQL 컨테이너 시작

```docker compose up -d postgres```

4. 서버 시작 (개발 모드)

```pnpm start:dev```

또는 빌드 후 실행

```pnpm build && node dist/main```

전체 Docker로 실행하고 싶다면 (Node.js 없어도 됨, PostgreSQL + NestJS 앱 모두 컨테이너로 실행):

```docker compose up -d```

## 확인된 API 엔드포인트 목록

|Method|URL|설명|
|---|---|---|
|GET|/api/movies|전체 영화 목록|
|GET|/api/movies/now-playing|현재 상영 중|
|GET|/api/movies/coming-soon|곧 개봉|
|GET|/api/movies/featured|피처드 배너용|
|GET|/api/movies/:id|영화 상세|
|GET|/api/theaters|극장 목록 (거리순)|
|GET|/api/theaters/:id|극장 상세|
|GET|/api/showtimes?date=&movieId=&theaterId=|상영 시간표|
|GET|/api/bookings|전체 예매 내역|
|GET|/api/bookings/upcoming|다가오는 예매|
|GET|/api/bookings/past|지난 예매|
|GET|/api/users/profile|유저 프로필|

앱 시작 시 MockData.kt의 데이터가 자동으로 DB에 삽입되며, 이미 데이터가 있으면 스킵합니다.
