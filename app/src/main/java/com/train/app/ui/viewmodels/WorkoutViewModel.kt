package com.train.app.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.train.app.data.FirebaseManager
import com.train.app.data.models.Exercise
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

    var startTime by mutableLongStateOf(0L)
        private set

    var isSaving by mutableStateOf(false)
        private set

    var restTimeLeft by mutableIntStateOf(0)
        private set

    private var restTimerJob: Job? = null

    fun startWorkout(routine: Routine) {
        if (activeRoutine != null) return
        activeRoutine = routine
        startTime = System.currentTimeMillis()
        elapsedTime = 0L
        isRunning = true
    }

    suspend fun runTimer() {
        while (isRunning) {
            delay(1000)
            elapsedTime = System.currentTimeMillis() - startTime
        }
    }

    fun startRestTimer(seconds: Int = 60) {
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
        val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(ms)
        val minutes = totalMinutes % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun updateSet(exerciseId: String, setIndex: Int, weight: String, reps: String) {
        activeRoutine = activeRoutine?.let { routine ->
            val updatedExercises = routine.exercises.map { exercise ->
                if (exercise.id != exerciseId) return@map exercise

                val updatedSets = exercise.sets.toMutableList()
                val currentSet = updatedSets[setIndex]
                updatedSets[setIndex] = currentSet.copy(
                    weight = weight.toFloatOrNull() ?: currentSet.weight,
                    reps = reps.toIntOrNull() ?: currentSet.reps
                )
                exercise.copy(sets = updatedSets)
            }
            routine.copy(exercises = updatedExercises)
        }
    }

    fun toggleSet(exerciseId: String, setIndex: Int) {
        activeRoutine = activeRoutine?.let { routine ->
            val updatedExercises = routine.exercises.map { exercise ->
                if (exercise.id != exerciseId) return@map exercise

                val updatedSets = exercise.sets.toMutableList()
                val currentSet = updatedSets[setIndex]
                val newCompletedState = !currentSet.completed
                updatedSets[setIndex] = currentSet.copy(completed = newCompletedState)

                if (newCompletedState) {
                    startRestTimer()
                }

                exercise.copy(sets = updatedSets)
            }
            routine.copy(exercises = updatedExercises)
        }
    }

    fun finishWorkout(onComplete: () -> Unit) {
        val routine = activeRoutine ?: return
        val userId = FirebaseManager.auth.currentUser?.uid ?: return

        isSaving = true
        isRunning = false
        restTimerJob?.cancel()
        restTimeLeft = 0

        val endTime = System.currentTimeMillis()
        val duration = TimeUnit.MILLISECONDS.toMinutes(endTime - startTime).toInt()
        val completedExercises = routine.exercises.map { exercise ->
            exercise.copy(sets = exercise.sets)
        }
        val totalVolume = completedExercises.sumOf { exercise ->
            exercise.sets.filter { it.completed }.sumOf { set ->
                (set.weight * set.reps).toDouble()
            }
        }.toFloat()

        val session = WorkoutSession(
            routineId = routine.id,
            routineName = routine.name,
            startTime = startTime,
            endTime = endTime,
            durationMinutes = duration,
            exercises = completedExercises,
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
                activeRoutine = null
                elapsedTime = 0L
                onComplete()
            }
            .addOnFailureListener {
                isSaving = false
                isRunning = true
            }
    }
}