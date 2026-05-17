package com.train.app.data.ai

import com.train.app.data.models.Exercise
import com.train.app.data.models.Routine
import com.train.app.data.models.SetType
import com.train.app.data.models.WorkoutSet
import kotlinx.coroutines.delay
import java.util.UUID

object AiRoutineService {
    suspend fun generateRoutine(objective: String, muscleGroups: List<String>): Routine {
        // Simula processamento da LLM
        delay(2000)

        val groups = if (muscleGroups.isEmpty()) "Full Body" else muscleGroups.joinToString(" & ")
        val objTrim = objective.takeIf { it.isNotBlank() } ?: "Geral"
        val name = "AI: $groups ($objTrim)"
        
        val exercises = mutableListOf<Exercise>()
        
        // Lógica "Caveman LLM"
        val isStrength = objective.contains("força", ignoreCase = true)
        val reps = if (isStrength) 5 else 10
        val setsCount = if (isStrength) 5 else 3
        
        if (muscleGroups.isEmpty() || muscleGroups.contains("Peito")) {
            exercises.add(createExercise("Supino Plano", reps, setsCount))
        }
        if (muscleGroups.isEmpty() || muscleGroups.contains("Costas")) {
            exercises.add(createExercise("Puxada na Polia", reps, setsCount))
            exercises.add(createExercise("Remada Curvada", reps, setsCount))
        }
        if (muscleGroups.isEmpty() || muscleGroups.contains("Pernas")) {
            exercises.add(createExercise("Agachamento Livre", reps, setsCount))
            exercises.add(createExercise("Leg Press", reps, setsCount))
        }
        if (muscleGroups.contains("Braços")) {
            exercises.add(createExercise("Rosca Direta", reps + 2, setsCount))
            exercises.add(createExercise("Tríceps Corda", reps + 2, setsCount))
        }
        if (muscleGroups.contains("Core")) {
            exercises.add(createExercise("Prancha Abdominal", 0, setsCount))
        }

        if (exercises.isEmpty()) {
            exercises.add(createExercise("Exercício Adaptado", reps, setsCount))
        }

        return Routine(
            id = UUID.randomUUID().toString(),
            name = name,
            focus = objTrim,
            durationMinutes = exercises.size * 10,
            exercises = exercises
        )
    }

    private fun createExercise(name: String, reps: Int, setsCount: Int): Exercise {
        val sets = (1..setsCount).map {
            WorkoutSet(
                id = UUID.randomUUID().toString(),
                reps = reps,
                weight = 0f,
                completed = false,
                type = SetType.NORMAL
            )
        }
        return Exercise(
            id = UUID.randomUUID().toString(),
            name = name,
            instructions = "Gerado por AI.",
            sets = sets
        )
    }
}
