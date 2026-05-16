package com.train.app.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
    object WorkoutDetail : Screen("workout_detail/{sessionId}") {
        fun createRoute(sessionId: String) = "workout_detail/$sessionId"
    }
    object WorkoutSummary : Screen("workout_summary/{sessionId}") {
        fun createRoute(sessionId: String) = "workout_summary/$sessionId"
    }
    object ExerciseDetail : Screen("exercise_detail/{exerciseName}") {
        fun createRoute(exerciseName: String) = "exercise_detail/$exerciseName"
    }
    object ExerciseLibrary : Screen("exercise_library")
    object ExerciseLibraryDetail : Screen("exercise_library_detail/{exerciseId}") {
        fun createRoute(exerciseId: String) = "exercise_library_detail/$exerciseId"
    }
}