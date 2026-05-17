package com.train.app.data.ai

import com.train.app.data.models.Exercise
import com.train.app.data.models.Routine
import com.train.app.data.models.SetType
import com.train.app.data.models.WorkoutSet
import kotlinx.coroutines.delay
import java.util.UUID

object AiRoutineService {
    suspend fun generateRoutine(objective: String, muscleGroups: List<String>): Routine {
        // Simula processamento da IA com um tempo visível para feedback premium
        delay(1800)

        val objTrim = objective.takeIf { it.isNotBlank() } ?: "Hipertrofia"
        val splitName = if (muscleGroups.isEmpty()) "Corpo Inteiro" else muscleGroups.joinToString(" & ")
        val name = "Treino IA: $splitName"

        val exercises = mutableListOf<Exercise>()

        // Configuração de repetições e séries com base no objetivo
        val isStrength = objTrim.contains("força", ignoreCase = true) || objTrim.contains("force", ignoreCase = true)
        val isResilience = objTrim.contains("definição", ignoreCase = true) || objTrim.contains("resistência", ignoreCase = true) || objTrim.contains("saudável", ignoreCase = true)
        val isCalisthenics = objTrim.contains("calistenia", ignoreCase = true) || objTrim.contains("calisthenics", ignoreCase = true)

        val compoundReps = when {
            isStrength -> 5
            isResilience -> 15
            else -> 10 // Hipertrofia padrão
        }

        val isolationReps = when {
            isStrength -> 8
            isResilience -> 15
            else -> 12 // Hipertrofia padrão
        }

        val setsCount = when {
            isStrength -> 5
            isResilience -> 3
            else -> 4 // Hipertrofia padrão (4 séries)
        }

        // Mapeamento inteligente de exercícios reais com base nos grupos musculares selecionados
        val selectedSplits = if (muscleGroups.isEmpty()) listOf("Corpo Inteiro") else muscleGroups

        selectedSplits.forEach { split ->
            if (isCalisthenics) {
                when (split) {
                    "Peito" -> {
                        exercises.add(createExercise("Parallel Bar Dip", isolationReps, setsCount, "Fundos em barras paralelas focando peitoral inferior e tríceps."))
                        exercises.add(createExercise("Push-up", compoundReps, setsCount, "Flexões de braço clássicas com o peso do corpo."))
                    }
                    "Costas" -> {
                        exercises.add(createExercise("Pull-up", compoundReps, setsCount, "Elevações na barra fixa com pega pronada focando o grande dorsal."))
                        exercises.add(createExercise("Muscle-up", isolationReps, setsCount, "Exercício de força explosiva completo de puxada e empurre."))
                    }
                    "Pernas" -> {
                        exercises.add(createExercise("Pistol Squat", compoundReps, setsCount, "Agachamento unilateral profundo desafiando força de pernas e estabilidade."))
                    }
                    "Bíceps" -> {
                        exercises.add(createExercise("Pull-up", compoundReps, setsCount, "Elevações na barra fixa focando os flexores do cotovelo."))
                    }
                    "Tríceps" -> {
                        exercises.add(createExercise("Parallel Bar Dip", isolationReps, setsCount, "Fundos em barras paralelas focando os tríceps."))
                    }
                    "Ombros" -> {
                        exercises.add(createExercise("Handstand Push-up", compoundReps, setsCount, "Flexões em pino com apoio na parede para os ombros."))
                    }
                    "Core" -> {
                        exercises.add(createExercise("L-Sit", isolationReps, setsCount, "Suporte isométrico em L elevando as pernas paralelas ao solo."))
                        exercises.add(createExercise("Plank", 0, setsCount, "Prancha isométrica estática mantendo o alinhamento da coluna."))
                    }
                    "Corpo Inteiro" -> {
                        exercises.add(createExercise("Muscle-up", compoundReps, setsCount, "Exercício calisténico completo de tração superior."))
                        exercises.add(createExercise("Pistol Squat", compoundReps, setsCount, "Agachamento unilateral para pernas completas."))
                        exercises.add(createExercise("Parallel Bar Dip", compoundReps, setsCount, "Fundos em barras paralelas."))
                        exercises.add(createExercise("L-Sit", isolationReps, setsCount, "Controle de core isométrico completo."))
                    }
                }
            } else {
                when (split) {
                    "Peito" -> {
                        exercises.add(createExercise("Barbell Bench Press", compoundReps, setsCount, "Empurra a barra a partir do peito médio."))
                        exercises.add(createExercise("Incline Dumbbell Press", isolationReps, setsCount, "Press inclinado com halteres para a parte superior do peito."))
                    }
                    "Costas" -> {
                        exercises.add(createExercise("Lat Pulldown", compoundReps, setsCount, "Puxada na polia alta com foco nas dorsais."))
                        exercises.add(createExercise("Barbell Row", compoundReps, setsCount, "Remada curvada com barra para espessura das costas."))
                    }
                    "Pernas" -> {
                        exercises.add(createExercise("Back Squat", compoundReps, setsCount, "Agachamento livre com barra para pernas completas."))
                        exercises.add(createExercise("Romanian Deadlift", compoundReps, setsCount, "Peso morto romeno com foco em posteriores e glúteos."))
                        exercises.add(createExercise("Leg Extension", isolationReps, setsCount, "Extensão de pernas na máquina isolando os quadríceps."))
                    }
                    "Bíceps" -> {
                        exercises.add(createExercise("Barbell Curl", isolationReps, setsCount, "Rosca direta com barra para os bíceps."))
                        exercises.add(createExercise("Hammer Curl", isolationReps, setsCount, "Rosca martelo com halteres focando braquial e antebraços."))
                    }
                    "Tríceps" -> {
                        exercises.add(createExercise("Cable Triceps Pushdown", isolationReps, setsCount, "Extensão de tríceps na polia com foco nos tríceps."))
                    }
                    "Ombros" -> {
                        exercises.add(createExercise("Overhead Press", compoundReps, setsCount, "Press militar com barra de pé para os ombros."))
                        exercises.add(createExercise("Lateral Raise", isolationReps, setsCount, "Elevações laterais com halteres para a porção lateral do ombro."))
                    }
                    "Core" -> {
                        exercises.add(createExercise("Cable Crunch", isolationReps, setsCount, "Abdominais na polia alta de joelhos."))
                        exercises.add(createExercise("Plank", 0, setsCount, "Prancha abdominal isométrica estática."))
                    }
                    "Corpo Inteiro" -> {
                        exercises.add(createExercise("Back Squat", compoundReps, setsCount, "Agachamento livre focando membros inferiores."))
                        exercises.add(createExercise("Barbell Bench Press", compoundReps, setsCount, "Supino plano com barra focando peitoral."))
                        exercises.add(createExercise("Lat Pulldown", compoundReps, setsCount, "Puxada na polia alta focando as costas."))
                        exercises.add(createExercise("Overhead Press", compoundReps, setsCount, "Press militar com barra focando ombros."))
                    }
                }
            }
        }

        if (exercises.isEmpty()) {
            if (isCalisthenics) {
                exercises.add(createExercise("Pull-up", compoundReps, setsCount, "Exercício geral calisténico adaptado pela IA."))
                exercises.add(createExercise("Push-up", compoundReps, setsCount, "Flexões de braço clássicas."))
            } else {
                exercises.add(createExercise("Barbell Bench Press", compoundReps, setsCount, "Exercício geral adaptado pela IA."))
                exercises.add(createExercise("Back Squat", compoundReps, setsCount, "Agachamento livre adaptado."))
            }
        }

        return Routine(
            id = UUID.randomUUID().toString(),
            name = name,
            focus = objTrim,
            durationMinutes = exercises.size * 12,
            exercises = exercises
        )
    }

    private fun createExercise(name: String, reps: Int, setsCount: Int, instructions: String): Exercise {
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
            instructions = instructions,
            sets = sets
        )
    }
}
