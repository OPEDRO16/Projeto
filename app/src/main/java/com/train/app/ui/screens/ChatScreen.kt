package com.train.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.train.app.ui.components.TrainInput
import com.train.app.ui.theme.*

@Composable
fun ChatScreen() {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(
            text = "CHAT",
            style = AppTypography.headlineLarge,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Pesquisa
        TrainInput(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = "Encontrar parceiros de treino..."
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Ativos Agora
        Text(
            text = "ACTIVE NOW",
            style = AppTypography.labelMedium,
            color = OutlineBorder
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(8) { index ->
                ActiveUserAvatar(index)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Lista de Mensagens
        Text(
            text = "RECENT",
            style = AppTypography.labelMedium,
            color = OutlineBorder
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            items(10) { index ->
                ChatItem(index)
            }
        }
    }
}

@Composable
fun ActiveUserAvatar(index: Int) {
    Box(modifier = Modifier.size(56.dp)) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(SurfaceLevel1)
        )
        // Indicador Online (Ponto Verde)
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(Color(0xFF4CAF50))
                .align(Alignment.BottomEnd)
        )
    }
}

@Composable
fun ChatItem(index: Int) {
    val isUnread = index < 2
    val name = if (index == 0) "Elite Runners Club" else "Partner $index"

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SurfaceLevel1)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = AppTypography.bodyLarge,
                fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                color = TextPrimary
            )
            Text(
                text = "Are we still hitting legs today?",
                style = AppTypography.labelMedium,
                color = if (isUnread) TextPrimary else OutlineBorder,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "12:30",
                style = AppTypography.labelMedium,
                color = OutlineBorder
            )
            if (isUnread) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AccentBlue)
                )
            }
        }
    }
}