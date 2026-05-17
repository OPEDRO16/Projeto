package com.train.app.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.train.app.data.FirebaseManager
import com.train.app.data.models.Exercise
import com.train.app.data.models.Routine
import com.train.app.data.models.SetType
import com.train.app.data.models.WorkoutSession
import com.train.app.data.models.WorkoutSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class WorkoutViewModel : ViewModel() {
    enum class PreviousValuesMode {
        ANY_WORKOUT,
        SAME_ROUTINE
    }

    data class PreviousSetValue(
        val weight: Float = 0f,
        val reps: Int = 0,
        val completed: Boolean = false
    )

    data class PrNotification(
        val id: String = UUID.randomUUID().toString(),
        val exerciseName: String,
        val labels: List<String>
    )

    private data class ExercisePrBaseline(
        val heaviestWeight: Float = 0f,
        val bestReps: Int = 0,
        val bestSetVolume: Float = 0f,
        val bestOneRepMax: Float = 0f
    )

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

    var isLoadingPreviousValues by mutableStateOf(false)
        private set

    var previousValuesMode by mutableStateOf(PreviousValuesMode.ANY_WORKOUT)
        private set

    var autoCopyLastSet by mutableStateOf(true)
        private set

    val previousExerciseValues = mutableStateMapOf<String, List<PreviousSetValue>>()
    val livePrNotifications = mutableStateListOf<PrNotification>()

    private val exercisePrBaselines = mutableStateMapOf<String, ExercisePrBaseline>()

    private var startTime by mutableLongStateOf(0L)
    private var timerJob: Job? = null
    private var restTimerJob: Job? = null

    fun startWorkout(routine: Routine) {
        if (activeRoutine?.id == routine.id && isRunning) return

        activeRoutine = routine
        startTime = System.currentTimeMillis()
        elapsedTime = 0L
        isRunning = true
        previousExerciseValues.clear()
        exercisePrBaselines.clear()
        livePrNotifications.clear()

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isRunning) {
                elapsedTime = System.currentTimeMillis() - startTime
                delay(1000)
            }
        }

        loadPreviousValues(routine)
        loadPrBaselines(routine)
    }

    suspend fun runTimer() {
        if (!isRunning) return
    }

    fun updatePreviousValuesMode(mode: PreviousValuesMode) {
        previousValuesMode = mode
        activeRoutine?.let { loadPreviousValues(it) }
    }

    fun toggleAutoCopyLastSet() {
        autoCopyLastSet = !autoCopyLastSet
    }

    fun consumePrNotification(id: String) {
        livePrNotifications.removeAll { it.id == id }
    }

    private fun loadPreviousValues(routine: Routine) {
        val userId = FirebaseManager.auth.currentUser?.uid ?: return
        isLoadingPreviousValues = true

        FirebaseManager.firestore
            .collection("users")
            .document(userId)
            .collection("sessions")
            .get()
            .addOnSuccessListener { snapshot ->
                val sessions = snapshot.toObjects(WorkoutSession::class.java)
                    .sortedByDescending { it.startTime }

                routine.exercises.forEach { exercise ->
                    val matchedExercise = findPreviousExercise(routine, exercise, sessions)
                    previousExerciseValues[exercise.id] = matchedExercise?.sets?.map { set ->
                        PreviousSetValue(
                            weight = set.weight,
                            reps = set.reps,
                            completed = set.completed
                        )
                    }.orEmpty()
                }

                isLoadingPreviousValues = false
            }
            .addOnFailureListener {
                isLoadingPreviousValues = false
            }
    }

    private fun loadPrBaselines(routine: Routine) {
        val userId = FirebaseManager.auth.currentUser?.uid ?: return

        FirebaseManager.firestore
            .collection("users")
            .document(userId)
            .collection("sessions")
            .get()
            .addOnSuccessListener { snapshot ->
                val sessions = snapshot.toObjects(WorkoutSession::class.java)
                routine.exercises.forEach { exercise ->
                    exercisePrBaselines[exercise.id] = calculateBaselineForExercise(exercise, sessions)
                }
            }
    }

    private fun calculateBaselineForExercise(exercise: Exercise, sessions: List<WorkoutSession>): ExercisePrBaseline {
        val historySets = sessions
            .flatMap { session -> session.exercises }
            .filter { it.name.equals(exercise.name, ignoreCase = true) }
            .flatMap { it.sets }
            .filter { it.completed }

        val heaviestWeight = historySets.maxOfOrNull { it.weight } ?: 0f
        val bestReps = historySets.maxOfOrNull { it.reps } ?: 0
        val bestSetVolume = historySets.maxOfOrNull { it.weight * it.reps } ?: 0f
        val bestOneRepMax = historySets.maxOfOrNull { estimateOneRepMax(it.weight, it.reps) } ?: 0f

        return ExercisePrBaseline(
            heaviestWeight = heaviestWeight,
            bestReps = bestReps,
            bestSetVolume = bestSetVolume,
            bestOneRepMax = bestOneRepMax
        )
    }

    private fun findPreviousExercise(
        routine: Routine,
        exercise: Exercise,
        sessions: List<WorkoutSession>
    ): Exercise? {
        val filteredSessions = when (previousValuesMode) {
            PreviousValuesMode.ANY_WORKOUT -> sessions
            PreviousValuesMode.SAME_ROUTINE -> sessions.filter { it.routineId == routine.id }
        }

        return filteredSessions
            .flatMap { session -> session.exercises }
            .firstOrNull { previous -> previous.name.equals(exercise.name, ignoreCase = true) }
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

    fun stopRestTimer() {
        restTimerJob?.cancel()
        restTimeLeft = 0
    }

    fun formatTime(ms: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun formatPreviousValue(previousSet: PreviousSetValue): String {
        val weightText = if (previousSet.weight % 1f == 0f) {
            previousSet.weight.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", previousSet.weight)
        }
        return "$weightText kg × ${previousSet.reps}"
    }

    fun getPreviousSetValue(exerciseId: String, setIndex: Int): PreviousSetValue? {
        return previousExerciseValues[exerciseId]?.getOrNull(setIndex)
    }

    fun applyPreviousSetValue(exerciseId: String, setIndex: Int) {
        val previousSet = getPreviousSetValue(exerciseId, setIndex) ?: return
        updateSet(
            exerciseId = exerciseId,
            setIndex = setIndex,
            weight = previousSet.weight.toString(),
            reps = previousSet.reps.toString()
        )
    }

    fun addSet(exerciseId: String) {
        val routine = activeRoutine ?: return

        val updatedExercises = routine.exercises.map { exercise ->
            if (exercise.id != exerciseId) return@map exercise

            val updatedSets = exercise.sets.toMutableList()
            val baseSet = if (autoCopyLastSet) {
                updatedSets.lastOrNull()?.copy(completed = false) ?: WorkoutSet()
            } else {
                WorkoutSet()
            }
            updatedSets.add(baseSet)
            exercise.copy(sets = updatedSets)
        }

        activeRoutine = routine.copy(exercises = updatedExercises)
    }

    fun addExerciseToActive(exercise: Exercise) {
        val routine = activeRoutine ?: return
        if (routine.exercises.any { it.id == exercise.id }) return
        val newExercise = exercise.copy(
            sets = listOf(WorkoutSet())
        )
        activeRoutine = routine.copy(exercises = routine.exercises + newExercise)
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

    fun updateSetType(exerciseId: String, setIndex: Int, setType: SetType) {
        val routine = activeRoutine ?: return

        val updatedExercises = routine.exercises.map { exercise ->
            if (exercise.id != exerciseId) return@map exercise
            if (setIndex !in exercise.sets.indices) return@map exercise

            val updatedSets = exercise.sets.toMutableList()
            updatedSets[setIndex] = updatedSets[setIndex].copy(type = setType)
            exercise.copy(sets = updatedSets)
        }

        activeRoutine = routine.copy(exercises = updatedExercises)
    }

    fun toggleSet(exerciseId: String, setIndex: Int) {
        val routine = activeRoutine ?: return
        var exerciseName = ""
        var prLabels = emptyList<String>()

        val updatedExercises = routine.exercises.map { exercise ->
            if (exercise.id != exerciseId) return@map exercise
            if (setIndex !in exercise.sets.indices) return@map exercise

            exerciseName = exercise.name.ifBlank { "Exercise" }
            val updatedSets = exercise.sets.toMutableList()
            val currentSet = updatedSets[setIndex]
            val newCompletedState = !currentSet.completed
            val nextSet = updatedSets.getOrNull(setIndex + 1)

            updatedSets[setIndex] = currentSet.copy(completed = newCompletedState)

            if (newCompletedState) {
                prLabels = evaluatePrs(exercise.id, updatedSets[setIndex])

                if (nextSet?.type == SetType.DROPSET) {
                    stopRestTimer()
                } else {
                    startRestTimer()
                }
            }

            exercise.copy(sets = updatedSets)
        }

        activeRoutine = routine.copy(exercises = updatedExercises)

        if (prLabels.isNotEmpty()) {
            livePrNotifications.add(
                PrNotification(
                    exerciseName = exerciseName,
                    labels = prLabels
                )
            )
        }
    }

    private fun evaluatePrs(exerciseId: String, completedSet: WorkoutSet): List<String> {
        val baseline = exercisePrBaselines[exerciseId] ?: ExercisePrBaseline()
        val labels = mutableListOf<String>()

        if (completedSet.weight > baseline.heaviestWeight) labels.add("Heaviest Weight")
        if (completedSet.reps > baseline.bestReps) labels.add("Most Reps")

        val setVolume = completedSet.weight * completedSet.reps
        if (setVolume > baseline.bestSetVolume) labels.add("Best Set Volume")

        val oneRepMax = estimateOneRepMax(completedSet.weight, completedSet.reps)
        if (oneRepMax > baseline.bestOneRepMax) labels.add("Best 1RM")

        if (labels.isNotEmpty()) {
            exercisePrBaselines[exerciseId] = ExercisePrBaseline(
                heaviestWeight = maxOf(baseline.heaviestWeight, completedSet.weight),
                bestReps = maxOf(baseline.bestReps, completedSet.reps),
                bestSetVolume = maxOf(baseline.bestSetVolume, setVolume),
                bestOneRepMax = maxOf(baseline.bestOneRepMax, oneRepMax)
            )
        }

        return labels
    }

    fun finishWorkout(onComplete: (String) -> Unit) {
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
                onComplete(session.id)
            }
            .addOnFailureListener {
                isSaving = false
                activeRoutine = routine
                isRunning = false
            }
    }

    private fun estimateOneRepMax(weight: Float, reps: Int): Float {
        if (weight <= 0f || reps <= 0) return 0f
        return weight * (1f + reps / 30f)
    }

    private fun resetWorkoutState() {
        activeRoutine = null
        elapsedTime = 0L
        startTime = 0L
        isRunning = false
        restTimeLeft = 0
        isLoadingPreviousValues = false
        previousExerciseValues.clear()
        exercisePrBaselines.clear()
        livePrNotifications.clear()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        restTimerJob?.cancel()
    }
}