package com.train.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.train.app.data.FirebaseManager
import com.train.app.ui.components.TrainCard
import com.train.app.ui.components.TrainChip
import com.train.app.ui.components.TrainProgressBar
import com.train.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen() {
    val currentUser = FirebaseManager.auth.currentUser
    val username = currentUser?.email?.split("@")?.get(0)?.uppercase() ?: "ATLETA"

    // Obter data atual formatada
    val dateStr = SimpleDateFormat("EEEE, dd MMMM", Locale("pt", "PT")).format(Date())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(Modifier.statusBarsPadding()) }

        // Boas-vindas
        item {
            Column(Modifier.padding(vertical = 8.dp)) {
                Text(text = dateStr.uppercase(), style = AppTypography.labelSmall, color = OutlineBorder)
                Text(text = "FORÇA, $username", style = AppTypography.displayLarge.copy(fontSize = 32.sp))
            }
        }

        // Card de Consistência Semanal (Usa o TrainProgressBar restaurado)
        item {
            Text(text = "CONSISTÊNCIA DA SEMANA", style = AppTypography.labelSmall, color = OutlineBorder)
            Spacer(Modifier.height(8.dp))
            TrainCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Meta de Treinos", style = AppTypography.bodyLarge)
                        Text(
                            text = "3 / 5",
                            style = AppTypography.labelMedium.copy(fontFamily = FontFamily.Monospace, color = AccentBlue)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    TrainProgressBar(progress = 0.6f)
                }
            }
        }

        // Card de Volume Total Estimado
        item {
            Text(text = "MÉTRICAS DA SESSÃO ANTERIOR", style = AppTypography.labelSmall, color = OutlineBorder)
            Spacer(Modifier.height(8.dp))
            TrainCard {
                Column {
                    Text(text = "VOLUME TOTAL LEVANTADO", style = AppTypography.labelSmall, color = OutlineBorder)
                    Text(
                        text = "4,250 KG",
                        style = AppTypography.displayLarge.copy(fontSize = 36.sp, fontFamily = FontFamily.Monospace, color = AccentBlue)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TrainChip(text = "Hipertrofia")
                        TrainChip(text = "Recorde", isWarning = true)
                    }
                }
            }
        }
    }
}