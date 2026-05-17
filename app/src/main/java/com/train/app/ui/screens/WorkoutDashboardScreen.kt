package com.train.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FieldValue
import com.train.app.data.FirebaseManager
import com.train.app.data.models.Routine
import com.train.app.data.ai.AiRoutineService
import com.train.app.ui.components.TrainPrimaryButton
import com.train.app.ui.components.TrainSecondaryButton
import com.train.app.ui.theme.*
import android.widget.Toast
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDashboardScreen(
    onStartWorkout: (Routine?) -> Unit,
    onNavigateToEditor: () -> Unit,
    onNavigateToEditRoutine: (String) -> Unit = {},
    subscriptionTier: String = "FREE",
    onOpenSubscriptionPaywall: () -> Unit = {}
) {
    val currentUser = FirebaseManager.auth.currentUser
    var routines by remember { mutableStateOf<List<Routine>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var showAiDialog by remember { mutableStateOf(false) }
    var aiObjective by remember { mutableStateOf("Hipertrofia") }
    var aiLoading by remember { mutableStateOf(false) }
    var selectedMuscles by remember { mutableStateOf(setOf<String>()) }
    var aiTime by remember { mutableStateOf("1h") }
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            FirebaseManager.firestore.collection("users")
                .document(currentUser.uid)
                .collection("routines")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        routines = snapshot.toObjects(Routine::class.java)
                    }
                    isLoading = false
                }
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Treino", style = AppTypography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp), color = TextPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        val maxRoutinesText = if (subscriptionTier == "FREE") "3" else if (subscriptionTier == "PRO") "8" else "∞"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(TextPrimary.copy(alpha = 0.08f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${routines.size}/$maxRoutinesText",
                                color = OutlineBorder,
                                style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    if (subscriptionTier == "PRO") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                                    )
                                )
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "★ PRO",
                                color = Color.Black,
                                style = AppTypography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    } else if (subscriptionTier == "MASTER") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF3B82F6))
                                    )
                                )
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "👑 MASTER",
                                color = TextWhite,
                                style = AppTypography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(TextPrimary.copy(alpha = 0.08f))
                                .clickable { onOpenSubscriptionPaywall() }
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                                .border(0.5.dp, TextPrimary.copy(alpha = 0.15f), RoundedCornerShape(50))
                        ) {
                            Text(
                                text = "OBTER PREMIUM 👑",
                                color = TextPrimary,
                                style = AppTypography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { onStartWorkout(null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceLevel1)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = TextWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Iniciar Treinamento Vazio", color = TextWhite, style = AppTypography.bodyLarge)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val maxRoutines = if (subscriptionTier == "FREE") 3 else if (subscriptionTier == "PRO") 8 else 99999
                            if (routines.size >= maxRoutines) {
                                Toast.makeText(context, "Atingiu o limite de $maxRoutines rotinas do plano $subscriptionTier. Faça upgrade! 👑", Toast.LENGTH_LONG).show()
                                onOpenSubscriptionPaywall()
                            } else {
                                onNavigateToEditor()
                            }
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceLevel1),
                        contentPadding = PaddingValues(vertical = 0.dp)
                    ) {
                        Text("Nova Rotina", color = TextWhite, style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Button(
                        onClick = {
                            if (subscriptionTier == "FREE") {
                                Toast.makeText(context, "O gerador de rotinas com IA é um recurso exclusivo PRO/MASTER! 👑", Toast.LENGTH_LONG).show()
                                onOpenSubscriptionPaywall()
                            } else {
                                val aiRoutinesCount = routines.count { it.isAiGenerated }
                                if (subscriptionTier == "PRO" && aiRoutinesCount >= 3) {
                                    Toast.makeText(context, "Limite de 3 rotinas geradas com IA atingido no plano PRO. Faça upgrade para MASTER! 👑", Toast.LENGTH_LONG).show()
                                    onOpenSubscriptionPaywall()
                                } else {
                                    showAiDialog = true
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        contentPadding = PaddingValues(vertical = 0.dp)
                    ) {
                        Text("Nova Rotina AI", color = TextWhite, style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            if (isLoading) {
                item { CircularProgressIndicator(color = AccentBlue, modifier = Modifier.padding(32.dp)) }
            } else if (routines.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Ainda não tens rotinas.", color = OutlineBorder, style = AppTypography.bodyMedium)
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                if (subscriptionTier == "FREE") {
                                    Toast.makeText(context, "O gerador de rotinas com IA é um recurso exclusivo PRO/MASTER! 👑", Toast.LENGTH_LONG).show()
                                    onOpenSubscriptionPaywall()
                                } else {
                                    val aiRoutinesCount = routines.count { it.isAiGenerated }
                                    if (subscriptionTier == "PRO" && aiRoutinesCount >= 3) {
                                        Toast.makeText(context, "Limite de 3 rotinas geradas com IA atingido no plano PRO. Faça upgrade para MASTER! 👑", Toast.LENGTH_LONG).show()
                                        onOpenSubscriptionPaywall()
                                    } else {
                                        showAiDialog = true
                                    }
                                }
                            },
                            modifier = Modifier.width(280.dp).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                        ) {
                            Text("GERAR COM AI", color = TextWhite, style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToEditor,
                            modifier = Modifier.width(280.dp).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceLevel1)
                        ) {
                            Text("Criar Manualmente", color = TextWhite, style = AppTypography.bodyLarge)
                        }
                    }
                }
            } else {
                items(routines) { routine ->
                    RoutineCard(
                        routine = routine,
                        onStart = { onStartWorkout(routine) },
                        onEdit = { onNavigateToEditRoutine(routine.id) },
                        onDelete = {
                            if (currentUser != null) {
                                FirebaseManager.firestore
                                    .collection("users")
                                    .document(currentUser.uid)
                                    .collection("routines")
                                    .document(routine.id)
                                    .delete()
                            }
                        }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showAiDialog) {
        var aiStatusText by remember { mutableStateOf("Iniciando IA...") }
        LaunchedEffect(aiLoading) {
            if (aiLoading) {
                val statusLines = listOf(
                    "Analisando objetivos...",
                    "Selecionando os melhores exercícios...",
                    "Ajustando repetições e volume...",
                    "Finalizando treino customizado..."
                )
                for (line in statusLines) {
                    aiStatusText = line
                    kotlinx.coroutines.delay(450)
                }
            }
        }

        val objectiveOptions = listOf(
            "Hipertrofia" to "Hipertrofia",
            "Ganhar Força" to "Força",
            "Saudável" to "Saudável",
            "Definição" to "Definição",
            "Calistenia" to "Calistenia"
        )

        val splitOptions = listOf(
            "Peito" to "Peito",
            "Costas" to "Costas",
            "Pernas" to "Pernas",
            "Bíceps" to "Bíceps",
            "Tríceps" to "Tríceps",
            "Ombros" to "Ombros",
            "Core" to "Core / Abs",
            "Corpo Inteiro" to "Corpo Inteiro"
        )

        AlertDialog(
            onDismissRequest = { if (!aiLoading) showAiDialog = false },
            containerColor = SurfaceLevel1,
            title = { 
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Treinador Pessoal AI", 
                        color = TextPrimary, 
                        style = AppTypography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Crie uma rotina científica de forma instantânea.",
                        color = OutlineBorder,
                        style = AppTypography.bodySmall
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Objective Selection
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Qual é o teu objetivo principal?",
                            color = TextPrimary,
                            style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            objectiveOptions.forEach { (value, label) ->
                                val isSelected = aiObjective == value || (aiObjective.isEmpty() && value == "Hipertrofia")
                                Surface(
                                    modifier = Modifier.clickable {
                                        if (!aiLoading) aiObjective = value
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) AccentBlue.copy(alpha = 0.18f) else BackgroundDark,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AccentBlue) else androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder.copy(alpha = 0.2f))
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        color = if (isSelected) AccentBlue else OutlineBorder,
                                        style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    )
                                }
                            }
                        }
                    }

                    // Split Selection
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Quais os grupos musculares / divisão?",
                            color = TextPrimary,
                            style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            splitOptions.forEach { (value, label) ->
                                val isSelected = selectedMuscles.contains(value)
                                Surface(
                                    modifier = Modifier.clickable {
                                        if (!aiLoading) {
                                            selectedMuscles = if (isSelected) selectedMuscles - value else selectedMuscles + value
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) AccentBlue.copy(alpha = 0.18f) else BackgroundDark,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AccentBlue) else androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder.copy(alpha = 0.2f))
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        color = if (isSelected) AccentBlue else OutlineBorder,
                                        style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    )
                                }
                            }
                        }
                    }

                    // Time Selection
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Quanto tempo tens para treinar?",
                            color = TextPrimary,
                            style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(8.dp))
                        val timeOptions = listOf(
                            "30 min" to "⏱️ 30 min",
                            "1h" to "⚡ 1 hora",
                            "1h30" to "🔥 1h 30m",
                            "2h" to "🏆 2 horas"
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            timeOptions.forEach { (value, label) ->
                                val isSelected = aiTime == value
                                Surface(
                                    modifier = Modifier.clickable {
                                        if (!aiLoading) aiTime = value
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) AccentBlue.copy(alpha = 0.18f) else BackgroundDark,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AccentBlue) else androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder.copy(alpha = 0.2f))
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        color = if (isSelected) AccentBlue else OutlineBorder,
                                        style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    )
                                }
                            }
                        }
                    }

                    if (aiLoading) {
                        Spacer(Modifier.height(8.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = AccentBlue, strokeWidth = 3.dp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = aiStatusText,
                                color = AccentBlue,
                                style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (!aiLoading) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val activeUser = currentUser?.uid ?: "test_user_ai"
                                val finalObj = if (aiObjective.isEmpty()) "Hipertrofia" else aiObjective
                                aiLoading = true
                                coroutineScope.launch {
                                    try {
                                        val newRoutine = AiRoutineService.generateRoutine(finalObj, selectedMuscles.toList(), aiTime)
                                        val finalRoutine = newRoutine.copy(userId = activeUser, isAiGenerated = true)
                                        FirebaseManager.firestore.collection("users").document(activeUser)
                                            .collection("routines").document(finalRoutine.id).set(finalRoutine)
                                            
                                        aiLoading = false
                                        showAiDialog = false
                                        aiObjective = "Hipertrofia"
                                        selectedMuscles = emptySet()
                                        aiTime = "1h"
                                        Toast.makeText(context, "Treino gerado com sucesso!", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        android.util.Log.e("AiRoutine", "Crash no GERAR", e)
                                        aiLoading = false
                                        Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = AccentBlue
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("GERAR COM AI", color = TextWhite, style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            },
            dismissButton = {
                if (!aiLoading) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAiDialog = false },
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceLevel1,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2D2D))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("CANCELAR", color = OutlineBorder, style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun RoutineCard(
    routine: Routine,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceLevel1)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = routine.name.ifBlank { "Nova Rotina" },
                        style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    if (routine.isAiGenerated) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AccentBlue.copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                .border(0.5.dp, AccentBlue, RoundedCornerShape(4.dp))
                        ) {
                            Text(
                                text = "IA",
                                color = AccentBlue,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = AccentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color(0xFFFFB4AB),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            val exercisesText = routine.exercises.joinToString(", ") { it.name }
            Text(
                text = if (exercisesText.isBlank()) "Sem exercícios" else exercisesText,
                style = AppTypography.labelMedium,
                color = OutlineBorder,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Iniciar Rotina", color = TextWhite, style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}
