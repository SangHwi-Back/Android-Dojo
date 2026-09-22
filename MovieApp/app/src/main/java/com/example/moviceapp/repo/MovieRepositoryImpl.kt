package com.example.moviceapp.repo

import javax.inject.Inject
import javax.inject.Singleton

interface MovieRepository {
    suspend fun getMovies(token: String): APIResult<List<Movie>>
    suspend fun getMovies(token: String, path: String): APIResult<List<Movie>>
    suspend fun getFeaturedMovies(): APIResult<List<Movie>>
    suspend fun getMovieDetail(id: String): APIResult<Movie>
    suspend fun searchMovies(query: String): APIResult<List<Movie>>
    suspend fun randomMovies(): APIResult<List<Movie>>
}

@Singleton
class MovieRepositoryImpl @Inject constructor(
    private val service: MovieService
) : MovieRepository {
    override suspend fun getMovies(token: String): APIResult<List<Movie>> =
        service.getMovies(token).toAPIResult()
    override suspend fun getMovies(token: String, path: String): APIResult<List<Movie>> =
        service.getMovies(token, path).toAPIResult()
    override suspend fun getFeaturedMovies(): APIResult<List<Movie>> =
        service.getFeaturedMovies().toAPIResult()
    override suspend fun getMovieDetail(id: String): APIResult<Movie> =
        service.getMovieDetail(id).toAPIResult()
    override suspend fun searchMovies(query: String): APIResult<List<Movie>> =
        service.searchMovies(query).toAPIResult()
    override suspend fun randomMovies(): APIResult<List<Movie>> =
        service.randomMovies().toAPIResult()
}
