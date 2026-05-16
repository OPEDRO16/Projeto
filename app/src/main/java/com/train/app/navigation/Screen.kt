package com.train.app.navigation

sealed class Screen(val route: String, val title: String) {
    object Login : Screen("login", "Login")
    object Home : Screen("home", "Home")
    object Feed : Screen("feed", "Feed")
    object Evolution : Screen("evolution", "Evolution")
    object Routines : Screen("routines", "Routines")
    object RoutineEditor : Screen("routine_editor", "Routine Editor")
    object Chat : Screen("chat", "Chat")
    object Profile : Screen("profile", "Profile")
    object WorkoutCalendar : Screen("workout_calendar", "Workout Calendar")
    object WorkoutDetail : Screen("workout_detail/{sessionId}", "Workout Detail") {
        fun createRoute(sessionId: String): String = "workout_detail/$sessionId"
    }
}