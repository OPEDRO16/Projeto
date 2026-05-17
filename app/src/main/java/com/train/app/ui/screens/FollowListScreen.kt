package com.train.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.train.app.data.FirebaseManager
import com.train.app.data.models.UserProfile
import com.google.firebase.firestore.FieldValue
import com.train.app.ui.components.TrainSecondaryButton
import com.train.app.ui.theme.AccentBlue
import com.train.app.ui.theme.AppTypography
import com.train.app.ui.theme.BackgroundDark
import com.train.app.ui.theme.OutlineBorder

enum class FollowMode { FOLLOWERS, FOLLOWING }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowListScreen(
    mode: FollowMode,
    onBack: () -> Unit
) {
    val currentUser = FirebaseManager.auth.currentUser
    var myProfile by remember { mutableStateOf<UserProfile?>(null) }
    var userList by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val title = if (mode == FollowMode.FOLLOWERS) "Seguidores" else "A Seguir"

    LaunchedEffect(currentUser?.uid) {
        if (currentUser == null) { isLoading = false; return@LaunchedEffect }

        FirebaseManager.firestore.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { doc ->
                myProfile = doc.toObject(UserProfile::class.java)
                val profile = myProfile ?: run { isLoading = false; return@addOnSuccessListener }

                // The friends list doubles as "following" in our current social model.
                // Followers = people whose 'friends' list contains my ID.
                // Following = people in my 'friends' list.
                when (mode) {
                    FollowMode.FOLLOWING -> {
                        val ids = profile.friends
                        if (ids.isEmpty()) {
                            userList = emptyList()
                            isLoading = false
                            return@addOnSuccessListener
                        }
                        val chunks = ids.chunked(10)
                        val temp = mutableListOf<UserProfile>()
                        var remaining = chunks.size
                        for (chunk in chunks) {
                            FirebaseManager.firestore.collection("users")
                                .whereIn("id", chunk)
                                .get()
                                .addOnSuccessListener { snap ->
                                    temp.addAll(snap.toObjects(UserProfile::class.java))
                                    remaining--
                                    if (remaining == 0) {
                                        userList = temp.toList()
                                        isLoading = false
                                    }
                                }
                                .addOnFailureListener { isLoading = false }
                        }
                    }
                    FollowMode.FOLLOWERS -> {
                        // Fetch all users who have currentUser.uid in their 'friends' array
                        FirebaseManager.firestore.collection("users")
                            .whereArrayContains("friends", currentUser.uid)
                            .get()
                            .addOnSuccessListener { snap ->
                                userList = snap.toObjects(UserProfile::class.java)
                                    .filter { it.id != currentUser.uid }
                                isLoading = false
                            }
                            .addOnFailureListener { isLoading = false }
                    }
                }
            }
            .addOnFailureListener { isLoading = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, style = AppTypography.headlineLarge.copy(fontSize = 20.sp)) },
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        color = AccentBlue,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                userList.isEmpty() -> {
                    Text(
                        text = if (mode == FollowMode.FOLLOWERS)
                            "Ainda não tens seguidores."
                        else
                            "Ainda não segues ninguém.",
                        color = OutlineBorder,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(userList) { user ->
                            UserListItem(
                                user = user,
                                actionButton = {
                                    TrainSecondaryButton(
                                        text = "Remover",
                                        onClick = {
                                            if (currentUser != null) {
                                                FirebaseManager.firestore.runBatch { batch ->
                                                    val myRef = FirebaseManager.firestore.collection("users").document(currentUser.uid)
                                                    val theirRef = FirebaseManager.firestore.collection("users").document(user.id)
                                                    batch.update(myRef, "friends", FieldValue.arrayRemove(user.id))
                                                    batch.update(theirRef, "friends", FieldValue.arrayRemove(currentUser.uid))
                                                }.addOnSuccessListener {
                                                    userList = userList.filter { it.id != user.id }
                                                }
                                            }
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
