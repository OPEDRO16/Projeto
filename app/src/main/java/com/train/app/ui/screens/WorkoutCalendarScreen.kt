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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.train.app.data.FirebaseManager
import com.train.app.data.models.WorkoutSession
import com.train.app.ui.theme.AccentBlue
import com.train.app.ui.theme.AccentPurple
import com.train.app.ui.theme.AccentYellow
import com.train.app.ui.theme.AppTypography
import com.train.app.ui.theme.BackgroundDark
import com.train.app.ui.theme.OutlineBorder
import com.train.app.ui.theme.SurfaceLevel0
import com.train.app.ui.theme.SurfaceLevel1
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun WorkoutCalendarScreen(
    onBack: () -> Unit = {},
    onOpenWorkoutDetail: (String) -> Unit = {}
) {
    var sessions by remember { mutableStateOf<List<WorkoutSession>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedDayKey by remember { mutableStateOf<String?>(null) }

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
                selectedDayKey = sessions.firstOrNull()?.let { dayKey(it.startTime) }
                isLoading = false
            }
            .addOnFailureListener { error ->
                errorMessage = error.message ?: "Erro ao carregar calendário"
                isLoading = false
            }
    }

    val groupedSessions = sessions.groupBy { dayKey(it.startTime) }
    val currentMonthDays = buildCurrentMonthDays()
    val selectedSessions = groupedSessions[selectedDayKey].orEmpty().sortedByDescending { it.startTime }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("CALENDÁRIO", style = AppTypography.headlineLarge)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBack() },
                shape = RoundedCornerShape(10.dp),
                color = AccentPurple.copy(alpha = 0.16f)
            ) {
                Text(
                    text = "VOLTAR AO PERFIL",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    style = AppTypography.labelSmall,
                    color = AccentPurple,
                    fontWeight = FontWeight.Bold
                )
            }
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
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceLevel0
                    ) {
                        Text(
                            text = errorMessage ?: "Erro",
                            modifier = Modifier.padding(14.dp),
                            color = AccentYellow,
                            style = AppTypography.bodyMedium
                        )
                    }
                }
            }

            else -> {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceLevel0
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = SimpleDateFormat("MMMM yyyy", Locale("pt", "PT")).format(Date()).replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(Locale("pt", "PT")) else it.toString()
                                },
                                style = AppTypography.headlineLarge.copy(fontSize = 20.sp),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            MonthCalendarGrid(
                                days = currentMonthDays,
                                workoutDays = groupedSessions.keys,
                                selectedDayKey = selectedDayKey,
                                onDayClick = { selectedDayKey = it }
                            )
                        }
                    }
                }

                item {
                    Text("TREINOS DO DIA", style = AppTypography.labelSmall, color = OutlineBorder)
                }

                if (selectedSessions.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceLevel0
                        ) {
                            Text(
                                text = "Nenhum treino neste dia.",
                                modifier = Modifier.padding(14.dp),
                                color = OutlineBorder,
                                style = AppTypography.bodyMedium
                            )
                        }
                    }
                } else {
                    itemsIndexed(selectedSessions) { _, session ->
                        CalendarWorkoutCard(
                            session = session,
                            onClick = { onOpenWorkoutDetail(session.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthCalendarGrid(
    days: List<CalendarDay>,
    workoutDays: Set<String>,
    selectedDayKey: String?,
    onDayClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("S", "T", "Q", "Q", "S", "S", "D").forEach { label ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(label, style = AppTypography.labelSmall, color = OutlineBorder)
                }
            }
        }

        days.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                week.forEach { day ->
                    val isWorkoutDay = workoutDays.contains(day.key)
                    val isSelected = selectedDayKey == day.key

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .clickable(enabled = day.isCurrentMonth) { onDayClick(day.key) },
                        color = when {
                            !day.isCurrentMonth -> Color.Transparent
                            isSelected -> AccentBlue
                            isWorkoutDay -> AccentBlue.copy(alpha = 0.18f)
                            else -> SurfaceLevel1
                        },
                        shape = CircleShape
                    ) {
                        Box(
                            modifier = Modifier.height(42.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.dayNumber,
                                color = when {
                                    !day.isCurrentMonth -> OutlineBorder.copy(alpha = 0.35f)
                                    isSelected -> Color.White
                                    isWorkoutDay -> AccentBlue
                                    else -> Color.White
                                },
                                style = AppTypography.labelMedium.copy(fontFamily = FontFamily.Monospace)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarWorkoutCard(
    session: WorkoutSession,
    onClick: () -> Unit
) {
    val time = remember(session.startTime) {
        SimpleDateFormat("HH:mm", Locale("pt", "PT")).format(Date(session.startTime))
    }
    val completedSets = session.exercises.sumOf { exercise ->
        exercise.sets.count { it.completed }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = SurfaceLevel0
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.routineName.ifBlank { "Treino" },
                        style = AppTypography.headlineLarge.copy(fontSize = 18.sp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(time, style = AppTypography.labelSmall, color = OutlineBorder)
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
                CalendarMetric("DURAÇÃO", "${session.durationMinutes} min")
                CalendarMetric("SÉRIES", completedSets.toString())
                CalendarMetric("EXERCÍCIOS", session.exercises.size.toString())
            }
        }
    }
}

@Composable
private fun CalendarMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = AppTypography.labelSmall, color = OutlineBorder)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = AppTypography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            color = Color.White
        )
    }
}

private data class CalendarDay(
    val key: String,
    val dayNumber: String,
    val isCurrentMonth: Boolean
)

private fun buildCurrentMonthDays(): List<CalendarDay> {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        firstDayOfWeek = Calendar.MONDAY
    }

    val month = calendar.get(Calendar.MONTH)
    val shift = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    calendar.add(Calendar.DAY_OF_MONTH, -shift)

    return buildList {
        repeat(42) {
            add(
                CalendarDay(
                    key = dayKey(calendar.timeInMillis),
                    dayNumber = calendar.get(Calendar.DAY_OF_MONTH).toString(),
                    isCurrentMonth = calendar.get(Calendar.MONTH) == month
                )
            )
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }
}

private fun dayKey(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(timestamp))
}