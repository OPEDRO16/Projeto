package com.train.app.data.models

import java.util.UUID

/**
 * Representa uma rotina pré-definida de treino.
 */
data class Routine(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val name: String = "",
    val focus: String = "Força",
    val durationMinutes: Int = 0,
    val lastTrained: Long? = null,
    val exercises: List<Exercise> = emptyList()
)

/**
 * Representa um exercício individual dentro de uma rotina ou sessão.
 */
data class Exercise(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val instructions: String = "",
    val sets: List<WorkoutSet> = emptyList(),
    val isCompleted: Boolean = false
)

/**
 * Representa uma série individual (set) de um exercício.
 */
data class WorkoutSet(
    val id: String = UUID.randomUUID().toString(),
    val reps: Int = 0,
    val weight: Float = 0f,
    val completed: Boolean = false,
    val type: SetType = SetType.NORMAL
)

enum class SetType {
    NORMAL, WARMUP, DROPSET, FAILURE
}

/**
 * Representa uma sessão de treino ativa ou concluída (Histórico).
 */
data class WorkoutSession(
    val id: String = UUID.randomUUID().toString(),
    val routineId: String = "",
    val routineName: String = "",
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = 0,
    val durationMinutes: Int = 0,
    val exercises: List<Exercise> = emptyList(),
    val totalVolume: Float = 0f
)