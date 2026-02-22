package com.example.app.shared.preference.domain.usecase

import com.example.app.shared.preference.domain.repository.ThemeRepository
import javax.inject.Inject

class SetThemeUseCase @Inject constructor(
    private val themeRepository: ThemeRepository
) {
    suspend operator fun invoke(isDark: Boolean) {
        themeRepository.setDarkTheme(isDark)
    }
}
