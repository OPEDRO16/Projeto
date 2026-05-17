package com.train.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.train.app.data.ExerciseLibraryRepository
import com.train.app.data.models.Exercise
import com.train.app.data.models.Routine
import com.train.app.data.models.WorkoutSet
import com.train.app.ui.theme.AccentBlue
import com.train.app.ui.theme.AccentPurple
import com.train.app.ui.theme.AccentYellow
import com.train.app.ui.theme.AppTypography
import com.train.app.ui.theme.BackgroundDark
import com.train.app.ui.theme.OutlineBorder
import com.train.app.ui.theme.SurfaceLevel0
import java.util.UUID

@Composable
fun RoutineEditorScreen(
    routineId: String? = null,
    onSaveComplete: () -> Unit = {}
) {
    var routineName by remember { mutableStateOf("") }
    val selectedExercises = remember { mutableStateListOf<Exercise>() }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showInlineLibrary by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf("All") }
    val isEditing = !routineId.isNullOrBlank()

    // Pre-load existing routine when editing
    LaunchedEffect(routineId) {
        val uid = Firebase.auth.currentUser?.uid ?: return@LaunchedEffect
        if (!routineId.isNullOrBlank()) {
            Firebase.firestore
                .collection("users").document(uid)
                .collection("routines").document(routineId)
                .get()
                .addOnSuccessListener { doc ->
                    val routine = doc.toObject(Routine::class.java)
                    if (routine != null) {
                        routineName = routine.name
                        selectedExercises.clear()
                        selectedExercises.addAll(routine.exercises)
                    }
                }
        }
    }

    val allExercises = remember { ExerciseLibraryRepository.exercises }
    val muscleFilters = remember(allExercises) {
        listOf("All") + allExercises.map { it.primaryMuscle }.distinct().sorted()
    }

    val filteredExercises = remember(query, selectedMuscle) {
        ExerciseLibraryRepository.filterExercises(
            query = query,
            primaryMuscle = selectedMuscle,
            equipment = "All",
            difficulty = "All",
            category = "All"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    if (isEditing) "EDITAR ROTINA" else "ROUTINE EDITOR",
                    style = AppTypography.headlineLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isEditing) "Altera os exercícios e o nome desta rotina."
                           else "Adiciona exercícios da library para montar a tua rotina.",
                    style = AppTypography.bodyMedium,
                    color = OutlineBorder
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceLevel0
                ) {
                    BasicTextField(
                        value = routineName,
                        onValueChange = { routineName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        textStyle = AppTypography.bodyMedium.copy(color = Color.White),
                        cursorBrush = SolidColor(AccentBlue),
                        decorationBox = { innerTextField ->
                            if (routineName.isBlank()) {
                                Text("Nome da rotina", color = OutlineBorder)
                            }
                            innerTextField()
                        }
                    )
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            errorMessage = null
                            showInlineLibrary = !showInlineLibrary
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = AccentPurple.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = if (showInlineLibrary) "FECHAR BIBLIOTECA" else "ADICIONAR EXERCÍCIOS DA LIBRARY",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                        style = AppTypography.labelSmall,
                        color = AccentPurple,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (showInlineLibrary) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceLevel0
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "BIBLIOTECA DE EXERCÍCIOS",
                                style = AppTypography.labelSmall,
                                color = AccentPurple
                            )

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = BackgroundDark
                            ) {
                                BasicTextField(
                                    value = query,
                                    onValueChange = { query = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 14.dp),
                                    textStyle = AppTypography.bodyMedium.copy(color = Color.White),
                                    cursorBrush = SolidColor(AccentBlue),
                                    decorationBox = { innerTextField ->
                                        if (query.isBlank()) {
                                            Text("Pesquisar exercício", color = OutlineBorder)
                                        }
                                        innerTextField()
                                    }
                                )
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                muscleFilters.forEach { option ->
                                    Surface(
                                        modifier = Modifier.clickable { selectedMuscle = option },
                                        shape = RoundedCornerShape(999.dp),
                                        color = if (selectedMuscle == option) AccentPurple.copy(alpha = 0.18f) else BackgroundDark
                                    ) {
                                        Text(
                                            text = option,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            style = AppTypography.labelSmall,
                                            color = if (selectedMuscle == option) AccentPurple else OutlineBorder
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "${filteredExercises.size} exercícios encontrados",
                                style = AppTypography.labelSmall,
                                color = AccentBlue
                            )
                        }
                    }
                }

                itemsIndexed(filteredExercises) { index, libraryItem ->
                    val alreadyAdded = selectedExercises.any { it.id == libraryItem.id }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceLevel0
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = libraryItem.name,
                                    style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${libraryItem.primaryMuscle} • ${libraryItem.equipment}",
                                    style = AppTypography.bodyMedium,
                                    color = OutlineBorder
                                )
                            }

                            Surface(
                                modifier = Modifier.clickable {
                                    if (!alreadyAdded) {
                                        selectedExercises.add(
                                            Exercise(
                                                id = "${libraryItem.id}_${UUID.randomUUID()}",
                                                name = libraryItem.name,
                                                instructions = libraryItem.instructions.joinToString("\n"),
                                                sets = listOf(
                                                    WorkoutSet(),
                                                    WorkoutSet(),
                                                    WorkoutSet()
                                                ),
                                                completed = false
                                            )
                                        )
                                        showInlineLibrary = true
                                        errorMessage = null
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (alreadyAdded) SurfaceLevel0 else AccentBlue.copy(alpha = 0.16f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (alreadyAdded) Icons.Default.Check else Icons.Default.Add,
                                        contentDescription = null,
                                        tint = if (alreadyAdded) OutlineBorder else AccentBlue
                                    )
                                    Text(
                                        text = if (alreadyAdded) "ADDED" else "ADD",
                                        style = AppTypography.labelSmall,
                                        color = if (alreadyAdded) OutlineBorder else AccentBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "${selectedExercises.size} exercícios selecionados",
                    style = AppTypography.labelSmall,
                    color = AccentBlue
                )
            }

            if (selectedExercises.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceLevel0
                    ) {
                        Text(
                            text = "Ainda não adicionaste exercícios.",
                            modifier = Modifier.padding(14.dp),
                            style = AppTypography.bodyMedium,
                            color = OutlineBorder
                        )
                    }
                }
            }

            itemsIndexed(selectedExercises) { index, exercise ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceLevel0
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = exercise.name,
                                style = AppTypography.headlineLarge.copy(fontSize = 18.sp),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${exercise.sets.size} sets base",
                                style = AppTypography.bodyMedium,
                                color = OutlineBorder
                            )
                        }

                        IconButton(
                            onClick = {
                                selectedExercises.removeAt(index)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remover exercício",
                                tint = AccentYellow
                            )
                        }
                    }
                }
            }

            if (errorMessage != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceLevel0
                    ) {
                        Text(
                            text = errorMessage ?: "Erro",
                            modifier = Modifier.padding(14.dp),
                            color = AccentYellow,
                            style = AppTypography.bodyMedium
                        )
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!isSaving) {
                                saveRoutine(
                                    routineName = routineName,
                                    exercises = selectedExercises.toList(),
                                    existingRoutineId = if (isEditing) routineId else null,
                                    onSavingChange = { isSaving = it },
                                    onError = { errorMessage = it },
                                    onSuccess = onSaveComplete
                                )
                            }
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = AccentBlue.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = if (isSaving) "A GUARDAR..." else "GUARDAR ROTINA",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                        style = AppTypography.labelSmall,
                        color = AccentBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = AccentBlue
            )
        }
    }
}

private fun saveRoutine(
    routineName: String,
    exercises: List<Exercise>,
    existingRoutineId: String? = null,
    onSavingChange: (Boolean) -> Unit,
    onError: (String?) -> Unit,
    onSuccess: () -> Unit
) {
    val userId = Firebase.auth.currentUser?.uid
    if (userId == null) { onError("Utilizador não autenticado"); return }
    if (routineName.isBlank()) { onError("Indica um nome para a rotina"); return }
    if (exercises.isEmpty()) { onError("Adiciona pelo menos um exercício"); return }

    onError(null)
    onSavingChange(true)

    val finalId = if (!existingRoutineId.isNullOrBlank()) existingRoutineId
                  else UUID.randomUUID().toString()

    val routine = Routine(
        id = finalId,
        userId = userId,
        name = routineName,
        exercises = exercises
    )

    Firebase.firestore
        .collection("users")
        .document(userId)
        .collection("routines")
        .document(finalId)
        .set(routine)
        .addOnSuccessListener {
            onSavingChange(false)
            onSuccess()
        }
        .addOnFailureListener { error ->
            onSavingChange(false)
            onError(error.message ?: "Erro ao guardar rotina")
        }
}