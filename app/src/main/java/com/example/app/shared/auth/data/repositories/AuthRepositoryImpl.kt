package com.example.app.shared.auth.data.repositories

import com.example.app.core.network.models.AuthResponse
import com.example.app.core.network.models.LoginRequest
import com.example.app.core.network.models.RegisterRequest
import com.example.app.core.network.models.RepositoryResult
import com.example.app.core.network.models.safeApiCall
import com.example.app.shared.auth.data.remote.AuthApi
import com.example.app.shared.auth.domain.repositories.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi
) : AuthRepository {

    override suspend fun login(email: String, password: String): RepositoryResult<AuthResponse> {
        return safeApiCall { authApi.login(LoginRequest(email, password)) }
    }

    override suspend fun register(email: String, password: String, name: String): RepositoryResult<AuthResponse> {
        return safeApiCall { authApi.register(RegisterRequest(email, password, name)) }
    }
}
