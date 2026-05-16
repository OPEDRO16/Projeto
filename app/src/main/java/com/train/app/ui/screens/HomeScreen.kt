package com.train.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.train.app.data.FirebaseManager
import com.train.app.data.models.WorkoutSession
import com.train.app.ui.components.TrainCard
import com.train.app.ui.components.TrainChip
import com.train.app.ui.components.TrainProgressBar
import com.train.app.ui.theme.AccentBlue
import com.train.app.ui.theme.AccentYellow
import com.train.app.ui.theme.AppTypography
import com.train.app.ui.theme.BackgroundDark
import com.train.app.ui.theme.OutlineBorder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen() {
    val currentUser = FirebaseManager.auth.currentUser
    val username = currentUser?.email?.substringBefore("@")?.uppercase() ?: "ATLETA"
    val dateStr = remember {
        SimpleDateFormat("EEEE, dd MMMM", Locale("pt", "PT")).format(Date()).uppercase()
    }

    var sessions by remember { mutableStateOf<List<WorkoutSession>>(emptyList()) }
    var isLoading by remember { mutableStateOf(currentUser != null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser?.uid) {
        if (currentUser == null) {
            isLoading = false
            return@LaunchedEffect
        }

        FirebaseManager.firestore
            .collection("users")
            .document(currentUser.uid)
            .collection("sessions")
            .get()
            .addOnSuccessListener { snapshot ->
                sessions = snapshot.toObjects(WorkoutSession::class.java)
                    .sortedByDescending { it.startTime }
                isLoading = false
            }
            .addOnFailureListener { error ->
                errorMessage = error.message ?: "Erro ao carregar dashboard"
                isLoading = false
            }
    }

    val weeklyGoal = 5
    val weeklySessions = sessions.count { session -> isInCurrentWeek(session.startTime) }
    val weeklyProgress = if (weeklyGoal == 0) 0f else (weeklySessions.toFloat() / weeklyGoal).coerceIn(0f, 1f)
    val latestSession = sessions.maxByOrNull { it.startTime }
    val latestVolume = latestSession?.totalVolume?.toInt() ?: 0
    val latestFocus = latestSession?.routineName ?: "Sem treino recente"
    val hasNewRecord = sessions.size > 1 && latestSession != null && latestSession.totalVolume >= sessions.maxOf { it.totalVolume }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(Modifier.statusBarsPadding()) }

        item {
            Column(Modifier.padding(vertical = 8.dp)) {
                Text(dateStr, style = AppTypography.labelSmall, color = OutlineBorder)
                Text(
                    text = "FORÇA, $username",
                    style = AppTypography.displayLarge.copy(fontSize = 32.sp)
                )
            }
        }

        if (isLoading) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            }
        } else if (errorMessage != null) {
            item {
                TrainCard {
                    Text(errorMessage!!, color = AccentYellow)
                }
            }
        } else {
            item {
                Text("CONSISTÊNCIA DA SEMANA", style = AppTypography.labelSmall, color = OutlineBorder)
                Spacer(Modifier.height(8.dp))
                TrainCard {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Meta de Treinos", style = AppTypography.bodyLarge)
                            Text(
                                text = "$weeklySessions / $weeklyGoal",
                                style = AppTypography.labelMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = AccentBlue
                                )
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        TrainProgressBar(progress = weeklyProgress)
                    }
                }
            }

            item {
                Text("MÉTRICAS DA SESSÃO ANTERIOR", style = AppTypography.labelSmall, color = OutlineBorder)
                Spacer(Modifier.height(8.dp))
                TrainCard {
                    Column {
                        Text("VOLUME TOTAL LEVANTADO", style = AppTypography.labelSmall, color = OutlineBorder)
                        Text(
                            text = "$latestVolume KG",
                            style = AppTypography.displayLarge.copy(
                                fontSize = 36.sp,
                                fontFamily = FontFamily.Monospace,
                                color = AccentBlue
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TrainChip(text = latestFocus)
                            if (hasNewRecord) {
                                TrainChip(text = "Recorde", isWarning = true)
                            }
                        }
                    }
                }
            }

            item {
                Text("RESUMO RÁPIDO", style = AppTypography.labelSmall, color = OutlineBorder)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HomeStatCard(
                        title = "TREINOS",
                        value = sessions.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    HomeStatCard(
                        title = "MINUTOS",
                        value = sessions.sumOf { it.durationMinutes }.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    TrainCard(modifier = modifier) {
        Column {
            Text(title, style = AppTypography.labelSmall, color = OutlineBorder)
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

private fun isInCurrentWeek(timestamp: Long): Boolean {
    val now = Calendar.getInstance()
    val sessionDate = Calendar.getInstance().apply { timeInMillis = timestamp }

    return now.get(Calendar.YEAR) == sessionDate.get(Calendar.YEAR) &&
            now.get(Calendar.WEEK_OF_YEAR) == sessionDate.get(Calendar.WEEK_OF_YEAR)
}