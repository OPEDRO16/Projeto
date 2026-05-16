package com.train.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.train.app.data.models.Exercise
import com.train.app.data.models.WorkoutSession

// Renomeado para evitar "Conflicting declarations" com o Theme.kt
private val LocalHanken = FontFamily.Default
private val LocalMono = FontFamily.Monospace

@Composable
fun WorkoutSummaryScreen(
    session: WorkoutSession,
    onClose: () -> Unit,
    onShare: () -> Unit
) {
    val totalVolume = session.exercises.sumOf { ex ->
        ex.sets.filter { it.completed }.sumOf { (it.weight * it.reps).toDouble() }
    }

    val totalSets = session.exercises.sumOf { ex -> ex.sets.count { it.completed } }

    Scaffold(
        containerColor = Color(0xFF141313), // Surface de acordo com DESIGN.md
        bottomBar = {
            Column(modifier = Modifier.padding(20.dp)) {
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A62D0)),
                    shape = RoundedCornerShape(4.dp) // Radius SM/Default de acordo com o design
                ) {
                    Text(
                        text = "CONCLUIR",
                        fontFamily = LocalHanken,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE6E1E1).copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PARTILHAR NO FEED",
                        color = Color.White,
                        fontFamily = LocalHanken
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(40.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF0A62D0),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "TREINO CONCLUÍDO!",
                    style = TextStyle(
                        fontFamily = LocalHanken,
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp,
                        color = Color.White
                    )
                )
                Text(
                    text = session.routineName.uppercase(),
                    style = TextStyle(
                        fontFamily = LocalMono,
                        fontSize = 14.sp,
                        color = Color(0xFFF3D869), // Accent Yellow (PR/Alert)
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(40.dp))
            }

            // Grelha de Métricas Rápidas
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryMetric(label = "DURAÇÃO", value = "${session.durationMinutes}m")
                    SummaryMetric(label = "VOLUME", value = "${totalVolume.toInt()}kg")
                    SummaryMetric(label = "SÉRIES", value = "$totalSets")
                }
                Spacer(modifier = Modifier.height(40.dp))
                HorizontalDivider(color = Color(0xFF2B2A2A), thickness = 1.dp)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "RESUMO DOS EXERCÍCIOS",
                    modifier = Modifier.fillMaxWidth(),
                    style = TextStyle(
                        fontFamily = LocalMono,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF968F92) // Outline color
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Lista de Exercícios Realizados
            items(session.exercises) { exercise ->
                ExerciseSummaryItem(exercise)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun SummaryMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = LocalMono,
                fontSize = 10.sp,
                color = Color(0xFF968F92),
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = LocalHanken,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}

@Composable
fun ExerciseSummaryItem(exercise: Exercise) {
    val completedSets = exercise.sets.filter { it.completed }
    if (completedSets.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1C1B1B), RoundedCornerShape(8.dp)) // Surface-container-low
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name,
                style = TextStyle(
                    fontFamily = LocalHanken,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            )
            Text(
                text = "${completedSets.size} SÉRIES",
                style = TextStyle(
                    fontFamily = LocalMono,
                    fontSize = 11.sp,
                    color = Color(0xFF0A62D0),
                    fontWeight = FontWeight.Bold
                )
            )
        }

        val bestSet = completedSets.maxByOrNull { it.weight }
        if (bestSet != null) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "RECORD",
                    style = TextStyle(
                        fontFamily = LocalMono,
                        fontSize = 9.sp,
                        color = Color(0xFFF3D869),
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${bestSet.weight}kg x ${bestSet.reps}",
                    style = TextStyle(
                        fontFamily = LocalMono,
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}