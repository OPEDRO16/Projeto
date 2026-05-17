package com.train.app.ui.screens

import androidx.compose.runtime.*

@Composable
fun ExerciseLibraryDetailScreen(
    exerciseId: String,
    onBack: () -> Unit = {},
    onOpenWorkout: (String) -> Unit = {}
) {
    ExerciseDetailScreen(
        exerciseName = exerciseId,
        onBack = onBack,
        onOpenWorkout = onOpenWorkout
    )
}