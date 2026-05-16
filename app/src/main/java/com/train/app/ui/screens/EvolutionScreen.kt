package com.train.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.ktx.toObject
import com.train.app.data.FirebaseManager
import com.train.app.data.models.Exercise
import com.train.app.data.models.WorkoutSession
import com.train.app.ui.components.TrainCard
import com.train.app.ui.theme.AccentBlue
import com.train.app.ui.theme.AccentYellow
import com.train.app.ui.theme.AppTypography
import com.train.app.ui.theme.BackgroundDark
import com.train.app.ui.theme.OutlineBorder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EvolutionScreen() {
    var sessions by remember { mutableStateOf<List<WorkoutSession>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                sessions = snapshot.documents.mapNotNull { document ->
                    document.toObject<WorkoutSession>()?.copy(id = document.id)
                }.sortedByDescending { it.startTime }
                isLoading = false
            }
            .addOnFailureListener { error ->
                errorMessage = error.message ?: "Erro ao carregar progresso"
                isLoading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text("EVOLUÇÃO", style = AppTypography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            }

            errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = errorMessage!!, color = AccentYellow)
                }
            }

            sessions.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Ainda não existem treinos concluídos.", color = OutlineBorder)
                }
            }

            else -> {
                val totalWorkouts = sessions.size
                val totalVolume = sessions.sumOf { it.totalVolume.toDouble() }.toInt()
                val totalMinutes = sessions.sumOf { it.durationMinutes }
                val bestSession = sessions.maxByOrNull { it.totalVolume }
                val bestExercise = findBestExercise(sessions)

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatsCard(
                                title = "TREINOS",
                                value = totalWorkouts.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            StatsCard(
                                title = "VOLUME",
                                value = "${totalVolume} KG",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatsCard(
                                title = "MINUTOS",
                                value = totalMinutes.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            StatsCard(
                                title = "MELHOR",
                                value = bestSession?.routineName ?: "-",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        TrainCard {
                            Column {
                                Text(
                                    text = "DESTAQUE",
                                    style = AppTypography.labelSmall,
                                    color = OutlineBorder
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = bestExercise?.first ?: "Sem recordes ainda",
                                    style = AppTypography.headlineLarge.copy(fontSize = 22.sp),
                                    color = AccentBlue
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = bestExercise?.second ?: "Conclui mais treinos para ver recordes.",
                                    color = Color.White
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "ÚLTIMAS SESSÕES",
                            style = AppTypography.labelSmall,
                            color = OutlineBorder
                        )
                    }

                    items(sessions) { session ->
                        SessionCard(session = session)
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun StatsCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    TrainCard(modifier = modifier) {
        Column {
            Text(
                text = title,
                style = AppTypography.labelSmall,
                color = OutlineBorder
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = AppTypography.headlineLarge.copy(
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace
                ),
                color = Color.White
            )
        }
    }
}

@Composable
private fun SessionCard(session: WorkoutSession) {
    val date = remember(session.startTime) {
        SimpleDateFormat("dd MMM yyyy", Locale("pt", "PT")).format(Date(session.startTime))
    }

    val completedSets = session.exercises.sumOf { exercise ->
        exercise.sets.count { it.completed }
    }

    val cardBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1D1C1C),
            Color(0xFF151414)
        )
    )

    TrainCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBrush, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = session.routineName.ifBlank { "Treino" },
                        style = AppTypography.headlineLarge.copy(fontSize = 20.sp),
                        color = Color.White
                    )
                    Text(
                        text = date,
                        style = AppTypography.labelSmall,
                        color = OutlineBorder
                    )
                }
                Text(
                    text = "${session.totalVolume.toInt()} KG",
                    style = AppTypography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                    color = AccentBlue
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF2B2A2A))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SessionMetric(label = "DURAÇÃO", value = "${session.durationMinutes} min")
                SessionMetric(label = "SÉRIES", value = completedSets.toString())
                SessionMetric(label = "EXERCÍCIOS", value = session.exercises.size.toString())
            }
        }
    }
}

@Composable
private fun SessionMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = AppTypography.labelSmall,
            color = OutlineBorder
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = Color.White,
            style = AppTypography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
        )
    }
}

private fun findBestExercise(sessions: List<WorkoutSession>): Pair<String, String>? {
    val allCompletedSets = sessions.flatMap { session ->
        session.exercises.flatMap { exercise ->
            exercise.sets
                .filter { it.completed }
                .map { set -> Triple(exercise, set.weight, set.reps) }
        }
    }

    val best = allCompletedSets.maxByOrNull { (_, weight, reps) -> weight * reps }
        ?: return null

    return best.first.name to "Melhor registo: ${best.second.toInt()} kg x ${best.third} reps"
}