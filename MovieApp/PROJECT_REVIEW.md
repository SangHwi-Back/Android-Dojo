# MovieApp 프로젝트 종합 점검 보고서

**작성일**: 2026-06-24
**프로젝트**: Android 영화 예매 앱 + NestJS 서버

---

## 📋 목차

1. [프로젝트 개요](#프로젝트-개요)
2. [현재 구조 분석](#현재-구조-분석)
3. [수정 및 보완 사항](#수정-및-보완-사항)
4. [서버 개발 가이드](#서버-개발-가이드)
5. [테스트 전략 및 시작 가이드](#테스트-전략-및-시작-가이드)
6. [우선순위 및 액션 아이템](#우선순위-및-액션-아이템)

---

## 프로젝트 개요

### 기술 스택

**Android (Kotlin)**
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 36
- 주요 라이브러리:
  - Hilt (DI)
  - Retrofit + OkHttp (네트워킹)
  - Navigation Component + SafeArgs
  - ViewBinding + DataBinding
  - Coil (이미지 로딩)
  - Kotlin Coroutines

**Backend (NestJS + TypeScript)**
- NestJS v10
- TypeORM + PostgreSQL
- HTTPS (mkcert)
- 포트: 3009

### 예매 플로우

```
BookFragment (영화 선택)
    ↓
BookTheaterFragment (극장 선택)
    ↓
BookScheduleFragment (날짜/시간 선택)
    ↓
BookSeatFragment (좌석 선택)
    ↓
(예매 확인 화면 미구현)
```

---

## 현재 구조 분석

### 1. 아키텍처

**장점**
- MVVM 패턴 적용 (ViewModel + Repository)
- Hilt를 통한 의존성 주입 구현
- Repository 인터페이스를 통한 추상화
- Navigation Component 활용한 명확한 화면 전환

**구조**
```
app/src/main/java/com/example/moviceapp/
├── book/                    # 예매 관련 UI
│   ├── BookFragment.kt
│   ├── BookViewModel.kt
│   ├── BookTheaterFragment.kt
│   ├── BookScheduleFragment.kt
│   └── BookSeatFragment.kt
├── search/                  # 검색/홈 화면
│   ├── SearchFragment.kt
│   ├── SearchViewModel.kt
│   └── MovieBottomSheet.kt
├── myinfo/                  # 사용자 정보
│   └── MyInfoFragment.kt
├── repo/                    # 데이터 레이어
│   ├── MovieRepository.kt
│   ├── MovieRepositoryImpl.kt
│   ├── MovieService.kt
│   ├── RetrofitModule.kt
│   └── MockData.kt
└── common/                  # 공통 컴포넌트
    ├── ThumbnailAdapter.kt
    └── Fragment+Util.kt
```

### 2. 데이터 레이어 분석

**MovieService.kt** (app/src/main/java/com/example/moviceapp/repo/MovieService.kt:1)
```kotlin
interface MovieService {
    @GET("/api/movies")
    fun getMovies(): Call<List<Movie>>

    @GET("/api/movies/{path}")
    fun getMovies(@Path("path") path: String): Call<List<Movie>>

    @GET("/api/movies/{id}")
    fun getMovieDetail(@Path("id") id: String): Call<Movie>
}
```

**문제점**:
- `/api/movies/{path}`와 `/api/movies/{id}` 엔드포인트가 충돌 가능
- `{id}`는 숫자인데 `String`으로 받고 있음

**MovieRepositoryImpl.kt** (app/src/main/java/com/example/moviceapp/repo/MovieRepositoryImpl.kt:18)
- `APIResult` sealed class로 성공/실패 처리
- suspend 함수로 코루틴 지원
- 에러 처리는 throw로 처리 (상위 레이어에 위임)

### 3. 예매 플로우 상세

#### BookFragment.kt (app/src/main/java/com/example/moviceapp/book/BookFragment.kt:19)

**현재 구현**
- 영화 목록을 3열 Grid로 표시
- ViewModel에서 영화 데이터 가져오기
- 클릭 시 BookTheaterFragment로 이동

**문제점**
- lifecycleScope.launch에서 에러 처리 없음 (43-46번 줄)
- 로딩 상태 UI 없음
- 빈 리스트 처리 없음

#### BookTheaterFragment.kt (app/src/main/java/com/example/moviceapp/book/BookTheaterFragment.kt:21)

**현재 구현**
- Mock 데이터 사용 (`TheatersMock.list`)
- 선택한 영화 정보 표시
- 극장 목록 표시 및 선택

**문제점**
- 서버 연동 없이 Mock 데이터만 사용 (54번 줄)
- 영화별 상영 극장 필터링 없음
- 거리 계산 로직 없음 (GPS 연동 미구현)

#### BookScheduleFragment.kt (app/src/main/java/com/example/moviceapp/book/BookScheduleFragment.kt:26)

**현재 구현**
- 날짜 선택 (오늘부터 7일)
- 시간 선택 (Mock 데이터)
- 선택한 날짜에 따라 시간표 동적 변경

**문제점**
- Mock 데이터만 사용 (`ShowtimeMock`)
- 영화 + 극장 조합에 따른 실제 상영시간표 조회 없음
- 잔여 좌석 정보 미표시

#### BookSeatFragment.kt (app/src/main/java/com/example/moviceapp/book/BookSeatFragment.kt:15)

**현재 구현**
- 96개 좌석 고정 표시 (A1-H12)
- Custom ItemDecoration으로 좌석 그룹 간격 처리

**문제점**
- 좌석 선택 로직 없음
- 이미 예매된 좌석 표시 없음
- 좌석 등급(일반/VIP) 없음
- 예매 확정 기능 없음
- 다음 화면 없음

---

## 수정 및 보완 사항

### 🔴 Critical (즉시 수정 필요)

#### 1. API 엔드포인트 충돌 해결

**문제**
```kotlin
// MovieService.kt
@GET("/api/movies/{path}")        // now-playing, coming-soon 등
fun getMovies(@Path("path") path: String): Call<List<Movie>>

@GET("/api/movies/{id}")          // 숫자 ID
fun getMovieDetail(@Path("id") id: String): Call<Movie>
```

**해결 방안**
```kotlin
interface MovieService {
    @GET("/api/movies")
    fun getMovies(): Call<List<Movie>>

    @GET("/api/movies/category/{category}")  // 카테고리 명시
    fun getMoviesByCategory(@Path("category") category: String): Call<List<Movie>>

    @GET("/api/movies/{id}")
    fun getMovieDetail(@Path("id") id: Int): Call<Movie>  // Int로 변경
}
```

**서버 컨트롤러 수정 필요**
```typescript
// movies.controller.ts
@Get('category/:category')  // 새 경로
findByCategory(@Param('category') category: string) {
  return this.moviesService.findByCategory(category);
}
```

#### 2. 에러 처리 및 로딩 상태

**BookFragment.kt 개선**
```kotlin
// 현재
lifecycleScope.launch {
    val movies = viewModel.getMovies()
    adapter.submitList(movies)
}

// 개선안
private fun loadMovies() {
    lifecycleScope.launch {
        binding.progressBar.visibility = View.VISIBLE
        binding.errorText.visibility = View.GONE

        try {
            val movies = viewModel.getMovies()
            if (movies.isEmpty()) {
                showEmptyState()
            } else {
                adapter.submitList(movies)
            }
        } catch (e: Exception) {
            showError(e.message ?: "영화 목록을 불러올 수 없습니다")
        } finally {
            binding.progressBar.visibility = View.GONE
        }
    }
}
```

**레이아웃에 추가 필요**
- ProgressBar (로딩 중)
- TextView (에러 메시지)
- 빈 상태 UI

#### 3. BookSeatFragment 좌석 선택 기능

**현재 상태**: 좌석 표시만 됨
**필요 기능**:
- 좌석 선택/해제 토글
- 이미 예매된 좌석 비활성화
- 최대 선택 개수 제한
- 선택된 좌석 표시 (색상 변경)
- 예매 확정 버튼

**구현 방향**
```kotlin
data class Seat(
    val id: String,          // "A1", "B2"
    val status: SeatStatus,  // AVAILABLE, OCCUPIED, SELECTED
    val price: Int = 10000
)

enum class SeatStatus {
    AVAILABLE,   // 선택 가능
    OCCUPIED,    // 이미 예매됨
    SELECTED     // 사용자가 선택
}

class BookSeatFragment : Fragment() {
    private val selectedSeats = mutableListOf<Seat>()
    private val maxSeats = 4

    private fun onSeatClick(seat: Seat) {
        when (seat.status) {
            OCCUPIED -> showToast("이미 예매된 좌석입니다")
            AVAILABLE -> selectSeat(seat)
            SELECTED -> deselectSeat(seat)
        }
    }
}
```

### 🟡 High Priority (단기 개선)

#### 4. 서버 연동 완성

**BookTheaterFragment Mock 데이터 제거**

현재: `TheatersMock.list` 사용
개선: 서버 API 호출

```kotlin
// 새 ViewModel 필요
@HiltViewModel
class BookTheaterViewModel @Inject constructor(
    private val repository: TheaterRepository
) : ViewModel() {
    suspend fun getTheaters(movieId: Int): List<Theater> {
        return repository.getTheatersByMovie(movieId)
    }
}

// Repository 추가
interface TheaterRepository {
    suspend fun getTheatersByMovie(movieId: Int): List<Theater>
    suspend fun getTheaterById(id: Int): Theater
}
```

**서버 API 구현 필요**
```typescript
// theaters.controller.ts
@Get('movie/:movieId')
findByMovie(@Param('movieId', ParseIntPipe) movieId: number) {
  return this.theatersService.findByMovie(movieId);
}
```

#### 5. BookScheduleFragment 상영시간표 API 연동

```kotlin
// ShowtimeRepository
interface ShowtimeRepository {
    suspend fun getShowtimes(
        movieId: Int,
        theaterId: Int,
        date: String
    ): List<Showtime>
}

// Showtime 모델
data class Showtime(
    val id: Int,
    val time: String,           // "14:30"
    val hall: String,           // "IMAX Hall 1"
    val availableSeats: Int,    // 42
    val totalSeats: Int         // 120
)
```

**서버 엔드포인트**
```
GET /api/showtimes?movieId=1&theaterId=2&date=2026-05-15
```

#### 6. 예매 확정 화면 구현

**BookConfirmFragment.kt** 신규 생성 필요

화면 구성:
- 선택한 영화 정보
- 극장, 날짜, 시간
- 좌석 목록
- 총 금액
- 결제 버튼

**예매 API**
```kotlin
interface BookingRepository {
    suspend fun createBooking(booking: BookingRequest): Booking
}

data class BookingRequest(
    val movieId: Int,
    val theaterId: Int,
    val showtimeId: Int,
    val seats: List<String>,
    val userId: Int? = null  // Guest는 null
)
```

#### 7. BuildConfig 포트 불일치

**문제**
- build.gradle.kts: PORT = 3000 가정 (build.gradle.kts:35)
- 서버 실제 포트: 3009 (main.ts:17)

**해결**
```kotlin
// local.properties
network.ip.local=192.168.x.x
network.port.local=3009  // 실제 포트로 통일
```

### 🟢 Medium Priority (중기 개선)

#### 8. 이미지 URL 처리

현재 Movie 모델:
```kotlin
data class Movie(
    val posterURL: String? = null,
    val backdropURL: String? = null,
)
```

- nullable인데 기본값 처리 일관성 부족
- BookTheaterFragment:38에서 fallback 사용 중

**개선안**
```kotlin
// 확장 함수 활용
fun ImageView.loadMoviePoster(url: String?) {
    load(url) {
        placeholder(R.drawable.placeholder_movie)
        error(R.drawable.error_movie)
        crossfade(true)
    }
}
```

#### 9. 검색 기능 구현

SearchFragment가 있지만 실제 검색 기능은 미구현

**필요 사항**
```kotlin
// SearchViewModel에 추가
suspend fun searchMovies(query: String): List<Movie> {
    return repository.searchMovies(query)
}

// API
@GET("/api/movies/search")
fun searchMovies(@Query("q") query: String): Call<List<Movie>>
```

#### 10. MyInfoFragment 예매 내역 연동

현재: Mock 데이터
개선: 서버에서 사용자 예매 내역 조회

```kotlin
interface BookingRepository {
    suspend fun getUserBookings(userId: Int): List<Booking>
    suspend fun getUpcomingBookings(userId: Int): List<Booking>
}
```

### 🔵 Low Priority (장기 개선)

#### 11. 오프라인 대응

- Room Database로 로컬 캐싱
- 네트워크 연결 확인
- Retry 로직

#### 12. 사용자 인증

현재: Guest만 지원
향후: 회원가입/로그인

#### 13. 결제 시스템

현재: 좌석 선택까지만
향후: 실제 결제 연동

#### 14. 푸시 알림

예매 시간 30분 전 알림 등

---

## 서버 개발 가이드

### 현재 서버 구조

```
movie-app-server/src/
├── movies/             # 영화 API
├── theaters/           # 극장 API
├── showtimes/          # 상영시간표 API
├── bookings/           # 예매 API
├── users/              # 사용자 API
└── database/           # DB 설정, Seeder
```

### 개발 방식 권장사항

#### 1. API 우선 개발 (API-First Development)

**순서**
1. API 명세 정의 (Swagger/OpenAPI)
2. 서버 엔드포인트 구현
3. Android에서 Retrofit Service 정의
4. Repository 구현
5. ViewModel에서 호출
6. UI 연결

**예시: 극장 목록 조회**

**Step 1: 서버 구현**
```typescript
// theaters.controller.ts
@Get('movie/:movieId')
@ApiOperation({ summary: '특정 영화를 상영하는 극장 목록' })
@ApiResponse({ status: 200, type: [Theater] })
findByMovie(@Param('movieId', ParseIntPipe) movieId: number) {
  return this.theatersService.findByMovie(movieId);
}

// theaters.service.ts
async findByMovie(movieId: number): Promise<Theater[]> {
  return this.theaterRepository
    .createQueryBuilder('theater')
    .innerJoin('theater.showtimes', 'showtime')
    .where('showtime.movieId = :movieId', { movieId })
    .distinct(true)
    .getMany();
}
```

**Step 2: Android 구현**
```kotlin
// MovieService.kt
@GET("/api/theaters/movie/{movieId}")
fun getTheatersByMovie(@Path("movieId") movieId: Int): Call<List<Theater>>

// TheaterRepository.kt
interface TheaterRepository {
    suspend fun getTheatersByMovie(movieId: Int): List<Theater>
}

@Singleton
class TheaterRepositoryImpl @Inject constructor(
    private val service: MovieService
) : TheaterRepository {
    override suspend fun getTheatersByMovie(movieId: Int): List<Theater> {
        return when (val result = service.getTheatersByMovie(movieId).toAPIResult()) {
            is APIResult.Success -> result.data
            is APIResult.Failure -> throw result.error
        }
    }
}
```

#### 2. 공통 에러 처리

**서버측**
```typescript
// common/filters/http-exception.filter.ts
@Catch()
export class AllExceptionsFilter implements ExceptionFilter {
  catch(exception: unknown, host: ArgumentsHost) {
    const ctx = host.switchToHttp();
    const response = ctx.getResponse();

    const status = exception instanceof HttpException
      ? exception.getStatus()
      : 500;

    response.status(status).json({
      statusCode: status,
      message: exception.message || 'Internal server error',
      timestamp: new Date().toISOString(),
    });
  }
}
```

**Android측**
```kotlin
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val code: Int, val message: String) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}
```

#### 3. DTO 활용

**서버**: Entity와 DTO 분리
```typescript
// entities/movie.entity.ts
@Entity()
export class Movie {
  @PrimaryGeneratedColumn()
  id: number;

  @Column()
  title: string;

  @Column({ type: 'text', nullable: true })
  description: string;

  @Column({ type: 'decimal', precision: 3, scale: 1 })
  rating: number;

  // ... 다른 필드들
}

// dto/movie.dto.ts
export class MovieDto {
  id: number;
  title: string;
  duration: string;
  rating: number;
  posterURL?: string;

  // Entity를 간단하게 변환
  static fromEntity(movie: Movie): MovieDto {
    return {
      id: movie.id,
      title: movie.title,
      duration: `${movie.durationMinutes}m`,
      rating: movie.rating,
      posterURL: movie.posterPath,
    };
  }
}
```

#### 4. 개발 환경 분리

**local.properties (Android - Git 무시됨)**
```properties
network.ip.local=192.168.1.100  # 개발 PC IP
network.port.local=3009
```

**.env (NestJS - Git 무시됨)**
```env
PORT=3009
DB_HOST=localhost
DB_PORT=5432
DB_USERNAME=postgres
DB_PASSWORD=yourpassword
DB_DATABASE=movieapp
```

#### 5. Mock 서버 활용

실제 DB 없이도 개발 가능하도록

```typescript
// movies.service.ts
@Injectable()
export class MoviesService {
  private useMockData = process.env.USE_MOCK === 'true';

  async findAll(): Promise<Movie[]> {
    if (this.useMockData) {
      return MOCK_MOVIES;  // 하드코딩된 데이터
    }
    return this.movieRepository.find();
  }
}
```

#### 6. API 버저닝

향후 호환성을 위해
```typescript
// main.ts
app.setGlobalPrefix('api/v1');

// URL: https://localhost:3009/api/v1/movies
```

#### 7. CORS 설정

```typescript
// main.ts
app.enableCors({
  origin: [
    'http://localhost:3000',  // 웹 개발용
    // Android는 자체 인증서 신뢰 필요
  ],
  credentials: true,
});
```

#### 8. 로깅

```typescript
// 요청/응답 로깅
import { Logger } from '@nestjs/common';

@Controller('movies')
export class MoviesController {
  private readonly logger = new Logger(MoviesController.name);

  @Get()
  findAll() {
    this.logger.log('Fetching all movies');
    return this.moviesService.findAll();
  }
}
```

### 추천 개발 흐름

```
[서버] Entity 정의 → [서버] Service 구현 → [서버] Controller 생성
   ↓
[테스트] Postman/curl로 API 확인
   ↓
[Android] Data Model 정의 → [Android] Service 인터페이스
   ↓
[Android] Repository 구현 → [Android] ViewModel
   ↓
[Android] UI 연결 및 테스트
```

### 서버 실행 체크리스트

1. PostgreSQL 실행 확인
2. `.env` 파일 설정
3. `npm run start:dev` 실행
4. https://localhost:3009/api 접속 확인
5. Android local.properties IP/포트 일치 확인
6. mkcert 인증서 유효성 확인

---

## 테스트 전략 및 시작 가이드

### 현재 테스트 상태

**프로젝트는 테스트 가능한 상태이나 실제 테스트는 거의 없음**

현재 파일:
- `ExampleUnitTest.kt`: 기본 샘플만
- `ExampleInstrumentedTest.kt`: 기본 샘플만

의존성은 이미 추가됨:
```kotlin
// build.gradle.kts
testImplementation(libs.junit)                         // JUnit 4.13.2
androidTestImplementation(libs.androidx.junit)         // AndroidX Test
androidTestImplementation(libs.androidx.espresso.core) // Espresso
androidTestImplementation(libs.androidx.navigation.testing) // Navigation 테스트
```

### 테스트 전략

#### 1. Unit Test (JVM)

**목적**: ViewModel, Repository, 비즈니스 로직 테스트

**테스트해야 할 클래스**
- `BookViewModel`
- `SearchViewModel`
- `MovieRepositoryImpl`
- `APIResult` 변환 로직

**필요 추가 의존성**
```kotlin
// build.gradle.kts
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
testImplementation("io.mockk:mockk:1.13.8")  // Mocking
testImplementation("app.cash.turbine:turbine:1.0.0")  // Flow 테스트
```

**예시: BookViewModel 테스트**

```kotlin
// app/src/test/java/com/example/moviceapp/book/BookViewModelTest.kt
package com.example.moviceapp.book

import com.example.moviceapp.repo.Movie
import com.example.moviceapp.repo.MovieRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookViewModelTest {

    private lateinit var repository: MovieRepository
    private lateinit var viewModel: BookViewModel

    @Before
    fun setup() {
        repository = mockk()
        viewModel = BookViewModel(repository)
    }

    @Test
    fun `getMovies returns list from repository`() = runTest {
        // Given
        val mockMovies = listOf(
            Movie(1, "Dune", "2h", 8.5, "2026-01-01", emptyList(), "Epic")
        )
        coEvery { repository.getMovies() } returns mockMovies

        // When
        val result = viewModel.getMovies()

        // Then
        assertEquals(1, result.size)
        assertEquals("Dune", result[0].title)
    }

    @Test(expected = Exception::class)
    fun `getMovies throws exception when repository fails`() = runTest {
        // Given
        coEvery { repository.getMovies() } throws Exception("Network error")

        // When
        viewModel.getMovies()

        // Then: exception thrown
    }
}
```

**실행**
```bash
./gradlew test
# 또는 Android Studio에서 클래스 우클릭 → Run 'BookViewModelTest'
```

#### 2. Integration Test (Android Device)

**목적**: Fragment, Navigation, UI 테스트

**필요 추가 의존성**
```kotlin
androidTestImplementation("androidx.test:runner:1.5.2")
androidTestImplementation("androidx.test:rules:1.5.0")
androidTestImplementation("com.google.dagger:hilt-android-testing:2.57.1")
kspAndroidTest("com.google.dagger:hilt-android-compiler:2.57.1")
androidTestImplementation("androidx.fragment:fragment-testing:1.5.6")
```

**예시: BookFragment 테스트**

```kotlin
// app/src/androidTest/java/com/example/moviceapp/book/BookFragmentTest.kt
package com.example.moviceapp.book

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.moviceapp.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BookFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun fragment_displays_recycler_view() {
        // Given
        launchFragmentInContainer<BookFragment>(
            themeResId = R.style.Theme_MovieApp
        )

        // Then
        onView(withId(R.id.bookRecyclerGridView))
            .check(matches(isDisplayed()))
    }
}
```

**실행**
```bash
./gradlew connectedAndroidTest
# 에뮬레이터나 실기기 필요
```

#### 3. Navigation Test

**예시**
```kotlin
// app/src/androidTest/java/com/example/moviceapp/NavigationTest.kt
package com.example.moviceapp

import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @Test
    fun navigate_from_book_to_theater_fragment() {
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )

        // 테스트 시나리오 작성
        // ...
    }
}
```

### 테스트 시작 가이드

#### Phase 1: ViewModel 테스트부터 시작 (추천)

1. `BookViewModelTest.kt` 생성
2. Mock Repository로 격리된 테스트
3. 성공/실패 케이스 모두 작성

**시작 체크리스트**
- [ ] kotlinx-coroutines-test 추가
- [ ] mockk 추가
- [ ] BookViewModelTest 클래스 생성
- [ ] getMovies() 성공 케이스 테스트
- [ ] getMovies() 실패 케이스 테스트
- [ ] 테스트 실행 및 통과 확인

#### Phase 2: Repository 테스트

1. Fake MovieService 구현
2. APIResult 변환 로직 테스트
3. 네트워크 에러 시나리오 테스트

```kotlin
// FakeMovieService.kt
class FakeMovieService : MovieService {
    var shouldFail = false
    var mockMovies = emptyList<Movie>()

    override fun getMovies(): Call<List<Movie>> {
        return if (shouldFail) {
            throw IOException("Network error")
        } else {
            FakeCall(mockMovies)
        }
    }
}
```

#### Phase 3: UI 테스트 (선택)

UI 테스트는 시간이 오래 걸리므로 우선순위를 낮춤

**시작한다면**
- [ ] Hilt Android Testing 설정
- [ ] Fragment Testing 설정
- [ ] 간단한 화면 표시 테스트부터

### TDD 적용 예시

**새 기능: 좌석 선택**

1. **테스트 먼저 작성**
```kotlin
@Test
fun `좌석 선택 시 선택 상태로 변경된다`() {
    // Given
    val seat = Seat("A1", SeatStatus.AVAILABLE)

    // When
    viewModel.selectSeat(seat)

    // Then
    assertEquals(SeatStatus.SELECTED, viewModel.seats.value.find { it.id == "A1" }?.status)
}

@Test
fun `이미 선택된 좌석 클릭 시 선택 해제된다`() {
    // Given
    val seat = Seat("A1", SeatStatus.SELECTED)

    // When
    viewModel.deselectSeat(seat)

    // Then
    assertEquals(SeatStatus.AVAILABLE, viewModel.seats.value.find { it.id == "A1" }?.status)
}
```

2. **테스트 실패 확인**
3. **기능 구현**
4. **테스트 통과 확인**

### 테스트 가능성을 높이는 구조

**나쁜 예**
```kotlin
class BookFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            val movies = RetrofitModule.provideApiService(/* ... */).getMovies().await()
            adapter.submitList(movies)
        }
    }
}
```

**좋은 예** (현재 구조)
```kotlin
class BookFragment : Fragment() {
    private val viewModel: BookViewModel by viewModels()  // Hilt 주입

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            val movies = viewModel.getMovies()  // 테스트 가능
            adapter.submitList(movies)
        }
    }
}
```

### 커버리지 목표

**현실적인 목표**
- Unit Test: 70% 이상
- Integration Test: 주요 플로우만
- UI Test: Critical Path만

**우선순위**
1. ViewModel (높음)
2. Repository (높음)
3. Navigation (중간)
4. UI (낮음)

---

## 우선순위 및 액션 아이템

### Week 1: Critical Issues

**목표: 앱이 정상 동작하도록 핵심 수정**

- [ ] API 엔드포인트 충돌 해결
  - MovieService.kt 수정
  - 서버 컨트롤러 경로 변경
  - 테스트 (Postman)

- [ ] BookFragment 에러 처리 추가
  - 로딩 상태 UI
  - 에러 메시지 표시
  - 빈 리스트 처리

- [ ] BookSeatFragment 좌석 선택 기능
  - Seat 데이터 모델
  - 선택/해제 로직
  - UI 업데이트

- [ ] 빌드 설정 포트 통일
  - local.properties 확인
  - 서버 포트 통일

### Week 2: 서버 연동

**목표: Mock 데이터 제거, 실제 API 연동**

- [ ] TheaterRepository 구현
  - 서버 API 구현
  - Android Repository 구현
  - BookTheaterFragment 연동

- [ ] ShowtimeRepository 구현
  - 서버 API 구현
  - Android Repository 구현
  - BookScheduleFragment 연동

- [ ] 예매 확정 화면
  - BookConfirmFragment 생성
  - Navigation 추가
  - 예매 API 연동

### Week 3: 테스트 및 품질 개선

**목표: 안정성 확보**

- [ ] Unit Test 작성
  - BookViewModelTest
  - SearchViewModelTest
  - MovieRepositoryImplTest

- [ ] 에러 처리 일관성
  - 공통 에러 처리 로직
  - 네트워크 에러 UI
  - Retry 메커니즘

- [ ] 이미지 로딩 개선
  - 확장 함수 활용
  - Placeholder/Error 이미지

### Week 4+: 추가 기능

- [ ] 검색 기능 구현
- [ ] MyInfoFragment 서버 연동
- [ ] 사용자 인증
- [ ] 오프라인 대응 (Room)

---

## 참고 자료

### Android 아키텍처 가이드
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Guide to app architecture](https://developer.android.com/jetpack/guide)

### 테스트
- [Android Testing Codelab](https://developer.android.com/codelabs/advanced-android-kotlin-training-testing-basics)
- [Testing with Hilt](https://developer.android.com/training/dependency-injection/hilt-testing)

### NestJS
- [NestJS Docs](https://docs.nestjs.com/)
- [TypeORM Documentation](https://typeorm.io/)

---

## 결론

### 주요 발견 사항

1. **아키텍처는 견고함**: MVVM + Hilt + Repository 패턴 잘 적용됨
2. **서버 연동 미완성**: Mock 데이터에 의존 중
3. **테스트 부재**: 테스트 가능한 구조이나 실제 테스트 없음
4. **예매 플로우 불완전**: 좌석 선택까지만, 확정 기능 없음
5. **에러 처리 부족**: 네트워크 실패, 빈 데이터 처리 미흡

### 권장 개발 순서

```
1. API 엔드포인트 수정 (1일)
   ↓
2. 에러 처리 및 로딩 상태 추가 (2일)
   ↓
3. BookSeatFragment 완성 (3일)
   ↓
4. 서버 API 구현 및 연동 (1주)
   ↓
5. 예매 확정 기능 (3일)
   ↓
6. Unit Test 작성 (1주)
   ↓
7. 추가 기능 (검색, 인증 등)
```

### 프로젝트 건강도: 7/10

**강점**
- 좋은 아키텍처 기반
- Hilt DI 활용
- Navigation Component 사용
- 서버 구조 잘 설계됨

**약점**
- 서버 연동 미완성
- 테스트 부재
- 에러 처리 부족
- 예매 플로우 미완성

**다음 스텝**: Week 1 Critical Issues부터 시작 권장

---

# BookChooseInfoFragment 리팩토링 점검 (예매 플로우 재설계)

> 이전 섹션의 `BookTheaterFragment` / `BookScheduleFragment` / `BookSeatFragment` 3-Fragment 구조는 이후 **단일 `BookChooseInfoFragment` + ViewPager2 2단** 구조로 완전히 교체되었습니다. `nav_graph.xml`에서 세 Fragment 관련 destination이 모두 제거되고 `bookChooseInfoFragment` 하나로 대체된 것을 확인했습니다. 위 섹션의 해당 파일 관련 작업 항목(4~10번)은 더 이상 유효하지 않으며, 이 섹션이 최신 상태입니다.

## 구조 요약

- **상단 ViewPager2** (`movie_view_pager`): 영화 배열을 스와이프로 넘기는 캐러셀. `MoviePagerAdapter` 사용.
- **하단 ViewPager2** (`movie_choose_info_view_pager`): `isEnabled = false`로 스와이프 잠금, `BookChooseInformationAdapter`가 페이지 3개(`THEATER`/`SHOWTIME`/`SEAT`, `item_book_choose_theater.xml` / `item_book_choose_showtime.xml` / `item_book_choose_seat.xml`)를 고정 개수(`getItemCount() = 3`)로 관리. 페이지 이동은 `BookChooseInfoFragment.goNextAnimated()`가 스냅샷+애니메이션으로 수행.
- **상태 관리**: `BookChooseInfoViewModel`이 `BookChooseInfoModel`을 `MutableStateFlow`로 보관, 극장/날짜/시간/좌석 리스트는 각각 별도 `StateFlow`로 분리.

## 🔴 Critical — 지금 실행하면 즉시 문제가 되는 것들

### 1. StateFlow 갱신이 실제로 발생하지 않음 (가장 근본적인 버그)

`BookChooseInfoViewModel.kt`의 아래 함수들이 전부 같은 패턴의 버그를 가지고 있습니다:

```kotlin
fun selectTheater(theater: Theater) {
    _model.value.selectedTheater = theater }   // ❌ 기존 객체를 in-place mutation
```

`selectTheater`, `selectShowDate`, `selectShowtime`, `selectSeat`, `goBookInfo`(`_model.value.currentBookInfo = ...`), `refreshMovieInfo`, `loadMovieInfo`(`model.value.currentBookInfo = info`) 전부 `_model.value`가 가리키는 **동일한 객체의 필드만 변경**하고 있습니다. `MutableStateFlow`는 새 참조가 할당되어야(`_model.value = 새_객체`) 구독자에게 재방출하므로, 위 함수들을 호출해도 `BookChooseInfoFragment`의 `viewModel.model.collect { ... }`(→ `goNextButton.isEnabled` 갱신)나 `BookChooseInformationAdapter`의 리바인딩이 전혀 트리거되지 않습니다. `setMovieAndRefresh`만 유일하게 `_model.value = BookChooseInfoModel(...)`로 새 객체를 할당해서 정상 동작합니다.

**해결 방향**: 나머지 함수들도 `_model.value = _model.value.copy(selectedTheater = theater)` 형태로 바꿔야 함.

### 2. `ShowtimeViewHolder`·`BookChooseSeatViewHolder`의 `bind()`가 `TODO()`

- `ShowtimeDateViewHolder`/`ShowtimeTimeViewHolder`는 정상 구현되어 있지만, 상위 추상 클래스를 보면 헷갈릴 수 있어 확인 필요 — 실제로는 두 서브클래스 모두 구현되어 있어 문제 없음.
- 반면 `BookChooseShowtimeViewHolder.kt:25`와 `BookChooseSeatViewHolder.kt:11`의 `bind()`는 **`TODO("Not yet implemented")`를 던짐**. `BookChooseInformationAdapter.onBindViewHolder`가 이 `bind()`를 호출하므로, SHOWTIME/SEAT 페이지가 화면에 붙는 순간 `NotImplementedError`로 **크래시**.

### 3. 클릭 이벤트가 사실상 전부 비어있음 (사용자가 언급한 부분)

| 위치 | 현재 상태 |
|---|---|
| `BookChooseTheaterViewHolder.kt:19` | `TheaterListAdapter {}` — 극장 클릭 콜백이 빈 람다. `viewModel.selectTheater(...)` 호출 누락 |
| `ShowtimeAdapter.kt:32` | `onClickListener(date: String) { TODO(...) }` — 날짜/시간 버튼 클릭 처리 미구현 |
| `BookChooseSeatViewHolder.kt:13` | `setSeats(seats: List<String>) {}` — 완전히 빈 함수. 어댑터·LayoutManager 생성 자체가 없음 (Theater/Showtime과 달리 RecyclerView가 아예 세팅 안 됨) |
| `BookChooseInfoFragment.kt` | `binding.goNextButton`에 `setOnClickListener`가 **아예 없음** — 버튼을 눌러도 `viewModel.goBookInfo()`가 호출되지 않아 다음 화면으로 못 넘어감 |

### 4. `ShowtimeClickHandler.onClickListener(date: String)` 시그니처가 날짜/시간 클릭을 구분 못 함

`ShowtimeDateViewHolder`와 `ShowtimeTimeViewHolder`가 동일한 `handler.onClickListener(model)`을 호출합니다. `model`은 버튼에 표시된 문자열(`"May 15"` 또는 `"6:45 PM"`)일 뿐이라, 어댑터(`ShowtimeAdapter`, 곧 handler)가 이 콜백만으로는 "날짜를 누른 것인지 시간을 누른 것인지" 구분할 근거가 약함(문자열 형식으로 추측 가능하지만 취약). 인터페이스를 `onClickDate(date: String)` / `onClickTime(slot: ShowtimeSlot)`로 분리하거나, `viewType`을 같이 넘기는 방식으로 재설계 필요.

## 🟡 누락된 기능

### 5. 좌석 데이터가 실제 API 연동이 안 되고 하드코딩

`BookChooseInfoViewModel.loadMovieInfo`의 `BookInfo.SEAT` 분기:
```kotlin
BookInfo.SEAT {
    // TODO: Fetch seats from repository
    _seatList.value = listOf("Wait", "For", "A", "While")
}
```
서버에 좌석 조회 엔드포인트 자체가 없는 상태로 보입니다 (Showtime에는 `availableSeats: number` 카운트만 있고 좌석별 상세 정보/점유 여부가 없음). 좌석 단위 데이터 모델을 서버에 추가할지, 클라이언트에서 `availableSeats` 개수만큼 임의 생성할지 결정 필요.

### 6. 날짜 선택 후 시간 목록 재조회 트리거가 없음

`selectShowDate(date)`가 모델만 바꾸고 끝나며, 주석으로 `// Go next when time is set`만 남아있습니다. 실제로는 날짜 선택 시 `loadMovieInfo(SHOWTIME, isShowDate = false)`를 호출해 그 날짜의 시간 슬롯을 다시 불러와야 하는데 이 연결이 빠져있습니다. (3번의 `ShowtimeAdapter.onClickListener` 미구현과 맞물린 문제 — 날짜 클릭 시 `viewModel.selectShowDate(date)` + `viewModel.loadMovieInfo(SHOWTIME)`를 함께 호출해야 함)

### 7. 선택 상태 하이라이트가 Theater에만 있고 Showtime/Seat엔 없음

`BookChooseTheaterViewHolder`의 내부 `TheaterListAdapter`는 `selectedPosition`을 추적해 선택된 극장을 색으로 구분합니다. 반면 `ShowtimeAdapter`(날짜/시간 버튼)와 `BookChooseSeatViewHolder`(좌석)에는 이런 선택 상태 추적이 전혀 없어 사용자가 뭘 선택했는지 시각적으로 알 수 없습니다.

## 🔵 설계 확인이 필요한 부분 (구현 전에 확정 필요)

### 8. ~~상단 3버튼(`button_theater`/`button_showtime`/`button_seat`)의 용도가 불명확~~ → 제외 결정

~~극장보다 상영시간을 먼저 고를 수 있게 하는 자유 이동 탭으로 고려됐던 버튼들.~~ 논의 결과 이론적으로 맞지 않아(시간 선택은 극장이 정해져야 의미가 있는 종속 관계) **제외하기로 결정**. `fragment_book_choose_info.xml`의 `button_theater`/`button_showtime`/`button_seat` 3개 `MaterialButton`과 관련 클릭 리스너(`viewModel.loadMovieInfo(THEATER/SHOWTIME/SEAT)` 호출부)를 제거 대상으로 표시.

### 9. 좌석 선택 이후 플로우가 정의되어 있지 않음

`BookChooseInfoViewModel.getNextBookInfo()`는 `SEAT` 다음을 다시 `THEATER`로 되돌립니다(`else -> BookInfo.THEATER`). 좌석까지 선택하고 "다음"을 누르면 예매가 확정되어야 할 텐데, 예매 확정/결제 화면에 대한 설계가 없습니다. (이전 섹션 "Week 2: 예매 확정 화면" 항목과 동일한 이슈가 여전히 남아있음)

## ⚪ 마이너 / 정리 대상

- `item_book_choose_movie.xml` — 빈 ConstraintLayout이며 코드베이스 어디서도 참조되지 않음 (`ItemBookChooseMovieBinding` 사용처 없음). 안 쓰면 삭제 검토.
- `ShowtimeTimeViewHolder.kt:6` — 주석 처리된 죽은 코드 `// NO USE` / `//const val SHOWTIME_TIME_VIEW_HOLDER_TYPE = 1` 정리.
- `BookChooseTheaterViewHolder.bind()`가 호출될 때마다 `layoutManager`/`adapter`를 재할당함 — 뷰홀더가 재사용될 경우 불필요한 재작업 (치명적이진 않음).

## 권장 작업 순서

1. **StateFlow 재할당 패턴으로 전면 수정** (1번) — 이게 선행되지 않으면 이후 클릭 이벤트를 다 연결해도 화면이 반응하지 않음
2. **TODO() 크래시 두 곳 제거** (2번) — `bind()` 구현 필수, 최소한 앱이 죽지 않게
3. **상단 3버튼 제거** (8번) — 자유 이동 탭 설계 폐기 확정, 관련 클릭 리스너 및 버튼 UI 정리
4. **클릭 콜백 연결** (3, 4번) — 극장 선택, 날짜/시간 선택 분리, 좌석 어댑터 신설, `goNextButton` 클릭 리스너 추가
5. **날짜→시간 재조회 연결** (6번)
6. **선택 하이라이트 통일** (7번)
7. 설계 확인 필요한 9번은 구현 전에 먼저 답을 정하고 진행
8. 좌석 API(5번)는 서버 작업이 선행되어야 하므로 별도 트랙으로 분리 가능

이 섹션은 리뷰 전용이며 아직 어떤 코드도 수정하지 않았습니다. 우선순위 확인 후 순서대로 작업을 진행할 예정입니다.

---

## 완료된 작업 (BookChooseInfoFragment 구현)

**1~8번 항목 구현 완료, 빌드 성공 확인.**

- **StateFlow 갱신 방식 수정** — `BookChooseInfoViewModel`의 `selectTheater`/`selectShowDate`/`selectShowtime`/`selectSeat`/`goBookInfo`/`loadMovieInfo`/`refreshMovieInfo`를 전부 `_model.update { it.copy(...) }` 패턴으로 교체. `MutableStateFlow.update{}`는 CAS 루프라 동시성 문제도 함께 방지.
- **크래시 유발 `TODO()` 제거** — `BookChooseShowtimeViewHolder.bind()`/`BookChooseSeatViewHolder.bind()`가 더 이상 예외를 던지지 않음.
- **상단 3버튼 제거** — `fragment_book_choose_info.xml`에서 `info_choose_linear_layout`(button_theater/showtime/seat) 삭제, `movie_view_pager` 상단 제약을 parent로 재연결. `BookChooseInfoFragment.kt`의 관련 클릭 리스너도 함께 제거.
- **클릭 콜백 전면 연결**
  - 극장 선택: `TheaterListAdapter`의 콜백이 `viewModel.selectTheater(theater)` 호출
  - 날짜/시간 선택: `ShowtimeClickHandler`를 `onClickDate`/`onClickTime` 두 메서드로 분리(기존엔 문자열 하나로 뭉쳐 있어 구분 불가능했던 부분), `ShowtimeAdapter`가 각각 `viewModel.selectShowDate(date)` + `viewModel.loadMovieInfo(SHOWTIME, isShowDate=false)`, `viewModel.selectShowtime(date, slot)` 호출로 연결
  - 좌석 선택: `BookChooseSeatViewHolder`에 `SeatListAdapter` 신규 구현(레이아웃매니저/어댑터 세팅 자체가 없던 상태였음), 클릭 시 `viewModel.selectSeat(seat)` 호출
  - `go_next_button`: 이제까지 클릭 리스너가 아예 없었음 → `viewModel.getNextBookInfo()`로 다음 단계를 계산하고 `viewModel.goBookInfo(next)` + 해당 단계의 데이터 `loadMovieInfo` 호출을 연결. 레이아웃의 잘못된 제약(`layout_constraintTop_toBottomOf="parent"`로 버튼이 화면 밖으로 밀려나 있던 문제)도 함께 수정.
- **날짜→시간 재조회 연결** — 날짜 클릭 시 `loadMovieInfo(SHOWTIME, isShowDate=false)`를 호출해 그 날짜의 실제 시간 슬롯을 다시 받아오도록 연결.
- **선택 상태 하이라이트 통일** — `ShowtimeAdapter`(날짜/시간), `SeatListAdapter`(좌석) 모두 `TheaterListAdapter`와 동일하게 내부 selectedIndex 추적 + 색상 반전(`green_accent`/`background_secondary`, 좌석은 `seat_selected`/`seat_available`) 방식으로 통일.
- **어댑터가 데이터 갱신을 아예 받지 못하던 잠재 버그 수정** — 구현 중 발견: `BookChooseInformationAdapter`의 `theaters`/`showDateList`/`showTimeList`/`seats`는 `onCreateViewHolder`가 딱 1번 호출될 때만 초기값으로 소비되고, 이후 Fragment의 `collect`가 받아온 새 데이터는 이미 생성된 ViewHolder에 전달할 경로가 없었음(`onBindViewHolder`가 `holder.bind(model)`만 호출하고 `setTheaters`/`setShowDateList` 등을 다시 부르지 않았음). `theaters`/`showDateList`/`showTimeList`/`seats`에 커스텀 setter를 추가해 값이 바뀔 때마다 `notifyItemChanged`를 호출하도록 하고, `onBindViewHolder`에서 ViewHolder 타입별로 최신 리스트를 다시 주입하도록 수정. 이게 없으면 클릭 이벤트를 다 연결해도 실제 화면 데이터가 절대 갱신되지 않았을 것.
- **최초 진입 시 극장 목록 로드 추가** — 버튼 제거로 인해 극장 목록을 처음 트리거할 방법이 사라져서, `onViewCreated` 끝에 `viewModel.loadMovieInfo(THEATER)` 최초 호출을 추가.
- **`ShowtimeAdapter`의 인덱싱 버그 수정** — 기존 `timeSlots[position].time`이 절대 position을 그대로 시간 리스트 인덱스로 써서 날짜 개수만큼 어긋나 있었음(`position - dates.size`로 수정).
- **마이너 정리** — `item_book_choose_movie.xml`(미사용 빈 레이아웃) 삭제, `ShowtimeTimeViewHolder.kt`의 죽은 주석 코드 제거(실제 상수는 `ShowtimeAdapter.kt`로 이동).

**의도적으로 보류한 항목**
- 9번(좌석 선택 이후 플로우/예매 확정 화면) — 설계 미확정 상태라 손대지 않음. 현재는 좌석까지 선택 후 "다음"을 누르면 `getNextBookInfo()`가 다시 `THEATER`로 순환하는 기존 동작이 그대로 유지됨.
- 5번(좌석 API 서버 연동) — `BookChooseInfoViewModel.loadMovieInfo`의 SEAT 분기는 여전히 `listOf("Wait", "For", "A", "While")` 하드코딩. 클릭/선택/하이라이트는 이 placeholder 데이터로 전부 정상 동작하도록 만들어뒀으므로, 서버에 실제 좌석 엔드포인트가 추가되면 이 부분만 교체하면 됨.

---

## 참고 자료 — 최신 영화 예매 앱의 좌석 선택 뷰는 어떻게 만드는가

지금 이 프로젝트의 `BookChooseSeatViewHolder`는 `GridLayoutManager` 기반 `RecyclerView`로 좌석을 그리드로 뿌리는 방식입니다. 이 방식 자체는 실무에서도 흔히 쓰이지만, 실제 서비스들은 몇 가지를 더 신경 씁니다. 아래는 CGV/메가박스/Fandango류 앱들이 공통으로 채택하는 패턴을 정리한 것입니다.

### 1. 렌더링 방식 — 왜 대부분 "커스텀 뷰 + Canvas"로 가는가

좌석 배치는 순수 그리드가 아닙니다. 통로, 커브형 스크린, 짝수 열만 있는 층, VIP존의 배치 변경 등 불규칙한 형태가 많습니다. `RecyclerView` + `GridLayoutManager`는 "고정된 열 수"를 전제로 하기 때문에, 홀마다 열 개수·통로 위치가 다르면 `spanSizeLookup`을 홀마다 새로 계산해야 하고, 확대/축소(핀치 줌)나 좌석 간 정밀한 간격 조정이 번거로워집니다.

그래서 실무에서는 두 갈래로 갈립니다:

- **소규모/심플한 UI**: 지금 이 프로젝트처럼 `RecyclerView` + `GridLayoutManager` + `ItemDecoration`으로 통로 간격을 표현. 구현이 빠르고 RecyclerView의 재활용(뷰홀더 풀링) 이점을 그대로 가져감. 좌석 수가 수백 석 이내면 이 방식으로도 충분히 부드럽게 동작.
- **정교한 좌석맵(대부분의 실제 서비스)**: `Canvas` 기반 커스텀 `View`(또는 Compose의 `Canvas`/`drawScope`)에 좌석 좌표를 직접 계산해서 그림. 각 좌석을 `Path`/`RoundRect`로 그리고, `MotionEvent`(또는 Compose의 `pointerInput`)로 터치 좌표를 좌석 인덱스로 역산해 선택 처리. 이 방식이 각광받는 이유:
  - 임의의 좌석 배치(비정형 통로, 커브, VIP 박스석 등)를 좌표 기반으로 자유롭게 표현 가능
  - 핀치 줌/팬(pan)을 `Matrix` 변환 하나로 자연스럽게 처리 — RecyclerView로는 이게 훨씬 번거로움
  - 좌석 수백~수천 개를 한 화면에 그려도 뷰 객체 생성 비용이 없음(그리기만 하면 되므로)

지금 프로젝트 규모(수십~백여 석, 홀 3종)에서는 RecyclerView 방식을 유지하는 게 합리적입니다. 다만 나중에 "핀치 줌으로 좌석맵 전체를 보다가 확대해서 정밀 선택" 같은 요구가 생기면 Canvas 기반으로 갈아탈 근거가 됩니다.

### 2. 좌석 상태(state) 모델링

실무 앱은 좌석 하나에 최소 아래 상태를 갖습니다(지금 프로젝트의 `Seat` 엔티티는 "배치" 정보만 있고 "상태"는 아직 없는 상태):

```
enum SeatStatus { AVAILABLE, SELECTED, OCCUPIED, HELD, DISABLED }
```

- `OCCUPIED`: 이미 확정 결제된 좌석
- `HELD`: 다른 사용자가 지금 막 선택 중이라 임시로 잠긴 좌석 (아래 3번 항목 참고)
- `DISABLED`: 물리적으로 없는 자리(간격 맞추기용 빈 칸, 고장난 좌석 등)

지금 프로젝트의 `Seat` 엔티티(배치도)와 실제 "이 상영 회차에 이 좌석이 비어있는지"는 별개 테이블로 분리하는 게 일반적입니다 — `Seat`는 극장×홀의 물리적 배치(불변에 가까움), `ShowtimeSeatStatus`(혹은 `SeatHold`) 같은 테이블이 "특정 Showtime + 특정 Seat"의 상태를 담당합니다. 그래야 같은 물리적 좌석이 회차마다 다른 예약 상태를 가질 수 있습니다.

### 3. 동시성 처리 — "좌석 임시 잠금(Seat Hold)"

여러 사용자가 동시에 같은 좌석을 고를 수 있으므로, 실무 앱은 거의 예외 없이 다음 흐름을 씁니다:

1. 사용자가 좌석을 탭하면 클라이언트는 즉시 낙관적으로 `SELECTED` 표시
2. 동시에 서버에 "이 좌석 N분간 홀드해줘" 요청 (`POST /showtimes/{id}/seats/hold`) — 서버는 해당 좌석을 `HELD` 상태로 바꾸고 TTL(보통 5~10분)을 건다
3. 결제/예매 확정 API 호출 시 홀드가 유효한지 검증 후 `OCCUPIED`로 전환
4. TTL이 지나거나 사용자가 이탈하면 서버가 자동으로 `HELD` → `AVAILABLE`로 되돌림(Redis TTL, DB 스케줄러, 혹은 Bull/Cron 잡)

다른 사용자의 실시간 좌석 상태 변경(누가 방금 그 좌석을 잡았다)을 반영하려면 WebSocket이나 짧은 주기 폴링으로 좌석맵을 다시 받아옵니다. 지금 이 프로젝트는 좌석 조회(`GET /api/seats`)만 있고 홀드/동시성 개념이 전혀 없는 상태라, 나중에 실제 결제 흐름을 붙일 때 반드시 고려해야 할 부분입니다.

### 4. UX 디테일

- **범례(legend)**: 화면 하단에 "이용가능/선택됨/예약불가/VIP" 색상 표를 항상 보여줌
- **스크린 표시**: 좌석맵 상단에 곡선형 "SCREEN THIS WAY" 바를 그려 방향감을 줌
- **선택 요약 바**: 좌석 선택 시 화면 하단에 고정으로 "A3, A4 · 2석 · ₩24,000"처럼 실시간 요약을 보여주고, 여기서 바로 결제로 진행
- **최대 선택 개수 제한**: 보통 1회 예매당 8~10석 제한을 두고, 넘으면 선택 불가 토스트
- **접근성**: 색상만으로 상태를 구분하지 않고 아이콘/테두리 스타일도 같이 사용(색맹 사용자 고려)

### 5. 현재 프로젝트에 적용한다면

- 지금 아키텍처(RecyclerView + GridLayoutManager, `Seat` 엔티티에 row/column/aisle/floor/seatType)는 "물리적 배치"를 표현하는 부분으로는 충분히 실무적입니다.
- 다음 단계로 필요한 건 **"이 배치도 위에서 이 회차, 이 순간에 어떤 좌석이 비어있는지"**를 나타내는 상태 계층입니다. 예를 들어 `ShowtimeSeat` 같은 중간 테이블(`showtime_id`, `seat_id`, `status`, `held_until`)을 추가하는 방향을 고려해볼 수 있습니다.
- 동시성(좌석 홀드)까지 가려면 서버에 TTL 기반 잠금 로직이 필요한데, 이건 지금 프로젝트 규모(로컬 개발/학습용)에서는 우선순위가 낮을 수 있습니다 — 다만 "왜 실제 서비스들이 이렇게 복잡하게 만드는지"를 이해하는 참고 자료로 남겨둡니다.

---

## 완료된 작업 — Seat 현재 상태(status) 및 홀드(Hold) 로직 구현

바로 위 참고 자료에서 설명한 "좌석 상태 모델링"과 "좌석 임시 잠금(Seat Hold)" 개념을 실제로 `Seat` 엔티티에 적용했습니다. 별도의 `ShowtimeSeat` 중간 테이블로 분리하지 않고, 사용자가 요청한 대로 **`Seat` 엔티티 자체에 상태 필드를 직접 추가**하는 단순한 형태로 구현했습니다 (지금 프로젝트 규모에서는 이쪽이 더 적절하다고 판단).

### 상태값 설계 — 왜 5개가 아니라 4개인가

```typescript
export enum SeatStatus {
  AVAILABLE = 'AVAILABLE',
  HELD = 'HELD',
  OCCUPIED = 'OCCUPIED',
  DISABLED = 'DISABLED',
}
```

참고 자료에서 언급했던 5가지 상태(`AVAILABLE/SELECTED/OCCUPIED/HELD/DISABLED`) 중 `SELECTED`는 일부러 빼고 4개만 서버에 persist했습니다. `SELECTED`는 "사용자가 지금 탭해서 고르는 중"이라는, **아직 서버에 알릴 필요조차 없는 순수 클라이언트 로컬 UI 상태**입니다. 서버가 알아야 하는 건 "이 사용자가 결제까지 이어갈 생각으로 이 좌석을 붙잡고 있다"는 `HELD` 뿐입니다. `SELECTED`까지 서버에 얹으면 소유자 정보 없이 상태만 있는 애매한 필드가 되어 오히려 혼란을 유발합니다.

### `HELD` 상태에 필요한 추가 필드

```typescript
@Column({ name: 'held_by_user_id', nullable: true })
heldByUserId: string | null;

@Column({ name: 'held_until', type: 'timestamp', nullable: true })
heldUntil: Date | null;
```

`HELD`는 "누가, 언제까지"가 없으면 의미가 없는 상태라 두 필드를 추가로 붙였습니다. 지금 프로젝트에는 실제 로그인/인증 시스템이 없으므로 `userId`는 클라이언트가 만들어 보내는 임의의 식별자(기기 UUID 등)로 취급합니다 — `users` 테이블과의 FK 관계는 아닙니다.

### 동시성 처리 — read-then-write가 아니라 트랜잭션 + row lock

가장 신경 쓴 부분입니다. "좌석을 조회해서 비어있으면 잠근다"를 순진하게 구현하면(조회 → 검사 → 저장이 별개의 쿼리) 두 요청이 동시에 들어왔을 때 둘 다 "비어있다"고 읽은 뒤 둘 다 저장에 성공해버리는 경쟁 상태(race condition)가 생깁니다. 이를 막기 위해 `SeatsService.holdSeat/releaseSeat/confirmSeat` 전부 TypeORM 트랜잭션 안에서 `lock: { mode: 'pessimistic_write' }`(Postgres의 `SELECT ... FOR UPDATE`)로 해당 좌석 row를 잠근 뒤 검사·수정을 한 트랜잭션 안에서 원자적으로 처리하도록 만들었습니다.

### 만료 처리 — 스케줄러 없이 lazy expiry로 해결

Redis TTL이나 Bull/Cron 잡을 새로 도입하는 대신, 두 지점에서 "지금 시각 기준으로 만료됐는지"를 즉석에서 확인하는 방식(lazy expiry)을 택했습니다:

- `holdSeat`/`releaseSeat`/`confirmSeat`: row를 잠근 직후 `heldUntil`이 지났으면 그 자리에서 `AVAILABLE`로 되돌리고 이어서 로직을 진행 (`expireIfNeeded`)
- `findAll`(좌석 목록 조회): 조회 직전에 해당 극장·홀에서 만료된 `HELD` 좌석들을 일괄 `UPDATE`로 `AVAILABLE`로 정리한 뒤 조회

이 방식은 별도 백그라운드 프로세스 없이도 "다음에 누군가 이 좌석을 만지거나 조회하는 시점"에 자연스럽게 정리되므로 지금 프로젝트 규모에 적절합니다. 다만 아무도 조회/시도하지 않는 좌석은 만료 이후에도 DB에는 `HELD`로 남아있을 수 있다는 한계가 있습니다 — 실제 서비스라면 주기적 배치 작업으로 보완합니다.

### 새 엔드포인트

| Method | URL | 설명 |
|---|---|---|
| POST | `/api/seats/:id/hold` | `{ userId, ttlMinutes? }` — 좌석을 TTL 동안 임시 잠금. 이미 `OCCUPIED`면 409, `DISABLED`면 400, 다른 사용자가 `HELD` 중이면 409 |
| POST | `/api/seats/:id/release` | `{ userId }` — 본인이 잡은 홀드만 해제 가능(남의 것 시도 시 403), 이미 안 잠겨있으면 그대로 반환(idempotent) |
| POST | `/api/seats/:id/confirm` | `{ userId }` — 본인이 유효하게 홀드 중인 좌석만 `OCCUPIED`로 확정. 홀드 없이 호출하면 400 |
| PATCH | `/api/seats/:id/status` | 관리용 직접 상태 지정(`AVAILABLE`/`OCCUPIED`/`DISABLED`). `HELD`는 소유자/TTL 정보가 필요해 여기선 거부하고 `/hold`를 쓰도록 안내 |

### 실제 검증 시나리오

1. userA가 좌석 홀드(TTL 5초) → 201, `status: HELD`
2. userB가 같은 좌석 즉시 홀드 시도 → **409 Conflict** ("다른 사용자가 잡고 있음")
3. userB가 남의 홀드를 release 시도 → **403 Forbidden**
4. 5초 대기 후 TTL 만료 → userB가 다시 홀드 시도 → **성공** (만료된 홀드는 자동으로 풀림)
5. userB가 confirm → **`OCCUPIED`로 확정**
6. `OCCUPIED` 상태에서 userC가 홀드 시도 → **409 Conflict** ("이미 예약됨")
7. PATCH로 `HELD`를 직접 지정 시도 → **400** (차단)
8. 홀드 없이 confirm 시도 → **400** ("활성 홀드 없음")

전부 기대대로 동작했고, 테스트에 사용한 좌석은 다시 `AVAILABLE`로 원복해뒀습니다.

### 알려진 한계 (지금 규모에서는 의도적으로 남겨둔 것들)

- **단일 프로세스 전제**: row lock은 하나의 Postgres 인스턴스 안에서만 유효합니다. 서버를 여러 대로 수평 확장하면 이 자체로는 문제없지만(Postgres가 잠금을 담당하므로), 만약 나중에 Redis 같은 별도 캐시 레이어로 홀드를 옮기면 그때는 분산 락 문제를 새로 고려해야 합니다.
- **`userId` 위조 가능**: 인증이 없으므로 클라이언트가 임의의 `userId` 문자열을 보내면 그대로 신뢰합니다. 실제 서비스라면 인증 토큰에서 추출한 사용자 식별자를 써야 합니다.
- **만료 좌석의 게으른 정리**: 위에서 언급한 대로, 아무도 건드리지 않는 좌석은 만료 후에도 조회 전까지 `HELD`로 남아있을 수 있습니다.