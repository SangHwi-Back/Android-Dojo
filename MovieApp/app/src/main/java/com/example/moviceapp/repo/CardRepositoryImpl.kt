package com.example.moviceapp.repo

import javax.inject.Inject
import javax.inject.Singleton

interface CardRepository {
    suspend fun findAll(): APIResult<List<PaymentMethodDto>>
    suspend fun create(dto: CreatePaymentMethodDto): APIResult<PaymentMethodDto>
    suspend fun update(id: Int, dto: UpdatePaymentMethodDto): APIResult<PaymentMethodDto>
    suspend fun remove(id: Int): APIResult<Unit>
}

@Singleton
class CardRepositoryImpl @Inject constructor(
    private val service: CardService
) : CardRepository {

    override suspend fun findAll(): APIResult<List<PaymentMethodDto>> =
        service.findAll().toAPIResult()

    override suspend fun create(dto: CreatePaymentMethodDto): APIResult<PaymentMethodDto> =
        service.create(dto).toAPIResult()

    override suspend fun update(id: Int, dto: UpdatePaymentMethodDto): APIResult<PaymentMethodDto> =
        service.update(id, dto).toAPIResult()

    override suspend fun remove(id: Int): APIResult<Unit> =
        service.remove(id).toAPIResult()
}