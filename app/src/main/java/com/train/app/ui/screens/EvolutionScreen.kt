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
import androidx.compose.foundation.layout.width
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
import com.train.app.ui.theme.AccentBlue
import com.train.app.ui.theme.AccentPurple
import com.train.app.ui.theme.AccentYellow
import com.train.app.ui.theme.AppTypography
import com.train.app.ui.theme.BackgroundDark
import com.train.app.ui.theme.OutlineBorder
import com.train.app.ui.theme.SurfaceLevel0
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private data class WeeklyPoint(
    val label: String,
    val workouts: Int,
    val volume: Float
)

private data class ExerciseStat(
    val name: String,
    val sessions: Int,
    val heaviestWeight: Float,
    val totalVolume: Float,
    val bestReps: Int,
    val bestSetVolume: Float,
    val bestSessionVolume: Float,
    val estimatedOneRepMax: Float,
    val lastPerformedAt: Long
)

@Composable
fun EvolutionScreen(
    onOpenExercise: (String) -> Unit = {}
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var sessions by remember { mutableStateOf<List<WorkoutSession>>(emptyList()) }
    var selectedExerciseName by remember { mutableStateOf<String?>(null) }

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
                errorMessage = error.message ?: "Erro ao carregar evolução"
                isLoading = false
            }
    }

    val totalWorkouts = sessions.size
    val totalVolume = sessions.sumOf { it.totalVolume.toDouble() }.toFloat()
    val totalDuration = sessions.sumOf { it.durationMinutes }
    val totalSets = sessions.sumOf { session ->
        session.exercises.sumOf { exercise ->
            exercise.sets.count { it.completed }
        }
    }
    val averageDuration = if (sessions.isNotEmpty()) totalDuration / sessions.size else 0
    val exerciseStats = remember(sessions) { buildExerciseStats(sessions) }
    val strongestExercise = exerciseStats.maxByOrNull { it.heaviestWeight }
    val weeklyData = remember(sessions) { buildWeeklyData(sessions) }
    val selectedExercise = exerciseStats.firstOrNull { it.name == selectedExerciseName } ?: exerciseStats.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("EVOLUÇÃO", style = AppTypography.headlineLarge)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Resumo global, records e detalhe por exercício.",
                style = AppTypography.bodyMedium,
                color = OutlineBorder
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

            sessions.isEmpty() -> {
                item {
                    TrainCard {
                        Text("Ainda não existem treinos concluídos para mostrar evolução.", color = OutlineBorder)
                    }
                }
            }

            else -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard("WORKOUTS", totalWorkouts.toString(), Modifier.weight(1f))
                        MetricCard("VOLUME", "${totalVolume.roundToInt()} KG", Modifier.weight(1f))
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard("SÉRIES", totalSets.toString(), Modifier.weight(1f))
                        MetricCard("MÉDIA", "$averageDuration min", Modifier.weight(1f))
                    }
                }

                strongestExercise?.let { top ->
                    item {
                        TrainCard {
                            Column {
                                Text("TOP RECORD", style = AppTypography.labelSmall, color = AccentPurple)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = top.name,
                                    style = AppTypography.headlineLarge.copy(fontSize = 22.sp),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Heaviest: ${formatWeight(top.heaviestWeight)} kg • 1RM est.: ${formatWeight(top.estimatedOneRepMax)} kg",
                                    style = AppTypography.bodyMedium,
                                    color = OutlineBorder
                                )
                            }
                        }
                    }
                }

                item {
                    TrainCard {
                        Column {
                            Text("ÚLTIMAS 6 SEMANAS", style = AppTypography.labelSmall, color = AccentBlue)
                            Spacer(modifier = Modifier.height(12.dp))
                            weeklyData.forEach { point ->
                                WeeklyBarRow(point, weeklyData.maxOfOrNull { it.volume } ?: 1f)
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }

                item {
                    Text("RANKING DE EXERCÍCIOS", style = AppTypography.labelSmall, color = OutlineBorder)
                }

                items(exerciseStats.take(6)) { exercise ->
                    ExerciseRankingCard(
                        exercise = exercise,
                        isSelected = selectedExercise?.name == exercise.name,
                        onClick = { selectedExerciseName = exercise.name },
                        onOpenExercise = { onOpenExercise(exercise.name) }
                    )
                }

                selectedExercise?.let { detail ->
                    item {
                        TrainCard(modifier = Modifier.clickable { onOpenExercise(detail.name) }) {
                            Column {
                                Text("DETALHE DO EXERCÍCIO", style = AppTypography.labelSmall, color = AccentPurple)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = detail.name,
                                    style = AppTypography.headlineLarge.copy(fontSize = 22.sp),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    MetricCard(
                                        title = "HEAVIEST",
                                        value = "${formatWeight(detail.heaviestWeight)} kg",
                                        modifier = Modifier.weight(1f)
                                    )
                                    MetricCard(
                                        title = "1RM EST.",
                                        value = "${formatWeight(detail.estimatedOneRepMax)} kg",
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    MetricCard(
                                        title = "BEST SET VOL",
                                        value = "${detail.bestSetVolume.roundToInt()} kg",
                                        modifier = Modifier.weight(1f)
                                    )
                                    MetricCard(
                                        title = "BEST SESSION",
                                        value = "${detail.bestSessionVolume.roundToInt()} kg",
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color(0xFF2B2A2A))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Sessões: ${detail.sessions} • Melhor reps: ${detail.bestReps} • Última vez: ${formatSessionDate(detail.lastPerformedAt)}",
                                    style = AppTypography.bodyMedium,
                                    color = OutlineBorder
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tocar para abrir histórico completo do exercício",
                                    style = AppTypography.labelSmall,
                                    color = AccentPurple
                                )
                            }
                        }
                    }
                }

                item {
                    TrainCard {
                        Column {
                            Text("ÚLTIMO TREINO", style = AppTypography.labelSmall, color = AccentBlue)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = sessions.first().routineName.ifBlank { "Treino" },
                                style = AppTypography.headlineLarge.copy(fontSize = 20.sp),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${formatSessionDate(sessions.first().startTime)} • ${sessions.first().durationMinutes} min • ${sessions.first().totalVolume.roundToInt()} kg",
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
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
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
private fun WeeklyBarRow(point: WeeklyPoint, maxVolume: Float) {
    val ratio = if (maxVolume <= 0f) 0f else point.volume / maxVolume
    val widthFraction = ratio.coerceIn(0.08f, 1f)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(point.label, style = AppTypography.labelSmall, color = Color.White)
            Text(
                text = "${point.workouts} treinos • ${point.volume.roundToInt()} kg",
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
                    .background(AccentBlue, RoundedCornerShape(99.dp))
            )
        }
    }
}

@Composable
private fun ExerciseRankingCard(
    exercise: ExerciseStat,
    isSelected: Boolean,
    onClick: () -> Unit,
    onOpenExercise: () -> Unit
) {
    TrainCard(modifier = Modifier.clickable { onClick() }) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = AppTypography.headlineLarge.copy(fontSize = 18.sp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${exercise.sessions} sessões • ${exercise.totalVolume.roundToInt()} kg total",
                        style = AppTypography.bodyMedium,
                        color = OutlineBorder
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) AccentPurple.copy(alpha = 0.18f) else SurfaceLevel0
                    ) {
                        Text(
                            text = if (isSelected) "ATIVO" else "VER",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            style = AppTypography.labelSmall,
                            color = if (isSelected) AccentPurple else AccentBlue
                        )
                    }
                    Surface(
                        modifier = Modifier.clickable { onOpenExercise() },
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
            }
        }
    }
}

private fun buildExerciseStats(sessions: List<WorkoutSession>): List<ExerciseStat> {
    val grouped = sessions.flatMap { session ->
        session.exercises.map { exercise -> session to exercise }
    }.groupBy { (_, exercise) -> exercise.name.trim().lowercase(Locale.getDefault()) }

    return grouped.mapNotNull { (_, pairs) ->
        val validPairs = pairs.filter { (_, exercise) -> exercise.name.isNotBlank() }
        if (validPairs.isEmpty()) return@mapNotNull null

        val displayName = validPairs.first().second.name
        val allSets = validPairs.flatMap { it.second.sets }
        val completedSets = allSets.filter { it.completed }
        val heaviestWeight = allSets.maxOfOrNull { it.weight } ?: 0f
        val bestReps = allSets.maxOfOrNull { it.reps } ?: 0
        val bestSetVolume = completedSets.maxOfOrNull { it.weight * it.reps } ?: 0f
        val estimatedOneRepMax = allSets.maxOfOrNull { estimateOneRepMax(it.weight, it.reps) } ?: 0f
        val bestSessionVolume = validPairs.maxOfOrNull { (_, exercise) ->
            exercise.sets.filter { it.completed }.sumOf { set -> (set.weight * set.reps).toDouble() }.toFloat()
        } ?: 0f
        val totalVolume = validPairs.sumOf { (_, exercise) ->
            exercise.sets.filter { it.completed }.sumOf { set -> (set.weight * set.reps).toDouble() }
        }.toFloat()
        val lastPerformedAt = validPairs.maxOfOrNull { (session, _) -> session.startTime } ?: 0L

        ExerciseStat(
            name = displayName,
            sessions = validPairs.size,
            heaviestWeight = heaviestWeight,
            totalVolume = totalVolume,
            bestReps = bestReps,
            bestSetVolume = bestSetVolume,
            bestSessionVolume = bestSessionVolume,
            estimatedOneRepMax = estimatedOneRepMax,
            lastPerformedAt = lastPerformedAt
        )
    }.sortedByDescending { it.totalVolume }
}

private fun buildWeeklyData(sessions: List<WorkoutSession>): List<WeeklyPoint> {
    val result = mutableListOf<WeeklyPoint>()

    for (offset in 5 downTo 0) {
        val weekCalendar = Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, -offset)
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val start = weekCalendar.timeInMillis
        val end = Calendar.getInstance().apply {
            timeInMillis = start
            add(Calendar.DAY_OF_YEAR, 7)
        }.timeInMillis

        val weekSessions = sessions.filter { it.startTime in start until end }
        val label = SimpleDateFormat("dd MMM", Locale("pt", "PT")).format(Date(start))
        val volume = weekSessions.sumOf { it.totalVolume.toDouble() }.toFloat()

        result.add(
            WeeklyPoint(
                label = label,
                workouts = weekSessions.size,
                volume = volume
            )
        )
    }

    return result
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