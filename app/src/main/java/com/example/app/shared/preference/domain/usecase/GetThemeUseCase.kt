package com.example.app.shared.preference.domain.usecase

import com.example.app.shared.preference.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetThemeUseCase @Inject constructor(
    private val themeRepository: ThemeRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return themeRepository.isDarkTheme()
    }
}
