package com.train.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.train.app.data.FirebaseManager
import com.train.app.ui.components.TrainCard
import com.train.app.ui.components.TrainInput
import com.train.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class ProgressPoint(val date: Long, val value: Float)

@Composable
fun EvolutionScreen() {
    var selectedExercise by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var sessions by remember { mutableStateOf<List<WorkoutSession>>(emptyList()) }
    val userId = FirebaseManager.auth.currentUser?.uid

    // Carregamento de dados históricos do utilizador
    LaunchedEffect(userId) {
        if (userId != null) {
            FirebaseManager.firestore
                .collection("users")
                .document(userId)
                .collection("sessions")
                .orderBy("endTime")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        sessions = snapshot.toObjects(WorkoutSession::class.java)
                    }
                }
        }
    }

    // Identificação de todos os exercícios realizados
    val uniqueExercises = remember(sessions) {
        sessions.flatMap { it.exercises }
            .map { it.name }
            .distinct()
            .sortedBy { it }
    }

    val filteredExercises = uniqueExercises.filter {
        it.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        if (selectedExercise == null) {
            // VISTA 1: LISTAGEM DE EXERCÍCIOS
            Column(Modifier.padding(16.dp)) {
                Text("ESTATÍSTICAS", style = AppTypography.headlineLarge)
                Text("Analisa o teu progresso por exercício", style = AppTypography.bodyLarge, color = OutlineBorder)

                Spacer(Modifier.height(16.dp))

                TrainInput(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Pesquisar exercícios..."
                )

                Spacer(Modifier.height(16.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredExercises) { name ->
                        ExerciseListItem(name = name, onClick = { selectedExercise = name })
                    }
                }
            }
        } else {
            // VISTA 2: DETALHE DO EXERCÍCIO COM GRÁFICO
            val exerciseChartData = remember(sessions, selectedExercise) {
                sessions.mapNotNull { session ->
                    val exercise = session.exercises.find { it.name == selectedExercise }
                    val maxWeight = exercise?.sets?.maxOfOrNull { it.weight } ?: 0f
                    if (maxWeight > 0) ProgressPoint(session.endTime, maxWeight) else null
                }
            }

            ExerciseDetailView(
                exerciseName = selectedExercise!!,
                chartData = exerciseChartData,
                onBack = { selectedExercise = null }
            )
        }
    }
}

@Composable
fun ExerciseListItem(name: String, onClick: () -> Unit) {
    TrainCard(modifier = Modifier.clickable { onClick() }) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FitnessCenter, null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(text = name, style = AppTypography.bodyLarge)
            }
            Icon(Icons.Default.ChevronRight, null, tint = OutlineBorder)
        }
    }
}

@Composable
fun ExerciseDetailView(exerciseName: String, chartData: List<ProgressPoint>, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Surface(color = SurfaceLevel1, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.statusBarsPadding().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = TextPrimary)
                }
                Text(
                    text = exerciseName.uppercase(),
                    style = AppTypography.labelMedium,
                    color = AccentBlue,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            item {
                Text("PROGRESSÃO DE CARGA MÁXIMA", style = AppTypography.labelSmall, color = OutlineBorder)
                Spacer(Modifier.height(12.dp))
                EvolutionChart(points = chartData)
            }

            item {
                Text("HISTÓRICO", style = AppTypography.labelSmall, color = OutlineBorder)
                Spacer(Modifier.height(12.dp))
            }

            items(chartData.reversed()) { point ->
                val dateStr = SimpleDateFormat("dd MMM, yyyy", Locale.forLanguageTag("pt-PT")).format(Date(point.date))
                TrainCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(dateStr, style = AppTypography.bodyLarge)
                            Text("Peso Máximo", style = AppTypography.labelSmall, color = OutlineBorder)
                        }
                        Text(
                            text = "${point.value} KG",
                            style = AppTypography.displayLarge.copy(fontSize = 20.sp, color = AccentBlue, fontFamily = FontFamily.Monospace)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EvolutionChart(points: List<ProgressPoint>) {
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .background(SurfaceLevel1, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
        .padding(16.dp)
    ) {
        if (points.size < 2) return@Canvas

        val maxVal = points.maxOf { it.value }
        val minVal = points.minOf { it.value }
        val range = if (maxVal == minVal) 1f else maxVal - minVal

        val width = size.width
        val height = size.height
        val stepX = width / (points.size - 1)

        val path = Path()
        points.forEachIndexed { index, point ->
            val x = index * stepX
            val normalizedY = (point.value - minVal) / range
            val y = height - (normalizedY * height)

            if (index == 0) path.moveTo(x, y)
            else path.lineTo(x, y)

            drawCircle(color = AccentBlue, radius = 4.dp.toPx(), center = Offset(x, y))
        }

        drawPath(path = path, color = AccentBlue, style = Stroke(width = 2.dp.toPx()))
    }
}