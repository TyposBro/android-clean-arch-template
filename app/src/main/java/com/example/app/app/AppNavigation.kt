package com.example.app.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.app.app.navigation.AppDestinations
import com.example.app.app.presentation.screen.MainScreen
import com.example.app.feature.auth.presentation.screens.LoginScreen
import com.example.app.feature.notes.presentation.screens.NoteDetailScreen
import com.example.app.feature.onboarding.presentation.screens.OnboardingScreen
import com.example.app.feature.settings.presentation.screens.SettingsScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(AppDestinations.ONBOARDING) {
            OnboardingScreen()
        }

        composable(AppDestinations.LOGIN) {
            LoginScreen()
        }

        composable(AppDestinations.MAIN_HUB) {
            MainScreen()
        }

        composable(
            route = AppDestinations.NOTE_DETAIL,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
            NoteDetailScreen(noteId = noteId)
        }

        composable(AppDestinations.SETTINGS) {
            SettingsScreen()
        }
    }
}
