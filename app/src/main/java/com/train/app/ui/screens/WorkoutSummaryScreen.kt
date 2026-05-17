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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.IconButton
import android.widget.Toast
import java.util.UUID
import com.train.app.data.models.Post
import com.train.app.data.models.ChatRoom
import com.train.app.data.models.Message
import com.train.app.data.models.UserProfile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.OptIn
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.train.app.data.FirebaseManager
import com.train.app.data.ExerciseLibraryRepository
import com.train.app.data.models.WorkoutSession
import com.train.app.ui.components.TrainCard
import com.train.app.ui.components.TrainPrimaryButton
import com.train.app.ui.components.TrainSecondaryButton
import com.train.app.ui.components.UserAvatar
import com.train.app.ui.theme.AccentBlue
import com.train.app.ui.theme.AccentPurple
import com.train.app.ui.theme.AccentYellow
import com.train.app.ui.theme.AppTypography
import com.train.app.ui.theme.BackgroundDark
import com.train.app.ui.theme.OutlineBorder
import com.train.app.ui.theme.SurfaceLevel0
import com.train.app.ui.theme.SurfaceLevel1
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private data class SummaryPr(
    val exerciseName: String,
    val labels: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSummaryScreen(
    sessionId: String,
    targetUserId: String? = null,
    onBack: () -> Unit = {},
    onNavigateToCreatePost: (String) -> Unit = {},
    onOpenPostComments: (String) -> Unit = {}
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var session by remember { mutableStateOf<WorkoutSession?>(null) }
    var allSessions by remember { mutableStateOf<List<WorkoutSession>>(emptyList()) }
    var userName by remember { mutableStateOf("") }
    var userAvatarUri by remember { mutableStateOf<String?>(null) }

    var postState by remember { mutableStateOf<Post?>(null) }
    var postToShare by remember { mutableStateOf<Post?>(null) }
    var chatRooms by remember { mutableStateOf<List<ChatRoom>>(emptyList()) }
    var currentUserProfile by remember { mutableStateOf<UserProfile?>(null) }

    val currentAuthId = FirebaseManager.auth.currentUser?.uid
    val effectiveUserId = targetUserId ?: currentAuthId

    LaunchedEffect(sessionId) {
        FirebaseManager.firestore.collection("posts")
            .whereEqualTo("workoutSessionId", sessionId)
            .limit(1)
            .addSnapshotListener { snap, _ ->
                if (snap != null && !snap.isEmpty) {
                    postState = snap.documents.first().toObject(Post::class.java)?.copy(id = snap.documents.first().id)
                } else {
                    postState = null
                }
            }
    }

    LaunchedEffect(currentAuthId) {
        if (currentAuthId != null) {
            FirebaseManager.firestore.collection("chatRooms")
                .whereArrayContains("members", currentAuthId)
                .addSnapshotListener { snap, _ ->
                    if (snap != null) {
                        chatRooms = snap.documents.mapNotNull { doc ->
                            doc.toObject(ChatRoom::class.java)?.copy(id = doc.id)
                        }
                    }
                }
            FirebaseManager.firestore.collection("users").document(currentAuthId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        currentUserProfile = snapshot.toObject(UserProfile::class.java)
                    }
                }
        }
    }

    LaunchedEffect(effectiveUserId, sessionId) {
        if (effectiveUserId == null) {
            isLoading = false
            errorMessage = "Utilizador não autenticado"
            return@LaunchedEffect
        }

        FirebaseManager.firestore
            .collection("users")
            .document(effectiveUserId)
            .get()
            .addOnSuccessListener { doc ->
                userName = doc.getString("name").orEmpty().ifBlank { doc.getString("email")?.substringBefore("@").orEmpty() }
                userAvatarUri = doc.getString("photoUrl")
            }

        FirebaseManager.firestore
            .collection("users")
            .document(effectiveUserId)
            .collection("sessions")
            .get()
            .addOnSuccessListener { snapshot ->
                val sessions = snapshot.toObjects(WorkoutSession::class.java)
                allSessions = sessions
                session = sessions.firstOrNull { it.id == sessionId }
                if (session == null) {
                    errorMessage = "Sessão não encontrada"
                }
                isLoading = false
            }
            .addOnFailureListener { error ->
                errorMessage = error.message ?: "Erro ao carregar resumo do treino"
                isLoading = false
            }
    }

    val currentSession = session
    val completedSets = currentSession?.exercises?.sumOf { exercise ->
        exercise.sets.count { it.completed }
    } ?: 0
    val exerciseCount = currentSession?.exercises?.count { exercise ->
        exercise.sets.any { it.completed }
    } ?: 0
    val prSummary = remember(currentSession, allSessions) {
        if (currentSession == null) emptyList() else buildSessionPrSummary(currentSession, allSessions)
    }

    val durationText = if (currentSession != null) {
        val hrs = currentSession.durationMinutes / 60
        val mins = currentSession.durationMinutes % 60
        if (hrs > 0) "${hrs}h ${mins}min" else "${mins}min"
    } else "0min"

    val volumeText = if (currentSession != null) {
        String.format(Locale.US, "%,.1f", currentSession.totalVolume).replace('.', ',') + " kg"
    } else "0 kg"

    val muscleBreakdown = remember(currentSession) {
        if (currentSession == null) emptyList<Pair<String, Int>>() else {
            val counts = mutableMapOf<String, Int>()
            currentSession.exercises.forEach { exercise ->
                val matchingLib = ExerciseLibraryRepository.exercises.firstOrNull { it.name.equals(exercise.name, ignoreCase = true) }
                val rawMuscle = matchingLib?.primaryMuscle ?: "Outros"
                val ptMuscle = when (rawMuscle.lowercase(Locale.ROOT)) {
                    "chest", "upper chest" -> "Peito"
                    "lats", "upper back" -> "Costas"
                    "biceps", "triceps", "forearms", "brachialis" -> "Braços"
                    "shoulders", "side delts", "rear delts", "front delts", "upper traps" -> "Ombros"
                    "quads", "hamstrings", "glutes", "calves", "adductors" -> "Pernas"
                    "core", "abs", "obliques" -> "Core"
                    else -> "Outros"
                }
                val completedCount = exercise.sets.count { it.completed }
                if (completedCount > 0) {
                    counts[ptMuscle] = (counts[ptMuscle] ?: 0) + completedCount
                }
            }
            counts.toList().sortedByDescending { it.second }
        }
    }

    val totalCompletedSetsForBreakdown = remember(muscleBreakdown) {
        muscleBreakdown.sumOf { it.second }.toFloat()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Detalhe de Treinamento",
                    style = AppTypography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                    color = Color.White
                )
            }
        }

        when {
            isLoading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentBlue)
                    }
                }
            }

            errorMessage != null -> {
                item {
                    TrainCard {
                        Text(errorMessage!!, color = AccentYellow)
                    }
                }
            }

            currentSession == null -> {
                item {
                    TrainCard {
                        Text("Sem dados para este treino.", color = OutlineBorder)
                    }
                }
            }

            else -> {
                // User Profile Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            photoUrl = userAvatarUri,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = userName.ifBlank { "atleta" },
                                style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatDate(currentSession.startTime),
                                style = AppTypography.bodyMedium.copy(color = OutlineBorder.copy(alpha = 0.8f), fontSize = 13.sp)
                            )
                        }
                    }
                }

                // Routine name
                item {
                    Text(
                        text = currentSession.routineName.ifBlank { "Treinamento" },
                        style = AppTypography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Metrics Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tempo", style = AppTypography.labelSmall.copy(color = OutlineBorder))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = durationText,
                                style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                                color = Color.White
                            )
                        }
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text("Volume", style = AppTypography.labelSmall.copy(color = OutlineBorder))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = volumeText,
                                style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                                color = Color.White
                            )
                        }
                        Column(modifier = Modifier.weight(0.8f)) {
                            Text("Séries", style = AppTypography.labelSmall.copy(color = OutlineBorder))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = completedSets.toString(),
                                style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                                color = Color.White
                            )
                        }
                    }
                }

                // Social Actions Row
                item {
                    val isLiked = currentAuthId != null && postState?.likedBy?.contains(currentAuthId) == true
                    val likesCount = postState?.likedBy?.size ?: 0
                    val commentsCount = postState?.commentsCount ?: 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Like Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (currentAuthId != null && postState != null) {
                                        val postRef = FirebaseManager.firestore.collection("posts").document(postState!!.id)
                                        if (isLiked) {
                                            postRef.update("likedBy", com.google.firebase.firestore.FieldValue.arrayRemove(currentAuthId))
                                        } else {
                                            postRef.update("likedBy", com.google.firebase.firestore.FieldValue.arrayUnion(currentAuthId))
                                        }
                                    }
                                }
                                .padding(vertical = 6.dp, horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = "Gostar",
                                tint = if (isLiked) AccentYellow else Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$likesCount",
                                style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        // Comments Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (postState != null) {
                                        onOpenPostComments(postState!!.id)
                                    }
                                }
                                .padding(vertical = 6.dp, horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Comentários",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$commentsCount",
                                style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        // Share Button
                        IconButton(
                            onClick = {
                                if (postState != null) {
                                    postToShare = postState
                                } else if (effectiveUserId == currentAuthId) {
                                    onNavigateToCreatePost(sessionId)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Partilhar",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = Color(0xFF232222), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                }

                // Divisão Muscular
                if (muscleBreakdown.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = "Divisão Muscular",
                                style = AppTypography.bodyMedium.copy(color = OutlineBorder.copy(alpha = 0.7f), fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            muscleBreakdown.forEach { (muscle, count) ->
                                val percentage = if (totalCompletedSetsForBreakdown > 0f) {
                                    ((count / totalCompletedSetsForBreakdown) * 100).roundToInt()
                                } else 0

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                ) {
                                    Text(
                                        text = muscle,
                                        style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(14.dp)
                                                .background(SurfaceLevel0, RoundedCornerShape(4.dp))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(count / totalCompletedSetsForBreakdown)
                                                    .background(AccentBlue, RoundedCornerShape(4.dp))
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "$percentage%",
                                            style = AppTypography.bodyMedium.copy(color = OutlineBorder, fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = Color(0xFF232222), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }

                // Treinamento Section
                item {
                    Text(
                        text = "Treinamento",
                        style = AppTypography.bodyMedium.copy(color = OutlineBorder.copy(alpha = 0.7f), fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                val completedExercises = currentSession.exercises.filter { it.sets.any { it.completed } }
                items(completedExercises) { exercise ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        // Exercise Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(SurfaceLevel0, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = AccentYellow,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = exercise.name,
                                style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                                color = AccentBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Table headers
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "SÉRIE",
                                style = AppTypography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = OutlineBorder,
                                modifier = Modifier.weight(0.3f)
                            )
                            Text(
                                text = "PESO & REPETIÇÕES",
                                style = AppTypography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = OutlineBorder,
                                modifier = Modifier.weight(0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Sets rows
                        val completedSetsOfExercise = exercise.sets.filter { it.completed }
                        completedSetsOfExercise.forEachIndexed { setIdx, set ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${setIdx + 1}",
                                    style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.weight(0.3f)
                                )
                                val weightStr = if (set.weight % 1f == 0f) set.weight.roundToInt().toString() else String.format(Locale.US, "%.1f", set.weight)
                                Text(
                                    text = "$weightStr kg x ${set.reps}",
                                    style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Normal),
                                    color = Color.White,
                                    modifier = Modifier.weight(0.7f)
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0xFF1F1E1E), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))
                }
            }
        }
    }

    if (postToShare != null) {
        val context = LocalContext.current
        ModalBottomSheet(
            onDismissRequest = { postToShare = null },
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
                    text = "Partilhar Post",
                    style = AppTypography.headlineLarge.copy(fontSize = 20.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Escolhe uma conversa para enviar este post:",
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
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(chatRooms.size) { index ->
                            val room = chatRooms[index]
                            ShareChatItem(
                                room = room,
                                currentUserId = currentAuthId.orEmpty(),
                                onShare = {
                                    val myUid = currentAuthId.orEmpty()
                                    val myName = currentUserProfile?.name ?: "Parceiro"
                                    val post = postToShare!!

                                    val db = FirebaseManager.firestore
                                    val newMsg = Message(
                                        id = UUID.randomUUID().toString(),
                                        senderId = myUid,
                                        senderName = myName,
                                        text = "Partilhou um post de ${post.userName}",
                                        timestamp = System.currentTimeMillis(),
                                        sharedPostId = post.id,
                                        sharedPostContent = post.description,
                                        sharedPostAuthorName = post.userName,
                                        sharedPostImageUrl = post.imageUrl,
                                        sharedWorkoutSessionId = post.workoutSessionId,
                                        sharedPostUserId = post.userId
                                    )

                                    db.collection("chatRooms").document(room.id)
                                        .collection("messages").document(newMsg.id).set(newMsg)
                                        .addOnSuccessListener {
                                            db.collection("chatRooms").document(room.id).update(
                                                "lastMessageContent", "Partilhou um post de ${post.userName}",
                                                "lastMessageSender", myName,
                                                "lastMessageTime", System.currentTimeMillis()
                                            )
                                            Toast.makeText(context, "Post partilhado!", Toast.LENGTH_SHORT).show()
                                            postToShare = null
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
private fun SummaryMetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = SurfaceLevel0
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
private fun SummaryPrCard(pr: SummaryPr) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = AccentPurple.copy(alpha = 0.14f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = pr.exerciseName,
                style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pr.labels.forEach { label ->
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = AccentPurple.copy(alpha = 0.22f)
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = AppTypography.labelSmall,
                            color = AccentYellow
                        )
                    }
                }
            }
        }
    }
}

private fun buildSessionPrSummary(
    currentSession: WorkoutSession,
    allSessions: List<WorkoutSession>
): List<SummaryPr> {
    val pastSessions = allSessions.filter { it.id != currentSession.id }

    return currentSession.exercises.mapNotNull { exercise ->
        val completedSets = exercise.sets.filter { it.completed }
        if (completedSets.isEmpty()) return@mapNotNull null

        val pastSets = pastSessions
            .flatMap { session -> session.exercises }
            .filter { it.name.equals(exercise.name, ignoreCase = true) }
            .flatMap { it.sets }
            .filter { it.completed }

        val labels = mutableListOf<String>()
        val currentHeaviest = completedSets.maxOfOrNull { it.weight } ?: 0f
        val currentBestReps = completedSets.maxOfOrNull { it.reps } ?: 0
        val currentBestSetVolume = completedSets.maxOfOrNull { it.weight * it.reps } ?: 0f
        val currentBestOneRm = completedSets.maxOfOrNull { estimateOneRepMax(it.weight, it.reps) } ?: 0f
        val currentSessionVolume = completedSets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()

        val pastHeaviest = pastSets.maxOfOrNull { it.weight } ?: 0f
        val pastBestReps = pastSets.maxOfOrNull { it.reps } ?: 0
        val pastBestSetVolume = pastSets.maxOfOrNull { it.weight * it.reps } ?: 0f
        val pastBestOneRm = pastSets.maxOfOrNull { estimateOneRepMax(it.weight, it.reps) } ?: 0f
        val pastBestSessionVolume = pastSessions
            .flatMap { session -> session.exercises.filter { it.name.equals(exercise.name, ignoreCase = true) } }
            .maxOfOrNull { pastExercise ->
                pastExercise.sets.filter { it.completed }.sumOf { set -> (set.weight * set.reps).toDouble() }.toFloat()
            } ?: 0f

        if (currentHeaviest > pastHeaviest) labels.add("Heaviest Weight")
        if (currentBestReps > pastBestReps) labels.add("Most Reps")
        if (currentBestSetVolume > pastBestSetVolume) labels.add("Best Set Volume")
        if (currentBestOneRm > pastBestOneRm) labels.add("Best 1RM")
        if (currentSessionVolume > pastBestSessionVolume) labels.add("Best Session Volume")

        if (labels.isEmpty()) null else SummaryPr(exercise.name, labels)
    }
}

private fun estimateOneRepMax(weight: Float, reps: Int): Float {
    if (weight <= 0f || reps <= 0) return 0f
    return weight * (1f + reps / 30f)
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val weekdayFormat = SimpleDateFormat("EEEE", Locale("pt", "PT"))
    val weekday = weekdayFormat.format(date).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "PT")) else it.toString() }
    
    val restFormat = SimpleDateFormat("MMM d, yyyy - h:mma", Locale("en", "US"))
    val rest = restFormat.format(date)
    return "$weekday, $rest"
}

private fun formatWeight(weight: Float): String {
    return if (weight % 1f == 0f) weight.roundToInt().toString() else String.format(Locale.US, "%.1f", weight)
}