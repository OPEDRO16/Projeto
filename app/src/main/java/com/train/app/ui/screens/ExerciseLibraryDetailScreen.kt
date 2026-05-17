package com.train.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.train.app.data.ExerciseLibraryRepository

@Composable
fun ExerciseLibraryDetailScreen(
    exerciseId: String,
    onBack: () -> Unit = {},
    onOpenWorkout: (String) -> Unit = {}
) {
    val exercise = remember(exerciseId) { ExerciseLibraryRepository.getById(exerciseId) }
    val exerciseName = exercise?.name ?: exerciseId

    ExerciseDetailScreen(
        exerciseName = exerciseName,
        onBack = onBack,
        onOpenWorkout = onOpenWorkout
    )
}