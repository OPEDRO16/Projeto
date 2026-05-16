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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.train.app.data.FirebaseManager
import com.train.app.data.models.WorkoutSession
import com.train.app.ui.components.TrainCard
import com.train.app.ui.components.TrainPrimaryButton
import com.train.app.ui.components.TrainSecondaryButton
import com.train.app.ui.theme.AccentBlue
import com.train.app.ui.theme.AccentYellow
import com.train.app.ui.theme.AppTypography
import com.train.app.ui.theme.BackgroundDark
import com.train.app.ui.theme.OutlineBorder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    onOpenCalendar: () -> Unit = {},
    onOpenWorkoutDetail: (String) -> Unit = {}
) {
    val currentUser = FirebaseManager.auth.currentUser
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
                errorMessage = error.message ?: "Erro ao carregar perfil"
                isLoading = false
            }
    }

    if (currentUser == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Text("Sem utilizador autenticado", color = AccentYellow)
        }
        return
    }

    val username = currentUser.displayName ?: currentUser.email?.substringBefore("@")?.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    } ?: "Atleta"

    val totalVolume = sessions.sumOf { it.totalVolume.toDouble() }.toInt()
    val totalWorkouts = sessions.size
    val totalMinutes = sessions.sumOf { it.durationMinutes }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("PERFIL", style = AppTypography.headlineLarge)
        }

        item {
            TrainCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(AccentBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = username.uppercase(),
                                style = AppTypography.headlineLarge.copy(fontSize = 22.sp),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentUser.email ?: "Conta sem email",
                                style = AppTypography.bodyLarge,
                                color = OutlineBorder
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFF2B2A2A))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ProfileStatItem("TREINOS", totalWorkouts.toString())
                        ProfileStatItem("VOLUME", "${totalVolume}KG")
                        ProfileStatItem("MIN", totalMinutes.toString())
                    }
                }
            }
        }

        item {
            CalendarEntryCard(
                sessions = sessions,
                onOpenCalendar = onOpenCalendar
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TrainSecondaryButton(
                    text = "CALENDÁRIO",
                    onClick = onOpenCalendar,
                    modifier = Modifier.weight(1f)
                )
                TrainPrimaryButton(
                    text = "TERMINAR SESSÃO",
                    onClick = { FirebaseManager.auth.signOut() },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text("HISTÓRICO DE TREINOS", style = AppTypography.labelSmall, color = OutlineBorder)
        }

        when {
            isLoading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
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
                        Text("Ainda não existem treinos concluídos.", color = OutlineBorder)
                    }
                }
            }

            else -> {
                items(sessions.take(12)) { session ->
                    WorkoutHistoryCard(
                        session = session,
                        onClick = { onOpenWorkoutDetail(session.id) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun ProfileStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = AppTypography.labelSmall, color = OutlineBorder)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = AppTypography.headlineLarge.copy(
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace
            ),
            color = Color.White
        )
    }
}

@Composable
private fun CalendarEntryCard(
    sessions: List<WorkoutSession>,
    onOpenCalendar: () -> Unit
) {
    val trainedDays = sessions.map {
        SimpleDateFormat("dd/MM", Locale("pt", "PT")).format(Date(it.startTime))
    }.distinct().take(6)

    TrainCard(
        modifier = Modifier.clickable { onOpenCalendar() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("CALENDÁRIO", style = AppTypography.labelSmall, color = OutlineBorder)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (trainedDays.isEmpty()) "Sem dias registados ainda"
                    else trainedDays.joinToString("  •  "),
                    color = Color.White,
                    style = AppTypography.bodyLarge
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = AccentBlue
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = OutlineBorder
                )
            }
        }
    }
}

@Composable
private fun WorkoutHistoryCard(
    session: WorkoutSession,
    onClick: () -> Unit
) {
    val date = remember(session.startTime) {
        SimpleDateFormat("dd MMM yyyy", Locale("pt", "PT")).format(Date(session.startTime))
    }
    val completedSets = session.exercises.sumOf { exercise ->
        exercise.sets.count { it.completed }
    }

    TrainCard(
        modifier = Modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                    Text(date, style = AppTypography.labelSmall, color = OutlineBorder)
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
                HistoryMetric("DURAÇÃO", "${session.durationMinutes} min")
                HistoryMetric("SÉRIES", completedSets.toString())
                HistoryMetric("EXERCÍCIOS", session.exercises.size.toString())
            }
        }
    }
}

@Composable
private fun HistoryMetric(label: String, value: String) {
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