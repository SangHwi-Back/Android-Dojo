package com.example.moviceapp.repo

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.PATCH

/**
 * UserService 였으나 너무 일반적인 이름이라 좀 더 앱과 직접적인 연관이 있는 이름으로 변경
 */
interface MovieAppUserService {
    @GET("/api/users/me")
    fun getUser(): Call<UserEntity>
    @PATCH("/api/users/me")
    fun updateUser(user: UserEntity): Call<UserEntity>
}