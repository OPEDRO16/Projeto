package com.train.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.train.app.data.models.Exercise
import com.train.app.data.models.Routine
import com.train.app.data.models.WorkoutSession
import com.train.app.data.models.WorkoutSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class WorkoutViewModel : ViewModel() {

    // Estado da Sessão Ativa
    var activeRoutine by mutableStateOf<Routine?>(null)
        private set

    var exercises by mutableStateOf<List<Exercise>>(emptyList())
        private set

    var startTime by mutableStateOf(0L)
        private set

    var elapsedSeconds by mutableStateOf(0L)
        private set

    private var timerJob: Job? = null

    // Iniciar um treino a partir de uma rotina
    fun startWorkout(routine: Routine) {
        activeRoutine = routine
        exercises = routine.exercises.map { it.copy(id = UUID.randomUUID().toString()) }
        startTime = System.currentTimeMillis()
        elapsedSeconds = 0
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                elapsedSeconds++
            }
        }
    }

    // Atualizar uma série específica
    fun updateSet(exerciseIndex: Int, setIndex: Int, updatedSet: WorkoutSet) {
        val newList = exercises.toMutableList()
        val currentExercise = newList[exerciseIndex]
        val newSets = currentExercise.sets.toMutableList()

        newSets[setIndex] = updatedSet
        newList[exerciseIndex] = currentExercise.copy(sets = newSets)

        exercises = newList
    }

    // Criar o objeto de sessão final para o Firestore
    fun finishWorkout(): WorkoutSession? {
        val routine = activeRoutine ?: return null
        timerJob?.cancel()

        return WorkoutSession(
            id = UUID.randomUUID().toString(),
            routineId = routine.id,
            routineName = routine.name,
            startTime = startTime,
            endTime = System.currentTimeMillis(),
            durationMinutes = (elapsedSeconds / 60).toInt(),
            exercises = exercises.filter { ex -> ex.sets.any { it.completed } }
        )
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}