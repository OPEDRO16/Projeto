package com.train.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.train.app.ui.components.*
import com.train.app.ui.theme.*

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Cabeçalho
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SurfaceLevel1)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Alex Mercer",
                    style = AppTypography.headlineLarge,
                    color = TextPrimary
                )
                Text(
                    text = "Pushing limits. Day by day.",
                    style = AppTypography.bodyLarge,
                    color = OutlineBorder
                )
            }
        }

        // Botões de Ação
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TrainSecondaryButton(
                text = "EDIT PROFILE",
                onClick = { /* TODO */ },
                modifier = Modifier.weight(1f)
            )
            TrainSecondaryButton(
                text = "SHARE",
                onClick = { /* TODO */ },
                modifier = Modifier.weight(1f)
            )
        }

        // Estatísticas
        TrainCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "142", style = AppTypography.headlineLarge, color = TextPrimary)
                    Text(text = "WORKOUTS", style = AppTypography.labelMedium, color = OutlineBorder)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "84K", style = AppTypography.headlineLarge, color = TextPrimary)
                    Text(text = "VOLUME (KG)", style = AppTypography.labelMedium, color = OutlineBorder)
                }
            }
        }

        // Exercícios Frequentes
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "FREQUENT EXERCISES",
                style = AppTypography.labelMedium,
                color = OutlineBorder
            )
            Row {
                TrainChip(text = "Squat", isWarning = false)
                TrainChip(text = "Bench Press", isWarning = false)
                TrainChip(text = "Deadlift", isWarning = true)
            }
        }

        // Calendário e Amigos
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TrainCard(modifier = Modifier.weight(1f)) {
                Column {
                    Text(text = "CALENDAR", style = AppTypography.labelMedium, color = OutlineBorder)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "12 Day Streak",
                        style = AppTypography.bodyLarge,
                        color = AccentYellow,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            TrainCard(modifier = Modifier.weight(1f)) {
                Column {
                    Text(text = "FRIENDS", style = AppTypography.labelMedium, color = OutlineBorder)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "24 Active",
                        style = AppTypography.bodyLarge,
                        color = AccentBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Atividade Recente
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "RECENT ACTIVITY",
                style = AppTypography.labelMedium,
                color = OutlineBorder
            )
            repeat(3) { index ->
                val title = if (index % 2 == 0) "Upper Body Power" else "Legs Heavy"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = title,
                            style = AppTypography.bodyLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Yesterday • 45 min",
                            style = AppTypography.labelMedium,
                            color = OutlineBorder
                        )
                    }
                    Text(
                        text = "420 kcal",
                        style = AppTypography.labelMedium,
                        color = AccentPurple
                    )
                }
            }
        }
    }
}