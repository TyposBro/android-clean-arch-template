package com.example.app.shared.auth.domain.repositories

import com.example.app.core.network.models.AuthResponse
import com.example.app.core.network.models.RepositoryResult

interface AuthRepository {
    suspend fun login(email: String, password: String): RepositoryResult<AuthResponse>
    suspend fun register(email: String, password: String, name: String): RepositoryResult<AuthResponse>
}
