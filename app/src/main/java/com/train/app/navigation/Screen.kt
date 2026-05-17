package com.train.app.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
    object WorkoutDetail : Screen("workout_detail/{sessionId}") {
        fun createRoute(sessionId: String) = "workout_detail/$sessionId"
    }
    object WorkoutSummary : Screen("workout_summary/{sessionId}?userId={userId}") {
        fun createRoute(sessionId: String, userId: String? = null): String {
            return if (userId != null) "workout_summary/$sessionId?userId=$userId" else "workout_summary/$sessionId"
        }
    }
    object ExerciseDetail : Screen("exercise_detail/{exerciseName}") {
        fun createRoute(exerciseName: String) = "exercise_detail/$exerciseName"
    }
    object ExerciseLibrary : Screen("exercise_library")
    object ExerciseLibraryDetail : Screen("exercise_library_detail/{exerciseId}") {
        fun createRoute(exerciseId: String) = "exercise_library_detail/$exerciseId"
    }
    object CreatePost : Screen("create_post/{sessionId}") {
        fun createRoute(sessionId: String) = "create_post/$sessionId"
    }
    object PostComments : Screen("post_comments/{postId}") {
        fun createRoute(postId: String) = "post_comments/$postId"
    }
    object EditProfile : Screen("edit_profile")
    object Friends : Screen("friends")
}