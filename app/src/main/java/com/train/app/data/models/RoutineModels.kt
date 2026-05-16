package com.train.app.data.models

data class Routine(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val focus: String = "Força", // Ex: Força, Hipertrofia
    val durationMinutes: Int = 0,
    val lastTrained: Long? = null,
    val exercises: List<Exercise> = emptyList()
)

data class Exercise(
    val id: String = "",
    val name: String = "",
    val instructions: String = "",
    val sets: List<WorkoutSet> = emptyList()
)

data class WorkoutSet(
    val reps: Int = 0,
    val weight: Float = 0f,
    val completed: Boolean = false
)