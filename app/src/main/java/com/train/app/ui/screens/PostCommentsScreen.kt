package com.train.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.Query
import com.train.app.data.FirebaseManager
import com.train.app.data.models.Comment
import com.train.app.data.models.UserProfile
import com.train.app.ui.components.TrainInput
import com.train.app.ui.components.UserAvatar
import com.train.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCommentsScreen(
    postId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var newCommentText by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }

    val currentUser = FirebaseManager.auth.currentUser

    LaunchedEffect(postId) {
        val listener = FirebaseManager.firestore
            .collection("posts")
            .document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    comments = snapshot.toObjects(Comment::class.java)
                }
            }
        // Cleanup not strictly necessary in simple flow but good practice if keeping references
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comentários", style = AppTypography.headlineLarge.copy(fontSize = 20.sp)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                color = SurfaceLevel1,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        TrainInput(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            placeholder = "Adicionar um comentário..."
                        )
                    }
                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank() && currentUser != null && !isPosting) {
                                isPosting = true
                                val comment = Comment(
                                    id = UUID.randomUUID().toString(),
                                    postId = postId,
                                    userId = currentUser.uid,
                                    userName = currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "Atleta",
                                    text = newCommentText.trim()
                                )

                                val postRef = FirebaseManager.firestore.collection("posts").document(postId)
                                val commentRef = postRef.collection("comments").document(comment.id)

                                FirebaseManager.firestore.runBatch { batch ->
                                    batch.set(commentRef, comment)
                                     batch.update(postRef, "commentsCount", com.google.firebase.firestore.FieldValue.increment(1))
                                }.addOnSuccessListener {
                                    newCommentText = ""
                                    isPosting = false
                                }.addOnFailureListener {
                                    isPosting = false
                                    Toast.makeText(context, "Erro ao publicar", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (newCommentText.isNotBlank()) AccentBlue else SurfaceLevel0)
                    ) {
                        if (isPosting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (comments.isEmpty()) {
                item {
                    Text("Ainda não há comentários. Sê o primeiro!", color = OutlineBorder, modifier = Modifier.padding(16.dp))
                }
            }

            items(comments) { comment ->
                CommentItem(comment = comment)
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    var commenterProfile by remember(comment.userId) { mutableStateOf<UserProfile?>(null) }

    LaunchedEffect(comment.userId) {
        FirebaseManager.firestore.collection("users").document(comment.userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    commenterProfile = doc.toObject(UserProfile::class.java)
                }
            }
    }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        UserAvatar(
            photoUrl = commenterProfile?.photoUrl,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.userName,
                    style = AppTypography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = SimpleDateFormat("dd MMM • HH:mm", Locale("pt", "PT")).format(Date(comment.timestamp)),
                    style = AppTypography.labelSmall,
                    color = OutlineBorder
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.text,
                style = AppTypography.bodyMedium,
                color = TextPrimary
            )
        }
    }
}
