package com.train.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.train.app.data.FirebaseManager
import com.train.app.data.models.*
import com.train.app.ui.components.*
import com.train.app.ui.theme.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit

// --- MODELO DE SESSÃO ---
data class WorkoutSession(
    val id: String = UUID.randomUUID().toString(),
    val routineId: String = "",
    val routineName: String = "",
    val startTime: Long = 0,
    val endTime: Long = 0,
    val durationMinutes: Int = 0,
    val exercises: List<Exercise> = emptyList()
)

// --- VIEWMODEL DO TREINO ---
class WorkoutViewModel : ViewModel() {
    var activeRoutine by mutableStateOf<Routine?>(null)
    var elapsedTime by mutableLongStateOf(0L)
    var isRunning by mutableStateOf(false)
    var startTime by mutableLongStateOf(0L)
    var isSaving by mutableStateOf(false)

    // Lógica do Temporizador de Descanso
    var restTimeLeft by mutableIntStateOf(0)
    private var restTimerJob: Job? = null

    fun startWorkout(routine: Routine) {
        if (activeRoutine == null) {
            activeRoutine = routine
            startTime = System.currentTimeMillis()
            isRunning = true
        }
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
                restTimeLeft--
            }
        }
    }

    fun formatTime(ms: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(ms)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun updateSet(exerciseId: String, setIndex: Int, weight: String, reps: String) {
        activeRoutine = activeRoutine?.let { routine ->
            val updatedExercises = routine.exercises.map { exercise ->
                if (exercise.id == exerciseId) {
                    val updatedSets = exercise.sets.toMutableList().apply {
                        val set = this[setIndex]
                        this[setIndex] = set.copy(
                            weight = weight.toFloatOrNull() ?: set.weight,
                            reps = reps.toIntOrNull() ?: set.reps
                        )
                    }
                    exercise.copy(sets = updatedSets)
                } else exercise
            }
            routine.copy(exercises = updatedExercises)
        }
    }

    fun toggleSet(exerciseId: String, setIndex: Int) {
        activeRoutine = activeRoutine?.let { routine ->
            val updatedExercises = routine.exercises.map { exercise ->
                if (exercise.id == exerciseId) {
                    val updatedSets = exercise.sets.toMutableList().apply {
                        val set = this[setIndex]
                        val newCompletedState = !set.completed
                        this[setIndex] = set.copy(completed = newCompletedState)

                        // Se marcou como completo, inicia descanso
                        if (newCompletedState) {
                            startRestTimer()
                        }
                    }
                    exercise.copy(sets = updatedSets)
                } else exercise
            }
            routine.copy(exercises = updatedExercises)
        }
    }

    fun finishWorkout(onComplete: () -> Unit) {
        val routine = activeRoutine ?: return
        val userId = FirebaseManager.auth.currentUser?.uid ?: return

        isSaving = true
        isRunning = false
        val endTime = System.currentTimeMillis()
        val duration = TimeUnit.MILLISECONDS.toMinutes(elapsedTime).toInt()

        val session = WorkoutSession(
            routineId = routine.id,
            routineName = routine.name,
            startTime = startTime,
            endTime = endTime,
            durationMinutes = duration,
            exercises = routine.exercises
        )

        viewModelScope.launch {
            FirebaseManager.firestore
                .collection("users")
                .document(userId)
                .collection("sessions")
                .document(session.id)
                .set(session)
                .addOnSuccessListener {
                    isSaving = false
                    onComplete()
                }
                .addOnFailureListener {
                    isSaving = false
                }
        }
    }
}

