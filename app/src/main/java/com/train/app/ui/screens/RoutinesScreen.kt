package com.train.app.ui.screens

import androidx.compose.foundation.clickable
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
            FloatingActionButton(onClick = onNavigateToEditor, containerColor = AccentBlue, contentColor = Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Nova Rotina")
            }
        },
        containerColor = BackgroundDark
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("TREINAR", style = AppTypography.headlineLarge)
                TrainSecondaryButton("CRIAR COM AI", onClick = { showAiDialog = true })
            }
            Spacer(Modifier.height(16.dp))
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AccentBlue) }
            } else if (routines.isEmpty()) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ainda não tens rotinas.", color = OutlineBorder)
                    Spacer(Modifier.height(16.dp))
                    TrainSecondaryButton("CRIAR PRIMEIRA ROTINA", onClick = onNavigateToEditor)
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
            AlertDialog(
                onDismissRequest = { if (!aiLoading) showAiDialog = false },
                containerColor = SurfaceLevel1,
                title = { Text("Criar Treino com AI", color = Color.White) },
                text = {
                    Column {
                        TrainInput(
                            value = aiObjective,
                            onValueChange = { aiObjective = it },
                            placeholder = "Objetivo (ex: Ganhar força)"
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Grupos Musculares (Opcional):", color = OutlineBorder, style = AppTypography.labelSmall)
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(muscleOptions) { muscle ->
                                val isSelected = selectedMuscles.contains(muscle)
                                Surface(
                                    modifier = Modifier.clickable {
                                        selectedMuscles = if (isSelected) selectedMuscles - muscle else selectedMuscles + muscle
                                    },
                                    shape = AppShapes.small,
                                    color = if (isSelected) AccentBlue else SurfaceLevel0
                                ) {
                                    Text(
                                        text = muscle,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        color = if (isSelected) Color.White else OutlineBorder,
                                        style = AppTypography.labelSmall
                                    )
                                }
                            }
                        }
                        if (aiLoading) {
                            Spacer(Modifier.height(16.dp))
                            CircularProgressIndicator(color = AccentBlue, modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                    }
                },
                confirmButton = {
                    if (!aiLoading) {
                        TrainPrimaryButton("GERAR", onClick = {
                            val activeUser = userId ?: "test_user_ai"
                            aiLoading = true
                            coroutineScope.launch {
                                try {
                                    val newRoutine = AiRoutineService.generateRoutine(aiObjective, selectedMuscles.toList())
                                    val finalRoutine = newRoutine.copy(userId = activeUser)
                                    FirebaseManager.firestore.collection("users").document(activeUser)
                                        .collection("routines").document(finalRoutine.id).set(finalRoutine)
                                        
                                    aiLoading = false
                                    showAiDialog = false
                                    aiObjective = ""
                                    selectedMuscles = emptySet()
                                    Toast.makeText(context, "Treino gerado com sucesso!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    android.util.Log.e("AiRoutine", "Crash no GERAR", e)
                                    aiLoading = false
                                    Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        })
                    }
                },
                dismissButton = {
                    if (!aiLoading) {
                        TrainSecondaryButton("CANCELAR", onClick = { showAiDialog = false })
                    }
                }
            )
        }
    }
}