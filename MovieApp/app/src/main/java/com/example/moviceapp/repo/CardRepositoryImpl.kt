package com.example.moviceapp.repo

import com.example.moviceapp.AppException.AuthException
import com.example.moviceapp.myinfo.getAuthToken
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

    override suspend fun findAll(): APIResult<List<PaymentMethodDto>> {
        val token = getAuthToken() ?: return APIResult.Failure(AuthException.NotAuthenticated())
        return service.findAll(token).toAPIResult()
    }

    override suspend fun create(dto: CreatePaymentMethodDto): APIResult<PaymentMethodDto> {
        val token = getAuthToken() ?: return APIResult.Failure(AuthException.NotAuthenticated())
        return service.create(token, dto).toAPIResult()
    }


    override suspend fun update(id: Int, dto: UpdatePaymentMethodDto): APIResult<PaymentMethodDto> {
        val token = getAuthToken() ?: return APIResult.Failure(AuthException.NotAuthenticated())
        return service.update(token, id, dto).toAPIResult()
    }

    override suspend fun remove(id: Int): APIResult<Unit> {
        val token = getAuthToken() ?: return APIResult.Failure(AuthException.NotAuthenticated())
        return service.remove(token, id).toAPIResult()
    }
}