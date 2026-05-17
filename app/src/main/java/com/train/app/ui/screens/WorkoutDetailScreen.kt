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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.train.app.data.FirebaseManager
import com.train.app.data.models.Exercise
import com.train.app.data.models.WorkoutSession
import com.train.app.ui.components.TrainCard
import com.train.app.ui.components.TrainSecondaryButton
import com.train.app.ui.theme.AccentBlue
import com.train.app.ui.theme.AccentYellow
import com.train.app.ui.theme.AppTypography
import com.train.app.ui.theme.BackgroundDark
import com.train.app.ui.theme.OutlineBorder
import com.train.app.ui.theme.SurfaceLevel0
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WorkoutDetailScreen(
    sessionId: String,
    onBack: () -> Unit = {}
) {
    var session by remember { mutableStateOf<WorkoutSession?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
            .document(sessionId)
            .get()
            .addOnSuccessListener { document ->
                session = document.toObject(WorkoutSession::class.java)
                isLoading = false
            }
            .addOnFailureListener { error ->
                errorMessage = error.message ?: "Erro ao carregar treino"
                isLoading = false
            }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Detalhe do Treino",
                    style = AppTypography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
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

            session == null -> {
                item {
                    TrainCard {
                        Text("Treino não encontrado.", color = OutlineBorder)
                    }
                }
            }

            else -> {
                val workout = session!!
                val date = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("pt", "PT")).format(Date(workout.startTime))
                val completedSets = workout.exercises.sumOf { exercise ->
                    exercise.sets.count { it.completed }
                }
                val totalReps = workout.exercises.sumOf { exercise ->
                    exercise.sets.filter { it.completed }.sumOf { it.reps }
                }

                item {
                    TrainCard {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = workout.routineName.ifBlank { "Treino" },
                                style = AppTypography.headlineLarge.copy(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(date, style = AppTypography.labelSmall, color = OutlineBorder)
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = Color(0xFF2B2A2A))
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SummaryBadge(
                                    title = "VOLUME",
                                    value = "${workout.totalVolume.toInt()} KG",
                                    modifier = Modifier.weight(1f)
                                )
                                SummaryBadge(
                                    title = "SÉRIES",
                                    value = completedSets.toString(),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SummaryBadge(
                                    title = "REPS",
                                    value = totalReps.toString(),
                                    modifier = Modifier.weight(1f)
                                )
                                SummaryBadge(
                                    title = "DURAÇÃO",
                                    value = "${workout.durationMinutes} min",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                item {
                    Text("EXERCÍCIOS", style = AppTypography.labelSmall, color = OutlineBorder)
                }

                itemsIndexed(workout.exercises) { index, exercise ->
                    PremiumExerciseCard(
                        exercise = exercise,
                        exerciseNumber = index + 1
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryBadge(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = SurfaceLevel0
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = AppTypography.labelSmall, color = OutlineBorder)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = AppTypography.bodyLarge.copy(
                    color = AccentBlue,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun PremiumExerciseCard(
    exercise: Exercise,
    exerciseNumber: Int
) {
    val completedSets = exercise.sets.count { it.completed }

    TrainCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "EXERCÍCIO $exerciseNumber",
                        style = AppTypography.labelSmall,
                        color = AccentBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = exercise.name.ifBlank { "Exercício" },
                        style = AppTypography.headlineLarge.copy(fontSize = 19.sp),
                        color = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentBlue.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "$completedSets/${exercise.sets.size} sets",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        style = AppTypography.labelSmall,
                        color = AccentBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF2B2A2A))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TableHeaderCell("SET")
                TableHeaderCell("KG")
                TableHeaderCell("REPS")
                TableHeaderCell("OK")
            }

            Spacer(modifier = Modifier.height(8.dp))

            exercise.sets.forEachIndexed { index, set ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (set.completed) AccentBlue.copy(alpha = 0.08f) else SurfaceLevel0
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableValueCell((index + 1).toString())
                        TableValueCell(formatWeight(set.weight))
                        TableValueCell(set.reps.toString())
                        TableValueCell(if (set.completed) "✓" else "-")
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String) {
    Text(
        text = text,
        modifier = Modifier.width(68.dp),
        style = AppTypography.labelSmall,
        color = OutlineBorder,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun TableValueCell(text: String) {
    Text(
        text = text,
        modifier = Modifier.width(68.dp),
        style = AppTypography.bodyLarge.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        ),
        color = Color.White,
        textAlign = TextAlign.Center
    )
}

private fun formatWeight(weight: Float): String {
    return if (weight % 1f == 0f) weight.toInt().toString() else String.format(Locale.US, "%.1f", weight)
}