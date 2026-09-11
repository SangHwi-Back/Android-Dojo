package com.example.moviceapp.repo

import javax.inject.Inject
import javax.inject.Singleton

interface MovieAppUserRepository {
    suspend fun getUser(): APIResult<UserEntity>
    suspend fun updateUser(user: UserEntity): APIResult<UserEntity>
}

@Singleton
class MovieAppUserServiceImpl @Inject constructor(
    val service: MovieAppUserService
) : MovieAppUserRepository {
    override suspend fun getUser(): APIResult<UserEntity> =
        service.getUser().toAPIResult()

    override suspend fun updateUser(user: UserEntity): APIResult<UserEntity> =
        service.updateUser(user).toAPIResult()
}