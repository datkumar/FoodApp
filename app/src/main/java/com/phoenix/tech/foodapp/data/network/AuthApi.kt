package com.phoenix.tech.foodapp.data.network

import com.phoenix.tech.foodapp.data.model.LoginRequest
import com.phoenix.tech.foodapp.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("forgot-password")
    suspend fun forgotPassword(@Body email: String): Response<Unit>
}
