package com.train.app.navigation

sealed class Screen(val route: String, val title: String) {
    object Login : Screen("login", "Login")
    object Home : Screen("home", "Home")
    object Feed : Screen("feed", "Feed")
    object Routines : Screen("routines", "Routines")
    object Chat : Screen("chat", "Chat")
    object Profile : Screen("profile", "Profile")
}