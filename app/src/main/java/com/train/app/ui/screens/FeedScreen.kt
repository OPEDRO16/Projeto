package com.train.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.google.firebase.firestore.FieldValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.Query
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import com.train.app.data.models.ChatRoom
import com.train.app.data.models.Message
import android.widget.Toast
import java.util.UUID
import com.train.app.data.FirebaseManager
import com.train.app.data.models.Post
import com.train.app.data.models.UserProfile
import com.train.app.ui.components.UserAvatar
import com.train.app.ui.theme.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onOpenPostComments: (String) -> Unit = {},
    onOpenWorkoutDetail: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var currentUserProfile by remember { mutableStateOf<UserProfile?>(null) }
    val currentUser = FirebaseManager.auth.currentUser

    var selectedFilter by remember { mutableStateOf("publico") } // "publico" or "seguindo"
    var showFilterDropdown by remember { mutableStateOf(false) }

    var postToShare by remember { mutableStateOf<Post?>(null) }
    var chatRooms by remember { mutableStateOf<List<ChatRoom>>(emptyList()) }

    LaunchedEffect(currentUser?.uid) {
        val myUid = currentUser?.uid
        if (myUid != null) {
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

    LaunchedEffect(Unit) {
        FirebaseManager.firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    posts = snapshot.toObjects(Post::class.java)
                }
            }
    }

    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            FirebaseManager.firestore.collection("users").document(currentUser.uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        currentUserProfile = snapshot.toObject(UserProfile::class.java)?.apply { id = snapshot.id }
                    }
                }
        }
    }

    val filteredPosts = remember(posts, selectedFilter, currentUserProfile) {
        if (selectedFilter == "publico") {
            posts.filter { 
                it.visibility == "public" || 
                (it.visibility == "friends" && currentUserProfile != null && it.userId in currentUserProfile!!.friends)
            }
        } else {
            posts.filter { 
                it.userId == currentUser?.uid || 
                (currentUserProfile != null && it.userId in currentUserProfile!!.friends)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showFilterDropdown = true }
                        ) {
                            Text(
                                text = if (selectedFilter == "publico") "Público" else "Amigos",
                                style = AppTypography.headlineLarge.copy(fontSize = 24.sp),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextPrimary)
                        }

                        DropdownMenu(
                            expanded = showFilterDropdown,
                            onDismissRequest = { showFilterDropdown = false },
                            modifier = Modifier.background(SurfaceLevel1)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Público", color = if (selectedFilter == "publico") AccentBlue else TextPrimary, style = AppTypography.bodyLarge) },
                                onClick = {
                                    selectedFilter = "publico"
                                    showFilterDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Amigos", color = if (selectedFilter == "seguindo") AccentBlue else TextPrimary, style = AppTypography.bodyLarge) },
                                onClick = {
                                    selectedFilter = "seguindo"
                                    showFilterDropdown = false
                                }
                            )
                        }
                    }
                },
                actions = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(paddingValues)
        ) {
            val userTier = currentUserProfile?.subscriptionTier ?: "FREE"
            items(filteredPosts.size) { index ->
                val post = filteredPosts[index]

                HevyPostItem(
                    post = post,
                    currentUserId = currentUser?.uid,
                    currentUserProfile = currentUserProfile,
                    onOpenComments = onOpenPostComments,
                    onOpenDetail = onOpenWorkoutDetail,
                    onSharePost = { postToShare = it }
                )

                // Inject sponsored cards periodically for FREE and PRO tiers (every 3 posts)
                if (userTier != "MASTER" && (index + 1) % 3 == 0) {
                    HevySponsoredAdCard(index = index)
                }
            }
        }
    }

    if (postToShare != null) {
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
                                currentUserId = currentUser?.uid.orEmpty(),
                                onShare = {
                                    val myUid = currentUser?.uid.orEmpty()
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
fun HevyPostItem(
    post: Post,
    currentUserId: String?,
    currentUserProfile: UserProfile?,
    onOpenComments: (String) -> Unit,
    onOpenDetail: (String, String) -> Unit,
    onSharePost: (Post) -> Unit
) {
    val isLiked = currentUserId != null && post.likedBy.contains(currentUserId)
    val localSentRequests = remember { mutableStateListOf<String>() }
    
    var posterProfile by remember(post.userId) { mutableStateOf<UserProfile?>(null) }
    LaunchedEffect(post.userId) {
        FirebaseManager.firestore.collection("users").document(post.userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    posterProfile = doc.toObject(UserProfile::class.java)?.apply { id = doc.id }
                }
            }
    }

    var postBitmapState by remember(post.imageUrl) { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    LaunchedEffect(post.imageUrl) {
        if (!post.imageUrl.isNullOrBlank() && (post.imageUrl.startsWith("content://") || post.imageUrl.startsWith("file://"))) {
            val loadedBitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val uri = Uri.parse(post.imageUrl)
                com.train.app.ui.components.decodeUriSafely(context, uri, maxDimension = 1080)
            }
            postBitmapState = loadedBitmap
        } else {
            postBitmapState = null
        }
    }

    var commentsCountState by remember(post.id, post.commentsCount) { mutableStateOf(post.commentsCount) }
    LaunchedEffect(post.id) {
        FirebaseManager.firestore
            .collection("posts")
            .document(post.id)
            .collection("comments")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    commentsCountState = snapshot.size()
                }
            }
    }

    // Calcular tempo decorrido
    val timeAgo = remember(post.timestamp) {
        val now = System.currentTimeMillis()
        val diff = now - post.timestamp
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        if (hours > 24) "${TimeUnit.MILLISECONDS.toDays(diff)} d"
        else if (hours > 0) "há $hours horas"
        else if (minutes > 0) "há $minutes min"
        else "agora"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        // Post Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(
                    photoUrl = posterProfile?.photoUrl,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = posterProfile?.name ?: post.userName, style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        
                        val tier = posterProfile?.subscriptionTier ?: "FREE"
                        if (tier == "PRO") {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                                        )
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "★ PRO",
                                    color = Color.Black,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        } else if (tier == "MASTER") {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF3B82F6))
                                        )
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "👑 MASTER",
                                    color = Color.White,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                    Text(text = timeAgo, style = AppTypography.labelSmall, color = OutlineBorder)
                }
            }
            val isFriend = currentUserProfile?.friends?.contains(post.userId) == true
            val isMe = post.userId == currentUserId
            val hasSentRequest = posterProfile?.friendRequests?.contains(currentUserId) == true || localSentRequests.contains(post.userId)
            val hasReceivedRequest = currentUserProfile?.friendRequests?.contains(post.userId) == true

            if (!isMe && !isFriend) {
                when {
                    hasReceivedRequest -> {
                        Text(
                            text = "Aceitar", 
                            style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold), 
                            color = AccentYellow,
                            modifier = Modifier.clickable {
                                if (currentUserId != null) {
                                    FirebaseManager.firestore.runBatch { batch ->
                                        val myRef = FirebaseManager.firestore.collection("users").document(currentUserId)
                                        val theirRef = FirebaseManager.firestore.collection("users").document(post.userId)
                                        batch.update(myRef, "friendRequests", FieldValue.arrayRemove(post.userId))
                                        batch.update(myRef, "friends", FieldValue.arrayUnion(post.userId))
                                        batch.update(theirRef, "friends", FieldValue.arrayUnion(currentUserId))
                                    }
                                }
                            }
                        )
                    }
                    hasSentRequest -> {
                        Text(
                            text = "Pendente", 
                            style = AppTypography.bodyMedium, 
                            color = OutlineBorder
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(AccentBlue.copy(alpha = 0.15f))
                                .clickable {
                                    if (currentUserId != null) {
                                        localSentRequests.add(post.userId)
                                        FirebaseManager.firestore.collection("users").document(post.userId)
                                            .update("friendRequests", FieldValue.arrayUnion(currentUserId))
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Adicionar Amigo",
                                tint = AccentBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Title and Stats
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (post.workoutSessionId != null) {
                        onOpenDetail(post.workoutSessionId!!, post.userId)
                    }
                }
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = post.workoutName ?: "Treinamento",
                style = AppTypography.headlineLarge.copy(fontSize = 18.sp),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(36.dp)
            ) {
                Column {
                    Text("Tempo", style = AppTypography.labelSmall, color = OutlineBorder)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("${post.workoutDuration ?: 0}min", style = AppTypography.bodyLarge, color = TextPrimary)
                }
                Column {
                    Text("Volume", style = AppTypography.labelSmall, color = OutlineBorder)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("${post.workoutVolume?.toInt() ?: 0} kg", style = AppTypography.bodyLarge, color = TextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Big Image Section or Premium Summary Card
        val currentBitmap = postBitmapState
        if (!post.imageUrl.isNullOrBlank() || currentBitmap != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f) // Slightly more standard than 1f for modern timeline aspect ratio
                    .background(SurfaceLevel1)
                    .clickable {
                        if (post.workoutSessionId != null) {
                            onOpenDetail(post.workoutSessionId!!, post.userId)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (currentBitmap != null) {
                    Image(
                        bitmap = currentBitmap.asImageBitmap(),
                        contentDescription = "Workout Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    CircularProgressIndicator(color = AccentBlue)
                }
            }
        } else {
            // Premium Summary Card for posts without image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceLevel1)
                    .clickable {
                        if (post.workoutSessionId != null) {
                            onOpenDetail(post.workoutSessionId!!, post.userId)
                        }
                    }
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = AccentYellow,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Treino Registado com Sucesso!",
                            style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Ver exercícios e resumo completo",
                            style = AppTypography.labelMedium,
                            color = AccentYellow
                        )
                    }
                }
            }
        }

        // Action Bar (Like, Comment, Share)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (currentUserId != null) {
                    val postRef = FirebaseManager.firestore.collection("posts").document(post.id)
                    if (isLiked) {
                        postRef.update("likedBy", FieldValue.arrayRemove(currentUserId))
                    } else {
                        postRef.update("likedBy", FieldValue.arrayUnion(currentUserId))
                    }
                }
            }) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder, 
                    contentDescription = "Like", 
                    tint = if (isLiked) AccentYellow else TextPrimary
                )
            }
            Text(text = "${post.likedBy.size}", style = AppTypography.bodyMedium, color = TextPrimary)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            IconButton(onClick = { onOpenComments(post.id) }) {
                Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Comment", tint = TextPrimary)
            }
            Text(text = "$commentsCountState", style = AppTypography.bodyMedium, color = TextPrimary)
            
            Spacer(modifier = Modifier.width(16.dp))
 
            IconButton(onClick = { onSharePost(post) }) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = TextPrimary)
            }
        }

        // Liked By Text and Description
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            if (post.likedBy.isNotEmpty()) {
                val likerName = if (isLiked) "ti" else "um utilizador"
                val others = post.likedBy.size - 1
                val likedText = if (others > 0) "Gostado por $likerName e outras $others pessoas" else "Gostado por $likerName"
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Small mock avatar stack could go here
                    Text(
                        text = likedText,
                        style = AppTypography.bodyMedium,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (post.description.isNotBlank()) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary)) {
                            append("${posterProfile?.name ?: post.userName} ")
                        }
                        withStyle(style = SpanStyle(color = TextPrimary)) {
                            append(post.description)
                        }
                    },
                    style = AppTypography.bodyMedium
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(
            thickness = 2.dp,
            color = OutlineBorder.copy(alpha = 0.35f)
        )
    }
}

