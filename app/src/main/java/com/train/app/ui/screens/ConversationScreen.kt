package com.train.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import com.google.firebase.firestore.FieldValue
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.train.app.data.FirebaseManager
import com.train.app.data.models.ChatRoom
import com.train.app.data.models.Message
import com.train.app.data.models.Routine
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
fun ConversationScreen(
    roomId: String,
    onBack: () -> Unit,
    onOpenPostComments: (String) -> Unit = {},
    onOpenWorkoutDetail: (String, String?) -> Unit = { _, _ -> }
) {
    val currentUser = FirebaseManager.auth.currentUser
    val context = LocalContext.current

    var room by remember { mutableStateOf<ChatRoom?>(null) }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var inputMessage by remember { mutableStateOf("") }
    var myProfile by remember { mutableStateOf<UserProfile?>(null) }

    var showRoutineSheet by remember { mutableStateOf(false) }
    var myRoutines by remember { mutableStateOf<List<Routine>>(emptyList()) }

    var showGroupDetails by remember { mutableStateOf(false) }
    var groupMembersProfiles by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    val localSentRequests = remember { mutableStateListOf<String>() }

    var showAddMembersSheet by remember { mutableStateOf(false) }
    var myFriendsProfiles by remember { mutableStateOf<List<UserProfile>>(emptyList()) }

    val listState = rememberLazyListState()

    // Fetch details of group members reactively
    LaunchedEffect(showGroupDetails, room?.members) {
        val memberUids = room?.members ?: emptyList()
        if (showGroupDetails && memberUids.isNotEmpty()) {
            FirebaseManager.firestore.collection("users")
                .whereIn("id", memberUids)
                .get()
                .addOnSuccessListener { snap ->
                    groupMembersProfiles = snap.toObjects(UserProfile::class.java)
                }
        }
    }

    // Fetch my friends' profiles for addition
    LaunchedEffect(showAddMembersSheet, myProfile?.friends) {
        val friendUids = myProfile?.friends ?: emptyList()
        if (showAddMembersSheet && friendUids.isNotEmpty()) {
            FirebaseManager.firestore.collection("users")
                .whereIn("id", friendUids)
                .get()
                .addOnSuccessListener { snap ->
                    myFriendsProfiles = snap.toObjects(UserProfile::class.java)
                }
        }
    }

    // Listen to Room Info
    LaunchedEffect(roomId) {
        FirebaseManager.firestore.collection("chatRooms").document(roomId)
            .addSnapshotListener { snap, _ ->
                if (snap != null && snap.exists()) {
                    val r = snap.toObject(ChatRoom::class.java)?.copy(id = snap.id)
                    room = r
                    // If I was removed from this group, return back
                    if (r?.isGroup == true && currentUser != null && !r.members.contains(currentUser.uid)) {
                        onBack()
                        Toast.makeText(context, "Foste removido deste grupo.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    // Listen to Messages
    LaunchedEffect(roomId) {
        FirebaseManager.firestore.collection("chatRooms").document(roomId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    messages = snap.documents.mapNotNull { doc ->
                        doc.toObject(Message::class.java)?.copy(id = doc.id)
                    }
                }
            }
    }

    // Listen to my profile dynamically
    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            FirebaseManager.firestore.collection("users").document(currentUser.uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        myProfile = snapshot.toObject(UserProfile::class.java)
                    }
                }
        }
    }

    // Fetch my routines for sharing bottom sheet
    LaunchedEffect(showRoutineSheet) {
        val myUid = currentUser?.uid
        if (showRoutineSheet && myUid != null) {
            FirebaseManager.firestore.collection("users").document(myUid)
                .collection("routines")
                .get()
                .addOnSuccessListener { snap ->
                    myRoutines = snap.toObjects(Routine::class.java)
                }
        }
    }

    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Send Message Helper
    val sendMessage = { textToSend: String, routine: Routine? ->
        val myUid = currentUser?.uid
        val myName = myProfile?.name ?: "Parceiro"
        if (myUid != null && (textToSend.trim().isNotEmpty() || routine != null)) {
            val messageId = UUID.randomUUID().toString()
            val msg = Message(
                id = messageId,
                senderId = myUid,
                senderName = myName,
                text = textToSend.trim(),
                timestamp = System.currentTimeMillis(),
                sharedRoutineId = routine?.id,
                sharedRoutineName = routine?.name,
                sharedRoutineExercises = routine?.exercises?.map { it.name }
            )

            // Add to messages subcollection
            FirebaseManager.firestore.collection("chatRooms").document(roomId)
                .collection("messages").document(messageId)
                .set(msg)

            // Update Room Preview
            val lastContent = if (routine != null) "Partilhou a rotina: ${routine.name}" else textToSend
            FirebaseManager.firestore.collection("chatRooms").document(roomId)
                .update(
                    mapOf(
                        "lastMessageContent" to lastContent,
                        "lastMessageSender" to myName,
                        "lastMessageTime" to System.currentTimeMillis()
                    )
                )

            inputMessage = ""
        }
    }

    // Fetch details of group members or other member
    val otherMemberId = room?.members?.find { it != currentUser?.uid }
    var contactProfile by remember(otherMemberId) { mutableStateOf<UserProfile?>(null) }
    LaunchedEffect(otherMemberId, room?.isGroup) {
        if (otherMemberId != null && room?.isGroup == false) {
            FirebaseManager.firestore.collection("users").document(otherMemberId)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        contactProfile = doc.toObject(UserProfile::class.java)
                    }
                }
        }
    }

    val headerTitle = if (room?.isGroup == true) room?.name.orEmpty() else contactProfile?.name ?: "Conversa"

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .clickable(enabled = room?.isGroup == true) {
                                showGroupDetails = true
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (room?.isGroup == true) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AccentBlue.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = AccentBlue)
                            }
                        } else {
                            UserAvatar(photoUrl = contactProfile?.photoUrl, modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = headerTitle,
                                style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                                color = Color.White
                            )
                            if (room?.isGroup == true) {
                                Text(
                                    text = "${room?.members?.size ?: 0} membros (Ver membros)",
                                    style = AppTypography.labelSmall,
                                    color = OutlineBorder
                                )
                            }
                        }
                    }
                },
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
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(paddingValues)
        ) {
            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages.size) { index ->
                    val msg = messages[index]
                    val isMe = msg.senderId == currentUser?.uid
                    MessageRow(
                        message = msg,
                        isMe = isMe,
                        isGroup = room?.isGroup == true,
                        onImportRoutine = { routineId, senderId ->
                            val myUid = currentUser?.uid
                            if (myUid != null) {
                                FirebaseManager.firestore
                                    .collection("users")
                                    .document(senderId)
                                    .collection("routines")
                                    .document(routineId)
                                    .get()
                                    .addOnSuccessListener { doc ->
                                        if (doc.exists()) {
                                            val routine = doc.toObject(Routine::class.java)
                                            if (routine != null) {
                                                val newRoutine = routine.copy(
                                                    id = UUID.randomUUID().toString(),
                                                    userId = myUid
                                                )
                                                FirebaseManager.firestore
                                                    .collection("users")
                                                    .document(myUid)
                                                    .collection("routines")
                                                    .document(newRoutine.id)
                                                    .set(newRoutine)
                                                    .addOnSuccessListener {
                                                        Toast.makeText(context, "Rotina \"${routine.name}\" importada!", Toast.LENGTH_SHORT).show()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                                                    }
                                            }
                                        } else {
                                            Toast.makeText(context, "Rotina indisponível ou apagada.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        },
                        onOpenPostComments = onOpenPostComments,
                        onOpenWorkoutDetail = onOpenWorkoutDetail
                    )
                }
            }

            // Input Bar
            Surface(
                color = SurfaceLevel1,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showRoutineSheet = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = AccentBlue.copy(alpha = 0.1f),
                            contentColor = AccentBlue
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Partilhar")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TrainInput(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = "Escreve uma mensagem...",
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(max = 120.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { sendMessage(inputMessage, null) },
                        enabled = inputMessage.trim().isNotEmpty(),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (inputMessage.trim().isNotEmpty()) AccentBlue else OutlineBorder.copy(alpha = 0.1f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Enviar")
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet to choose a routine to share
    if (showRoutineSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRoutineSheet = false },
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
                    text = "Partilhar Rotina",
                    style = AppTypography.headlineLarge.copy(fontSize = 20.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Seleciona uma rotina para enviar ao chat:",
                    style = AppTypography.bodyMedium,
                    color = OutlineBorder
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (myRoutines.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Não tens rotinas criadas para partilhar.", color = OutlineBorder)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(myRoutines.size) { index ->
                            val r = myRoutines[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BackgroundDark, shape = RoundedCornerShape(8.dp))
                                    .clickable {
                                        showRoutineSheet = false
                                        sendMessage("", r)
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(AccentBlue.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = AccentBlue)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(r.name, style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                    Text("${r.exercises.size} exercícios", style = AppTypography.labelSmall, color = OutlineBorder)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGroupDetails && room?.isGroup == true) {
        // We render a beautiful, sleek dark-themed full screen overlay!
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header of Overlay
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showGroupDetails = false }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Membros do Grupo",
                                style = AppTypography.headlineLarge.copy(fontSize = 20.sp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = room?.name.orEmpty(),
                                style = AppTypography.labelSmall,
                                color = OutlineBorder
                            )
                        }
                    }

                    if (room?.createdById == currentUser?.uid) {
                        Button(
                            onClick = { showAddMembersSheet = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Adicionar", style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                // Member Profiles List
                if (groupMembersProfiles.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentBlue)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(groupMembersProfiles.size) { index ->
                            val member = groupMembersProfiles[index]
                            val isMe = member.id == currentUser?.uid
                            val isFriend = myProfile?.friends?.contains(member.id) == true

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceLevel1, shape = RoundedCornerShape(12.dp))
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(
                                    photoUrl = member.photoUrl,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(BackgroundDark)
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = member.name,
                                        style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = member.email,
                                        style = AppTypography.labelSmall,
                                        color = OutlineBorder,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Social Action
                                when {
                                    isMe -> {
                                        Text(
                                            text = "(Tu)",
                                            style = AppTypography.labelSmall,
                                            color = OutlineBorder
                                        )
                                    }
                                    isFriend -> {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color(0xFF4CAF50),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Amigo",
                                                style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFF4CAF50)
                                            )
                                        }
                                    }
                                    else -> {
                                        // Friend Requests checks
                                        val hasPendingFromThem = myProfile?.friendRequests?.contains(member.id) == true
                                        val hasPendingFromMe = member.friendRequests.contains(currentUser?.uid) || localSentRequests.contains(member.id)

                                        if (hasPendingFromThem) {
                                            // Yellow Accept Button
                                            Button(
                                                onClick = {
                                                    val db = FirebaseManager.firestore
                                                    val batch = db.batch()
                                                    val myRef = db.collection("users").document(currentUser!!.uid)
                                                    val otherRef = db.collection("users").document(member.id)

                                                    batch.update(myRef, "friends", FieldValue.arrayUnion(member.id))
                                                    batch.update(otherRef, "friends", FieldValue.arrayUnion(currentUser.uid))
                                                    batch.update(myRef, "friendRequests", FieldValue.arrayRemove(member.id))

                                                    batch.commit()
                                                        .addOnSuccessListener {
                                                            Toast.makeText(context, "Agora são amigos!", Toast.LENGTH_SHORT).show()
                                                        }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = AccentYellow,
                                                    contentColor = Color.Black
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text("Aceitar", style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                            }
                                        } else if (hasPendingFromMe) {
                                            // Grey Pendente label
                                            Text(
                                                text = "Pendente",
                                                style = AppTypography.labelSmall,
                                                color = OutlineBorder
                                            )
                                        } else {
                                            // Blue + Amigo button
                                            Button(
                                                onClick = {
                                                    localSentRequests.add(member.id)
                                                    FirebaseManager.firestore.collection("users")
                                                        .document(member.id)
                                                        .update("friendRequests", FieldValue.arrayUnion(currentUser!!.uid))
                                                        .addOnSuccessListener {
                                                            Toast.makeText(context, "Pedido enviado!", Toast.LENGTH_SHORT).show()
                                                        }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = AccentBlue,
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text("+ Amigo", style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                            }
                                        }
                                    }
                                }

                                val isCreator = room?.createdById == currentUser?.uid
                                if (isCreator && !isMe) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            FirebaseManager.firestore.collection("chatRooms").document(roomId)
                                                .update("members", FieldValue.arrayRemove(member.id))
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "${member.name} removido do grupo.", Toast.LENGTH_SHORT).show()
                                                }
                                        },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = Color.Red.copy(alpha = 0.1f),
                                            contentColor = Color.Red
                                        ),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Expulsar",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val myUid = currentUser?.uid.orEmpty()
                        val db = FirebaseManager.firestore
                        val roomRef = db.collection("chatRooms").document(roomId)

                        roomRef.get().addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                val currentRoom = snapshot.toObject(ChatRoom::class.java)
                                if (currentRoom != null) {
                                    val updatedMembers = currentRoom.members.filter { it != myUid }

                                    if (updatedMembers.isEmpty()) {
                                        roomRef.delete().addOnSuccessListener {
                                            Toast.makeText(context, "Saíste do grupo. O grupo foi dissolvido.", Toast.LENGTH_SHORT).show()
                                            showGroupDetails = false
                                            onBack()
                                        }
                                    } else {
                                        val wasCreator = currentRoom.createdById == myUid
                                        val newCreatorId = if (wasCreator) {
                                            updatedMembers[0]
                                        } else {
                                            currentRoom.createdById
                                        }

                                        val updates = mapOf(
                                            "members" to updatedMembers,
                                            "createdById" to newCreatorId
                                        )

                                        roomRef.update(updates).addOnSuccessListener {
                                            Toast.makeText(context, "Saíste do grupo.", Toast.LENGTH_SHORT).show()
                                            showGroupDetails = false
                                            onBack()
                                        }
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red.copy(alpha = 0.15f),
                        contentColor = Color.Red
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Sair do Grupo",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sair do Grupo",
                        style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    if (showAddMembersSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddMembersSheet = false },
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
                    text = "Adicionar ao Grupo",
                    style = AppTypography.headlineLarge.copy(fontSize = 20.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Selecione um amigo para adicionar ao grupo:",
                    style = AppTypography.bodyMedium,
                    color = OutlineBorder
                )

                Spacer(modifier = Modifier.height(16.dp))

                val nonGroupFriends = myFriendsProfiles.filter { friend ->
                    room?.members?.contains(friend.id) == false
                }

                if (nonGroupFriends.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Não tens mais amigos para adicionar.", color = OutlineBorder)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(nonGroupFriends.size) { index ->
                            val friend = nonGroupFriends[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BackgroundDark, shape = RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(photoUrl = friend.photoUrl, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = friend.name,
                                    style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        val db = FirebaseManager.firestore
                                        db.collection("chatRooms").document(roomId)
                                            .update("members", FieldValue.arrayUnion(friend.id))
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "${friend.name} adicionado!", Toast.LENGTH_SHORT).show()
                                            }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccentBlue,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Adicionar", style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold))
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

@Composable
fun MessageRow(
    message: Message,
    isMe: Boolean,
    isGroup: Boolean,
    onImportRoutine: (String, String) -> Unit,
    onOpenPostComments: (String) -> Unit,
    onOpenWorkoutDetail: (String, String?) -> Unit
) {
    val align = if (isMe) Alignment.End else Alignment.Start
    val bubbleColor = if (isMe) AccentBlue else SurfaceLevel1
    val txtColor = Color.White
    val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        // Sender Name in Groups
        if (!isMe && isGroup) {
            Text(
                text = message.senderName,
                style = AppTypography.labelSmall,
                color = AccentYellow,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }

        // Message Bubble
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = bubbleColor,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (message.sharedRoutineId != null) {
                // Shared Routine Bubble
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = AccentYellow, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rotina Partilhada",
                            style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = AccentYellow
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.sharedRoutineName.orEmpty(),
                        style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val exercises = message.sharedRoutineExercises.orEmpty()
                    if (exercises.isNotEmpty()) {
                        exercises.take(3).forEach { exName ->
                            Text(
                                text = "• $exName",
                                style = AppTypography.bodyMedium,
                                color = OutlineBorder,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (exercises.size > 3) {
                            Text(
                                text = "...e mais ${exercises.size - 3} exercícios",
                                style = AppTypography.labelSmall,
                                color = OutlineBorder
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onImportRoutine(message.sharedRoutineId, message.senderId) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentYellow,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                    ) {
                        Text(
                            "ADICIONAR",
                            style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            } else if (message.sharedPostId != null) {
                // Shared Post Bubble
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (message.sharedWorkoutSessionId != null) {
                                onOpenWorkoutDetail(message.sharedWorkoutSessionId, message.sharedPostUserId)
                            }
                        }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = AccentYellow, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Post Partilhado",
                            style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = AccentYellow
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Treino de ${message.sharedPostAuthorName.orEmpty()}",
                        style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (!message.sharedPostContent.isNullOrBlank()) {
                        Text(
                            text = message.sharedPostContent,
                            style = AppTypography.bodyMedium,
                            color = Color.White,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Display shared post image if present
                    if (!message.sharedPostImageUrl.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        var postBitmap by remember(message.sharedPostImageUrl) { mutableStateOf<Bitmap?>(null) }
                        val context = LocalContext.current
                        LaunchedEffect(message.sharedPostImageUrl) {
                            if (message.sharedPostImageUrl.startsWith("content://") || message.sharedPostImageUrl.startsWith("file://")) {
                                try {
                                    val uri = Uri.parse(message.sharedPostImageUrl)
                                    val loaded = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                        com.train.app.ui.components.decodeUriSafely(context, uri, maxDimension = 360)
                                    }
                                    postBitmap = loaded
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        val currentBmp = postBitmap
                        if (currentBmp != null) {
                            Image(
                                bitmap = currentBmp.asImageBitmap(),
                                contentDescription = "Shared Post Photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onOpenPostComments(message.sharedPostId) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentYellow,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                    ) {
                        Text(
                            "VER COMENTÁRIOS",
                            style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            } else {
                // Text Message
                Text(
                    text = message.text,
                    style = AppTypography.bodyMedium,
                    color = txtColor
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Time text at bottom of bubble
            Text(
                text = timeString,
                style = AppTypography.labelSmall.copy(fontSize = 9.sp),
                color = OutlineBorder.copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
