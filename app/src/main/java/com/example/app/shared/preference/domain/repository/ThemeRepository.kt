package com.example.app.shared.preference.domain.repository

import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    fun isDarkTheme(): Flow<Boolean>
    suspend fun setDarkTheme(isDark: Boolean)
}
