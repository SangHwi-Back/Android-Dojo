package com.example.moviceapp.repo

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface CardService {
    @GET("/api/cards")
    suspend fun findAll(): Call<List<PaymentMethodDto>>

    @POST("/api/cards")
    suspend fun create(@Body dto: CreatePaymentMethodDto): Call<PaymentMethodDto>

    @PATCH("/api/cards/{id}")
    suspend fun update(
        @Path("id") id: Int,
        @Body dto: UpdatePaymentMethodDto
    ) : Call<PaymentMethodDto>

    @DELETE("/api/cards/{id}")
    suspend fun remove(@Path("id") id: Int): Call<Unit>
}
