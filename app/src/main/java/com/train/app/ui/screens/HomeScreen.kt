package com.train.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.train.app.ui.components.*
import com.train.app.ui.theme.*

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "DASHBOARD",
            style = AppTypography.headlineLarge,
            color = TextPrimary
        )

        // Today's Focus
        TrainCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "TODAY'S FOCUS",
                    style = AppTypography.labelMedium,
                    color = OutlineBorder
                )
                Text(
                    text = "Upper Body Power",
                    style = AppTypography.headlineLarge,
                    color = TextPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "45 MIN",
                        style = AppTypography.labelMedium,
                        color = AccentPurple
                    )
                    Text(
                        text = "420 KCAL",
                        style = AppTypography.labelMedium,
                        color = AccentYellow
                    )
                }
                TrainPrimaryButton(
                    text = "START WORKOUT",
                    onClick = { /* TODO */ },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Weekly Metrics
        TrainCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "WEEKLY METRICS",
                    style = AppTypography.labelMedium,
                    color = OutlineBorder
                )

                // Volume
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Volume", style = AppTypography.bodyLarge, color = TextPrimary)
                    Text(text = "12,400 KG", style = AppTypography.labelMedium, color = AccentBlue)
                }
                TrainProgressBar(progress = 0.7f)

                Spacer(modifier = Modifier.height(4.dp))

                // Recovery
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Recovery", style = AppTypography.bodyLarge, color = TextPrimary)
                    Text(text = "85%", style = AppTypography.labelMedium, color = AccentPurple)
                }
                TrainProgressBar(progress = 0.85f)
            }
        }

        // Quick Routines
        TrainCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "QUICK ROUTINES",
                    style = AppTypography.labelMedium,
                    color = OutlineBorder
                )
                Text(
                    text = "15 Min Kettlebell Core",
                    style = AppTypography.bodyLarge,
                    color = TextPrimary
                )
                TrainSecondaryButton(
                    text = "VIEW ROUTINE",
                    onClick = { /* TODO */ },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}