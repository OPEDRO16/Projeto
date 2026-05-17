package com.train.app.data.models

import com.google.firebase.firestore.PropertyName
import java.util.UUID

/**
 * Representa uma rotina pré-definida de treino.
 */
data class Routine(
    var id: String = UUID.randomUUID().toString(),
    var userId: String = "",
    var name: String = "",
    var focus: String = "Força",
    var durationMinutes: Int = 0,
    var lastTrained: Long? = null,
    var exercises: List<Exercise> = emptyList()
)

/**
 * Representa um exercício individual dentro de uma rotina ou sessão.
 */
data class Exercise(
    var id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var instructions: String = "",
    var sets: List<WorkoutSet> = emptyList(),
    @get:PropertyName("completed")
    @set:PropertyName("completed")
    @PropertyName("completed")
    var completed: Boolean = false
)

/**
 * Representa uma série individual (set) de um exercício.
 */
data class WorkoutSet(
    var id: String = UUID.randomUUID().toString(),
    var reps: Int = 0,
    var weight: Float = 0f,
    @get:PropertyName("completed")
    @set:PropertyName("completed")
    @PropertyName("completed")
    var completed: Boolean = false,
    var type: SetType = SetType.NORMAL
)

enum class SetType {
    NORMAL, WARMUP, DROPSET, FAILURE
}

/**
 * Representa uma sessão de treino ativa ou concluída (Histórico).
 */
data class WorkoutSession(
    var id: String = UUID.randomUUID().toString(),
    var routineId: String = "",
    var routineName: String = "",
    var startTime: Long = System.currentTimeMillis(),
    var endTime: Long = 0,
    var durationMinutes: Int = 0,
    var exercises: List<Exercise> = emptyList(),
    var totalVolume: Float = 0f
)