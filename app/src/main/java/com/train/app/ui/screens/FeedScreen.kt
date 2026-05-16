package com.train.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.Query
import com.train.app.data.FirebaseManager
import com.train.app.data.models.Post
import com.train.app.ui.components.TrainChip
import com.train.app.ui.theme.*

@Composable
fun FeedScreen() {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }

    LaunchedEffect(Unit) {
        FirebaseManager.firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    posts = snapshot.toObjects(Post::class.java)
                }
            }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(posts.size) { index ->
            PostItem(posts[index])
        }
    }
}

@Composable
fun PostItem(post: Post) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        // Header Post
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceLevel1)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = post.userName, style = AppTypography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = "Agora", style = AppTypography.labelMedium, color = OutlineBorder)
            }
        }

        // Media + Tags
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(SurfaceLevel1)
        ) {
            // Tag Overlaid
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                TrainChip(text = post.category, isWarning = false)
            }
        }

        // Interações
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = "Like", tint = TextPrimary)
                }
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Comment", tint = TextPrimary)
                }
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = TextPrimary)
                }
            }
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.BookmarkBorder, contentDescription = "Save", tint = TextPrimary)
            }
        }

        // Legenda
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(text = "${post.likes} likes", style = AppTypography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary)) {
                        append("${post.userName} ")
                    }
                    withStyle(style = SpanStyle(color = TextPrimary)) {
                        append(post.description)
                    }
                },
                style = AppTypography.bodyLarge
            )
        }
    }
}