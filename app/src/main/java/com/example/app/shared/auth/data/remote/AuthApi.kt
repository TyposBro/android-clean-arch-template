package com.example.app.shared.auth.data.remote

import com.example.app.core.network.models.AuthResponse
import com.example.app.core.network.models.LoginRequest
import com.example.app.core.network.models.RefreshTokenRequest
import com.example.app.core.network.models.RefreshTokenResponse
import com.example.app.core.network.models.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshAccessToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>
}
