package com.train.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.train.app.ui.components.*
import com.train.app.ui.theme.*

@Composable
fun RoutinesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(
            text = "ROUTINES",
            style = AppTypography.headlineLarge,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Cabeçalho / Ações
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TrainSecondaryButton(
                text = "IMPORT / EXPORT",
                onClick = { /* TODO */ },
                modifier = Modifier.weight(1f)
            )
            TrainPrimaryButton(
                text = "CREATE",
                onClick = { /* TODO */ },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Lista de Rotinas
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(5) { index ->
                RoutineCardItem(index)
            }
        }
    }
}

@Composable
fun RoutineCardItem(index: Int) {
    val title = if (index % 2 == 0) "Push Day Heavy" else "Legs Volume"
    val tag = if (index % 2 == 0) "Strength" else "Hypertrophy"
    val isWarning = index % 2 == 0

    TrainCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header do Cartão
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = AppTypography.headlineLarge,
                    color = TextPrimary
                )
                TrainChip(text = tag, isWarning = isWarning)
            }

            // Métricas (Exercícios e Tempo)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = "Exercises",
                        tint = OutlineBorder,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "6 EXERCISES",
                        style = AppTypography.labelMedium,
                        color = TextPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = "Duration",
                        tint = OutlineBorder,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "~45 MIN",
                        style = AppTypography.labelMedium,
                        color = TextPrimary
                    )
                }
            }

            // Texto Auxiliar
            Text(
                text = "Last trained: 2 days ago",
                style = AppTypography.bodyLarge,
                color = OutlineBorder
            )
        }
    }
}