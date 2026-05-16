package com.train.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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

@Composable
fun WorkoutTrackerScreen(
    routine: Routine,
    onFinish: (String) -> Unit,
    workoutViewModel: WorkoutViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceLevel1
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = routine.name.uppercase(),
                                style = AppTypography.labelMedium,
                                color = AccentBlue
                            )
                            Text(
                                text = workoutViewModel.formatTime(workoutViewModel.elapsedTime),
                                style = AppTypography.displayLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 34.sp
                                )
                            )
                        }

                        if (workoutViewModel.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = AccentBlue
                            )
                        } else {
                            TrainPrimaryButton(
                                text = "CONCLUIR",
                                onClick = { workoutViewModel.finishWorkout(onFinish) }
                            )
                        }
                    }

                    AnimatedVisibility(visible = workoutViewModel.restTimeLeft > 0) {
                        Row(
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .background(
                                    color = AccentBlue.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DESCANSO: ${workoutViewModel.restTimeLeft}s",
                                style = AppTypography.labelMedium,
                                color = AccentBlue
                            )
                        }
                    }

                    if (workoutViewModel.livePrNotifications.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val pr = workoutViewModel.livePrNotifications.first()
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AccentPurple.copy(alpha = 0.16f)
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TrackerSettingChip(
                            text = if (workoutViewModel.previousValuesMode == WorkoutViewModel.PreviousValuesMode.ANY_WORKOUT)
                                "Previous: Any workout" else "Previous: Same routine",
                            isActive = true,
                            onClick = {
                                val newMode = if (workoutViewModel.previousValuesMode == WorkoutViewModel.PreviousValuesMode.ANY_WORKOUT) {
                                    WorkoutViewModel.PreviousValuesMode.SAME_ROUTINE
                                } else {
                                    WorkoutViewModel.PreviousValuesMode.ANY_WORKOUT
                                }
                                workoutViewModel.updatePreviousValuesMode(newMode)
                            },
                            modifier = Modifier.weight(1f)
                        )

                        TrackerSettingChip(
                            text = if (workoutViewModel.autoCopyLastSet) "Add set: Copy last" else "Add set: Empty",
                            isActive = workoutViewModel.autoCopyLastSet,
                            onClick = { workoutViewModel.toggleAutoCopyLastSet() },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    AnimatedVisibility(visible = workoutViewModel.isLoadingPreviousValues) {
                        Text(
                            text = "A carregar previous values...",
                            modifier = Modifier.padding(top = 10.dp),
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
                            }
                        )
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
private fun TrackerSettingChip(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) AccentBlue.copy(alpha = 0.12f) else SurfaceLevel0
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            style = AppTypography.labelSmall,
            color = if (isActive) AccentBlue else OutlineBorder,
            textAlign = TextAlign.Center
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
    onAddSet: () -> Unit
) {
    TrainCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.name.ifBlank { "Exercício" },
                    style = AppTypography.headlineLarge.copy(fontSize = 20.sp)
                )

                Surface(
                    modifier = Modifier.clickable { onAddSet() },
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceLevel0
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ADD SET",
                            style = AppTypography.labelSmall,
                            color = AccentBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "PREVIOUS",
                style = AppTypography.labelSmall,
                color = AccentBlue,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            exercise.sets.forEachIndexed { index, set ->
                val previousSet = workoutViewModel.getPreviousSetValue(exercise.id, index)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    if (previousSet != null && (previousSet.weight > 0f || previousSet.reps > 0)) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onApplyPrevious(index) },
                            shape = RoundedCornerShape(8.dp),
                            color = AccentBlue.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Set ${index + 1}",
                                    style = AppTypography.labelSmall,
                                    color = OutlineBorder
                                )
                                Text(
                                    text = workoutViewModel.formatPreviousValue(previousSet),
                                    style = AppTypography.bodyLarge.copy(
                                        color = AccentBlue,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    SetTypeRow(
                        selectedType = set.type,
                        onTypeSelected = { type -> onUpdateSetType(index, type) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SetTypeBadge(set.type)

                        WorkoutDataInput(
                            value = if (set.weight > 0f) set.weight.toString() else "",
                            onValueChange = { value ->
                                onUpdateSet(index, value, set.reps.toString())
                            },
                            modifier = Modifier.weight(1f)
                        )

                        WorkoutDataInput(
                            value = if (set.reps > 0) set.reps.toString() else "",
                            onValueChange = { value ->
                                onUpdateSet(index, set.weight.toString(), value)
                            },
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = { onToggleSet(index) },
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .background(
                                    color = if (set.completed) AccentBlue else SurfaceLevel0,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (set.completed) Color.White else OutlineBorder
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SetTypeRow(
    selectedType: SetType,
    onTypeSelected: (SetType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SetTypeChip("W", SetType.WARMUP, selectedType, onTypeSelected, AccentYellow)
        SetTypeChip("N", SetType.NORMAL, selectedType, onTypeSelected, AccentBlue)
        SetTypeChip("F", SetType.FAILURE, selectedType, onTypeSelected, Color(0xFFE05A5A))
        SetTypeChip("D", SetType.DROPSET, selectedType, onTypeSelected, Color(0xFF6AA8FF))
    }
}

@Composable
private fun SetTypeChip(
    label: String,
    type: SetType,
    selectedType: SetType,
    onTypeSelected: (SetType) -> Unit,
    accent: Color
) {
    val active = selectedType == type
    Surface(
        modifier = Modifier.clickable { onTypeSelected(type) },
        shape = RoundedCornerShape(8.dp),
        color = if (active) accent.copy(alpha = 0.18f) else SurfaceLevel0
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = AppTypography.labelSmall,
            color = if (active) accent else OutlineBorder
        )
    }
}

@Composable
private fun SetTypeBadge(type: SetType) {
    val (label, color) = when (type) {
        SetType.WARMUP -> "W" to AccentYellow
        SetType.NORMAL -> "N" to AccentBlue
        SetType.FAILURE -> "F" to Color(0xFFE05A5A)
        SetType.DROPSET -> "D" to Color(0xFF6AA8FF)
        else -> "N" to AccentBlue
    }

    Surface(
        modifier = Modifier
            .width(32.dp)
            .padding(end = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.16f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                modifier = Modifier.padding(vertical = 11.dp),
                style = AppTypography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WorkoutDataInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .height(40.dp)
            .background(SurfaceLevel0, RoundedCornerShape(8.dp))
            .border(1.dp, OutlineBorder.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = AppTypography.bodyLarge.copy(
                color = TextPrimary,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            ),
            cursorBrush = SolidColor(AccentBlue)
        )
    }
}