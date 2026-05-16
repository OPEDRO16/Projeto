package com.train.app.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.train.app.data.FirebaseManager
import com.train.app.data.models.Routine
import com.train.app.data.models.WorkoutSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class WorkoutViewModel : ViewModel() {
    var activeRoutine by mutableStateOf<Routine?>(null)
        private set

    var elapsedTime by mutableLongStateOf(0L)
        private set

    var isRunning by mutableStateOf(false)
        private set

    var isSaving by mutableStateOf(false)
        private set

    var restTimeLeft by mutableIntStateOf(0)
        private set

    private var startTime by mutableLongStateOf(0L)
    private var timerJob: Job? = null
    private var restTimerJob: Job? = null

    fun startWorkout(routine: Routine) {
        if (activeRoutine?.id == routine.id && isRunning) return

        activeRoutine = routine
        startTime = System.currentTimeMillis()
        elapsedTime = 0L
        isRunning = true

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isRunning) {
                elapsedTime = System.currentTimeMillis() - startTime
                delay(1000)
            }
        }
    }

    suspend fun runTimer() {
        if (!isRunning) return
    }

    fun startRestTimer(seconds: Int = 90) {
        restTimerJob?.cancel()
        restTimeLeft = seconds
        restTimerJob = viewModelScope.launch {
            while (restTimeLeft > 0) {
                delay(1000)
                restTimeLeft -= 1
            }
        }
    }

    fun formatTime(ms: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun updateSet(exerciseId: String, setIndex: Int, weight: String, reps: String) {
        val routine = activeRoutine ?: return

        val updatedExercises = routine.exercises.map { exercise ->
            if (exercise.id != exerciseId) return@map exercise
            if (setIndex !in exercise.sets.indices) return@map exercise

            val updatedSets = exercise.sets.toMutableList()
            val currentSet = updatedSets[setIndex]

            updatedSets[setIndex] = currentSet.copy(
                weight = weight.replace(',', '.').toFloatOrNull() ?: currentSet.weight,
                reps = reps.toIntOrNull() ?: currentSet.reps
            )

            exercise.copy(sets = updatedSets)
        }

        activeRoutine = routine.copy(exercises = updatedExercises)
    }

    fun toggleSet(exerciseId: String, setIndex: Int) {
        val routine = activeRoutine ?: return

        val updatedExercises = routine.exercises.map { exercise ->
            if (exercise.id != exerciseId) return@map exercise
            if (setIndex !in exercise.sets.indices) return@map exercise

            val updatedSets = exercise.sets.toMutableList()
            val currentSet = updatedSets[setIndex]
            val newCompletedState = !currentSet.completed

            updatedSets[setIndex] = currentSet.copy(completed = newCompletedState)

            if (newCompletedState) {
                startRestTimer()
            }

            exercise.copy(sets = updatedSets)
        }

        activeRoutine = routine.copy(exercises = updatedExercises)
    }

    fun finishWorkout(onComplete: () -> Unit) {
        val routine = activeRoutine ?: return
        val userId = FirebaseManager.auth.currentUser?.uid ?: return

        isSaving = true
        isRunning = false
        timerJob?.cancel()
        restTimerJob?.cancel()
        restTimeLeft = 0

        val endTime = System.currentTimeMillis()
        val durationMinutes = TimeUnit.MILLISECONDS.toMinutes(endTime - startTime).toInt()
        val totalVolume = routine.exercises.sumOf { exercise ->
            exercise.sets.filter { it.completed }.sumOf { set ->
                (set.weight * set.reps).toDouble()
            }
        }.toFloat()

        val session = WorkoutSession(
            routineId = routine.id,
            routineName = routine.name,
            startTime = startTime,
            endTime = endTime,
            durationMinutes = durationMinutes,
            exercises = routine.exercises,
            totalVolume = totalVolume
        )

        FirebaseManager.firestore
            .collection("users")
            .document(userId)
            .collection("sessions")
            .document(session.id)
            .set(session)
            .addOnSuccessListener {
                isSaving = false
                resetWorkoutState()
                onComplete()
            }
            .addOnFailureListener {
                isSaving = false
                activeRoutine = routine
                isRunning = false
            }
    }

    private fun resetWorkoutState() {
        activeRoutine = null
        elapsedTime = 0L
        startTime = 0L
        isRunning = false
        restTimeLeft = 0
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        restTimerJob?.cancel()
    }
}