package com.example.moviceapp.repo

import javax.inject.Inject
import javax.inject.Singleton

interface MovieAppUserRepository {
    suspend fun getUser(token: String?): APIResult<UserEntity>
    suspend fun updateUser(token: String?, user: UserEntity): APIResult<UserEntity>
}

@Singleton
class MovieAppUserServiceImpl @Inject constructor(
    val service: MovieAppUserService
) : MovieAppUserRepository {
    override suspend fun getUser(token: String?): APIResult<UserEntity> =
        service.getUser(token).toAPIResult()

    override suspend fun updateUser(token: String?, user: UserEntity): APIResult<UserEntity> =
        service.updateUser(token, user).toAPIResult()
}