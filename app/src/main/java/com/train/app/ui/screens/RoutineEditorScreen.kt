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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
import com.train.app.data.models.ExerciseLibraryItem
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
import com.train.app.ui.theme.SurfaceLevel1
import com.train.app.ui.theme.TextWhite
import java.util.UUID

@Composable
fun RoutineEditorScreen(
    routineId: String? = null,
    onSaveComplete: () -> Unit = {},
    onBack: () -> Unit = {}
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

    val currentUserId = Firebase.auth.currentUser?.uid
    var customExercises by remember { mutableStateOf<List<ExerciseLibraryItem>>(emptyList()) }
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            Firebase.firestore.collection("users")
                .document(currentUserId)
                .collection("custom_exercises")
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot != null) {
                        customExercises = snapshot.toObjects(ExerciseLibraryItem::class.java)
                    }
                }
        }
    }

    val baseExercises = remember { ExerciseLibraryRepository.exercises }
    val combinedExercises = remember(baseExercises, customExercises) {
        baseExercises + customExercises
    }

    val muscleFilters = remember(combinedExercises) {
        listOf("All") + combinedExercises.map { it.primaryMuscle }.distinct().sorted()
    }

    val filteredExercises = remember(combinedExercises, query, selectedMuscle) {
        combinedExercises.filter { exercise ->
            val matchesQuery = query.isBlank() ||
                    exercise.name.contains(query, ignoreCase = true) ||
                    exercise.primaryMuscle.contains(query, ignoreCase = true)
            val matchesMuscle = selectedMuscle == "All" || exercise.primaryMuscle == selectedMuscle
            matchesQuery && matchesMuscle
        }
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
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = TextWhite)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditing) "Editar Rotina" else "Criar Rotina",
                        style = AppTypography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                        color = TextWhite
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isEditing) "Altera os exercícios e o nome desta rotina."
                           else "Adiciona exercícios para montar a tua rotina personalizada.",
                    style = AppTypography.bodyMedium,
                    color = OutlineBorder
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceLevel1,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2D2D))
                ) {
                    BasicTextField(
                        value = routineName,
                        onValueChange = { routineName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        textStyle = AppTypography.bodyMedium.copy(color = TextWhite),
                        cursorBrush = SolidColor(AccentBlue),
                        decorationBox = { innerTextField ->
                            if (routineName.isBlank()) {
                                Text("Nome da rotina (ex: Costas & Bíceps)", color = OutlineBorder)
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
                    shape = RoundedCornerShape(12.dp),
                    color = AccentBlue.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentBlue.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (showInlineLibrary) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (showInlineLibrary) "FECHAR BIBLIOTECA" else "ADICIONAR EXERCÍCIOS",
                            style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = AccentBlue
                        )
                    }
                }
            }

            if (showInlineLibrary) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceLevel1,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2D2D))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "PESQUISAR NA BIBLIOTECA",
                                style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = AccentBlue
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
                                    textStyle = AppTypography.bodyMedium.copy(color = TextWhite),
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
                                    val isSelected = selectedMuscle == option
                                    Surface(
                                        modifier = Modifier.clickable { selectedMuscle = option },
                                        shape = RoundedCornerShape(999.dp),
                                        color = if (isSelected) AccentBlue.copy(alpha = 0.18f) else BackgroundDark,
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AccentBlue) else null
                                    ) {
                                        Text(
                                            text = option,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) AccentBlue else OutlineBorder
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
                        color = SurfaceLevel1,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2D2D))
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
                                    color = TextWhite
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
                                color = if (alreadyAdded) SurfaceLevel1 else AccentBlue.copy(alpha = 0.16f)
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
                                        text = if (alreadyAdded) "ADICIONADO" else "ADICIONAR",
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
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceLevel1,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2D2D))
                    ) {
                        Text(
                            text = "Ainda não adicionaste exercícios.",
                            modifier = Modifier.padding(16.dp),
                            style = AppTypography.bodyMedium,
                            color = OutlineBorder
                        )
                    }
                }
            }

            itemsIndexed(selectedExercises) { index, exercise ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceLevel1,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2D2D))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = exercise.name,
                                style = AppTypography.headlineLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${exercise.sets.size} séries base",
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
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceLevel1,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentYellow.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = errorMessage ?: "Erro",
                            modifier = Modifier.padding(16.dp),
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
                    shape = RoundedCornerShape(12.dp),
                    color = AccentBlue
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isSaving) "A GUARDAR..." else "GUARDAR ROTINA",
                            style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextWhite
                        )
                    }
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