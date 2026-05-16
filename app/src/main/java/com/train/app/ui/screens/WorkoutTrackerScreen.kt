package com.train.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.train.app.data.models.Exercise
import com.train.app.data.models.Routine
import com.train.app.ui.components.TrainCard
import com.train.app.ui.components.TrainPrimaryButton
import com.train.app.ui.theme.AccentBlue
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
    onFinish: () -> Unit,
    workoutViewModel: WorkoutViewModel = viewModel()
) {
    LaunchedEffect(routine.id) {
        workoutViewModel.startWorkout(routine)
    }

    LaunchedEffect(workoutViewModel.isRunning) {
        if (workoutViewModel.isRunning) {
            workoutViewModel.runTimer()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
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
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val activeRoutine = workoutViewModel.activeRoutine
            if (activeRoutine != null) {
                itemsIndexed(activeRoutine.exercises) { _, exercise ->
                    ExerciseActiveCard(
                        exercise = exercise,
                        onUpdateSet = { setIndex, weight, reps ->
                            workoutViewModel.updateSet(exercise.id, setIndex, weight, reps)
                        },
                        onToggleSet = { setIndex ->
                            workoutViewModel.toggleSet(exercise.id, setIndex)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseActiveCard(
    exercise: Exercise,
    onUpdateSet: (Int, String, String) -> Unit,
    onToggleSet: (Int) -> Unit
) {
    TrainCard {
        Column {
            Text(
                text = exercise.name.ifBlank { "Exercício" },
                style = AppTypography.headlineLarge.copy(fontSize = 20.sp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            exercise.sets.forEachIndexed { index, set ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}",
                        modifier = Modifier.width(28.dp),
                        style = AppTypography.labelMedium,
                        color = OutlineBorder
                    )

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