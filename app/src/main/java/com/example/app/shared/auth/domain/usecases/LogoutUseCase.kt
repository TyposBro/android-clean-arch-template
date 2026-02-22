package com.example.app.shared.auth.domain.usecases

import com.example.app.app.navigation.AppDestinations
import com.example.app.app.navigation.NavigationManager
import com.example.app.core.auth.SessionManager
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val sessionManager: SessionManager,
    private val navigationManager: NavigationManager
) {
    suspend operator fun invoke() {
        sessionManager.logout()
        navigationManager.navigateAndClearBackStack(AppDestinations.LOGIN)
    }
}
