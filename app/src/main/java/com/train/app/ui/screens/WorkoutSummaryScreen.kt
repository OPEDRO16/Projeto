package com.train.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private data class SummaryPr(
    val exerciseName: String,
    val labels: List<String>
)

@Composable
fun WorkoutSummaryScreen(
    sessionId: String,
    onBack: () -> Unit = {}
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var session by remember { mutableStateOf<WorkoutSession?>(null) }
    var allSessions by remember { mutableStateOf<List<WorkoutSession>>(emptyList()) }

    val userId = FirebaseManager.auth.currentUser?.uid

    LaunchedEffect(userId, sessionId) {
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
                val sessions = snapshot.toObjects(WorkoutSession::class.java)
                allSessions = sessions
                session = sessions.firstOrNull { it.id == sessionId }
                if (session == null) {
                    errorMessage = "Sessão não encontrada"
                }
                isLoading = false
            }
            .addOnFailureListener { error ->
                errorMessage = error.message ?: "Erro ao carregar resumo do treino"
                isLoading = false
            }
    }

    val currentSession = session
    val completedSets = currentSession?.exercises?.sumOf { exercise ->
        exercise.sets.count { it.completed }
    } ?: 0
    val exerciseCount = currentSession?.exercises?.count { exercise ->
        exercise.sets.any { it.completed }
    } ?: 0
    val prSummary = remember(currentSession, allSessions) {
        if (currentSession == null) emptyList() else buildSessionPrSummary(currentSession, allSessions)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("RESUMO DO TREINO", style = AppTypography.headlineLarge)
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

            currentSession == null -> {
                item {
                    TrainCard {
                        Text("Sem dados para este treino.", color = OutlineBorder)
                    }
                }
            }

            else -> {
                item {
                    TrainCard {
                        Column {
                            Text("OVERVIEW", style = AppTypography.labelSmall, color = AccentBlue)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentSession.routineName.ifBlank { "Treino" },
                                style = AppTypography.headlineLarge.copy(fontSize = 22.sp),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = formatDate(currentSession.startTime),
                                style = AppTypography.bodyMedium,
                                color = OutlineBorder
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SummaryMetricCard("DURAÇÃO", "${currentSession.durationMinutes} min", Modifier.weight(1f))
                                SummaryMetricCard("VOLUME", "${currentSession.totalVolume.roundToInt()} kg", Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SummaryMetricCard("SÉRIES", completedSets.toString(), Modifier.weight(1f))
                                SummaryMetricCard("EXERCÍCIOS", exerciseCount.toString(), Modifier.weight(1f))
                            }
                        }
                    }
                }

                if (prSummary.isNotEmpty()) {
                    item {
                        TrainCard {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = AccentYellow,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text("PERSONAL RECORDS", style = AppTypography.labelSmall, color = AccentPurple)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                prSummary.forEach { record ->
                                    SummaryPrCard(record)
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                }

                item {
                    TrainCard {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Insights,
                                    contentDescription = null,
                                    tint = AccentBlue,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text("EXERCÍCIOS", style = AppTypography.labelSmall, color = AccentBlue)
                            }
                        }
                    }
                }

                items(currentSession.exercises.filter { exercise -> exercise.sets.any { it.completed } }) { exercise ->
                    val completedExerciseSets = exercise.sets.filter { it.completed }
                    val bestWeight = completedExerciseSets.maxOfOrNull { it.weight } ?: 0f
                    val bestReps = completedExerciseSets.maxOfOrNull { it.reps } ?: 0
                    val volume = completedExerciseSets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()

                    TrainCard {
                        Column {
                            Text(
                                text = exercise.name,
                                style = AppTypography.headlineLarge.copy(fontSize = 18.sp),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFF2B2A2A))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Sets concluídos: ${completedExerciseSets.size}",
                                style = AppTypography.bodyMedium,
                                color = OutlineBorder
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Melhor peso: ${formatWeight(bestWeight)} kg • Melhor reps: $bestReps",
                                style = AppTypography.bodyMedium,
                                color = OutlineBorder
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Volume do exercício: ${volume.roundToInt()} kg",
                                style = AppTypography.bodyMedium,
                                color = OutlineBorder
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricCard(title: String, value: String, modifier: Modifier = Modifier) {
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
private fun SummaryPrCard(pr: SummaryPr) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = AccentPurple.copy(alpha = 0.14f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = pr.exerciseName,
                style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pr.labels.forEach { label ->
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = AccentPurple.copy(alpha = 0.22f)
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = AppTypography.labelSmall,
                            color = AccentYellow
                        )
                    }
                }
            }
        }
    }
}

private fun buildSessionPrSummary(
    currentSession: WorkoutSession,
    allSessions: List<WorkoutSession>
): List<SummaryPr> {
    val pastSessions = allSessions.filter { it.id != currentSession.id }

    return currentSession.exercises.mapNotNull { exercise ->
        val completedSets = exercise.sets.filter { it.completed }
        if (completedSets.isEmpty()) return@mapNotNull null

        val pastSets = pastSessions
            .flatMap { session -> session.exercises }
            .filter { it.name.equals(exercise.name, ignoreCase = true) }
            .flatMap { it.sets }
            .filter { it.completed }

        val labels = mutableListOf<String>()
        val currentHeaviest = completedSets.maxOfOrNull { it.weight } ?: 0f
        val currentBestReps = completedSets.maxOfOrNull { it.reps } ?: 0
        val currentBestSetVolume = completedSets.maxOfOrNull { it.weight * it.reps } ?: 0f
        val currentBestOneRm = completedSets.maxOfOrNull { estimateOneRepMax(it.weight, it.reps) } ?: 0f
        val currentSessionVolume = completedSets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()

        val pastHeaviest = pastSets.maxOfOrNull { it.weight } ?: 0f
        val pastBestReps = pastSets.maxOfOrNull { it.reps } ?: 0
        val pastBestSetVolume = pastSets.maxOfOrNull { it.weight * it.reps } ?: 0f
        val pastBestOneRm = pastSets.maxOfOrNull { estimateOneRepMax(it.weight, it.reps) } ?: 0f
        val pastBestSessionVolume = pastSessions
            .flatMap { session -> session.exercises.filter { it.name.equals(exercise.name, ignoreCase = true) } }
            .maxOfOrNull { pastExercise ->
                pastExercise.sets.filter { it.completed }.sumOf { set -> (set.weight * set.reps).toDouble() }.toFloat()
            } ?: 0f

        if (currentHeaviest > pastHeaviest) labels.add("Heaviest Weight")
        if (currentBestReps > pastBestReps) labels.add("Most Reps")
        if (currentBestSetVolume > pastBestSetVolume) labels.add("Best Set Volume")
        if (currentBestOneRm > pastBestOneRm) labels.add("Best 1RM")
        if (currentSessionVolume > pastBestSessionVolume) labels.add("Best Session Volume")

        if (labels.isEmpty()) null else SummaryPr(exercise.name, labels)
    }
}

private fun estimateOneRepMax(weight: Float, reps: Int): Float {
    if (weight <= 0f || reps <= 0) return 0f
    return weight * (1f + reps / 30f)
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("pt", "PT")).format(Date(timestamp))
}

private fun formatWeight(weight: Float): String {
    return if (weight % 1f == 0f) weight.roundToInt().toString() else String.format(Locale.US, "%.1f", weight)
}