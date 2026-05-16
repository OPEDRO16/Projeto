package com.train.app.navigation

sealed class Screen(val route: String, val title: String) {
    object Login : Screen("login", "Login")
    object Home : Screen("home", "Home")
    object Feed : Screen("feed", "Feed")
    object Evolution : Screen("evolution", "Evolução")
    object Routines : Screen("routines", "Treinar")
    object RoutineEditor : Screen("routine_editor", "Nova Rotina")
    object Chat : Screen("chat", "Chat")
    object Profile : Screen("profile", "Perfil")
}