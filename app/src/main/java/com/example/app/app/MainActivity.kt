package com.example.app.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.app.app.navigation.NavigationCommand
import com.example.app.app.navigation.NavigationManager
import com.example.app.app.presentation.viewmodel.MainViewModel
import com.example.app.core.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var navigationManager: NavigationManager
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val isReady by viewModel.isReady.collectAsState()

            AppTheme(darkTheme = isDarkTheme) {
                if (isReady) {
                    val navController = rememberNavController()

                    // Observe navigation commands from NavigationManager
                    LaunchedEffect(navController) {
                        navigationManager.commands.collect { command ->
                            when (command) {
                                is NavigationCommand.Navigate -> {
                                    navController.navigate(command.route) {
                                        launchSingleTop = command.singleTop
                                    }
                                }
                                is NavigationCommand.NavigateAndClearBackStack -> {
                                    navController.navigate(command.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                                is NavigationCommand.PopBackStack -> {
                                    navController.popBackStack()
                                }
                                is NavigationCommand.PopBackToRoute -> {
                                    navController.popBackStack(
                                        command.route,
                                        command.inclusive
                                    )
                                }
                            }
                        }
                    }

                    AppNavigation(
                        navController = navController,
                        startDestination = viewModel.startDestination,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
