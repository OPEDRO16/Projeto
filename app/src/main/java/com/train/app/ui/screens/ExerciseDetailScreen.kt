package com.train.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private data class ExerciseHistoryEntry(
    val sessionId: String,
    val routineName: String,
    val date: Long,
    val heaviestWeight: Float,
    val bestReps: Int,
    val setVolume: Float,
    val sessionVolume: Float,
    val estimatedOneRepMax: Float
)

@Composable
fun ExerciseDetailScreen(
    exerciseName: String,
    onBack: () -> Unit = {},
    onOpenWorkout: (String) -> Unit = {}
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var sessions by remember { mutableStateOf<List<WorkoutSession>>(emptyList()) }

    val decodedExerciseName = remember(exerciseName) {
        URLDecoder.decode(exerciseName, StandardCharsets.UTF_8.toString())
    }
    val userId = FirebaseManager.auth.currentUser?.uid

    LaunchedEffect(userId) {
        if (userId == null) {
            isLoading = false
            errorMessage = "Utilizador não autenticado"
            return@LaunchedEffect
        }

        FirebaseManager.firestore
            .collection("users")
            .document(userId)
            .collection("sessions")
            .get()
            .addOnSuccessListener { snapshot ->
                sessions = snapshot.toObjects(WorkoutSession::class.java)
                    .sortedByDescending { it.startTime }
                isLoading = false
            }
            .addOnFailureListener { error ->
                errorMessage = error.message ?: "Erro ao carregar exercício"
                isLoading = false
            }
    }

    val history = remember(sessions, decodedExerciseName) {
        buildExerciseHistory(sessions, decodedExerciseName)
    }
    val topEntry = history.maxByOrNull { it.heaviestWeight }
    val bestOneRm = history.maxOfOrNull { it.estimatedOneRepMax } ?: 0f
    val totalVolume = history.sumOf { it.sessionVolume.toDouble() }.toFloat()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("EXERCÍCIO", style = AppTypography.headlineLarge)
            Spacer(modifier = Modifier.height(10.dp))
            TrainSecondaryButton(
                text = "VOLTAR",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )
        }

        when {
            isLoading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentBlue)
                    }
                }
            }

            errorMessage != null -> {
                item {
                    TrainCard {
                        Text(errorMessage!!, color = AccentYellow)
                    }
                }
            }

            history.isEmpty() -> {
                item {
                    TrainCard {
                        Text("Sem histórico para este exercício.", color = OutlineBorder)
                    }
                }
            }

            else -> {
                item {
                    TrainCard {
                        Column {
                            Text("DETALHE", style = AppTypography.labelSmall, color = AccentPurple)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = decodedExerciseName,
                                style = AppTypography.headlineLarge.copy(fontSize = 22.sp),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ExerciseMetricCard("SESSÕES", history.size.toString(), Modifier.weight(1f))
                                ExerciseMetricCard("1RM EST.", "${formatWeight(bestOneRm)} kg", Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ExerciseMetricCard("HEAVIEST", "${formatWeight(topEntry?.heaviestWeight ?: 0f)} kg", Modifier.weight(1f))
                                ExerciseMetricCard("VOLUME", "${totalVolume.roundToInt()} kg", Modifier.weight(1f))
                            }
                        }
                    }
                }

                item {
                    TrainCard {
                        Column {
                            Text("HISTÓRICO", style = AppTypography.labelSmall, color = AccentBlue)
                            Spacer(modifier = Modifier.height(12.dp))
                            history.take(8).forEach { entry ->
                                HistoryBarRow(entry = entry, maxWeight = history.maxOfOrNull { it.heaviestWeight } ?: 1f)
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }

                item {
                    Text("SESSÕES", style = AppTypography.labelSmall, color = OutlineBorder)
                }

                items(history) { entry ->
                    ExerciseHistoryCard(
                        entry = entry,
                        onOpenWorkout = { onOpenWorkout(entry.sessionId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseMetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = SurfaceLevel0
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = AppTypography.labelSmall, color = OutlineBorder)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = AppTypography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue
                )
            )
        }
    }
}

@Composable
private fun HistoryBarRow(entry: ExerciseHistoryEntry, maxWeight: Float) {
    val ratio = if (maxWeight <= 0f) 0f else entry.heaviestWeight / maxWeight
    val widthFraction = ratio.coerceIn(0.08f, 1f)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(formatSessionDate(entry.date), style = AppTypography.labelSmall, color = Color.White)
            Text(
                text = "${formatWeight(entry.heaviestWeight)} kg • ${entry.bestReps} reps",
                style = AppTypography.labelSmall,
                color = OutlineBorder
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(Color(0xFF1B1B1F), RoundedCornerShape(99.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(widthFraction)
                    .height(10.dp)
                    .background(AccentPurple, RoundedCornerShape(99.dp))
            )
        }
    }
}

@Composable
private fun ExerciseHistoryCard(
    entry: ExerciseHistoryEntry,
    onOpenWorkout: () -> Unit
) {
    TrainCard(modifier = Modifier.clickable { onOpenWorkout() }) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.routineName.ifBlank { "Treino" },
                        style = AppTypography.headlineLarge.copy(fontSize = 18.sp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatSessionDate(entry.date),
                        style = AppTypography.bodyMedium,
                        color = OutlineBorder
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentPurple.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = "ABRIR",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        style = AppTypography.labelSmall,
                        color = AccentPurple
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF2B2A2A))
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Heaviest: ${formatWeight(entry.heaviestWeight)} kg • 1RM est.: ${formatWeight(entry.estimatedOneRepMax)} kg",
                style = AppTypography.bodyMedium,
                color = OutlineBorder
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Best reps: ${entry.bestReps} • Set volume: ${entry.setVolume.roundToInt()} kg • Session volume: ${entry.sessionVolume.roundToInt()} kg",
                style = AppTypography.bodyMedium,
                color = OutlineBorder
            )
        }
    }
}

private fun buildExerciseHistory(
    sessions: List<WorkoutSession>,
    exerciseName: String
): List<ExerciseHistoryEntry> {
    return sessions.mapNotNull { session ->
        val exercise = session.exercises.firstOrNull {
            it.name.equals(exerciseName, ignoreCase = true)
        } ?: return@mapNotNull null

        val allSets = exercise.sets
        val completedSets = allSets.filter { it.completed }
        val heaviestWeight = allSets.maxOfOrNull { it.weight } ?: 0f
        val bestReps = allSets.maxOfOrNull { it.reps } ?: 0
        val setVolume = completedSets.maxOfOrNull { it.weight * it.reps } ?: 0f
        val sessionVolume = completedSets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
        val estimatedOneRepMax = allSets.maxOfOrNull { estimateOneRepMax(it.weight, it.reps) } ?: 0f

        ExerciseHistoryEntry(
            sessionId = session.id,
            routineName = session.routineName,
            date = session.startTime,
            heaviestWeight = heaviestWeight,
            bestReps = bestReps,
            setVolume = setVolume,
            sessionVolume = sessionVolume,
            estimatedOneRepMax = estimatedOneRepMax
        )
    }.sortedByDescending { it.date }
}

private fun estimateOneRepMax(weight: Float, reps: Int): Float {
    if (weight <= 0f || reps <= 0) return 0f
    return weight * (1f + reps / 30f)
}

private fun formatSessionDate(timestamp: Long): String {
    return SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("pt", "PT")).format(Date(timestamp))
}

private fun formatWeight(weight: Float): String {
    return if (weight % 1f == 0f) weight.roundToInt().toString() else String.format(Locale.US, "%.1f", weight)
}