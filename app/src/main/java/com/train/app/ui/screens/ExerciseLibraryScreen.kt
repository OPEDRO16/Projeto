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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.train.app.data.ExerciseLibraryRepository
import com.train.app.data.models.ExerciseLibraryItem
import com.train.app.ui.components.TrainCard
import com.train.app.ui.theme.AccentBlue
import com.train.app.ui.theme.AccentPurple
import com.train.app.ui.theme.AppTypography
import com.train.app.ui.theme.BackgroundDark
import com.train.app.ui.theme.OutlineBorder
import com.train.app.ui.theme.SurfaceLevel0
import com.train.app.ui.theme.SurfaceLevel1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLibraryScreen(
    onOpenExercise: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
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

    Scaffold(
        containerColor = BackgroundDark
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Biblioteca de Exercícios",
                        style = AppTypography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                        color = Color.White
                    )
                }
                Text(
                    text = "Pesquisa, filtra e abre exercícios da biblioteca base.",
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
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
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
                val activeFiltersCount = listOf(selectedMuscle, selectedEquipment, selectedDifficulty, selectedCategory).count { it != "All" }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showFilters = !showFilters }
                        .background(SurfaceLevel1)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = if (activeFiltersCount > 0) AccentBlue else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Filtros de Pesquisa",
                            style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        if (activeFiltersCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(99.dp),
                                color = AccentBlue
                            ) {
                                Text(
                                    text = "$activeFiltersCount",
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                    style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                    color = BackgroundDark
                                )
                            }
                        }
                    }
                    Icon(
                        imageVector = if (showFilters) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = OutlineBorder
                    )
                }
            }

            if (!showFilters) {
                val hasActiveFilters = selectedMuscle != "All" || selectedEquipment != "All" || selectedDifficulty != "All" || selectedCategory != "All"
                if (hasActiveFilters) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedMuscle != "All") {
                                ActiveFilterChip("Músculo: $selectedMuscle") { selectedMuscle = "All" }
                            }
                            if (selectedEquipment != "All") {
                                ActiveFilterChip("Equipamento: $selectedEquipment") { selectedEquipment = "All" }
                            }
                            if (selectedDifficulty != "All") {
                                ActiveFilterChip("Dificuldade: $selectedDifficulty") { selectedDifficulty = "All" }
                            }
                            if (selectedCategory != "All") {
                                ActiveFilterChip("Categoria: $selectedCategory") { selectedCategory = "All" }
                            }
                        }
                    }
                }
            }

            if (showFilters) {
                item { FilterSection("MÚSCULO", muscleFilters, selectedMuscle) { selectedMuscle = it } }
                item { FilterSection("EQUIPAMENTO", equipmentFilters, selectedEquipment) { selectedEquipment = it } }
                item { FilterSection("DIFICULDADE", difficultyFilters, selectedDifficulty) { selectedDifficulty = it } }
                item { FilterSection("CATEGORIA", categoryFilters, selectedCategory) { selectedCategory = it } }
                
                val hasActiveFilters = selectedMuscle != "All" || selectedEquipment != "All" || selectedDifficulty != "All" || selectedCategory != "All"
                if (hasActiveFilters) {
                    item {
                        Text(
                            text = "Limpar Filtros",
                            style = AppTypography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color.Red),
                            modifier = Modifier
                                .clickable {
                                    selectedMuscle = "All"
                                    selectedEquipment = "All"
                                    selectedDifficulty = "All"
                                    selectedCategory = "All"
                                }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
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
}

@Composable
private fun FilterSection(
    title: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = AccentBlue
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { option ->
                val isSelected = selected == option
                Surface(
                    modifier = Modifier.clickable { onSelected(option) },
                    shape = RoundedCornerShape(999.dp),
                    color = if (isSelected) AccentBlue.copy(alpha = 0.18f) else SurfaceLevel1,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AccentBlue) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2D2D))
                ) {
                    Text(
                        text = option.replace('_', ' '),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) AccentBlue else OutlineBorder
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveFilterChip(
    text: String,
    onClear: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = AccentBlue.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, AccentBlue.copy(alpha = 0.4f)),
        modifier = Modifier.clickable { onClear() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = text,
                style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                color = AccentBlue
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Limpar",
                tint = AccentBlue,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}


@Composable
private fun ExerciseLibraryCard(
    exercise: ExerciseLibraryItem,
    onClick: () -> Unit
) {
    TrainCard(
        modifier = Modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(2.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = AppTypography.headlineLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.padding(top = 4.dp))
                    Text(
                        text = "${exercise.primaryMuscle} • ${exercise.equipment} • ${exercise.category}",
                        style = AppTypography.bodyMedium,
                        color = OutlineBorder
                    )
                    Spacer(modifier = Modifier.padding(top = 4.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AccentBlue.copy(alpha = 0.12f),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = exercise.difficulty.name.replace('_', ' '),
                            style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = AccentBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentBlue.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentBlue.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "ABRIR",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AccentBlue
                    )
                }
            }
        }
    }
}