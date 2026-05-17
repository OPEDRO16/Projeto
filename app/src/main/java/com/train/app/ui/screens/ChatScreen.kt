package com.train.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.train.app.data.FirebaseManager
import com.train.app.data.models.ChatRoom
import com.train.app.data.models.UserProfile
import com.train.app.ui.components.TrainInput
import com.train.app.ui.components.TrainPrimaryButton
import com.train.app.ui.components.UserAvatar
import com.train.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onOpenChat: (String) -> Unit
) {
    val currentUser = FirebaseManager.auth.currentUser
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var friendsProfiles by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var chatRooms by remember { mutableStateOf<List<ChatRoom>>(emptyList()) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var showGroupCreateDialog by remember { mutableStateOf(false) }

    // Listen to current user profile
    LaunchedEffect(currentUser?.uid) {
        if (currentUser == null) return@LaunchedEffect
        FirebaseManager.firestore.collection("users").document(currentUser.uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    userProfile = snapshot.toObject(UserProfile::class.java)?.apply { id = snapshot.id }
                }
            }
    }

    // Fetch friends profiles
    LaunchedEffect(userProfile?.friends) {
        val friendsUids = userProfile?.friends ?: emptyList()
        if (friendsUids.isEmpty()) {
            friendsProfiles = emptyList()
            return@LaunchedEffect
        }
        FirebaseManager.firestore.collection("users")
            .whereIn("id", friendsUids)
            .get()
            .addOnSuccessListener { snap ->
                friendsProfiles = snap.documents.mapNotNull { doc ->
                    doc.toObject(UserProfile::class.java)?.apply { id = doc.id }
                }
            }
    }

    // Listen to chat rooms where user is member
    LaunchedEffect(currentUser?.uid) {
        if (currentUser == null) return@LaunchedEffect
        FirebaseManager.firestore.collection("chatRooms")
            .whereArrayContains("members", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    val rooms = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ChatRoom::class.java)?.copy(id = doc.id)
                    }
                    chatRooms = rooms.sortedByDescending { it.lastMessageTime }
                }
            }
    }

    // Open or create DM Room helper
    val openOrCreateDM = { friendId: String ->
        val myUid = currentUser?.uid
        if (myUid != null) {
            val existing = chatRooms.find { !it.isGroup && it.members.contains(friendId) && it.members.contains(myUid) }
            if (existing != null) {
                onOpenChat(existing.id)
            } else {
                val newRoom = ChatRoom(
                    isGroup = false,
                    members = listOf(myUid, friendId),
                    createdById = myUid,
                    lastMessageContent = "Conversa iniciada",
                    lastMessageTime = System.currentTimeMillis()
                )
                FirebaseManager.firestore.collection("chatRooms")
                    .add(newRoom)
                    .addOnSuccessListener { ref ->
                        onOpenChat(ref.id)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Erro ao criar chat: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = AccentBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Chat")
            }
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Mensagens",
                style = AppTypography.headlineLarge.copy(fontSize = 28.sp),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pesquisa
            TrainInput(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Pesquisar conversas..."
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Ativos Agora (Amigos)
            if (friendsProfiles.isNotEmpty()) {
                Text(
                    text = "AMIGOS",
                    style = AppTypography.labelMedium,
                    color = OutlineBorder
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(friendsProfiles.size) { index ->
                        val friend = friendsProfiles[index]
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { openOrCreateDM(friend.id) }
                        ) {
                            Box(modifier = Modifier.size(56.dp)) {
                                UserAvatar(
                                    photoUrl = friend.photoUrl,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceLevel1)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50))
                                        .align(Alignment.BottomEnd)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = friend.name.split(" ").firstOrNull() ?: "",
                                style = AppTypography.labelSmall,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Lista de Mensagens
            Text(
                text = "RECENTES",
                style = AppTypography.labelMedium,
                color = OutlineBorder
            )
            Spacer(modifier = Modifier.height(12.dp))

            val filteredRooms = chatRooms.filter { room ->
                if (room.isGroup) {
                    room.name.contains(searchQuery, ignoreCase = true)
                } else {
                    true // Let them all pass or filter dynamically in the loop
                }
            }

            if (filteredRooms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma conversa ativa",
                        style = AppTypography.bodyMedium,
                        color = OutlineBorder
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredRooms.size) { index ->
                        val room = filteredRooms[index]
                        RecentChatItem(
                            room = room,
                            currentUserId = currentUser?.uid ?: "",
                            onClick = { onOpenChat(room.id) }
                        )
                    }
                }
            }
        }
    }

    // Selection Dialog for DM or Group
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = SurfaceLevel1,
            title = {
                Text(
                    "Nova Mensagem",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Option 1: Group Chat
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showCreateDialog = false
                                showGroupCreateDialog = true
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AccentBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = AccentBlue)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Criar Grupo", style = AppTypography.bodyLarge, color = TextWhite)
                            Text("Treinar e partilhar rotinas em conjunto", style = AppTypography.labelSmall, color = OutlineBorder)
                        }
                    }

                    Divider(color = OutlineBorder.copy(alpha = 0.3f))

                    // List Friends for DM
                    Text("OU CONVERSAR DIRETAMENTE:", style = AppTypography.labelSmall, color = AccentYellow)

                    if (friendsProfiles.isEmpty()) {
                        Text("Adiciona amigos primeiro para conversar!", style = AppTypography.bodyMedium, color = OutlineBorder)
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(friendsProfiles.size) { idx ->
                                val friend = friendsProfiles[idx]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showCreateDialog = false
                                            openOrCreateDM(friend.id)
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    UserAvatar(photoUrl = friend.photoUrl, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(friend.name, style = AppTypography.bodyMedium, color = TextWhite)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancelar", color = AccentBlue)
                }
            }
        )
    }

    // Group Creation Dialog
    if (showGroupCreateDialog) {
        var groupName by remember { mutableStateOf("") }
        val selectedFriends = remember { mutableStateListOf<String>() }

        AlertDialog(
            onDismissRequest = { showGroupCreateDialog = false },
            containerColor = SurfaceLevel1,
            title = {
                Text(
                    "Criar Novo Grupo",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    TrainInput(
                        value = groupName,
                        onValueChange = { groupName = it },
                        placeholder = "Nome do Grupo (ex: Leg Day Crew)"
                    )

                    Text("SELECIONAR MEMBROS:", style = AppTypography.labelSmall, color = AccentYellow)

                    if (friendsProfiles.isEmpty()) {
                        Text("Adiciona amigos primeiro!", style = AppTypography.bodyMedium, color = OutlineBorder)
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(friendsProfiles.size) { idx ->
                                val friend = friendsProfiles[idx]
                                val isChecked = selectedFriends.contains(friend.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isChecked) selectedFriends.remove(friend.id)
                                            else selectedFriends.add(friend.id)
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = {
                                            if (isChecked) selectedFriends.remove(friend.id)
                                            else selectedFriends.add(friend.id)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = AccentBlue,
                                            uncheckedColor = OutlineBorder
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    UserAvatar(photoUrl = friend.photoUrl, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(friend.name, style = AppTypography.bodyMedium, color = TextWhite)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TrainPrimaryButton(
                    text = "Criar",
                    onClick = {
                        val myUid = currentUser?.uid
                        val myName = userProfile?.name ?: "Parceiro"
                        if (myUid != null) {
                            if (groupName.trim().isEmpty()) {
                                Toast.makeText(context, "Insere um nome para o grupo", Toast.LENGTH_SHORT).show()
                                return@TrainPrimaryButton
                            }
                            if (selectedFriends.isEmpty()) {
                                Toast.makeText(context, "Seleciona pelo menos 1 amigo", Toast.LENGTH_SHORT).show()
                                return@TrainPrimaryButton
                            }

                            val newGroup = ChatRoom(
                                name = groupName.trim(),
                                isGroup = true,
                                members = selectedFriends.toList() + myUid,
                                createdById = myUid,
                                lastMessageContent = "Grupo criado por $myName",
                                lastMessageSender = myName,
                                lastMessageTime = System.currentTimeMillis()
                            )

                            FirebaseManager.firestore.collection("chatRooms")
                                .add(newGroup)
                                .addOnSuccessListener { ref ->
                                    showGroupCreateDialog = false
                                    onOpenChat(ref.id)
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Erro ao criar grupo: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showGroupCreateDialog = false }) {
                    Text("Cancelar", color = AccentBlue)
                }
            }
        )
    }
}

@Composable
fun RecentChatItem(
    room: ChatRoom,
    currentUserId: String,
    onClick: () -> Unit
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

    val displayName = if (room.isGroup) room.name else contactProfile?.name ?: "Parceiro de Treino"
    val photoUrl = if (room.isGroup) null else contactProfile?.photoUrl
    val dateString = if (room.lastMessageTime > 0L) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(room.lastMessageTime))
    } else {
        ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(SurfaceLevel1, shape = RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (room.isGroup) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AccentBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Group, contentDescription = null, tint = AccentBlue)
            }
        } else {
            UserAvatar(
                photoUrl = photoUrl,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(BackgroundDark)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(4.dp))
            val previewText = if (room.lastMessageSender != null && room.isGroup) {
                "${room.lastMessageSender}: ${room.lastMessageContent}"
            } else {
                room.lastMessageContent.orEmpty()
            }
            Text(
                text = previewText,
                style = AppTypography.bodyMedium,
                color = OutlineBorder,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = dateString,
                style = AppTypography.labelSmall,
                color = OutlineBorder
            )
        }
    }
}