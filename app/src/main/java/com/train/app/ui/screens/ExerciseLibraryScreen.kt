package com.train.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.train.app.data.ExerciseLibraryRepository
import com.train.app.data.models.ExerciseLibraryItem
import com.train.app.ui.components.TrainCard
import com.train.app.ui.theme.AccentBlue
import com.train.app.ui.theme.AccentPurple
import com.train.app.ui.theme.AppTypography
import com.train.app.ui.theme.BackgroundDark
import com.train.app.ui.theme.OutlineBorder
import com.train.app.ui.theme.SurfaceLevel0

@Composable
fun ExerciseLibraryScreen(
    onOpenExercise: (String) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf("All") }
    var selectedEquipment by remember { mutableStateOf("All") }
    var selectedDifficulty by remember { mutableStateOf("All") }
    var selectedCategory by remember { mutableStateOf("All") }

    val allExercises = remember { ExerciseLibraryRepository.exercises }
    val muscleFilters = remember(allExercises) { listOf("All") + allExercises.map { it.primaryMuscle }.distinct().sorted() }
    val equipmentFilters = remember(allExercises) { listOf("All") + allExercises.map { it.equipment }.distinct().sorted() }
    val difficultyFilters = remember(allExercises) { listOf("All") + allExercises.map { it.difficulty.name }.distinct().sorted() }
    val categoryFilters = remember(allExercises) { listOf("All") + allExercises.map { it.category }.distinct().sorted() }

    val filteredExercises = remember(query, selectedMuscle, selectedEquipment, selectedDifficulty, selectedCategory) {
        ExerciseLibraryRepository.filterExercises(
            query = query,
            primaryMuscle = selectedMuscle,
            equipment = selectedEquipment,
            difficulty = selectedDifficulty,
            category = selectedCategory
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("EXERCISE LIBRARY", style = AppTypography.headlineLarge)
            Spacer(modifier = Modifier.padding(top = 6.dp))
            Text(
                text = "Pesquisa, filtra e abre exercícios da biblioteca base.",
                style = AppTypography.bodyMedium,
                color = OutlineBorder
            )
        }

        item {
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
                            Text("Pesquisar exercício, músculo ou equipamento", color = OutlineBorder)
                        }
                        innerTextField()
                    }
                )
            }
        }

        item {
            FilterSection("MUSCLE", muscleFilters, selectedMuscle) { selectedMuscle = it }
        }

        item {
            FilterSection("EQUIPMENT", equipmentFilters, selectedEquipment) { selectedEquipment = it }
        }

        item {
            FilterSection("DIFFICULTY", difficultyFilters, selectedDifficulty) { selectedDifficulty = it }
        }

        item {
            FilterSection("CATEGORY", categoryFilters, selectedCategory) { selectedCategory = it }
        }

        item {
            Text(
                text = "${filteredExercises.size} exercícios",
                style = AppTypography.labelSmall,
                color = AccentBlue
            )
        }

        items(filteredExercises) { exercise ->
            ExerciseLibraryCard(exercise = exercise, onClick = { onOpenExercise(exercise.id) })
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = AppTypography.labelSmall, color = AccentPurple)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                Surface(
                    modifier = Modifier.clickable { onSelected(option) },
                    shape = RoundedCornerShape(999.dp),
                    color = if (selected == option) AccentPurple.copy(alpha = 0.18f) else SurfaceLevel0
                ) {
                    Text(
                        text = option.replace('_', ' '),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = AppTypography.labelSmall,
                        color = if (selected == option) AccentPurple else OutlineBorder
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseLibraryCard(
    exercise: ExerciseLibraryItem,
    onClick: () -> Unit
) {
    TrainCard(modifier = Modifier.clickable { onClick() }) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = AppTypography.headlineLarge.copy(fontSize = 18.sp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.padding(top = 4.dp))
                    Text(
                        text = "${exercise.primaryMuscle} • ${exercise.equipment} • ${exercise.category}",
                        style = AppTypography.bodyMedium,
                        color = OutlineBorder
                    )
                    Spacer(modifier = Modifier.padding(top = 4.dp))
                    Text(
                        text = exercise.difficulty.name.replace('_', ' '),
                        style = AppTypography.labelSmall,
                        color = AccentBlue
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentBlue.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = "ABRIR",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        style = AppTypography.labelSmall,
                        color = AccentBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}