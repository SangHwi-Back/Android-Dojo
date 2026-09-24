package com.example.moviceapp.repo

import retrofit2.Call
import retrofit2.http.*

interface CardService {
    @GET("/api/cards")
    fun findAll(@Header("Authorization") token: String): Call<List<PaymentMethodDto>>

    @POST("/api/cards")
    fun create(@Header("Authorization") token: String, @Body dto: CreatePaymentMethodDto): Call<PaymentMethodDto>

    @PATCH("/api/cards/{id}")
    fun update(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body dto: UpdatePaymentMethodDto
    ): Call<PaymentMethodDto>

    @DELETE("/api/cards/{id}")
    fun remove(@Header("Authorization") token: String, @Path("id") id: Int): Call<Unit>
}
