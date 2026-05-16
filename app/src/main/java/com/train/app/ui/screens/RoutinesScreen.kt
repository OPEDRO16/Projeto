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

    LaunchedEffect(userId) {
        if (userId != null) {
            FirebaseManager.firestore.collection("users").document(userId).collection("routines")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) routines = snapshot.toObjects(Routine::class.java)
                    isLoading = false
                }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("TREINAR", style = AppTypography.headlineLarge)
        Spacer(Modifier.height(16.dp))

        if (isLoading) CircularProgressIndicator(color = AccentBlue)
        else if (routines.isEmpty()) {
            Text("Sem rotinas criadas.", color = OutlineBorder)
            TrainSecondaryButton("CRIAR ROTINA DE TESTE", onClick = { createMockRoutine(userId!!) })
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(routines) { routine ->
                    TrainCard {
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(routine.name, style = AppTypography.headlineLarge.copy(fontSize = 20.sp))
                                TrainChip(routine.focus)
                            }
                            Text("${routine.exercises.size} Exercícios", color = OutlineBorder)
                            Spacer(Modifier.height(16.dp))
                            TrainPrimaryButton("COMEÇAR", onClick = { onStartWorkout(routine) }, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}

fun createMockRoutine(userId: String) {
    val mock = Routine(
        id = "mock_1",
        userId = userId,
        name = "Treino de Teste",
        focus = "Força",
        exercises = listOf(
            Exercise("1", "Supino Reto", sets = listOf(WorkoutSet(10, 60f), WorkoutSet(10, 60f)))
        )
    )
    FirebaseManager.firestore.collection("users").document(userId).collection("routines").document(mock.id).set(mock)
}