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

# MovieApp 추가수정 진행

현재 코드베이스 전체를 점검하겠습니다.

코드베이스를 점검한 결과, 문제를 정확히 짚어드리면 **애초에 프레이밍이 살짝 다릅니다.**

## 현재 구조 재확인

**Showtime 엔티티는 이미 정상입니다.** `movieId + theaterId + showDate + time + hall`을 각각 행으로 가지고 있고, seeder도 이미 모든 영화 × 모든 극장 × 모든 날짜 × 모든 시간 조합을 채워 넣고 있습니다. "한 영화가 여러 극장에서, 각 극장마다 여러 시간에 상영된다"는 요구사항은 Showtime 테이블 구조상 이미 지원됩니다.

**진짜 문제는 두 곳입니다:**

### 1. 극장 선택 스텝이 잘못된 테이블을 조회하고 있음

[bookings.controller.ts:35-41](movie-app-server/src/bookings/bookings.controller.ts#L35)의 `findTheater()` → [bookings.service.ts:32-39](movie-app-server/src/bookings/bookings.service.ts#L32)를 보면:

```ts
findTheater(movieId: string): Promise<Theater> {
  const bookPromise = this.bookingsRepo.findOne({ where: { movie: { id: parseInt(movieId) } } });
  return bookPromise.then(booking => booking.theater)
}
```

`Booking`(과거/예정된 실제 예매 내역) 테이블에서 movie로 아무 예매 1건을 찾아 그 예매에 딸린 극장 하나만 반환합니다. `Booking`은 "누가 언제 어느 극장 어느 좌석을 예매했는가"를 기록하는 테이블이지, "이 영화가 어느 극장들에서 상영 중인가"를 답하는 테이블이 아닙니다. 극장 목록은 **Showtime 테이블**에서 가져와야 합니다 — 이미 구현된 `findUniqueDates()`([showtimes.service.ts:26](movie-app-server/src/showtimes/showtimes.service.ts#L26))와 동일한 패턴으로, DISTINCT theater를 뽑으면 됩니다.

즉 "하나의 Booking에 여러 Theater가 반환되어야 한다"가 아니라, **"극장 목록 조회는 Booking이 아니라 Showtime에서 해야 한다"**가 맞는 진단입니다. 완료된 예매(Booking) 한 건은 실제로 극장 하나·날짜 하나·시간 하나로 확정되는 게 맞으므로 Booking 엔티티 자체는 지금처럼 단수 관계로 두는 게 맞습니다.

### 2. 시간 선택 단계가 통째로 빠져있음

현재 [BookScheduleFragment.kt](app/src/main/java/com/example/moviceapp/book/BookScheduleFragment.kt)는 날짜 그리드만 있고, 날짜를 고르자마자 `confirm_button`으로 바로 `BookSeatFragment`로 넘어갑니다. 시간(예: "6:45 PM", "9:00 PM")을 고르는 UI/로직이 없습니다. 다행히 서버 쪽 `GET /api/showtimes?movieId=&theaterId=&date=`([showtimes.controller.ts:14](movie-app-server/src/showtimes/showtimes.controller.ts#L14))는 이미 movie+theater+date로 필터링된 Showtime 리스트(시간 포함)를 반환하므로, **서버 신규 작업 없이** 이 엔드포인트로 시간 목록을 가져오면 됩니다.

## 발견한 버그 (별개, 하지만 지금 작업과 직결)

[BookTheaterFragment.kt:26-28](app/src/main/java/com/example/moviceapp/book/BookTheaterFragment.kt#L26):

```kotlin
class BookTheaterFragment(
    private val viewModel: BookTheaterViewModel
) : Fragment() {}
```

Fragment를 생성자 파라미터가 있는 형태로 만들면 Navigation Component가 리플렉션으로 인스턴스화할 때 기본 생성자(no-arg)를 찾지 못해 **크래시**가 납니다. Hilt를 쓰는 Fragment는 `by viewModels()` 위임으로 주입받아야지 생성자 주입은 불가합니다 (다른 Fragment들처럼). 지금 당장 BookTheaterFragment로 진입하면 바로 죽을 가능성이 높습니다.

## 작업 목록

**서버 (movie-app-server)**
1. `ShowtimesService`에 `findUniqueTheaters(movieId)` 추가 — `findUniqueDates`와 동일 패턴, DISTINCT theater 반환 (Theater 전체 객체 필요하므로 join 필요)
2. `ShowtimesController`에 `GET /api/showtimes/theaters?movieId=` 추가
3. `bookings.controller.ts`의 `findTheater`/`GET /bookings/theater`, `bookings.service.ts`의 `findTheater` 삭제 (더 이상 필요 없음, TODO였던 부분)

**안드로이드**
4. `BookTheaterFragment` 생성자 버그 수정 — `by viewModels()`로 전환
5. `BookingService.kt`: `getTheaters(movieId): Call<Theater>` → `Call<List<Theater>>`로 변경, 엔드포인트를 `/api/showtimes/theaters`로 교체
6. `BookingRepository`/`BookingRepositoryImpl`: `getTheater(): Theater?` → `getTheaters(): List<Theater>`
7. `BookTheaterViewModel`: 리스트 반환하도록 수정
8. `BookTheaterFragment`: `MoviePagerAdapter`의 `onMovieBind` 콜백에서 `theaterAdapter.submitList(listOf(theater))` → `submitList(theaters)`로 변경 (지금은 리스트를 만들어놓고 항상 1개짜리로 덮어쓰고 있음)
9. `BookScheduleFragment`: 날짜 그리드 아래에 시간 선택 RecyclerView 추가 — 날짜 선택 시 `GET /api/showtimes?movieId=&theaterId=&date=`로 시간 목록 조회, 시간까지 선택해야 `confirm_button` 활성화되도록 변경
10. 관련 레이아웃(`fragment_book_schedule.xml`)에 시간 선택 섹션 추가, `item_book_schedule_time.xml`(과거 스키마 참고) 재사용 또는 재작성

작은 것 하나 더: [fragment_book_theater.xml:17](app/src/main/res/layout/fragment_book_theater.xml#L17)에서 ViewPager2가 `app:layout_constraintBottom_toTopOf="@id/movie_view_pager"`로 자기 자신을 참조하고 있어 제약이 무효합니다. 시간 선택 UI 추가하면서 같이 정리하겠습니다.

바로 구현 진행하겠습니다.

**서버 작업부터 시작합니다.**

`/theaters`가 `/:id` 라우트보다 먼저 매칭돼야 합니다. Theaters 컨트롤러는 별개지만, showtimes 컨트롤러 순서는 `dates`, `theaters`, 그다음 `@Get()`(빈 경로) 순이라 문제없습니다. 이제 `bookings.controller.ts`/`bookings.service.ts`에서 TODO였던 부분을 제거합니다.

서버 빌드 확인합니다.

빌드 성공입니다. 실제로 동작하는지 서버를 띄워 테스트합니다.

`distinctOn` 정상 동작합니다. showtimes 필터 엔드포인트도 확인합니다.

이 엔드포인트로 시간까지 확보됩니다. 서버를 정리하고 안드로이드 작업으로 넘어갑니다.

**안드로이드 작업 시작.** BookingService/Repository/ViewModel부터 수정합니다.

`ShowtimeSlot` 데이터 클래스를 추가합니다.

BookingService.kt를 업데이트합니다.

BookingRepositoryImpl.kt를 업데이트합니다.

BookTheaterViewModel.kt를 업데이트합니다.

**BookTheaterFragment.kt** — 생성자 버그 수정 + 극장 리스트 반영.

`nextButton`이 극장 선택 후에도 비활성화될 일이 없도록, ViewPager2로 영화가 바뀌면 극장 목록도 초기화되니 selectedTheater와 버튼 상태도 리셋해야 합니다. 이를 반영해서 재작성합니다.

`onMovieBind` 콜백은 ViewPager2가 인접 페이지를 미리 바인딩할 때도 호출되어 실제 "현재 보이는 페이지"와 어긋날 수 있습니다. `BookScheduleFragment`가 쓰는 `registerOnPageChangeCallback` 패턴으로 통일하는 게 안전합니다.

BookTheaterFragment.kt를 registerOnPageChangeCallback 방식으로 수정합니다.

**BookScheduleFragment.kt** — 시간 선택 UI 추가.

기존 레이아웃이 있습니다. 날짜 버튼과 통일감 있게 `MaterialButton`으로 바꿉니다.

`fragment_book_schedule.xml`에 시간 그리드 섹션을 추가합니다.

date_recycler_view와 confirm_button 사이에 시간 선택 헤더+그리드를 스크롤 영역 안에 넣습니다.

BookScheduleViewModel에 시간 조회 함수를 추가합니다.

`select_time_title_text_view`가 문자열 리소스와 뷰 바인딩 ID로 잘 잡히는지 확인하고, `strings.xml`에 `label_select_time`이 있는지 확인합니다.

이제 안드로이드 빌드로 전체 검증합니다.

빌드 성공입니다. 서버도 최종 확인합니다.

모두 정상 동작합니다.

---

## 완료된 작업

**서버**
- `GET /api/showtimes/theaters?movieId=` 추가 — Showtime 테이블에서 DISTINCT 극장 목록 반환
- `bookings.controller.ts`/`bookings.service.ts`의 TODO였던 `findTheater` 로직 제거 (Booking 테이블을 극장 조회에 쓰던 잘못된 코드)

**안드로이드**
- `BookTheaterFragment` 생성자 주입 버그 수정 — `by viewModels()`로 전환 (그대로 뒀으면 진입 시 크래시)
- `BookingService`/`BookingRepository`/`BookTheaterViewModel`: 극장 단수 → 복수(`List<Theater>`) 반환으로 변경
- `MoviePagerAdapter`의 불안정한 `onMovieBind` 콜백 제거, `BookScheduleFragment`와 동일한 `registerOnPageChangeCallback` 패턴으로 통일 (ViewPager2가 인접 페이지를 미리 바인딩하면서 잘못된 극장 목록이 뜨는 버그를 사전 차단)
- `BookTheaterFragment`: 영화 페이지 전환 시 극장 목록 재조회 + 선택 상태·Next 버튼 초기화
- `BookScheduleFragment`: 날짜 선택 후 서버에서 실제 상영 시간 조회, 3열 그리드로 표시. 날짜와 시간을 모두 선택해야 Confirm 버튼 활성화
- `ShowtimeSlot` 데이터 클래스, `item_book_schedule_time.xml`(MaterialButton 통일), `fragment_book_schedule.xml`에 시간 섹션 레이아웃 추가

두 프로젝트 모두 빌드 성공, 신규 엔드포인트 3개(`theaters`, 필터링된 `showtimes`, `dates`) 실제 호출로 검증 완료했습니다.