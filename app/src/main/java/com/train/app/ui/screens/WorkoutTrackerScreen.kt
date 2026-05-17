package com.train.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.train.app.data.models.Exercise
import com.train.app.data.models.Routine
import com.train.app.data.models.SetType
import com.train.app.ui.components.TrainCard
import com.train.app.ui.components.TrainPrimaryButton
import com.train.app.ui.theme.AccentBlue
import com.train.app.ui.theme.AccentPurple
import com.train.app.ui.theme.AccentYellow
import com.train.app.ui.theme.AppTypography
import com.train.app.ui.theme.BackgroundDark
import com.train.app.ui.theme.OutlineBorder
import com.train.app.ui.theme.SurfaceLevel0
import com.train.app.ui.theme.SurfaceLevel1
import com.train.app.ui.theme.TextPrimary
import com.train.app.viewmodels.WorkoutViewModel

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.train.app.data.ExerciseLibraryRepository
import com.train.app.data.models.WorkoutSet
import java.util.UUID

@Composable
fun WorkoutTrackerScreen(
    routine: Routine,
    onFinish: (String) -> Unit,
    workoutViewModel: WorkoutViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showInlineLibrary by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf("All") }

    val allExercises = remember { ExerciseLibraryRepository.exercises }
    val muscleFilters = remember(allExercises) {
        listOf("All") + allExercises.map { it.primaryMuscle }.distinct().sorted()
    }

    val filteredExercises = remember(query, selectedMuscle) {
        ExerciseLibraryRepository.filterExercises(
            query = query,
            primaryMuscle = selectedMuscle,
            equipment = "All",
            difficulty = "All",
            category = "All"
        )
    }

    val activeRoutine = workoutViewModel.activeRoutine
    val completedSetsCount = activeRoutine?.exercises?.flatMap { it.sets }?.count { it.completed } ?: 0
    val totalVolume = activeRoutine?.exercises?.sumOf { exercise ->
        exercise.sets.filter { it.completed }.sumOf { (it.weight * it.reps).toDouble() }
    }?.toFloat() ?: 0f

    LaunchedEffect(routine.id) {
        workoutViewModel.startWorkout(routine)
    }

    LaunchedEffect(workoutViewModel.isRunning) {
        if (workoutViewModel.isRunning) {
            workoutViewModel.runTimer()
        }
    }

    LaunchedEffect(workoutViewModel.livePrNotifications.size) {
        val notification = workoutViewModel.livePrNotifications.firstOrNull() ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = "PR em ${notification.exerciseName}: ${notification.labels.joinToString()}"
        )
        workoutViewModel.consumePrNotification(notification.id)
    }

    if (showCancelDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancelar Treinamento?", color = Color.White) },
            text = { Text("Tens a certeza que desejas cancelar o treinamento atual? Todos os dados não guardados serão perdidos.", color = OutlineBorder) },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showCancelDialog = false
                        onFinish("")
                    }
                ) {
                    Text("SIM, CANCELAR", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showCancelDialog = false }) {
                    Text("VOLTAR", color = Color.White)
                }
            },
            containerColor = SurfaceLevel1
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceLevel0
            ) {
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    // Header Row (Matches screenshot perfectly)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AccentYellow.copy(alpha = 0.16f),
                                modifier = Modifier.clickable { showCancelDialog = true },
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentYellow.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "CANCELAR",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                    color = AccentYellow
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Treinamento",
                                style = AppTypography.headlineLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (workoutViewModel.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = AccentBlue
                                )
                            } else {
                                Button(
                                    onClick = { workoutViewModel.finishWorkout(onFinish) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccentBlue,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(99.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text("Concluir", style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }

                    // Dashboard Metrics Row (Matches screenshot perfectly)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("Duração", style = AppTypography.labelSmall, color = OutlineBorder)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = workoutViewModel.formatTime(workoutViewModel.elapsedTime),
                                    style = AppTypography.headlineLarge.copy(fontSize = 18.sp, color = AccentBlue)
                                )
                            }
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("Volume", style = AppTypography.labelSmall, color = OutlineBorder)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${totalVolume.toInt()} kg",
                                    style = AppTypography.headlineLarge.copy(fontSize = 18.sp, color = Color.White)
                                )
                            }
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("Séries", style = AppTypography.labelSmall, color = OutlineBorder)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$completedSetsCount",
                                    style = AppTypography.headlineLarge.copy(fontSize = 18.sp, color = Color.White)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = OutlineBorder.copy(alpha = 0.7f),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    AnimatedVisibility(visible = workoutViewModel.livePrNotifications.isNotEmpty()) {
                        val pr = workoutViewModel.livePrNotifications.firstOrNull()
                        if (pr != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AccentPurple.copy(alpha = 0.16f),
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = AccentYellow,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Novo PR em ${pr.exerciseName}: ${pr.labels.joinToString()}",
                                        style = AppTypography.bodyMedium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }


                    AnimatedVisibility(visible = workoutViewModel.isLoadingPreviousValues) {
                        Text(
                            text = "A carregar previous values...",
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 10.dp),
                            style = AppTypography.labelSmall,
                            color = OutlineBorder
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val activeRoutine = workoutViewModel.activeRoutine
                if (activeRoutine != null) {
                    items(activeRoutine.exercises.size) { index ->
                        val exercise = activeRoutine.exercises[index]
                        ExerciseActiveCard(
                            exercise = exercise,
                            workoutViewModel = workoutViewModel,
                            onUpdateSet = { setIndex, weight, reps ->
                                workoutViewModel.updateSet(exercise.id, setIndex, weight, reps)
                            },
                            onUpdateSetType = { setIndex, type ->
                                workoutViewModel.updateSetType(exercise.id, setIndex, type)
                            },
                            onToggleSet = { setIndex ->
                                workoutViewModel.toggleSet(exercise.id, setIndex)
                            },
                            onApplyPrevious = { setIndex ->
                                workoutViewModel.applyPreviousSetValue(exercise.id, setIndex)
                            },
                            onAddSet = {
                                workoutViewModel.addSet(exercise.id)
                            },
                            onRemoveExercise = {
                                workoutViewModel.removeExerciseFromActive(exercise.id)
                            },
                            onRemoveSet = { setIndex ->
                                workoutViewModel.removeSet(exercise.id, setIndex)
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showInlineLibrary = !showInlineLibrary
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = AccentPurple.copy(alpha = 0.16f)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (showInlineLibrary) "FECHAR BIBLIOTECA" else "ADICIONAR EXERCÍCIO",
                                style = AppTypography.labelSmall,
                                color = AccentPurple,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (showInlineLibrary) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceLevel0
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "BIBLIOTECA DE EXERCÍCIOS",
                                    style = AppTypography.labelSmall,
                                    color = AccentPurple
                                )

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = BackgroundDark
                                ) {
                                    BasicTextField(
                                        value = query,
                                        onValueChange = { query = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 14.dp),
                                        textStyle = AppTypography.bodyMedium.copy(color = Color.White),
                                        cursorBrush = SolidColor(AccentBlue),
                                        decorationBox = { innerTextField ->
                                            if (query.isBlank()) {
                                                Text("Pesquisar exercício", color = OutlineBorder)
                                            }
                                            innerTextField()
                                        }
                                    )
                                }

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    muscleFilters.forEach { option ->
                                        Surface(
                                            modifier = Modifier.clickable { selectedMuscle = option },
                                            shape = RoundedCornerShape(999.dp),
                                            color = if (selectedMuscle == option) AccentPurple.copy(alpha = 0.18f) else BackgroundDark
                                        ) {
                                            Text(
                                                text = option,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                style = AppTypography.labelSmall,
                                                color = if (selectedMuscle == option) AccentPurple else OutlineBorder
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "${filteredExercises.size} exercícios encontrados",
                                    style = AppTypography.labelSmall,
                                    color = AccentBlue
                                )
                            }
                        }
                    }

                    items(filteredExercises.size) { index ->
                        val libraryItem = filteredExercises[index]
                        val alreadyAdded = activeRoutine?.exercises?.any { it.name.equals(libraryItem.name, ignoreCase = true) } == true

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceLevel0
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = libraryItem.name,
                                        style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${libraryItem.primaryMuscle} • ${libraryItem.equipment}",
                                        style = AppTypography.bodyMedium,
                                        color = OutlineBorder
                                    )
                                }

                                Surface(
                                    modifier = Modifier.clickable {
                                        if (!alreadyAdded) {
                                            val newExercise = Exercise(
                                                id = "${libraryItem.id}_${UUID.randomUUID()}",
                                                name = libraryItem.name,
                                                instructions = libraryItem.instructions.joinToString("\n"),
                                                sets = listOf(
                                                    WorkoutSet(),
                                                    WorkoutSet(),
                                                    WorkoutSet()
                                                ),
                                                completed = false
                                            )
                                            workoutViewModel.addExerciseToActive(newExercise)
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (alreadyAdded) SurfaceLevel0 else AccentBlue.copy(alpha = 0.16f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (alreadyAdded) Icons.Default.Check else Icons.Default.Add,
                                            contentDescription = null,
                                            tint = if (alreadyAdded) OutlineBorder else AccentBlue
                                        )
                                        Text(
                                            text = if (alreadyAdded) "ADDED" else "ADD",
                                            style = AppTypography.labelSmall,
                                            color = if (alreadyAdded) OutlineBorder else AccentBlue
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}


@Composable
private fun ExerciseActiveCard(
    exercise: Exercise,
    workoutViewModel: WorkoutViewModel,
    onUpdateSet: (Int, String, String) -> Unit,
    onUpdateSetType: (Int, SetType) -> Unit,
    onToggleSet: (Int) -> Unit,
    onApplyPrevious: (Int) -> Unit,
    onAddSet: () -> Unit,
    onRemoveExercise: () -> Unit,
    onRemoveSet: (Int) -> Unit
) {
    var notes by rememberSaveable { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark)
            .padding(vertical = 12.dp)
    ) {
        // Exercise Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exercise Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SurfaceLevel1, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = AccentYellow,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name.ifBlank { "Exercício" },
                    style = AppTypography.headlineLarge.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                    color = AccentBlue
                )
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Mais opções",
                        tint = OutlineBorder
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(SurfaceLevel1)
                ) {
                    DropdownMenuItem(
                        text = { Text("Remover Exercício", color = Color.Red) },
                        onClick = {
                            showMenu = false
                            onRemoveExercise()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Notes text field
        BasicTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            textStyle = AppTypography.bodyMedium.copy(color = Color.White),
            cursorBrush = SolidColor(AccentBlue),
            decorationBox = { innerTextField ->
                if (notes.isEmpty()) {
                    Text("Adicionar notas aqui...", color = OutlineBorder.copy(alpha = 0.7f), style = AppTypography.bodyMedium)
                }
                innerTextField()
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Rest timer row
        val isTimerActive = workoutViewModel.restTimeLeft > 0
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
                if (isTimerActive) {
                    workoutViewModel.stopRestTimer()
                } else {
                    workoutViewModel.startRestTimer(90)
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = if (isTimerActive) AccentBlue else OutlineBorder,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isTimerActive) "Descanso: ${workoutViewModel.restTimeLeft}s" else "Descanso: DESATIVADO",
                style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isTimerActive) AccentBlue else OutlineBorder
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Table Headers
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SÉRIE",
                style = AppTypography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = OutlineBorder,
                modifier = Modifier.weight(0.15f)
            )
            Text(
                text = "ANTERIOR",
                style = AppTypography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = OutlineBorder,
                modifier = Modifier.weight(0.30f)
            )
            Text(
                text = "KG",
                style = AppTypography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = OutlineBorder,
                modifier = Modifier.weight(0.25f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "REPS",
                style = AppTypography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = OutlineBorder,
                modifier = Modifier.weight(0.20f),
                textAlign = TextAlign.Center
            )
            Box(
                modifier = Modifier.weight(0.10f),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = OutlineBorder,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sets List
        exercise.sets.forEachIndexed { index, set ->
            val previousSet = workoutViewModel.getPreviousSetValue(exercise.id, index)
            val previousText = if (previousSet != null && (previousSet.weight > 0f || previousSet.reps > 0)) {
                workoutViewModel.formatPreviousValue(previousSet)
            } else {
                "—"
            }

            SwipeableSetRow(
                exerciseId = exercise.id,
                index = index,
                set = set,
                previousText = previousText,
                previousSet = previousSet,
                onApplyPrevious = onApplyPrevious,
                onUpdateSet = onUpdateSet,
                onUpdateSetType = onUpdateSetType,
                onToggleSet = onToggleSet,
                onRemoveSet = onRemoveSet
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Add Set Button
        Button(
            onClick = onAddSet,
            colors = ButtonDefaults.buttonColors(
                containerColor = SurfaceLevel1,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Adicionar Série", style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
private fun WorkoutTrackerInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(SurfaceLevel1, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = AppTypography.bodyMedium.copy(
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            ),
            cursorBrush = SolidColor(AccentBlue)
        )
    }
}

@Composable
private fun SwipeableSetRow(
    exerciseId: String,
    index: Int,
    set: WorkoutSet,
    previousText: String,
    previousSet: com.train.app.viewmodels.WorkoutViewModel.PreviousSetValue?,
    onApplyPrevious: (Int) -> Unit,
    onUpdateSet: (Int, String, String) -> Unit,
    onUpdateSetType: (Int, SetType) -> Unit,
    onToggleSet: (Int) -> Unit,
    onRemoveSet: (Int) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Red background with trash icon
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0xFFE05A5A), RoundedCornerShape(8.dp))
                .clickable {
                    onRemoveSet(index)
                }
                .padding(end = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remover Série",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Foreground content
        Row(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .background(BackgroundDark)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < -80f) {
                                offsetX = -120f
                            } else {
                                offsetX = 0f
                            }
                        },
                        onDragCancel = {
                            offsetX = 0f
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount).coerceIn(-140f, 0f)
                    }
                }
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Set Number (Cycles through types when clicked!)
            val badgeColor = when (set.type) {
                SetType.NORMAL -> Color.White
                SetType.WARMUP -> AccentYellow
                SetType.FAILURE -> Color(0xFFE05A5A)
                SetType.DROPSET -> Color(0xFF6AA8FF)
            }
            val badgeText = when (set.type) {
                SetType.NORMAL -> "${index + 1}"
                SetType.WARMUP -> "W"
                SetType.FAILURE -> "F"
                SetType.DROPSET -> "D"
            }

            Text(
                text = badgeText,
                style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                color = badgeColor,
                modifier = Modifier
                    .weight(0.15f)
                    .clickable {
                        val nextType = when (set.type) {
                            SetType.NORMAL -> SetType.WARMUP
                            SetType.WARMUP -> SetType.FAILURE
                            SetType.FAILURE -> SetType.DROPSET
                            SetType.DROPSET -> SetType.NORMAL
                        }
                        onUpdateSetType(index, nextType)
                    }
            )

            // Previous Set
            Text(
                text = previousText,
                style = AppTypography.bodyMedium.copy(color = OutlineBorder.copy(alpha = 0.8f)),
                modifier = Modifier
                    .weight(0.30f)
                    .clickable {
                        if (previousSet != null) {
                            onApplyPrevious(index)
                        }
                    }
            )

            // Weight Input Box
            Box(
                modifier = Modifier
                    .weight(0.25f)
                    .padding(horizontal = 4.dp)
            ) {
                WorkoutTrackerInput(
                    value = if (set.weight > 0f) set.weight.toString() else "",
                    onValueChange = { valStr ->
                        onUpdateSet(index, valStr, set.reps.toString())
                    }
                )
            }

            // Reps Input Box
            Box(
                modifier = Modifier
                    .weight(0.20f)
                    .padding(horizontal = 4.dp)
            ) {
                WorkoutTrackerInput(
                    value = if (set.reps > 0) set.reps.toString() else "",
                    onValueChange = { valStr ->
                        onUpdateSet(index, set.weight.toString(), valStr)
                    }
                )
            }

            // Check circle
            Box(
                modifier = Modifier.weight(0.10f),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = if (set.completed) Color(0xFF4CAF50) else SurfaceLevel1,
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = if (set.completed) Color.Transparent else OutlineBorder.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .clickable { onToggleSet(index) },
                    contentAlignment = Alignment.Center
                ) {
                    if (set.completed) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Concluído",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}