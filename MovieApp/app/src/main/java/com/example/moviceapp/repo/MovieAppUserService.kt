package com.example.moviceapp.repo

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * UserService 였으나 너무 일반적인 이름이라 좀 더 앱과 직접적인 연관이 있는 이름으로 변경
 */
interface MovieAppUserService {
    @GET("/api/users/me")
    fun getUser(@Header("Authorization") token: String?): Call<UserEntity>
    @PATCH("/api/users/me")
    fun updateUser(@Header("Authorization") token: String?, @Body user: UserEntity): Call<UserEntity>
    @Multipart
    @POST("/api/users/me/profile-image")
    fun updateUserProfile(@Header("Authorization") token: String?, @Part image: MultipartBody.Part): Call<UserEntity>
}