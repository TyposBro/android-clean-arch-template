package com.example.app.shared.auth

import com.example.app.core.auth.SessionManager
import com.example.app.core.network.models.AuthResponse
import com.example.app.core.network.models.RepositoryResult
import com.example.app.shared.auth.domain.repositories.AuthRepository
import com.example.app.shared.auth.domain.usecases.LoginUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginUseCaseTest {

    private val authRepository: AuthRepository = mockk()
    private val sessionManager: SessionManager = mockk(relaxed = true)

    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setUp() {
        loginUseCase = LoginUseCase(authRepository, sessionManager)
    }

    @Test
    fun `login success calls sessionManager updateSession`() = runTest {
        val authResponse = AuthResponse(
            accessToken = "access-token-123",
            refreshToken = "refresh-token-456",
            expiresAt = 1700000000L
        )
        coEvery { authRepository.login("user@example.com", "password123") } returns
                RepositoryResult.Success(authResponse)

        val result = loginUseCase("user@example.com", "password123")

        assertTrue(result is RepositoryResult.Success)
        assertEquals(authResponse, (result as RepositoryResult.Success).data)

        verify {
            sessionManager.updateSession(
                accessToken = "access-token-123",
                refreshToken = "refresh-token-456",
                expiresAt = 1700000000L
            )
        }
    }

    @Test
    fun `login error returns error without updating session`() = runTest {
        val errorMessage = "Invalid credentials"
        coEvery { authRepository.login("user@example.com", "wrongpassword") } returns
                RepositoryResult.Error(errorMessage, code = 401)

        val result = loginUseCase("user@example.com", "wrongpassword")

        assertTrue(result is RepositoryResult.Error)
        assertEquals(errorMessage, (result as RepositoryResult.Error).message)
        assertEquals(401, result.code)

        verify(exactly = 0) {
            sessionManager.updateSession(any(), any(), any())
        }
    }
}
