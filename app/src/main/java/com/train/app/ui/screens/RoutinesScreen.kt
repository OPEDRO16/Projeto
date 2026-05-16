package com.train.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.train.app.ui.components.*
import com.train.app.ui.theme.*

@Composable
fun RoutinesScreen(onStartWorkout: (Routine) -> Unit, onNavigateToEditor: () -> Unit) {
    var routines by remember { mutableStateOf<List<Routine>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val userId = FirebaseManager.auth.currentUser?.uid

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
            Text("TREINAR", style = AppTypography.headlineLarge)
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
    }
}