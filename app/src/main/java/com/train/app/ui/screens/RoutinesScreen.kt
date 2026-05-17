package com.train.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.train.app.data.FirebaseManager
import com.train.app.data.models.*
import com.train.app.data.ai.AiRoutineService
import com.train.app.ui.components.*
import com.train.app.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@Composable
fun RoutinesScreen(onStartWorkout: (Routine) -> Unit, onNavigateToEditor: () -> Unit) {
    var routines by remember { mutableStateOf<List<Routine>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAiDialog by remember { mutableStateOf(false) }
    var aiObjective by remember { mutableStateOf("") }
    var aiLoading by remember { mutableStateOf(false) }
    val muscleOptions = listOf("Peito", "Costas", "Pernas", "Braços", "Core")
    var selectedMuscles by remember { mutableStateOf(setOf<String>()) }
    var aiTime by remember { mutableStateOf("1h") }
    val userId = FirebaseManager.auth.currentUser?.uid
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        if (userId != null) {
            FirebaseManager.firestore.collection("users").document(userId).collection("routines")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        routines = snapshot.toObjects(Routine::class.java)
                        isLoading = false
                    }
                }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToEditor, containerColor = AccentBlue, contentColor = TextWhite, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Nova Rotina")
            }
        },
        containerColor = BackgroundDark
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("TREINAR", style = AppTypography.headlineLarge)
                TrainPrimaryButton("CRIAR COM AI 🤖", onClick = { showAiDialog = true })
            }
            Spacer(Modifier.height(16.dp))
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AccentBlue) }
            } else if (routines.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Ainda não tens rotinas.", color = OutlineBorder, style = AppTypography.bodyMedium)
                    Spacer(Modifier.height(20.dp))
                    TrainPrimaryButton(
                        text = "GERAR ROTINA COM AI 🤖",
                        onClick = { showAiDialog = true },
                        modifier = Modifier.width(260.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    TrainSecondaryButton(
                        text = "Criar Manualmente",
                        onClick = onNavigateToEditor,
                        modifier = Modifier.width(260.dp)
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(routines) { routine ->
                        TrainCard {
                            Column {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(routine.name, style = AppTypography.headlineLarge.copy(fontSize = 20.sp))
                                    TrainChip(routine.focus)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("${routine.exercises.size} Exercícios", color = OutlineBorder)
                                Spacer(Modifier.height(16.dp))
                                TrainPrimaryButton("COMEÇAR TREINO", onClick = { onStartWorkout(routine) }, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
        }
        
        if (showAiDialog) {
            var aiStatusText by remember { mutableStateOf("Iniciando IA...") }
            LaunchedEffect(aiLoading) {
                if (aiLoading) {
                    val statusLines = listOf(
                        "Analisando objetivos...",
                        "Selecionando os melhores exercícios...",
                        "Ajustando repetições e volume...",
                        "Finalizando treino customizado..."
                    )
                    for (line in statusLines) {
                        aiStatusText = line
                        kotlinx.coroutines.delay(450)
                    }
                }
            }

            val objectiveOptions = listOf(
                "Hipertrofia" to "💪 Hipertrofia",
                "Ganhar Força" to "⚡ Força",
                "Saudável" to "🌱 Saudável",
                "Definição" to "🔥 Definição"
            )

            val splitOptions = listOf(
                "Peito" to "🍒 Peito",
                "Costas" to "🦇 Costas",
                "Pernas" to "🍗 Pernas",
                "Braços" to "🦾 Braços",
                "Ombros" to "🛡️ Ombros",
                "Core / Abs" to "🧩 Abs",
                "Peito & Ombros" to "🍒🛡️ Peito + Ombro",
                "Costas & Bíceps" to "🦇🦾 Costas + Bíceps",
                "Corpo Inteiro" to "🌍 Corpo Inteiro"
            )

            AlertDialog(
                onDismissRequest = { if (!aiLoading) showAiDialog = false },
                containerColor = SurfaceLevel1,
                title = { 
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Treinador Pessoal AI", 
                            color = TextWhite, 
                            style = AppTypography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Crie uma rotina científica de forma instantânea.",
                            color = OutlineBorder,
                            style = AppTypography.bodySmall
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Objective Selection
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Qual é o teu objetivo principal?",
                                color = TextWhite.copy(alpha = 0.9f),
                                style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                objectiveOptions.forEach { (value, label) ->
                                    val isSelected = aiObjective == value || (aiObjective.isEmpty() && value == "Hipertrofia")
                                    Surface(
                                        modifier = Modifier.clickable {
                                            if (!aiLoading) aiObjective = value
                                        },
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (isSelected) AccentBlue else SurfaceLevel0,
                                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2D2D))
                                    ) {
                                        Text(
                                            text = label,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            color = if (isSelected) TextWhite else OutlineBorder,
                                            style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        )
                                    }
                                }
                            }
                        }

                        // Split Selection
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Quais os grupos musculares / divisão?",
                                color = TextWhite.copy(alpha = 0.9f),
                                style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                splitOptions.forEach { (value, label) ->
                                    val isSelected = selectedMuscles.contains(value)
                                    Surface(
                                        modifier = Modifier.clickable {
                                            if (!aiLoading) {
                                                selectedMuscles = if (isSelected) selectedMuscles - value else selectedMuscles + value
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) AccentBlue else SurfaceLevel0,
                                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2D2D))
                                    ) {
                                        Text(
                                            text = label,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            color = if (isSelected) TextWhite else OutlineBorder,
                                            style = AppTypography.bodyMedium.copy(fontSize = 13.sp)
                                        )
                                    }
                                }
                            }
                        }

                        // Time Selection
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Quanto tempo tens para treinar?",
                                color = TextPrimary,
                                style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(Modifier.height(8.dp))
                            val timeOptions = listOf(
                                "30 min" to "⏱️ 30 min",
                                "1h" to "⚡ 1 hora",
                                "1h30" to "🔥 1h 30m",
                                "2h" to "🏆 2 horas"
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                timeOptions.forEach { (value, label) ->
                                    val isSelected = aiTime == value
                                    Surface(
                                        modifier = Modifier.clickable {
                                            if (!aiLoading) aiTime = value
                                        },
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (isSelected) AccentBlue.copy(alpha = 0.18f) else SurfaceLevel0,
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AccentBlue) else androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder.copy(alpha = 0.2f))
                                    ) {
                                        Text(
                                            text = label,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            color = if (isSelected) AccentBlue else OutlineBorder,
                                            style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        )
                                    }
                                }
                            }
                        }

                        if (aiLoading) {
                            Spacer(Modifier.height(8.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = AccentBlue, strokeWidth = 3.dp)
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = aiStatusText,
                                    color = AccentBlue,
                                    style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    if (!aiLoading) {
                        TrainPrimaryButton(
                            text = "GERAR COM AI 🤖",
                            onClick = {
                                val activeUser = userId ?: "test_user_ai"
                                val finalObj = if (aiObjective.isEmpty()) "Hipertrofia" else aiObjective
                                aiLoading = true
                                coroutineScope.launch {
                                    try {
                                        val newRoutine = AiRoutineService.generateRoutine(finalObj, selectedMuscles.toList(), aiTime)
                                        val finalRoutine = newRoutine.copy(userId = activeUser)
                                        FirebaseManager.firestore.collection("users").document(activeUser)
                                            .collection("routines").document(finalRoutine.id).set(finalRoutine)
                                            
                                        aiLoading = false
                                        showAiDialog = false
                                        aiObjective = ""
                                        selectedMuscles = emptySet()
                                        aiTime = "1h"
                                        Toast.makeText(context, "Treino gerado com sucesso!", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        android.util.Log.e("AiRoutine", "Crash no GERAR", e)
                                        aiLoading = false
                                        Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                dismissButton = {
                    if (!aiLoading) {
                        TrainSecondaryButton(
                            text = "CANCELAR",
                            onClick = { showAiDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            )
        }
    }
}