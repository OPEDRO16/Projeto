package com.train.app.data.models

enum class ExerciseForce {
    PUSH,
    PULL,
    LEGS,
    CORE,
    FULL_BODY
}

enum class ExerciseDifficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

data class ExerciseLibraryItem(
    val id: String = "",
    val name: String = "",
    val primaryMuscle: String = "",
    val secondaryMuscles: List<String> = emptyList(),
    val equipment: String = "",
    val category: String = "",
    val force: ExerciseForce = ExerciseForce.PUSH,
    val difficulty: ExerciseDifficulty = ExerciseDifficulty.BEGINNER,
    val instructions: List<String> = emptyList(),
    val tips: List<String> = emptyList(),
    val videoUrl: String = "",
    val isCustom: Boolean = false
)