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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
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
import com.train.app.ui.components.TrainCard
import com.train.app.ui.components.TrainPrimaryButton
import com.train.app.ui.components.TrainSecondaryButton
import com.train.app.ui.theme.AccentBlue
import com.train.app.ui.theme.AccentPurple
import com.train.app.ui.theme.AccentYellow
import com.train.app.ui.theme.AppTypography
import com.train.app.ui.theme.BackgroundDark
import com.train.app.ui.theme.OutlineBorder
import com.train.app.ui.theme.SurfaceLevel0
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineEditorScreen(
    onSaveComplete: () -> Unit = {}
) {
    var routineName by remember { mutableStateOf("") }
    val selectedExercises = remember { mutableStateListOf<Exercise>() }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showLibrarySheet by remember { mutableStateOf(false) }

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
                Text("ROUTINE EDITOR", style = AppTypography.headlineLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Adiciona exercícios da library para montar a tua rotina.",
                    style = AppTypography.bodyMedium,
                    color = OutlineBorder
                )
            }

            item {
                Surface(shape = RoundedCornerShape(10.dp), color = SurfaceLevel0) {
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
                TrainSecondaryButton(
                    text = "ADICIONAR EXERCÍCIOS DA LIBRARY",
                    onClick = {
                        errorMessage = null
                        showLibrarySheet = true
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text(
                    text = "${selectedExercises.size} exercícios selecionados",
                    style = AppTypography.labelSmall,
                    color = AccentBlue
                )
            }

            items(selectedExercises, key = { it.id }) { exercise ->
                TrainCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                        IconButton(onClick = { selectedExercises.removeAll { it.id == exercise.id } }) {
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
                    TrainCard {
                        Text(errorMessage ?: "Erro", color = AccentYellow)
                    }
                }
            }

            item {
                TrainPrimaryButton(
                    text = if (isSaving) "A GUARDAR..." else "GUARDAR ROTINA",
                    onClick = {
                        if (!isSaving) {
                            saveRoutine(
                                routineName = routineName,
                                exercises = selectedExercises.toList(),
                                onSavingChange = { isSaving = it },
                                onError = { errorMessage = it },
                                onSuccess = onSaveComplete
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = AccentBlue
            )
        }
    }

    if (showLibrarySheet) {
        ExerciseLibraryPickerSheet(
            alreadySelectedIds = selectedExercises.map { it.id }.toSet(),
            onDismiss = { showLibrarySheet = false },
            onAddExercise = { exercise ->
                if (selectedExercises.none { it.id == exercise.id }) {
                    selectedExercises.add(exercise)
                }
                showLibrarySheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseLibraryPickerSheet(
    alreadySelectedIds: Set<String>,
    onDismiss: () -> Unit,
    onAddExercise: (Exercise) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf("All") }
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

    LaunchedEffect(alreadySelectedIds.size) { }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BackgroundDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("ADD EXERCISE", style = AppTypography.headlineLarge)

            Surface(shape = RoundedCornerShape(10.dp), color = SurfaceLevel0) {
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
                        color = if (selectedMuscle == option) AccentPurple.copy(alpha = 0.18f) else SurfaceLevel0
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredExercises, key = { it.id }) { libraryItem ->
                    val alreadyAdded = libraryItem.id in alreadySelectedIds

                    TrainCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                        onAddExercise(
                                            Exercise(
                                                id = libraryItem.id,
                                                name = libraryItem.name,
                                                sets = listOf(
                                                    WorkoutSet(),
                                                    WorkoutSet(),
                                                    WorkoutSet()
                                                )
                                            )
                                        )
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
        }
    }
}

private fun saveRoutine(
    routineName: String,
    exercises: List<Exercise>,
    onSavingChange: (Boolean) -> Unit,
    onError: (String?) -> Unit,
    onSuccess: () -> Unit
) {
    val userId = Firebase.auth.currentUser?.uid
    if (userId == null) {
        onError("Utilizador não autenticado")
        return
    }

    if (routineName.isBlank()) {
        onError("Indica um nome para a rotina")
        return
    }

    if (exercises.isEmpty()) {
        onError("Adiciona pelo menos um exercício")
        return
    }

    onError(null)
    onSavingChange(true)

    val routine = Routine(
        id = UUID.randomUUID().toString(),
        name = routineName,
        exercises = exercises
    )

    Firebase.firestore
        .collection("users")
        .document(userId)
        .collection("routines")
        .document(routine.id)
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