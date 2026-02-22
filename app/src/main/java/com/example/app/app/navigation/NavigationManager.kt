package com.example.app.app.navigation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class NavigationCommand {
    data class Navigate(val route: String, val singleTop: Boolean = false) : NavigationCommand()
    data class NavigateAndClearBackStack(val route: String) : NavigationCommand()
    data class PopBackToRoute(val route: String, val inclusive: Boolean = false) : NavigationCommand()
    data object PopBackStack : NavigationCommand()
}

@Singleton
class NavigationManager @Inject constructor() {
    private val _commands = Channel<NavigationCommand>(Channel.BUFFERED)
    val commands = _commands.receiveAsFlow()

    fun navigate(route: String, singleTop: Boolean = false) {
        _commands.trySend(NavigationCommand.Navigate(route, singleTop))
    }

    fun navigateAndClearBackStack(route: String) {
        _commands.trySend(NavigationCommand.NavigateAndClearBackStack(route))
    }

    fun popBackStack() {
        _commands.trySend(NavigationCommand.PopBackStack)
    }

    fun popBackToRoute(route: String, inclusive: Boolean = false) {
        _commands.trySend(NavigationCommand.PopBackToRoute(route, inclusive))
    }
}