// --- ECRÃ PRINCIPAL DE TREINO ---
@Composable
fun WorkoutTrackerScreen(routine: Routine, onFinish: () -> Unit) {
    val vm: WorkoutViewModel = viewModel()

    LaunchedEffect(routine) {
        vm.startWorkout(routine)
    }

    LaunchedEffect(vm.isRunning) {
        if (vm.isRunning) vm.runTimer()
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        // Cabeçalho Performance
        Surface(
            color = SurfaceLevel1,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(0.5.dp, OutlineBorder)
        ) {
            Column(Modifier.statusBarsPadding().padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = routine.name.uppercase(), style = AppTypography.labelMedium, color = AccentBlue)
                        Text(
                            text = vm.formatTime(vm.elapsedTime),
                            style = AppTypography.displayLarge.copy(fontSize = 36.sp, fontFamily = FontFamily.Monospace)
                        )
                    }

                    if (vm.isSaving) {
                        CircularProgressIndicator(color = AccentBlue, modifier = Modifier.size(24.dp))
                    } else {
                        TrainPrimaryButton(text = "CONCLUIR", onClick = { vm.finishWorkout(onFinish) })
                    }
                }

                // Barra de Descanso Animada
                AnimatedVisibility(
                    visible = vm.restTimeLeft > 0,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth()
                            .background(AccentBlue.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, "Rest", tint = AccentBlue, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "DESCANSO: ${vm.restTimeLeft}s",
                            style = AppTypography.labelMedium,
                            color = AccentBlue
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "SALTAR",
                            modifier = Modifier.clickable { vm.startRestTimer(0) },
                            style = AppTypography.labelSmall,
                            color = OutlineBorder
                        )
                    }
                }
            }
        }

        // Lista de Exercícios
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            vm.activeRoutine?.let { active ->
                items(active.exercises) { exercise ->
                    ExerciseActiveCard(
                        exercise = exercise,
                        onUpdateSet = { idx, w, r -> vm.updateSet(exercise.id, idx, w, r) },
                        onToggleSet = { idx -> vm.toggleSet(exercise.id, idx) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseActiveCard(
    exercise: Exercise,
    onUpdateSet: (Int, String, String) -> Unit,
    onToggleSet: (Int) -> Unit
) {
    TrainCard {
        Column {
            Text(text = exercise.name, style = AppTypography.headlineLarge.copy(fontSize = 20.sp))
            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("SET", style = AppTypography.labelSmall, color = OutlineBorder, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
                Text("ANTERIOR", style = AppTypography.labelSmall, color = OutlineBorder, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                Text("KG", style = AppTypography.labelSmall, color = OutlineBorder, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("REPS", style = AppTypography.labelSmall, color = OutlineBorder, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Spacer(Modifier.width(48.dp))
            }

            Divider(color = OutlineBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

            exercise.sets.forEachIndexed { index, set ->
                val rowColor = if (set.completed) AccentBlue.copy(alpha = 0.1f) else Color.Transparent

                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(rowColor, RoundedCornerShape(4.dp))
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}",
                        style = AppTypography.labelMedium,
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "---",
                        style = AppTypography.labelMedium,
                        color = OutlineBorder,
                        modifier = Modifier.weight(1.5f),
                        textAlign = TextAlign.Center
                    )

                    WorkoutDataInput(
                        value = if (set.weight > 0) set.weight.toString() else "",
                        onValueChange = { onUpdateSet(index, it, set.reps.toString()) },
                        modifier = Modifier.weight(1f)
                    )

                    WorkoutDataInput(
                        value = if (set.reps > 0) set.reps.toString() else "",
                        onValueChange = { onUpdateSet(index, set.weight.toString(), it) },
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { onToggleSet(index) },
                        modifier = Modifier
                            .size(40.dp)
                            .padding(4.dp)
                            .background(if (set.completed) AccentBlue else SurfaceLevel0, RoundedCornerShape(4.dp))
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = if (set.completed) Color.White else OutlineBorder,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutDataInput(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .height(36.dp)
            .background(SurfaceLevel0, RoundedCornerShape(4.dp))
            .border(0.5.dp, OutlineBorder, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = AppTypography.labelMedium.copy(
                color = TextPrimary,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace
            ),
            cursorBrush = SolidColor(AccentBlue),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}