@Composable
fun ShareChatItem(
    room: ChatRoom,
    currentUserId: String,
    onShare: () -> Unit
) {
    val otherMemberId = room.members.find { it != currentUserId }
    var contactProfile by remember(otherMemberId) { mutableStateOf<UserProfile?>(null) }

    LaunchedEffect(otherMemberId) {
        if (otherMemberId != null && !room.isGroup) {
            FirebaseManager.firestore.collection("users").document(otherMemberId)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        contactProfile = doc.toObject(UserProfile::class.java)?.apply { id = doc.id }
                    }
                }
        }
    }

    val displayName = if (room.isGroup) room.name else contactProfile?.name ?: "Carregando..."
    val photoUrl = if (room.isGroup) null else contactProfile?.photoUrl

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceLevel1, shape = RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (room.isGroup) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AccentBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Group, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
            }
        } else {
            UserAvatar(
                photoUrl = photoUrl,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BackgroundDark)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = displayName,
            style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )

        Button(
            onClick = onShare,
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(28.dp)
        ) {
            Text("Partilhar", style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
private fun HevySponsoredAdCard(index: Int) {
    val context = LocalContext.current
    val brands = listOf(
        Triple("GYMSHARK 🦈", "Prepara o teu treino com 30% desconto na coleção Flex e calções Seamless. Leveza total para os teus treinos de força.", "COMPRAR AGORA"),
        Triple("NIKE ⚡", "Just Do It. Descobre os novos Metcon 9 concebidos para treinos de alta estabilidade e flexibilidade.", "VER MODELO"),
        Triple("OPTIMUM NUTRITION 🎖️", "A proteína de soro de leite mais vendida no mundo. Gold Standard Whey para alimentar a tua massa muscular.", "SABER MAIS")
    )
    val brand = remember(index) { brands[index % brands.size] }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .padding(horizontal = 16.dp)
            .background(SurfaceLevel1, RoundedCornerShape(12.dp))
            .border(1.dp, OutlineBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AccentBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = brand.first,
                        style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Patrocinado",
                        style = AppTypography.labelSmall,
                        color = AccentYellow,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = brand.second,
            style = AppTypography.bodyLarge,
            color = TextPrimary,
            lineHeight = 20.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                Toast.makeText(context, "A redirecionar para a oferta da ${brand.first.substringBefore(" ")}... 🌐", Toast.LENGTH_LONG).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = brand.third,
                style = AppTypography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(
            thickness = 2.dp,
            color = OutlineBorder.copy(alpha = 0.35f)
        )
    }
}