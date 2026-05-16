package com.train.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.train.app.data.ExerciseLibraryRepository
import com.train.app.data.FirebaseManager
import com.train.app.data.models.WorkoutSession
import com.train.app.ui.components.TrainCard
import com.train.app.ui.components.TrainSecondaryButton
import com.train.app.ui.theme.AccentBlue
import com.train.app.ui.theme.AccentPurple
import com.train.app.ui.theme.AccentYellow
import com.train.app.ui.theme.AppTypography
import com.train.app.ui.theme.BackgroundDark
import com.train.app.ui.theme.OutlineBorder
import com.train.app.ui.theme.SurfaceLevel0
import kotlin.math.roundToInt

@Composable
fun ExerciseLibraryDetailScreen(
    exerciseId: String,
    onBack: () -> Unit = {}
) {
    val exercise = remember(exerciseId) { ExerciseLibraryRepository.getById(exerciseId) }
    var isLoadingStats by remember { mutableStateOf(true) }
    var sessions by remember { mutableStateOf<List<WorkoutSession>>(emptyList()) }
    val userId = FirebaseManager.auth.currentUser?.uid

    LaunchedEffect(userId) {
        if (userId == null) {
            isLoadingStats = false
            return@LaunchedEffect
        }

        FirebaseManager.firestore
            .collection("users")
            .document(userId)
            .collection("sessions")
            .get()
            .addOnSuccessListener { snapshot ->
                sessions = snapshot.toObjects(WorkoutSession::class.java)
                isLoadingStats = false
            }
            .addOnFailureListener {
                isLoadingStats = false
            }
    }

    val exerciseHistory = remember(sessions, exercise?.name) {
        val name = exercise?.name ?: return@remember emptyList()
        sessions
            .flatMap { session -> session.exercises }
            .filter { it.name.equals(name, ignoreCase = true) }
    }

    val completedSets = exerciseHistory.flatMap { it.sets }.filter { it.completed }
    val heaviestWeight = completedSets.maxOfOrNull { it.weight } ?: 0f
    val bestReps = completedSets.maxOfOrNull { it.reps } ?: 0
    val bestSetVolume = completedSets.maxOfOrNull { it.weight * it.reps } ?: 0f
    val estimatedOneRm = completedSets.maxOfOrNull {
        estimateOneRepMax(it.weight, it.reps)
    } ?: 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("EXERCISE DETAIL", style = AppTypography.headlineLarge)
            Spacer(modifier = Modifier.height(10.dp))
            TrainSecondaryButton(
                text = "VOLTAR",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (exercise == null) {
            item {
                TrainCard {
                    Text("Exercício não encontrado.", color = AccentYellow)
                }
            }
            return@LazyColumn
        }

        item {
            TrainCard {
                Column {
                    Text(
                        text = exercise.name,
                        style = AppTypography.headlineLarge.copy(fontSize = 22.sp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${exercise.primaryMuscle} • ${exercise.equipment} • ${exercise.category}",
                        style = AppTypography.bodyMedium,
                        color = OutlineBorder
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ExerciseVideoPlaceholder(exercise.videoUrl)
                }
            }
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExerciseTag(exercise.difficulty.name)
                ExerciseTag(exercise.force.name.replace('_', ' '))
                exercise.secondaryMuscles.forEach { muscle ->
                    ExerciseTag(muscle)
                }
            }
        }

        item {
            Text("INSTRUÇÕES", style = AppTypography.labelSmall, color = AccentBlue)
        }

        itemsIndexed(exercise.instructions) { index, step ->
            TrainCard {
                Text(
                    text = "${index + 1}. $step",
                    style = AppTypography.bodyMedium,
                    color = Color.White
                )
            }
        }

        if (exercise.tips.isNotEmpty()) {
            item {
                Text("TIPS", style = AppTypography.labelSmall, color = AccentPurple)
            }

            itemsIndexed(exercise.tips) { _, tip ->
                TrainCard {
                    Text(tip, style = AppTypography.bodyMedium, color = OutlineBorder)
                }
            }
        }

        item {
            Text("PERSONAL RECORDS", style = AppTypography.labelSmall, color = AccentPurple)
        }

        if (isLoadingStats) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            }
        } else {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatsCard("Heaviest Weight", "${formatWeight(heaviestWeight)} kg")
                    StatsCard("Most Reps", bestReps.toString())
                    StatsCard("Best Set Volume", "${bestSetVolume.roundToInt()} kg")
                    StatsCard("Estimated 1RM", "${formatWeight(estimatedOneRm)} kg")
                }
            }
        }
    }
}

@Composable
private fun ExerciseVideoPlaceholder(videoUrl: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceLevel0
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "VIDEO DEMO",
                    style = AppTypography.labelSmall,
                    color = AccentBlue
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Liga Media3/ExoPlayer no Gradle para reprodução dentro da app.",
                    style = AppTypography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                if (videoUrl.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = videoUrl,
                        style = AppTypography.labelSmall,
                        color = OutlineBorder,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseTag(text: String) {
    Surface(shape = RoundedCornerShape(999.dp), color = SurfaceLevel0) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            style = AppTypography.labelSmall,
            color = AccentBlue
        )
    }
}

@Composable
private fun StatsCard(title: String, value: String) {
    Surface(shape = RoundedCornerShape(10.dp), color = SurfaceLevel0) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(title, style = AppTypography.labelSmall, color = OutlineBorder)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = AppTypography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}

private fun estimateOneRepMax(weight: Float, reps: Int): Float {
    if (weight <= 0f || reps <= 0) return 0f
    return weight * (1f + reps / 30f)
}

private fun formatWeight(weight: Float): String {
    return if (weight % 1f == 0f) {
        weight.roundToInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.1f", weight)
    }
}