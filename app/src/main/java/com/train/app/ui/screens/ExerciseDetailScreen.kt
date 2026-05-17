package com.train.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.train.app.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.train.app.data.ExerciseLibraryRepository
import com.train.app.data.FirebaseManager
import com.train.app.data.models.WorkoutSession
import com.train.app.data.models.ChatRoom
import com.train.app.data.models.UserProfile
import com.train.app.data.models.Message
import java.util.UUID
import android.widget.Toast
import com.train.app.ui.components.TrainCard
import com.train.app.ui.theme.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private data class ExerciseHistoryEntry(
    val sessionId: String,
    val routineName: String,
    val date: Long,
    val heaviestWeight: Float,
    val bestReps: Int,
    val setVolume: Float,
    val sessionVolume: Float,
    val estimatedOneRepMax: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseName: String,
    onBack: () -> Unit = {},
    onOpenWorkout: (String) -> Unit = {}
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var sessions by remember { mutableStateOf<List<WorkoutSession>>(emptyList()) }
    var selectedTab by remember { mutableStateOf("Resumo") }
    var isPlaying by remember { mutableStateOf(true) }

    var showShareSheet by remember { mutableStateOf(false) }
    var chatRooms by remember { mutableStateOf<List<ChatRoom>>(emptyList()) }
    val currentUser = FirebaseManager.auth.currentUser
    var currentUserProfile by remember { mutableStateOf<UserProfile?>(null) }

    LaunchedEffect(currentUser?.uid) {
        val myUid = currentUser?.uid
        if (myUid != null) {
            FirebaseManager.firestore.collection("users").document(myUid)
                .get()
                .addOnSuccessListener { doc ->
                    currentUserProfile = doc.toObject(UserProfile::class.java)
                }

            FirebaseManager.firestore.collection("chatRooms")
                .whereArrayContains("members", myUid)
                .addSnapshotListener { snap, _ ->
                    if (snap != null) {
                        chatRooms = snap.documents.mapNotNull { doc ->
                            doc.toObject(ChatRoom::class.java)?.copy(id = doc.id)
                        }
                    }
                }
        }
    }

    val decodedExerciseName = remember(exerciseName) {
        URLDecoder.decode(exerciseName, StandardCharsets.UTF_8.toString())
    }
    val userId = FirebaseManager.auth.currentUser?.uid

    // Fetch matching library exercise to show target muscles and instructions
    val libraryItem = remember(decodedExerciseName) {
        ExerciseLibraryRepository.exercises.firstOrNull {
            it.name.equals(decodedExerciseName, ignoreCase = true) ||
            it.id.equals(decodedExerciseName.replace(" ", "_").lowercase(), ignoreCase = true)
        }
    }

    LaunchedEffect(userId) {
        if (userId == null) {
            isLoading = false
            errorMessage = "Utilizador não autenticado"
            return@LaunchedEffect
        }

        FirebaseManager.firestore
            .collection("users")
            .document(userId)
            .collection("sessions")
            .get()
            .addOnSuccessListener { snapshot ->
                sessions = snapshot.toObjects(WorkoutSession::class.java)
                    .sortedByDescending { it.startTime }
                isLoading = false
            }
            .addOnFailureListener { error ->
                errorMessage = error.message ?: "Erro ao carregar exercício"
                isLoading = false
            }
    }

    val history = remember(sessions, decodedExerciseName) {
        buildExerciseHistory(sessions, decodedExerciseName)
    }
    val topEntry = history.maxByOrNull { it.heaviestWeight }
    val bestOneRm = history.maxOfOrNull { it.estimatedOneRepMax } ?: 0f
    val totalVolume = history.sumOf { it.sessionVolume.toDouble() }.toFloat()

    // Smooth animation loop to simulate demonstration
    val infiniteTransition = rememberInfiniteTransition(label = "exerciseAnimation")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "movement"
    )
    val activeProgress = if (isPlaying) animationProgress else 0.5f

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            Column(modifier = Modifier.background(BackgroundDark)) {
                // Header with back arrow, title, share and more options
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                    Text(
                        text = decodedExerciseName,
                        modifier = Modifier.weight(1f),
                        style = AppTypography.headlineLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = { showShareSheet = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Partilhar", tint = Color.White)
                    }
                }

                // Tab Bar ("Resumo", "Histórico", "Instruções")
                TabRow(
                    selectedTabIndex = when (selectedTab) {
                        "Resumo" -> 0
                        "Histórico" -> 1
                        else -> 2
                    },
                    containerColor = BackgroundDark,
                    contentColor = AccentBlue,
                    indicator = { tabPositions ->
                        val currentTab = when (selectedTab) {
                            "Resumo" -> 0
                            "Histórico" -> 1
                            else -> 2
                        }
                        Box(
                            Modifier
                                .tabIndicatorOffset(tabPositions[currentTab])
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(AccentBlue)
                        )
                    },
                    divider = {
                        HorizontalDivider(color = Color(0xFF2E2D2D))
                    }
                ) {
                    Tab(
                        selected = selectedTab == "Resumo",
                        onClick = { selectedTab = "Resumo" },
                        text = {
                            Text(
                                "Resumo",
                                style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (selectedTab == "Resumo") AccentBlue else OutlineBorder
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == "Histórico",
                        onClick = { selectedTab = "Histórico" },
                        text = {
                            Text(
                                "Histórico",
                                style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (selectedTab == "Histórico") AccentBlue else OutlineBorder
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == "Instruções",
                        onClick = { selectedTab = "Instruções" },
                        text = {
                            Text(
                                "Instruções",
                                style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (selectedTab == "Instruções") AccentBlue else OutlineBorder
                            )
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundDark)
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundDark)
                        .padding(paddingValues)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TrainCard {
                        Text(errorMessage!!, color = AccentYellow)
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundDark)
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp, top = 16.dp)
                ) {
                    if (selectedTab == "Resumo") {
                        // 1. Interactive Demonstration Video / Animation Card
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceLevel1,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    SurfaceLevel1,
                                                    SurfaceContainerLowest
                                                )
                                            )
                                        )
                                        .padding(16.dp)
                                ) {
                                    // Elegant background target circle decoration for high-end look
                                    Canvas(modifier = Modifier.matchParentSize()) {
                                        drawCircle(
                                            color = AccentBlue.copy(alpha = 0.04f),
                                            radius = size.minDimension * 0.5f,
                                            center = Offset(size.width * 0.85f, size.height * 0.3f)
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Top Row: Category tag and Equipment
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = ChipPurpleBg
                                            ) {
                                                Text(
                                                    text = "ANATOMIA",
                                                    style = AppTypography.labelSmall.copy(
                                                        color = AccentPurple,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            
                                            Text(
                                                text = libraryItem?.equipment ?: "Peso Corporal",
                                                style = AppTypography.labelSmall.copy(
                                                    color = AccentYellow,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }

                                        // Center Area: Main Target Muscle and Secondary info
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = libraryItem?.primaryMuscle ?: "Músculo Alvo",
                                                style = AppTypography.headlineMedium.copy(
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Secundários: ${libraryItem?.secondaryMuscles?.joinToString(", ") ?: "Nenhum"}",
                                                style = AppTypography.bodySmall,
                                                color = OutlineBorder
                                            )
                                        }

                                        // Bottom Area: Full-width YouTube button with glowing border and play icon
                                        val context = LocalContext.current
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFE50914).copy(alpha = 0.12f),
                                            border = BorderStroke(1.dp, Color(0xFFE50914).copy(alpha = 0.35f)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    val query = "como executar ${decodedExerciseName}"
                                                    val intent = Intent(
                                                        Intent.ACTION_VIEW,
                                                        Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")
                                                    )
                                                    context.startActivity(intent)
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Ver Demonstração",
                                                    tint = Color(0xFFE50914),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Ver Execução",
                                                    style = AppTypography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    ),
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Title & Target Muscle Subtitle
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = decodedExerciseName.lowercase(),
                                    style = AppTypography.headlineLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Primário: ${libraryItem?.primaryMuscle ?: "Corpo Inteiro"}",
                                    style = AppTypography.bodyMedium,
                                    color = OutlineBorder
                                )
                            }
                        }

                        // 3. Premium Interactive Progress Chart Card
                        item {
                            TrainCard {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Row with Latest metrics & time filter dropdown
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val latestEntry = history.firstOrNull()
                                        if (latestEntry != null) {
                                            Text(
                                                text = "${formatWeight(latestEntry.heaviestWeight)} kg • ${latestEntry.bestReps} reps",
                                                style = AppTypography.headlineLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                        } else {
                                            Text(
                                                text = "Sem Carga Recente",
                                                style = AppTypography.headlineLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                        }

                                        Text(
                                            text = "Últimos 3 meses",
                                            style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = AccentBlue,
                                            modifier = Modifier.clickable { /* Select filter */ }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Canvas Draw Custom Smooth Progress Chart
                                    val chartPoints = remember(history) {
                                        if (history.size >= 2) {
                                            history.map { it.heaviestWeight }.reversed()
                                        } else if (history.size == 1) {
                                            val w = history[0].heaviestWeight
                                            listOf(w * 0.8f, w * 0.9f, w, w)
                                        } else {
                                            // Beautiful Mock Curve when there is no history to showcase design
                                            listOf(0f, 0f, 0f, 0f)
                                        }
                                    }
                                    CustomSmoothLineChart(points = chartPoints)

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Metric Selector Chips
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(99.dp),
                                            color = AccentBlue,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                "Mais Repetições (Série)",
                                                color = Color.White,
                                                style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(99.dp),
                                            color = SurfaceLevel1,
                                            border = BorderStroke(1.dp, Color(0xFF2E2D2D)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                "Repetições de Sessão",
                                                color = OutlineBorder,
                                                style = AppTypography.labelSmall,
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 4. Recordes Pessoais Heading & Card
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🎖️", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Recordes Pessoais",
                                        style = AppTypography.headlineLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        item {
                            TrainCard {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Melhor Série", style = AppTypography.bodyMedium, color = OutlineBorder)
                                        Text(
                                            text = if (topEntry != null) "${formatWeight(topEntry.heaviestWeight)} kg x ${topEntry.bestReps} reps" else "0 kg x 0 reps",
                                            style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = AccentBlue
                                        )
                                    }
                                    HorizontalDivider(color = Color(0xFF2B2A2A))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Melhor 1RM Est.", style = AppTypography.bodyMedium, color = OutlineBorder)
                                        Text(
                                            text = "${formatWeight(bestOneRm)} kg",
                                            style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = AccentBlue
                                        )
                                    }
                                    HorizontalDivider(color = Color(0xFF2B2A2A))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Maior Volume de Sessão", style = AppTypography.bodyMedium, color = OutlineBorder)
                                        Text(
                                            text = "${totalVolume.roundToInt()} kg",
                                            style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = AccentBlue
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (selectedTab == "Histórico") {
                        if (history.isEmpty()) {
                            item {
                                TrainCard {
                                    Text("Sem histórico de treinos registado para este exercício.", color = OutlineBorder)
                                }
                            }
                        } else {
                            // Session Grid Metric Cards
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    ExerciseMetricCard("SESSÕES", history.size.toString(), Modifier.weight(1f))
                                    ExerciseMetricCard("1RM EST.", "${formatWeight(bestOneRm)} kg", Modifier.weight(1f))
                                }
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    ExerciseMetricCard("HEAVIEST", "${formatWeight(topEntry?.heaviestWeight ?: 0f)} kg", Modifier.weight(1f))
                                    ExerciseMetricCard("VOLUME", "${totalVolume.roundToInt()} kg", Modifier.weight(1f))
                                }
                            }

                            // Load History Bars Card
                            item {
                                TrainCard {
                                    Column {
                                        Text("HISTÓRICO DE CARGA", style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold), color = AccentBlue)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        history.take(8).forEach { entry ->
                                            HistoryBarRow(entry = entry, maxWeight = history.maxOfOrNull { it.heaviestWeight } ?: 1f)
                                            Spacer(modifier = Modifier.height(10.dp))
                                        }
                                    }
                                }
                            }

                            // All Completed Sessions title
                            item {
                                Text("TODAS AS SESSÕES", style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold), color = AccentBlue)
                            }

                            // History sessions list
                            items(history) { entry ->
                                ExerciseHistoryCard(
                                    entry = entry,
                                    onOpenWorkout = { onOpenWorkout(entry.sessionId) }
                                )
                            }
                        }
                    }

                    if (selectedTab == "Instruções") {
                        if (libraryItem == null) {
                            item {
                                TrainCard {
                                    Text(
                                        text = "Instruções gerais baseadas no plano corporal. Mantenha sempre a postura firme e os movimentos controlados.",
                                        style = AppTypography.bodyMedium,
                                        color = OutlineBorder
                                    )
                                }
                            }
                        } else {
                            // Step by Step Instructions Card
                            item {
                                TrainCard {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text("COMO EXECUTAR", style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold), color = AccentBlue)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        libraryItem.instructions.forEachIndexed { idx, step ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = AccentBlue.copy(alpha = 0.16f),
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Text(
                                                        text = (idx + 1).toString(),
                                                        color = AccentBlue,
                                                        style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        modifier = Modifier.wrapContentSize(Alignment.Center)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = step,
                                                    style = AppTypography.bodyMedium,
                                                    color = Color.White,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Safety & Performance Tips Card
                            if (libraryItem.tips.isNotEmpty()) {
                                item {
                                    TrainCard {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Text("DICAS EXTRA", style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold), color = AccentBlue)
                                            Spacer(modifier = Modifier.height(12.dp))
                                            libraryItem.tips.forEach { tip ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                    verticalAlignment = Alignment.Top
                                                ) {
                                                    Text(
                                                        text = "•",
                                                        color = AccentYellow,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = tip,
                                                        style = AppTypography.bodyMedium,
                                                        color = OutlineBorder,
                                                        modifier = Modifier.weight(1f)
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
            }
        }
    }

    if (showShareSheet) {
        ModalBottomSheet(
            onDismissRequest = { showShareSheet = false },
            containerColor = SurfaceLevel1,
            dragHandle = { BottomSheetDefaults.DragHandle(color = OutlineBorder) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Partilhar Exercício",
                    style = AppTypography.headlineLarge.copy(fontSize = 20.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Escolhe uma conversa para enviar este exercício:",
                    style = AppTypography.bodyMedium,
                    color = OutlineBorder
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (chatRooms.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Não tens conversas ou grupos ativos.", color = OutlineBorder)
                    }
                } else {
                    val context = LocalContext.current
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(chatRooms.size) { index ->
                            val room = chatRooms[index]
                            ShareChatItem(
                                room = room,
                                currentUserId = currentUser?.uid.orEmpty(),
                                onShare = {
                                    val myUid = currentUser?.uid.orEmpty()
                                    val myName = currentUserProfile?.name ?: "Parceiro"

                                    val db = FirebaseManager.firestore
                                    val newMsg = Message(
                                        id = UUID.randomUUID().toString(),
                                        senderId = myUid,
                                        senderName = myName,
                                        text = "Partilhou o exercício: ${decodedExerciseName}",
                                        timestamp = System.currentTimeMillis(),
                                        sharedPostId = "EXERCISE:${decodedExerciseName}",
                                        sharedPostContent = "Vê a anatomia, músculos ativados e execução deste exercício.",
                                        sharedPostAuthorName = decodedExerciseName,
                                        sharedPostImageUrl = null,
                                        sharedWorkoutSessionId = null,
                                        sharedPostUserId = null
                                    )

                                    db.collection("chatRooms").document(room.id)
                                        .collection("messages").document(newMsg.id).set(newMsg)
                                        .addOnSuccessListener {
                                            db.collection("chatRooms").document(room.id).update(
                                                "lastMessageContent", "Partilhou o exercício: ${decodedExerciseName}",
                                                "lastMessageSender", myName,
                                                "lastMessageTime", System.currentTimeMillis()
                                            )
                                            Toast.makeText(context, "Exercício partilhado!", Toast.LENGTH_SHORT).show()
                                            showShareSheet = false
                                        }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseMetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = SurfaceLevel1,
        border = BorderStroke(1.dp, Color(0xFF2E2D2D))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = AppTypography.labelSmall, color = OutlineBorder)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = AppTypography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue
                )
            )
        }
    }
}

@Composable
private fun HistoryBarRow(entry: ExerciseHistoryEntry, maxWeight: Float) {
    val ratio = if (maxWeight <= 0f) 0f else entry.heaviestWeight / maxWeight
    val widthFraction = ratio.coerceIn(0.08f, 1f)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(formatSessionDate(entry.date), style = AppTypography.labelSmall, color = Color.White)
            Text(
                text = "${formatWeight(entry.heaviestWeight)} kg • ${entry.bestReps} reps",
                style = AppTypography.labelSmall,
                color = OutlineBorder
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(Color(0xFF1B1B1F), RoundedCornerShape(99.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(widthFraction)
                    .height(10.dp)
                    .background(AccentBlue, RoundedCornerShape(99.dp))
            )
        }
    }
}

@Composable
private fun ExerciseHistoryCard(
    entry: ExerciseHistoryEntry,
    onOpenWorkout: () -> Unit
) {
    TrainCard(modifier = Modifier.clickable { onOpenWorkout() }) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.routineName.ifBlank { "Treino" },
                        style = AppTypography.headlineLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatSessionDate(entry.date),
                        style = AppTypography.bodyMedium,
                        color = OutlineBorder
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentBlue.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "ABRIR",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AccentBlue
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF2B2A2A))
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Heaviest: ${formatWeight(entry.heaviestWeight)} kg • 1RM est.: ${formatWeight(entry.estimatedOneRepMax)} kg",
                style = AppTypography.bodyMedium,
                color = OutlineBorder
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Best reps: ${entry.bestReps} • Set volume: ${entry.setVolume.roundToInt()} kg • Session volume: ${entry.sessionVolume.roundToInt()} kg",
                style = AppTypography.bodyMedium,
                color = OutlineBorder
            )
        }
    }
}

// Custom Smooth Bezier Progress Chart drawn directly on a Canvas
@Composable
private fun CustomSmoothLineChart(
    points: List<Float>,
    modifier: Modifier = Modifier
) {
    val cleanPoints = points.filter { it > 0f }
    if (cleanPoints.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Sem carga registada no histórico.", style = AppTypography.bodyMedium, color = OutlineBorder)
        }
        return
    }

    val maxVal = cleanPoints.maxOrNull() ?: 10f
    val minVal = cleanPoints.minOrNull() ?: 0f
    val range = (maxVal - minVal).coerceAtLeast(1f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(vertical = 12.dp, horizontal = 12.dp)
    ) {
        val width = size.width
        val height = size.height
        val sizeMinusOne = (cleanPoints.size - 1).coerceAtLeast(1)
        val stepX = width / sizeMinusOne

        val path = Path()
        val fillPath = Path()

        val renderedPoints = cleanPoints.mapIndexed { idx, point ->
            val x = idx * stepX
            val ratio = if (maxVal == minVal) 0.5f else (point - minVal) / range
            // Leave 10% padding top and bottom so chart fits beautifully
            val y = height - (ratio * height * 0.75f + height * 0.1f)
            Offset(x, y)
        }

        renderedPoints.forEachIndexed { idx, pt ->
            if (idx == 0) {
                path.moveTo(pt.x, pt.y)
                fillPath.moveTo(pt.x, height)
                fillPath.lineTo(pt.x, pt.y)
            } else {
                // Smooth curved line instead of sharp lines
                val prevPt = renderedPoints[idx - 1]
                val controlX = (prevPt.x + pt.x) / 2f
                path.cubicTo(controlX, prevPt.y, controlX, pt.y, pt.x, pt.y)
                fillPath.cubicTo(controlX, prevPt.y, controlX, pt.y, pt.x, pt.y)
            }

            if (idx == renderedPoints.size - 1) {
                fillPath.lineTo(pt.x, height)
                fillPath.close()
            }
        }

        // Draw glowing gradient underneath
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(AccentBlue.copy(alpha = 0.28f), Color.Transparent)
            )
        )

        // Draw primary line
        drawPath(
            path = path,
            color = AccentBlue,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        // Draw points with outer halos
        renderedPoints.forEach { pt ->
            // Outer Ring
            drawCircle(
                color = Color(0xFF1B1B1F),
                radius = 6.dp.toPx(),
                center = pt
            )
            // Inner glowing dot
            drawCircle(
                color = AccentBlue,
                radius = 4.dp.toPx(),
                center = pt
            )
        }
    }
}

@Composable
private fun getDrawableIdForExercise(exerciseName: String): Int {
    val lower = exerciseName.lowercase()
    return when {
        lower.contains("bench") || lower.contains("supino") || lower.contains("peito") || lower.contains("chest") -> {
            R.drawable.exercise_steps_chest
        }
        lower.contains("pull") || lower.contains("barra") || lower.contains("costas") || lower.contains("elevação") || lower.contains("lat") || lower.contains("back") || lower.contains("row") || lower.contains("muscle-up") || lower.contains("muscleup") -> {
            R.drawable.exercise_steps_back
        }
        lower.contains("squat") || lower.contains("agachamento") || lower.contains("perna") || lower.contains("leg") || lower.contains("panturrilha") || lower.contains("calf") -> {
            R.drawable.exercise_steps_legs
        }
        lower.contains("abs") || lower.contains("abdominal") || lower.contains("core") || lower.contains("plank") || lower.contains("prancha") || lower.contains("l-sit") || lower.contains("dragon") || lower.contains("flag") -> {
            R.drawable.exercise_steps_core
        }
        lower.contains("shoulder") || lower.contains("ombro") || lower.contains("press") || lower.contains("elevação lateral") || lower.contains("desenvolvimento") || lower.contains("handstand") -> {
            R.drawable.exercise_steps_shoulders
        }
        else -> {
            R.drawable.exercise_steps_arms
        }
    }
}

private fun buildExerciseHistory(
    sessions: List<WorkoutSession>,
    exerciseName: String
): List<ExerciseHistoryEntry> {
    return sessions.mapNotNull { session ->
        val exercise = session.exercises.firstOrNull {
            it.name.equals(exerciseName, ignoreCase = true)
        } ?: return@mapNotNull null

        val allSets = exercise.sets
        val completedSets = allSets.filter { it.completed }
        val heaviestWeight = allSets.maxOfOrNull { it.weight } ?: 0f
        val bestReps = allSets.maxOfOrNull { it.reps } ?: 0
        val setVolume = completedSets.maxOfOrNull { it.weight * it.reps } ?: 0f
        val sessionVolume = completedSets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
        val estimatedOneRepMax = allSets.maxOfOrNull { estimateOneRepMax(it.weight, it.reps) } ?: 0f

        ExerciseHistoryEntry(
            sessionId = session.id,
            routineName = session.routineName,
            date = session.startTime,
            heaviestWeight = heaviestWeight,
            bestReps = bestReps,
            setVolume = setVolume,
            sessionVolume = sessionVolume,
            estimatedOneRepMax = estimatedOneRepMax
        )
    }.sortedByDescending { it.date }
}

private fun estimateOneRepMax(weight: Float, reps: Int): Float {
    if (weight <= 0f || reps <= 0) return 0f
    return weight * (1f + reps / 30f)
}

private fun formatSessionDate(timestamp: Long): String {
    return SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("pt", "PT")).format(Date(timestamp))
}

private fun formatWeight(weight: Float): String {
    return if (weight % 1f == 0f) weight.roundToInt().toString() else String.format(Locale.US, "%.1f", weight)
}