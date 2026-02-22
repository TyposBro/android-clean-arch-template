package com.example.app.core.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class AuthSession(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val expiresAt: Long = 0
)

@Singleton
class SessionManager @Inject constructor(
    private val tokenManager: TokenManager
) {
    private val _sessionFlow = MutableStateFlow(AuthSession())
    val sessionFlow: StateFlow<AuthSession> = _sessionFlow.asStateFlow()

    init {
        // Load initial state from persistent storage
        _sessionFlow.update {
            AuthSession(
                accessToken = tokenManager.getToken(),
                refreshToken = tokenManager.getRefreshToken(),
                expiresAt = tokenManager.getExpiresAt()
            )
        }
    }

    fun updateSession(accessToken: String, refreshToken: String, expiresAt: Long) {
        tokenManager.saveToken(accessToken)
        tokenManager.saveRefreshToken(refreshToken)
        tokenManager.saveExpiresAt(expiresAt)

        _sessionFlow.update {
            it.copy(accessToken = accessToken, refreshToken = refreshToken, expiresAt = expiresAt)
        }
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            tokenManager.clearAll()
            _sessionFlow.value = AuthSession()
        }
    }
}
