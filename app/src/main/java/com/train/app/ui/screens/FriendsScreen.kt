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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FieldValue
import com.train.app.data.FirebaseManager
import com.train.app.data.models.UserProfile
import com.train.app.ui.components.TrainInput
import com.train.app.ui.components.TrainPrimaryButton
import com.train.app.ui.components.TrainSecondaryButton
import com.train.app.ui.components.UserAvatar
import com.train.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser = FirebaseManager.auth.currentUser

    var selectedTab by remember { mutableStateOf(0) }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    
    // Data states
    var friendsList by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var requestsList by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var searchResults by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    val sentRequestsList = remember { mutableStateListOf<String>() }

    // Fetch my profile
    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            FirebaseManager.firestore.collection("users").document(currentUser.uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        userProfile = snapshot.toObject(UserProfile::class.java)
                    }
                }
        }
    }

    // Fetch friends and requests based on userProfile updates
    LaunchedEffect(userProfile) {
        val profile = userProfile ?: return@LaunchedEffect
        
        // Fetch friends
        if (profile.friends.isNotEmpty()) {
            // Firestore 'in' has a limit of 10, mas para mock local funciona.
            // Para production com listas > 10, teríamos de usar outro método (e.g. subcoleções ou batch reads).
            val chunks = profile.friends.chunked(10)
            val friendsTemp = mutableListOf<UserProfile>()
            for (chunk in chunks) {
                FirebaseManager.firestore.collection("users")
                    .whereIn("id", chunk)
                    .get()
                    .addOnSuccessListener { snap ->
                        friendsTemp.addAll(snap.toObjects(UserProfile::class.java))
                        friendsList = friendsTemp.toList()
                    }
            }
        } else {
            friendsList = emptyList()
        }

        // Fetch requests
        if (profile.friendRequests.isNotEmpty()) {
            val chunks = profile.friendRequests.chunked(10)
            val reqTemp = mutableListOf<UserProfile>()
            for (chunk in chunks) {
                FirebaseManager.firestore.collection("users")
                    .whereIn("id", chunk)
                    .get()
                    .addOnSuccessListener { snap ->
                        reqTemp.addAll(snap.toObjects(UserProfile::class.java))
                        requestsList = reqTemp.toList()
                    }
            }
        } else {
            requestsList = emptyList()
        }
    }

    // Search logic
    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2) {
            isSearching = true
            // Pesquisa muito rudimentar (Firestore não suporta text search avançado sem Algolia/ElasticSearch)
            // Vamos descarregar todos para simplificar a protoype ou apenas usar orderBy
            FirebaseManager.firestore.collection("users").get()
                .addOnSuccessListener { snap ->
                    val allUsers = snap.toObjects(UserProfile::class.java)
                    val q = searchQuery.lowercase()
                    searchResults = allUsers.filter {
                        it.id != currentUser?.uid && 
                        (it.name.lowercase().contains(q) || it.email.lowercase().contains(q))
                    }
                    isSearching = false
                }
        } else {
            searchResults = emptyList()
            isSearching = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Amigos", style = AppTypography.headlineLarge.copy(fontSize = 20.sp)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceLevel1,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = BackgroundDark,
                contentColor = AccentBlue,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = AccentBlue
                        )
                    }
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("OS MEUS AMIGOS", color = if (selectedTab == 0) AccentBlue else OutlineBorder) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("ENCONTRAR", color = if (selectedTab == 1) AccentBlue else OutlineBorder) }
                )
            }

            if (selectedTab == 0) {
                // OS MEUS AMIGOS
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (friendsList.isEmpty()) {
                        item {
                            Text("Ainda não tens amigos adicionados.", color = OutlineBorder)
                        }
                    }
                    items(friendsList) { friend ->
                        UserListItem(
                            user = friend,
                            actionButton = {
                                TrainSecondaryButton(
                                    text = "REMOVER",
                                    onClick = {
                                        // Remove logic
                                        if (currentUser != null) {
                                            FirebaseManager.firestore.collection("users").document(currentUser.uid)
                                                .update("friends", FieldValue.arrayRemove(friend.id))
                                            FirebaseManager.firestore.collection("users").document(friend.id)
                                                .update("friends", FieldValue.arrayRemove(currentUser.uid))
                                        }
                                    }
                                )
                            }
                        )
                    }
                }
            } else {
                // ENCONTRAR E PEDIDOS
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (requestsList.isNotEmpty()) {
                        item {
                            Text("PEDIDOS PENDENTES", style = AppTypography.labelSmall, color = AccentBlue)
                        }
                        items(requestsList) { reqUser ->
                            UserListItem(
                                user = reqUser,
                                actionButton = {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                // Recusar
                                                if (currentUser != null) {
                                                    FirebaseManager.firestore.collection("users").document(currentUser.uid)
                                                        .update("friendRequests", FieldValue.arrayRemove(reqUser.id))
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceLevel1)
                                        ) {
                                            Text("Recusar", color = Color.White)
                                        }
                                        TrainPrimaryButton(
                                            text = "Aceitar",
                                            onClick = {
                                                // Aceitar
                                                if (currentUser != null) {
                                                    FirebaseManager.firestore.runBatch { batch ->
                                                        val myRef = FirebaseManager.firestore.collection("users").document(currentUser.uid)
                                                        val theirRef = FirebaseManager.firestore.collection("users").document(reqUser.id)
                                                        batch.update(myRef, "friendRequests", FieldValue.arrayRemove(reqUser.id))
                                                        batch.update(myRef, "friends", FieldValue.arrayUnion(reqUser.id))
                                                        batch.update(theirRef, "friends", FieldValue.arrayUnion(currentUser.uid))
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = SurfaceLevel1)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    item {
                        TrainInput(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = "Procurar por nome ou email..."
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (isSearching) {
                        item { CircularProgressIndicator(color = AccentBlue, modifier = Modifier.align(Alignment.CenterHorizontally)) }
                    } else if (searchQuery.length > 2 && searchResults.isEmpty()) {
                        item { Text("Nenhum utilizador encontrado.", color = OutlineBorder) }
                    }

                    items(searchResults) { resultUser ->
                        val isFriend = userProfile?.friends?.contains(resultUser.id) == true
                        val hasPendingFromThem = userProfile?.friendRequests?.contains(resultUser.id) == true
                        val hasPendingFromMe = resultUser.friendRequests.contains(currentUser?.uid) || sentRequestsList.contains(resultUser.id)

                        UserListItem(
                            user = resultUser,
                            actionButton = {
                                when {
                                    isFriend -> {
                                        Text("Amigos", color = AccentBlue, style = AppTypography.labelMedium)
                                    }
                                    hasPendingFromThem -> {
                                        Text("Pendente (Aceita Cima)", color = AccentYellow, style = AppTypography.labelMedium)
                                    }
                                    hasPendingFromMe -> {
                                        Text("Pedido Enviado", color = OutlineBorder, style = AppTypography.labelMedium)
                                    }
                                    else -> {
                                        TrainPrimaryButton(
                                            text = "Adicionar",
                                            onClick = {
                                                if (currentUser != null) {
                                                    sentRequestsList.add(resultUser.id)
                                                    FirebaseManager.firestore.collection("users").document(resultUser.id)
                                                        .update("friendRequests", FieldValue.arrayUnion(currentUser.uid))
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserListItem(
    user: UserProfile,
    actionButton: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceLevel0)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            UserAvatar(
                photoUrl = user.photoUrl,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(user.name, style = AppTypography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text(user.email, style = AppTypography.labelSmall, color = OutlineBorder)
            }
        }
        
        Box(modifier = Modifier.padding(start = 12.dp)) {
            actionButton()
        }
    }
}
