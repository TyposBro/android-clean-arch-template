package com.example.app.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.app.navigation.AppDestinations
import com.example.app.core.auth.SessionManager
import com.example.app.shared.preference.domain.usecase.GetThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    getThemeUseCase: GetThemeUseCase
) : ViewModel() {

    val isDarkTheme = getThemeUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    val startDestination: String
        get() = if (sessionManager.sessionFlow.value.accessToken != null) {
            AppDestinations.MAIN_HUB
        } else {
            AppDestinations.ONBOARDING
        }

    init {
        viewModelScope.launch {
            // Small delay to let token manager initialize
            kotlinx.coroutines.delay(100)
            _isReady.value = true
        }
    }
}
