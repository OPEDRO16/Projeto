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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.train.app.data.FirebaseManager
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
import com.train.app.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLibraryScreen(
    onOpenExercise: (String) -> Unit = {},
    onBack: () -> Unit = {},
    subscriptionTier: String = "FREE",
    onOpenSubscriptionPaywall: () -> Unit = {}
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedMuscle by remember { mutableStateOf("All") }
    var selectedEquipment by remember { mutableStateOf("All") }
    var selectedDifficulty by remember { mutableStateOf("All") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showCreateDialog by remember { mutableStateOf(false) }

    val currentUserId = FirebaseManager.auth.currentUser?.uid
    var customExercises by remember { mutableStateOf<List<ExerciseLibraryItem>>(emptyList()) }

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            FirebaseManager.firestore.collection("users")
                .document(currentUserId)
                .collection("custom_exercises")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        customExercises = snapshot.toObjects(ExerciseLibraryItem::class.java)
                    }
                }
        }
    }

    val allExercises = remember { ExerciseLibraryRepository.exercises }
    val combinedExercises = remember(allExercises, customExercises) {
        allExercises + customExercises
    }

    val muscleFilters = remember(combinedExercises) { listOf("All") + combinedExercises.map { it.primaryMuscle }.distinct().sorted() }
    val equipmentFilters = remember(combinedExercises) { listOf("All") + combinedExercises.map { it.equipment }.distinct().sorted() }
    val difficultyFilters = remember(combinedExercises) { listOf("All") + combinedExercises.map { it.difficulty.name }.distinct().sorted() }
    val categoryFilters = remember(combinedExercises) { listOf("All") + combinedExercises.map { it.category }.distinct().sorted() }

    val filteredExercises = remember(combinedExercises, query, selectedMuscle, selectedEquipment, selectedDifficulty, selectedCategory) {
        combinedExercises.filter { exercise ->
            val matchesQuery = query.isBlank() ||
                    exercise.name.contains(query, ignoreCase = true) ||
                    exercise.primaryMuscle.contains(query, ignoreCase = true) ||
                    exercise.secondaryMuscles.any { it.contains(query, ignoreCase = true) } ||
                    exercise.equipment.contains(query, ignoreCase = true)

            val matchesMuscle = selectedMuscle == "All" || exercise.primaryMuscle == selectedMuscle
            val matchesEquipment = selectedEquipment == "All" || exercise.equipment == selectedEquipment
            val matchesDifficulty = selectedDifficulty == "All" || exercise.difficulty.name == selectedDifficulty
            val matchesCategory = selectedCategory == "All" || exercise.category == selectedCategory

            matchesQuery && matchesMuscle && matchesEquipment && matchesDifficulty && matchesCategory
        }
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = TextWhite)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Biblioteca",
                            style = AppTypography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                            color = TextWhite
                        )
                    }

                    Button(
                        onClick = {
                            if (subscriptionTier == "FREE") {
                                android.widget.Toast.makeText(
                                    context,
                                    "A criação de exercícios personalizados é um recurso exclusivo PRO/MASTER! 👑",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                                onOpenSubscriptionPaywall()
                            } else {
                                showCreateDialog = true
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Criar",
                            color = Color.White,
                            style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
                Text(
                    text = "Pesquisa, filtra e abre exercícios da biblioteca base e personalizados.",
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
                        textStyle = AppTypography.bodyMedium.copy(color = TextWhite),
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
                            tint = if (activeFiltersCount > 0) AccentBlue else TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Filtros de Pesquisa",
                            style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextWhite
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
                ExerciseLibraryCard(
                    exercise = exercise,
                    onClick = { onOpenExercise(exercise.id) },
                    onDelete = if (exercise.isCustom) {
                        {
                            if (currentUserId != null) {
                                FirebaseManager.firestore.collection("users")
                                    .document(currentUserId)
                                    .collection("custom_exercises")
                                    .document(exercise.id)
                                    .delete()
                                    .addOnSuccessListener {
                                        android.widget.Toast.makeText(context, "Exercício eliminado!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    } else null
                )
            }
        }
    }

    if (showCreateDialog) {
        var newExerciseName by remember { mutableStateOf("") }
        var newExerciseMuscle by remember { mutableStateOf("Chest") }
        var newExerciseInstructions by remember { mutableStateOf("") }
        var newExerciseTips by remember { mutableStateOf("") }

        val musclesList = listOf("Chest", "Back", "Legs", "Shoulders", "Biceps", "Triceps", "Abs", "Cardio")

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = SurfaceLevel1,
            title = {
                Text(
                    text = "Criar Novo Exercício",
                    color = TextWhite,
                    style = AppTypography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp)
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text("Nome do Exercício", color = OutlineBorder, style = AppTypography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BackgroundDark,
                            border = BorderStroke(1.dp, Color(0xFF2E2D2D))
                        ) {
                            BasicTextField(
                                value = newExerciseName,
                                onValueChange = { newExerciseName = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                textStyle = AppTypography.bodyMedium.copy(color = TextWhite),
                                cursorBrush = SolidColor(AccentBlue),
                                decorationBox = { innerTextField ->
                                    if (newExerciseName.isBlank()) {
                                        Text("Ex: Supino Halteres", color = OutlineBorder.copy(alpha = 0.6f))
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }

                    Column {
                        Text("Músculo Alvo Principal", color = OutlineBorder, style = AppTypography.labelSmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            musclesList.forEach { muscle ->
                                val isSelected = newExerciseMuscle == muscle
                                Surface(
                                    modifier = Modifier.clickable { newExerciseMuscle = muscle },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) AccentBlue.copy(alpha = 0.18f) else BackgroundDark,
                                    border = BorderStroke(1.dp, if (isSelected) AccentBlue else Color(0xFF2E2D2D))
                                ) {
                                    Text(
                                        text = muscle,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        color = if (isSelected) AccentBlue else OutlineBorder,
                                        style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }

                    Column {
                        Text("Instruções / Passos (um por linha)", color = OutlineBorder, style = AppTypography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BackgroundDark,
                            border = BorderStroke(1.dp, Color(0xFF2E2D2D))
                        ) {
                            BasicTextField(
                                value = newExerciseInstructions,
                                onValueChange = { newExerciseInstructions = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 80.dp, max = 150.dp)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                textStyle = AppTypography.bodyMedium.copy(color = TextWhite),
                                cursorBrush = SolidColor(AccentBlue),
                                decorationBox = { innerTextField ->
                                    if (newExerciseInstructions.isBlank()) {
                                        Text(
                                            text = "Passo 1: Deitar no banco...\nPasso 2: Descer com controlo...",
                                            color = OutlineBorder.copy(alpha = 0.6f)
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }

                    Column {
                        Text("Dicas / Conselhos (um por linha)", color = OutlineBorder, style = AppTypography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BackgroundDark,
                            border = BorderStroke(1.dp, Color(0xFF2E2D2D))
                        ) {
                            BasicTextField(
                                value = newExerciseTips,
                                onValueChange = { newExerciseTips = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 60.dp, max = 120.dp)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                textStyle = AppTypography.bodyMedium.copy(color = TextWhite),
                                cursorBrush = SolidColor(AccentBlue),
                                decorationBox = { innerTextField ->
                                    if (newExerciseTips.isBlank()) {
                                        Text(
                                            text = "Dica: Manter os punhos alinhados...\nDica: Controlar a respiração...",
                                            color = OutlineBorder.copy(alpha = 0.6f)
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newExerciseName.isNotBlank() && currentUserId != null) {
                            val cleanName = newExerciseName.trim()
                            val slug = "custom_" + java.util.UUID.randomUUID().toString()
                            
                            val autoForce = when (newExerciseMuscle) {
                                "Chest", "Shoulders", "Triceps" -> com.train.app.data.models.ExerciseForce.PUSH
                                "Back", "Biceps" -> com.train.app.data.models.ExerciseForce.PULL
                                "Legs" -> com.train.app.data.models.ExerciseForce.LEGS
                                "Abs" -> com.train.app.data.models.ExerciseForce.CORE
                                else -> com.train.app.data.models.ExerciseForce.FULL_BODY
                            }

                            val customItem = ExerciseLibraryItem(
                                id = slug,
                                name = cleanName,
                                primaryMuscle = newExerciseMuscle,
                                secondaryMuscles = emptyList(),
                                equipment = "Custom",
                                category = "Custom",
                                force = autoForce,
                                difficulty = com.train.app.data.models.ExerciseDifficulty.BEGINNER,
                                instructions = newExerciseInstructions.lines().filter { it.isNotBlank() },
                                tips = newExerciseTips.lines().filter { it.isNotBlank() },
                                videoUrl = "",
                                isCustom = true
                            )

                            FirebaseManager.firestore.collection("users")
                                .document(currentUserId)
                                .collection("custom_exercises")
                                .document(slug)
                                .set(customItem)
                                .addOnSuccessListener {
                                    android.widget.Toast.makeText(context, "Exercício personalizado criado!", android.widget.Toast.LENGTH_SHORT).show()
                                    showCreateDialog = false
                                }
                                .addOnFailureListener { e ->
                                    android.widget.Toast.makeText(context, "Erro ao criar: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                }
                        } else {
                            android.widget.Toast.makeText(context, "Insira um nome válido", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Criar", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showCreateDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, Color(0xFF2E2D2D)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar", color = OutlineBorder)
                }
            }
        )
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
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    TrainCard(
        modifier = Modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = AppTypography.headlineLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.padding(top = 4.dp))
                    Text(
                        text = "${exercise.primaryMuscle} • ${exercise.equipment} • ${exercise.category}",
                        style = AppTypography.bodyMedium,
                        color = OutlineBorder
                    )
                    Spacer(modifier = Modifier.padding(top = 4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AccentBlue.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = exercise.difficulty.name.replace('_', ' '),
                                style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = AccentBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (exercise.isCustom) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AccentPurple.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "PERSONALIZADO",
                                    style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AccentPurple,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color(0xFFFFB4AB)
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
}