package com.train.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.train.app.data.FirebaseManager
import com.train.app.data.models.*
import com.train.app.ui.components.*
import com.train.app.ui.theme.*

@Composable
fun RoutinesScreen(onStartWorkout: (Routine) -> Unit) {
    var routines by remember { mutableStateOf<List<Routine>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val userId = FirebaseManager.auth.currentUser?.uid

    // Escuta em tempo real as rotinas do utilizador
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

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("AS TUAS ROTINAS", style = AppTypography.headlineLarge)
        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(color = AccentBlue)
        } else if (routines.isEmpty()) {
            Text("Ainda não tens rotinas criadas.", color = OutlineBorder)
            // Botão temporário para criar uma rotina de teste se a lista estiver vazia
            TrainSecondaryButton("CRIAR ROTINA DE TESTE", onClick = { createMockRoutine(userId!!) })
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(routines) { routine ->
                    RoutineCard(routine, onStartWorkout)
                }
            }
        }
    }
}

@Composable
fun RoutineCard(routine: Routine, onStartWorkout: (Routine) -> Unit) {
    TrainCard {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(routine.name, style = AppTypography.headlineLarge.copy(fontSize = 20.sp))
                TrainChip(routine.focus)
            }
            Spacer(Modifier.height(8.dp))
            Text("${routine.exercises.size} Exercícios", style = AppTypography.bodyLarge, color = OutlineBorder)
            Spacer(Modifier.height(16.dp))
            TrainPrimaryButton("COMEÇAR TREINO", onClick = { onStartWorkout(routine) }, modifier = Modifier.fillMaxWidth())
        }
    }
}

// Função auxiliar para não ficares com o ecrã vazio no primeiro teste
fun createMockRoutine(userId: String) {
    val mockRoutine = Routine(
        id = "mock_1",
        userId = userId,
        name = "Treino de Peito",
        focus = "Força",
        exercises = listOf(
            Exercise(id = "ex_1", name = "Supino Reto", sets = listOf(WorkoutSet(reps = 10, weight = 60f), WorkoutSet(reps = 10, weight = 60f))),
            Exercise(id = "ex_2", name = "Press inclinado", sets = listOf(WorkoutSet(reps = 12, weight = 20f)))
        )
    )
    FirebaseManager.firestore.collection("users").document(userId).collection("routines").document(mockRoutine.id).set(mockRoutine)
}