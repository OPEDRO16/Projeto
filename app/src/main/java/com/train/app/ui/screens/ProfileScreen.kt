package com.train.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.train.app.data.FirebaseManager
import com.train.app.ui.components.TrainCard
import com.train.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen() {
    val currentUser = FirebaseManager.auth.currentUser
    var sessions by remember { mutableStateOf<List<WorkoutSession>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Carregar histórico de sessões do Firestore em tempo real
    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid
        if (uid != null) {
            FirebaseManager.firestore
                .collection("users")
                .document(uid)
                .collection("sessions")
                .orderBy("endTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        sessions = snapshot.toObjects(WorkoutSession::class.java)
                    }
                    isLoading = false
                }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Espaço para a StatusBar
        item { Spacer(Modifier.statusBarsPadding()) }

        // Cabeçalho do Perfil
        item {
            ProfileHeader(email = currentUser?.email ?: "Atleta")
        }

        // Blocos de Estatísticas
        item {
            StatsRow(totalWorkouts = sessions.size)
        }

        // Título da Secção de Histórico
        item {
            Text(
                text = "HISTÓRICO RECENTE",
                style = AppTypography.labelMedium,
                color = OutlineBorder
            )
        }

        // Lista de Sessões
        if (isLoading) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            }
        } else if (sessions.isEmpty()) {
            item {
                Text(
                    "Ainda não completaste nenhum treino. Os teus registos aparecerão aqui!",
                    style = AppTypography.bodyLarge,
                    color = OutlineBorder,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        } else {
            items(sessions) { session ->
                SessionHistoryCard(session)
            }
        }

        // Botão de Logout
        item {
            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = {
                    FirebaseManager.auth.signOut()
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentYellow),
                border = BorderStroke(1.dp, AccentYellow.copy(alpha = 0.5f)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("SAIR DA CONTA", style = AppTypography.labelMedium)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileHeader(email: String) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(text = "PERFIL DO ATLETA", style = AppTypography.labelMedium, color = AccentBlue)
        Text(
            text = email.split("@")[0].uppercase(),
            style = AppTypography.displayLarge.copy(fontSize = 32.sp)
        )
        Text(text = email, style = AppTypography.bodyLarge, color = OutlineBorder)
    }
}

@Composable
fun StatsRow(totalWorkouts: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatBox(label = "TREINOS", value = totalWorkouts.toString(), modifier = Modifier.weight(1f))
        StatBox(label = "NÍVEL", value = (totalWorkouts / 5 + 1).toString(), modifier = Modifier.weight(1f))
    }
}

@Composable
fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    TrainCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, style = AppTypography.labelSmall, color = OutlineBorder)
            Text(
                text = value,
                style = AppTypography.displayLarge.copy(
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Monospace
                )
            )
        }
    }
}

@Composable
fun SessionHistoryCard(session: WorkoutSession) {
    // Correção: Uso de Locale.forLanguageTag para evitar depreciação
    val date = SimpleDateFormat("dd MMM, HH:mm", Locale.forLanguageTag("pt-PT"))
        .format(Date(session.endTime))

    TrainCard {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(session.routineName, style = AppTypography.headlineLarge.copy(fontSize = 18.sp))
                Text(date.uppercase(), style = AppTypography.labelSmall, color = OutlineBorder)
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, null, tint = AccentBlue, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${session.durationMinutes} MIN",
                    style = AppTypography.labelMedium,
                    color = AccentBlue
                )

                Spacer(Modifier.width(24.dp))

                Icon(Icons.Default.Timeline, null, tint = AccentPurple, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${session.exercises.size} EXERCÍCIOS",
                    style = AppTypography.labelMedium,
                    color = AccentPurple
                )
            }
        }
    }
}