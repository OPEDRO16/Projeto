package com.train.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.train.app.data.FirebaseManager
import com.train.app.data.models.*
import com.train.app.ui.components.*
import com.train.app.ui.theme.*
import java.util.UUID

@Composable
fun RoutineEditorScreen(onSaveComplete: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var focus by remember { mutableStateOf("Força") }
    val exercises = remember { mutableStateListOf<Exercise>() }
    var isSaving by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        Surface(color = SurfaceLevel1, modifier = Modifier.fillMaxWidth(), border = androidx.compose.foundation.BorderStroke(0.5.dp, OutlineBorder)) {
            Row(Modifier.statusBarsPadding().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "NOVA ROTINA", style = AppTypography.labelMedium, color = AccentBlue)
                if (isSaving) CircularProgressIndicator(color = AccentBlue, modifier = Modifier.size(24.dp))
                else {
                    IconButton(onClick = { if (name.isNotEmpty() && exercises.isNotEmpty()) { isSaving = true; saveRoutine(name, focus, exercises, onSaveComplete) } }) {
                        Icon(Icons.Default.Save, "Guardar", tint = AccentBlue)
                    }
                }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text("DETALHES", style = AppTypography.labelSmall, color = OutlineBorder)
                Spacer(Modifier.height(8.dp))
                TrainInput(value = name, onValueChange = { name = it }, placeholder = "Nome da Rotina")
                Spacer(Modifier.height(12.dp))
                TrainInput(value = focus, onValueChange = { focus = it }, placeholder = "Foco do Treino")
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("EXERCÍCIOS", style = AppTypography.labelSmall, color = OutlineBorder)
                    TextButton(onClick = { exercises.add(Exercise(id = UUID.randomUUID().toString(), name = "", sets = listOf(WorkoutSet(reps = 0, weight = 0f)))) }) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Text(" ADICIONAR", style = AppTypography.labelSmall, color = AccentBlue)
                    }
                }
            }
            itemsIndexed(exercises) { index, exercise ->
                TrainCard {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = exercise.name, onValueChange = { exercises[index] = exercise.copy(name = it) },
                                placeholder = { Text("Nome do Exercício", color = OutlineBorder.copy(alpha = 0.5f)) },
                                modifier = Modifier.weight(1f), textStyle = AppTypography.bodyLarge,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                            )
                            IconButton(onClick = { exercises.removeAt(index) }) {
                                Icon(Icons.Default.Delete, null, tint = AccentYellow, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

fun saveRoutine(name: String, focus: String, exercises: List<Exercise>, onComplete: () -> Unit) {
    val userId = FirebaseManager.auth.currentUser?.uid ?: return
    val routineId = UUID.randomUUID().toString()
    val newRoutine = Routine(id = routineId, userId = userId, name = name, focus = focus, exercises = exercises)
    FirebaseManager.firestore.collection("users").document(userId).collection("routines").document(routineId).set(newRoutine).addOnSuccessListener { onComplete() }
}