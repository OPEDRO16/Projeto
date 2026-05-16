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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
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
import com.google.firebase.auth.FirebaseUser
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
fun ProfileScreen() {
    val currentUser = FirebaseManager.auth.currentUser
    var routinesCount by remember { mutableStateOf(0) }
    var sessionsCount by remember { mutableStateOf(0) }
    var lastWorkoutDate by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(currentUser != null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser?.uid) {
        if (currentUser == null) {
            isLoading = false
            return@LaunchedEffect
        }

        val userId = currentUser.uid
        FirebaseManager.firestore
            .collection("users")
            .document(userId)
            .collection("routines")
            .get()
            .addOnSuccessListener { routinesSnapshot ->
                routinesCount = routinesSnapshot.size()

                FirebaseManager.firestore
                    .collection("users")
                    .document(userId)
                    .collection("sessions")
                    .get()
                    .addOnSuccessListener { sessionsSnapshot ->
                        val sessions = sessionsSnapshot.toObjects(WorkoutSession::class.java)
                        sessionsCount = sessions.size
                        lastWorkoutDate = sessions.maxByOrNull { it.startTime }?.startTime?.let { millis ->
                            SimpleDateFormat("dd MMM yyyy", Locale("pt", "PT")).format(Date(millis))
                        }
                        isLoading = false
                    }
                    .addOnFailureListener { error ->
                        errorMessage = error.message ?: "Erro ao carregar sessões"
                        isLoading = false
                    }
            }
            .addOnFailureListener { error ->
                errorMessage = error.message ?: "Erro ao carregar perfil"
                isLoading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text("PERFIL", style = AppTypography.headlineLarge)
        Spacer(modifier = Modifier.height(20.dp))

        if (currentUser == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sem utilizador autenticado", color = AccentYellow)
            }
            return@Column
        }

        ProfileHeader(currentUser)
        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            }

            errorMessage != null -> {
                TrainCard {
                    Text(errorMessage!!, color = AccentYellow)
                }
            }

            else -> {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileStatCard("ROTINAS", routinesCount.toString(), Modifier.weight(1f))
                    ProfileStatCard("TREINOS", sessionsCount.toString(), Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                TrainCard {
                    Column {
                        Text("RESUMO", style = AppTypography.labelSmall, color = OutlineBorder)
                        Spacer(modifier = Modifier.height(10.dp))
                        ProfileInfoRow("Email", currentUser.email ?: "Sem email")
                        ProfileInfoRow("Nome", currentUser.displayName ?: usernameFromEmail(currentUser.email))
                        ProfileInfoRow("Último treino", lastWorkoutDate ?: "Ainda sem treinos")
                        ProfileInfoRow("Plano", "Free")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TrainCard {
                    Column {
                        Text("CONTA", style = AppTypography.labelSmall, color = OutlineBorder)
                        Spacer(modifier = Modifier.height(12.dp))
                        TrainSecondaryButton(
                            text = "EDITAR PERFIL",
                            onClick = { },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        TrainPrimaryButton(
                            text = "TERMINAR SESSÃO",
                            onClick = { FirebaseManager.auth.signOut() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(user: FirebaseUser) {
    val displayName = user.displayName ?: usernameFromEmail(user.email)
    val email = user.email ?: "Conta sem email"

    TrainCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(AccentBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(36.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName.uppercase(),
                    style = AppTypography.headlineLarge.copy(fontSize = 22.sp),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(email, style = AppTypography.bodyLarge, color = OutlineBorder)
            }
        }
    }
}

@Composable
private fun ProfileStatCard(title: String, value: String, modifier: Modifier = Modifier) {
    TrainCard(modifier = modifier) {
        Column {
            Text(title, style = AppTypography.labelSmall, color = OutlineBorder)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = AppTypography.headlineLarge.copy(
                    fontSize = 26.sp,
                    fontFamily = FontFamily.Monospace
                ),
                color = Color.White
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label.uppercase(), style = AppTypography.labelSmall, color = OutlineBorder)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = AppTypography.bodyLarge, color = Color.White)
        Spacer(modifier = Modifier.height(12.dp))
    }
}

private fun usernameFromEmail(email: String?): String {
    if (email.isNullOrBlank()) return "ATLETA"
    return email.substringBefore("@").replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
    }
}