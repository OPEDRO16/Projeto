package com.train.app.data.ai

import com.train.app.data.models.Exercise
import com.train.app.data.models.Routine
import com.train.app.data.models.SetType
import com.train.app.data.models.WorkoutSet
import kotlinx.coroutines.delay
import java.util.UUID

object AiRoutineService {
    suspend fun generateRoutine(
        objective: String,
        muscleGroups: List<String>,
        availableTime: String = "1h"
    ): Routine {
        // Simula processamento da IA com um tempo visível para feedback premium
        delay(1800)

        val objTrim = objective.takeIf { it.isNotBlank() } ?: "Hipertrofia"
        val splitName = if (muscleGroups.isEmpty()) "Corpo Inteiro" else muscleGroups.joinToString(" & ")
        val timeLabel = when (availableTime) {
            "30 min" -> "Express (30m)"
            "1h" -> "Clássico (1h)"
            "1h30" -> "Volume (1h30)"
            "2h" -> "Elite (2h)"
            else -> "Padrão (1h)"
        }
        val name = "Treino IA: $splitName [$timeLabel]"

        // Configuração de repetições e séries com base no objetivo e tempo disponível
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

        // Séries por exercício adaptadas ao tempo disponível
        val setsCount = when (availableTime) {
            "30 min" -> 2
            "1h" -> 3
            "1h30" -> 4
            "2h" -> 5
            else -> 3
        }

        // Número máximo de exercícios totais no treino com base no tempo disponível
        val maxExercises = when (availableTime) {
            "30 min" -> 3
            "1h" -> 5
            "1h30" -> 7
            "2h" -> 9
            else -> 5
        }

        val exercises = mutableListOf<Exercise>()
        val selectedSplits = if (muscleGroups.isEmpty()) listOf("Corpo Inteiro") else muscleGroups

        // Banco de Exercícios: Padrão (Biblioteca) + AI Custom (Novos exercícios não existentes na biblioteca)
        val peitoPool = listOf(
            // Padrão
            Triple("Barbell Bench Press", compoundReps, "Empurra a barra a partir do peito médio."),
            Triple("Incline Dumbbell Press", isolationReps, "Press inclinado com halteres para a parte superior do peito."),
            // AI Custom
            Triple("Around the World Flyes", isolationReps, "Movimento circular completo com halteres isolando fibras internas e superiores do peito."),
            Triple("Incline Cable Flyes", isolationReps, "Aberturas em polia inclinada mantendo tensão constante no peitoral superior."),
            Triple("Deficit Push-up on Handles", isolationReps, "Flexões profundas sobre apoios de mão, aumentando a amplitude e alongamento do peito.")
        )

        val costasPool = listOf(
            // Padrão
            Triple("Lat Pulldown", compoundReps, "Puxada na polia alta com foco nas dorsais."),
            Triple("Barbell Row", compoundReps, "Remada curvada com barra para espessura das costas."),
            // AI Custom
            Triple("Dumbbell Kelso Row", isolationReps, "Remada inclinada focando na retração extrema e contração das escápulas e trapézio médio."),
            Triple("Meadows Row", compoundReps, "Remada unilateral com pega pronada na ponta da barra, isolando o dorsal inferior."),
            Triple("Chest-Supported Dumbbell Row", isolationReps, "Remada com halteres deitado num banco inclinado a 30 graus, eliminando batota lombar.")
        )

        val pernasPool = listOf(
            // Padrão
            Triple("Back Squat", compoundReps, "Agachamento livre com barra para pernas completas."),
            Triple("Romanian Deadlift", compoundReps, "Peso morto romeno com foco em posteriores e glúteos."),
            Triple("Leg Extension", isolationReps, "Extensão de pernas na máquina isolando os quadríceps."),
            // AI Custom
            Triple("Bulgarian Split Squat", isolationReps, "Agachamento unilateral com o pé traseiro elevado, focando em quadríceps e glúteos."),
            Triple("Sissy Squat", isolationReps, "Agachamento calisténico apoiado projetando os joelhos à frente, focando na cabeça reta do quadríceps."),
            Triple("Deficit Calf Raise", isolationReps, "Elevação de gémeos na ponta do degrau com alongamento máximo abaixo da linha do pé.")
        )

        val bicepsPool = listOf(
            // Padrão
            Triple("Barbell Curl", isolationReps, "Rosca direta com barra para os bíceps."),
            Triple("Hammer Curl", isolationReps, "Rosca martelo com halteres focando braquial e antebraços."),
            // AI Custom
            Triple("Spider Curl", isolationReps, "Rosca direta num banco inclinado de peito para baixo, maximizando a contração no topo."),
            Triple("Incline Dumbbell Curl", isolationReps, "Rosca com halteres em banco inclinado a 45 graus, alongando ao máximo a cabeça longa."),
            Triple("Zottman Curl", isolationReps, "Subida com pega supinada e descida com pega pronada, trabalhando bíceps e braquiorradial.")
        )

        val tricepsPool = listOf(
            // Padrão
            Triple("Cable Triceps Pushdown", isolationReps, "Extensão de tríceps na polia com foco nos tríceps."),
            // AI Custom
            Triple("Overhead Cable Extension", isolationReps, "Extensão de tríceps por cima da cabeça de costas para a polia, alongando a cabeça longa."),
            Triple("JM Press", compoundReps, "Híbrido de supino e extensão de tríceps com barra à testa, construindo força nos cotovelos."),
            Triple("Dumbbell Kickback", isolationReps, "Coice com halteres focando na contração total com braço paralelo ao torso.")
        )

        val ombrosPool = listOf(
            // Padrão
            Triple("Overhead Press", compoundReps, "Press militar com barra de pé para os ombros."),
            Triple("Lateral Raise", isolationReps, "Elevações laterais com halteres para a porção lateral do ombro."),
            // AI Custom
            Triple("Arnold Press", compoundReps, "Press de ombros iniciando com rotação de punhos a 180 graus, ativando deltoide frontal e lateral."),
            Triple("Cable Face Pull", isolationReps, "Puxada à testa na polia alta focando em deltoides posteriores e rotadores externos."),
            Triple("Incline Y-Raise", isolationReps, "Elevações em Y deitado em banco inclinado focando no deltoide posterior e trapézio inferior.")
        )

        val corePool = listOf(
            // Padrão
            Triple("Cable Crunch", isolationReps, "Abdominais na polia alta de joelhos."),
            Triple("Plank", 0, "Prancha abdominal isométrica estática."),
            // AI Custom
            Triple("Hanging Leg Raise", isolationReps, "Elevação de pernas suspenso na barra, ativando intensamente a parede abdominal inferior."),
            Triple("Russian Twist", isolationReps, "Rotação de tronco sentado com pernas elevadas, focando nos oblíquos externos."),
            Triple("Ab Wheel Rollout", isolationReps, "Deslizamento frontal com roda abdominal esticando o core e exigindo estabilização extrema.")
        )

        val corpoInteiroPool = listOf(
            // Padrão
            Triple("Back Squat", compoundReps, "Agachamento livre focando membros inferiores."),
            Triple("Barbell Bench Press", compoundReps, "Supino plano com barra focando peitoral."),
            Triple("Lat Pulldown", compoundReps, "Puxada na polia alta focando as costas."),
            Triple("Overhead Press", compoundReps, "Press militar com barra focando ombros."),
            // AI Custom
            Triple("Kettlebell Swing", compoundReps, "Balanço explosivo de kettlebell focando na articulação da anca e cadeia posterior."),
            Triple("Thrusters", compoundReps, "Agachamento completo seguido de press de ombros explosivo com halteres."),
            Triple("Burpees", isolationReps, "Movimento funcional de flexão seguido de salto vertical com peso corporal."),
            Triple("Man Maker", compoundReps, "Sequência complexa de flexão, remada unilateral, agachamento e press de ombros com halteres.")
        )

        // Seletor inteligente de lista de exercícios com base no músculo e tempo disponível
        val poolsToUse = selectedSplits.map { split ->
            when (split) {
                "Peito" -> peitoPool
                "Costas" -> costasPool
                "Pernas" -> pernasPool
                "Bíceps" -> bicepsPool
                "Tríceps" -> tricepsPool
                "Ombros" -> ombrosPool
                "Core", "Core / Abs" -> corePool
                "Corpo Inteiro" -> corpoInteiroPool
                else -> corpoInteiroPool
            }
        }

        // Distribui exercícios alternadamente entre os grupos musculares selecionados até atingir o limite
        var poolIndex = 0
        var exerciseIndex = 0
        while (exercises.size < maxExercises) {
            val currentPool = poolsToUse[poolIndex % poolsToUse.size]
            val item = currentPool.getOrNull(exerciseIndex)
            if (item != null) {
                val suffix = when (availableTime) {
                    "30 min" -> " (Foco Express: Descanso de 45s)"
                    "1h" -> " (Descanso sugerido: 90s)"
                    "1h30" -> " (Descanso: 90s. Última em Dropset!)"
                    "2h" -> " (Foco Força: Descanso de 120s-180s)"
                    else -> " (Descanso: 90s)"
                }
                exercises.add(
                    createExercise(
                        name = item.first,
                        reps = item.second,
                        setsCount = setsCount,
                        instructions = item.third + suffix,
                        availableTime = availableTime
                    )
                )
            }
            poolIndex++
            if (poolIndex >= poolsToUse.size) {
                poolIndex = 0
                exerciseIndex++
            }
            // Proteção contra fim de itens nos pools
            if (exerciseIndex >= 5) break
        }

        // Se faltarem exercícios, adiciona opções gerais adaptadas
        if (exercises.isEmpty()) {
            if (isCalisthenics) {
                exercises.add(createExercise("Pull-up", compoundReps, setsCount, "Exercício geral calisténico adaptado pela IA.", availableTime))
                exercises.add(createExercise("Push-up", compoundReps, setsCount, "Flexões de braço clássicas.", availableTime))
            } else {
                exercises.add(createExercise("Barbell Bench Press", compoundReps, setsCount, "Exercício geral adaptado pela IA.", availableTime))
                exercises.add(createExercise("Back Squat", compoundReps, setsCount, "Agachamento livre adaptado.", availableTime))
            }
        }

        return Routine(
            id = UUID.randomUUID().toString(),
            name = name,
            focus = objTrim,
            durationMinutes = when (availableTime) {
                "30 min" -> 30
                "1h" -> 60
                "1h30" -> 90
                "2h" -> 120
                else -> 60
            },
            exercises = exercises,
            isAiGenerated = true
        )
    }

    private fun createExercise(
        name: String,
        reps: Int,
        setsCount: Int,
        instructions: String,
        availableTime: String
    ): Exercise {
        val sets = (1..setsCount).map { idx ->
            val setType = if (availableTime == "1h30" && idx == setsCount) {
                SetType.DROPSET
            } else {
                SetType.NORMAL
            }
            WorkoutSet(
                id = UUID.randomUUID().toString(),
                reps = reps,
                weight = 0f,
                completed = false,
                type = setType
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
