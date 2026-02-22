package com.example.app.shared.auth.domain.usecases

import com.example.app.core.auth.SessionManager
import com.example.app.core.network.models.AuthResponse
import com.example.app.core.network.models.RepositoryResult
import com.example.app.shared.auth.domain.repositories.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(email: String, password: String): RepositoryResult<AuthResponse> {
        val result = authRepository.login(email, password)
        if (result is RepositoryResult.Success) {
            sessionManager.updateSession(
                accessToken = result.data.accessToken,
                refreshToken = result.data.refreshToken,
                expiresAt = result.data.expiresAt
            )
        }
        return result
    }
}
