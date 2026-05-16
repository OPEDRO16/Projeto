package com.train.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.train.app.data.models.Exercise
import com.train.app.data.models.WorkoutSet
import com.train.app.data.models.Routine

// Fallbacks de fontes caso não estejam no Theme.kt
// Idealmente estas devem ser definidas no seu ui/theme/Type.kt
val HankenGrotesk = FontFamily.Default
val JetBrainsMono = FontFamily.Monospace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTrackerScreen(
    routine: Routine, // Recebemos a rotina completa agora
    onFinishWorkout: (List<Exercise>) -> Unit
) {
    // Inicializamos o estado com os exercícios da rotina recebida
    var exercises by remember { mutableStateOf(routine.exercises) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = routine.name.uppercase(),
                            style = TextStyle(
                                fontFamily = HankenGrotesk,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = 0.05.em,
                                color = Color(0xFFE6E1E1)
                            )
                        )
                        Text(
                            text = "EM CURSO",
                            style = TextStyle(
                                fontFamily = JetBrainsMono,
                                fontSize = 10.sp,
                                color = Color(0xFF0A62D0),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { onFinishWorkout(exercises) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A62D0)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("FINALIZAR", fontFamily = JetBrainsMono, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF141313))
            )
        },
        containerColor = Color(0xFF141313)
    ) { padding ->
        if (exercises.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Nenhum exercício nesta rotina",
                    color = Color(0xFF968F92),
                    fontFamily = HankenGrotesk
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }

                itemsIndexed(exercises) { exerciseIndex, exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        onSetUpdate = { setIndex, updatedSet ->
                            val newList = exercises.toMutableList()
                            val newSets = exercise.sets.toMutableList()
                            newSets[setIndex] = updatedSet
                            newList[exerciseIndex] = exercise.copy(sets = newSets)
                            exercises = newList
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: Exercise,
    onSetUpdate: (Int, WorkoutSet) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = exercise.name,
                style = TextStyle(
                    fontFamily = HankenGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF0A62D0) // Accent Blue para o título do exercício
                )
            )
            IconButton(onClick = { /* Menu de opções do exercício */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color(0xFF968F92))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cabeçalho da Tabela inspirado no Hevy
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderText("SET", Modifier.weight(0.8f))
            HeaderText("KG", Modifier.weight(2f))
            HeaderText("REPS", Modifier.weight(2f))
            Box(Modifier.weight(0.8f))
        }

        // Linhas de Séries
        exercise.sets.forEachIndexed { index, set ->
            SetRow(
                setNumber = index + 1,
                set = set,
                onUpdate = { updatedSet -> onSetUpdate(index, updatedSet) }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun SetRow(
    setNumber: Int,
    set: WorkoutSet,
    onUpdate: (WorkoutSet) -> Unit
) {
    val isCompleted = set.completed
    val backgroundColor = if (isCompleted) Color(0xFF0A62D0).copy(alpha = 0.15f) else Color.Transparent
    val contentColor = if (isCompleted) Color.White else Color(0xFFE6E1E1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Número da Série
        Text(
            text = setNumber.toString(),
            modifier = Modifier.weight(0.8f),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontFamily = JetBrainsMono,
                color = if (isCompleted) Color(0xFF0A62D0) else Color(0xFF968F92),
                fontWeight = FontWeight.Bold
            )
        )

        // Campo de Peso
        DataInputField(
            value = if (set.weight == 0f) "" else set.weight.toString(),
            placeholder = "0",
            modifier = Modifier.weight(2f),
            isCompleted = isCompleted,
            onValueChange = {
                val newVal = it.replace(',', '.').toFloatOrNull() ?: 0f
                onUpdate(set.copy(weight = newVal))
            }
        )

        // Campo de Repetições
        DataInputField(
            value = if (set.reps == 0) "" else set.reps.toString(),
            placeholder = "0",
            modifier = Modifier.weight(2f),
            isCompleted = isCompleted,
            onValueChange = {
                val newVal = it.toIntOrNull() ?: 0
                onUpdate(set.copy(reps = newVal))
            }
        )

        // Botão de Check
        Box(
            modifier = Modifier.weight(0.8f),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = { onUpdate(set.copy(completed = !set.completed)) },
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (isCompleted) Color(0xFF0A62D0) else Color(0xFF201F1F),
                        RoundedCornerShape(4.dp)
                    )
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isCompleted) Color.White else Color(0xFF363434)
                )
            }
        }
    }
}

@Composable
fun HeaderText(text: String, modifier: Modifier) {
    Text(
        text = text,
        modifier = modifier,
        textAlign = TextAlign.Center,
        style = TextStyle(
            fontFamily = JetBrainsMono,
            fontSize = 11.sp,
            color = Color(0xFF968F92),
            fontWeight = FontWeight.Bold
        )
    )
}

@Composable
fun DataInputField(
    value: String,
    placeholder: String,
    modifier: Modifier,
    isCompleted: Boolean,
    onValueChange: (String) -> Unit
) {
    Box(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .fillMaxHeight(0.8f)
            .background(
                if (isCompleted) Color.Transparent else Color(0xFF201F1F),
                RoundedCornerShape(4.dp)
            )
            .border(
                1.dp,
                if (isCompleted) Color.Transparent else Color(0xFF363434),
                RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontFamily = JetBrainsMono,
                color = if (isCompleted) Color.White else Color.White,
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            cursorBrush = SolidColor(Color(0xFF0A62D0)),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            fontFamily = JetBrainsMono,
                            color = Color(0xFF4B4548),
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                innerTextField()
            }
        )
    }
}