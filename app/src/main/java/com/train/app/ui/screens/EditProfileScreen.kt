package com.train.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.train.app.data.FirebaseManager
import com.train.app.data.models.UserProfile
import com.train.app.ui.components.TrainInput
import com.train.app.ui.components.TrainPrimaryButton
import com.train.app.ui.components.UserAvatar
import com.train.app.ui.theme.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import com.google.firebase.firestore.DocumentReference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onOpenSubscriptionPaywall: () -> Unit
) {
    val context = LocalContext.current
    val currentUser = FirebaseManager.auth.currentUser

    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var appTheme by remember { mutableStateOf("DARK") }
    var customAccentColor by remember { mutableStateOf("#0A62D0") }
    var subscriptionTier by remember { mutableStateOf("FREE") }
    
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val profileFile = java.io.File(context.filesDir, "profile_pic_${currentUser?.uid}.jpg")
                    val outputStream = java.io.FileOutputStream(profileFile)
                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    photoUrl = Uri.fromFile(profileFile).toString() + "?t=${System.currentTimeMillis()}"
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao guardar foto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            FirebaseManager.firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val profile = doc.toObject(UserProfile::class.java)?.apply { id = doc.id }
                        if (profile != null) {
                            name = profile.name
                            bio = profile.bio ?: ""
                            photoUrl = profile.photoUrl ?: ""
                            appTheme = profile.appTheme
                            customAccentColor = profile.customAccentColor
                            subscriptionTier = profile.subscriptionTier
                        }
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(context, "Erro ao carregar perfil", Toast.LENGTH_SHORT).show()
                }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil", style = AppTypography.headlineLarge.copy(fontSize = 20.sp), color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Photo Selection
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(SurfaceLevel1)
                        .clickable {
                            galleryLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    UserAvatar(
                        photoUrl = photoUrl,
                        modifier = Modifier.fillMaxSize(),
                        placeholderIcon = Icons.Default.CameraAlt,
                        iconSizePercent = 0.4f
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Alterar Foto", style = AppTypography.labelMedium, color = AccentBlue)

                Spacer(modifier = Modifier.height(32.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("NICKNAME", style = AppTypography.labelSmall, color = OutlineBorder)
                    Spacer(modifier = Modifier.height(8.dp))
                    TrainInput(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "O teu nickname..."
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("BIOGRAFIA", style = AppTypography.labelSmall, color = OutlineBorder)
                    Spacer(modifier = Modifier.height(8.dp))
                    TrainInput(
                        value = bio,
                        onValueChange = { bio = it },
                        placeholder = "Escreve um pouco sobre ti..."
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("TEMA DA APLICAÇÃO", style = AppTypography.labelSmall, color = OutlineBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("DARK" to "Escuro 🌑", "LIGHT" to "Claro ☀️", "CUSTOM" to "Personalizado 🎨").forEach { (themeKey, label) ->
                            val isSelected = appTheme == themeKey
                            val hasAccess = when (themeKey) {
                                "DARK" -> true
                                "LIGHT" -> subscriptionTier == "PRO" || subscriptionTier == "MASTER"
                                else -> subscriptionTier == "MASTER"
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (hasAccess) {
                                            appTheme = themeKey
                                            currentThemeName = themeKey
                                        } else {
                                            val requiredTier = if (themeKey == "LIGHT") "PRO" else "MASTER"
                                            Toast.makeText(
                                                context,
                                                "O tema $label requer o plano $requiredTier! 👑",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            onOpenSubscriptionPaywall()
                                        }
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AccentBlue.copy(alpha = 0.15f) else SurfaceLevel1,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) AccentBlue else if (!hasAccess) OutlineBorder.copy(alpha = 0.3f) else OutlineBorder.copy(alpha = 0.2f)
                                )
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) AccentBlue else if (!hasAccess) TextPrimary.copy(alpha = 0.4f) else TextPrimary,
                                            style = AppTypography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (!hasAccess) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("👑", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (appTheme == "CUSTOM" && subscriptionTier == "MASTER") {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("COR DE DESTAQUE (MASTER EXCLUSIVO)", style = AppTypography.labelSmall, color = OutlineBorder)
                        Spacer(modifier = Modifier.height(10.dp))

                        val customColors = listOf(
                            "#00FF87" to "Verde Neon",
                            "#FF0055" to "Carmim",
                            "#00E5FF" to "Azul Elétrico",
                            "#FF6D00" to "Pôr do Sol",
                            "#D500F9" to "Violeta Cósmico",
                            "#00E676" to "Optimum Green"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            customColors.forEach { (colorHex, colorName) ->
                                val colorVal = Color(android.graphics.Color.parseColor(colorHex))
                                val isColorSelected = customAccentColor == colorHex

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(colorVal)
                                        .border(
                                            width = if (isColorSelected) 3.dp else 0.dp,
                                            color = Color.White,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            customAccentColor = colorHex
                                            currentCustomAccentColor = colorHex
                                        }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("PLANO & SUBSCREVER", style = AppTypography.labelSmall, color = OutlineBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceLevel1,
                        border = BorderStroke(1.dp, OutlineBorder.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Plano Atual",
                                    style = AppTypography.labelSmall,
                                    color = OutlineBorder
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (subscriptionTier == "PRO") {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                                                )
                                            )
                                            .padding(horizontal = 10.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "★ PRO",
                                            color = Color.Black,
                                            style = AppTypography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 0.5.sp
                                            )
                                        )
                                    }
                                } else if (subscriptionTier == "MASTER") {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF3B82F6))
                                                )
                                            )
                                            .padding(horizontal = 10.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "👑 MASTER",
                                            color = Color.White,
                                            style = AppTypography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 0.5.sp
                                            )
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(TextPrimary.copy(alpha = 0.08f))
                                            .padding(horizontal = 10.dp, vertical = 3.dp)
                                            .border(0.5.dp, TextPrimary.copy(alpha = 0.15f), RoundedCornerShape(50))
                                    ) {
                                        Text(
                                            text = "GRATUITO (FREE)",
                                            color = TextPrimary,
                                            style = AppTypography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 0.5.sp
                                            )
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = onOpenSubscriptionPaywall,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentBlue,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Alterar Plano",
                                    style = AppTypography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                TrainPrimaryButton(
                    text = if (isSaving) "A GUARDAR..." else "GUARDAR ALTERAÇÕES",
                    onClick = {
                        if (currentUser != null && !isSaving) {
                            isSaving = true
                            val updates = mapOf(
                                "name" to name.trim(),
                                "bio" to bio.trim(),
                                "photoUrl" to photoUrl,
                                "appTheme" to appTheme,
                                "customAccentColor" to customAccentColor
                            )
                            FirebaseManager.firestore.collection("users").document(currentUser.uid)
                                .update(updates)
                                .addOnSuccessListener {
                                    isSaving = false
                                    Toast.makeText(context, "Perfil atualizado!", Toast.LENGTH_SHORT).show()
                                    onBack()
                                }
                                .addOnFailureListener { e ->
                                    isSaving = false
                                    Toast.makeText(context, "Erro ao atualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                var showDeleteConfirmDialog by remember { mutableStateOf(false) }

                if (showDeleteConfirmDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirmDialog = false },
                        title = { Text("Limpar Dados de Testes", color = TextPrimary, fontWeight = FontWeight.Bold) },
                        text = { Text("Tens a certeza? Esta ação vai apagar permanentemente todas as publicações, utilizadores, rotinas, treinos, comentários, chats, mensagens e exercícios personalizados da base de dados.", color = TextPrimary) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteConfirmDialog = false
                                    isLoading = true
                                    val db = FirebaseManager.firestore
                                    val refsToDelete = mutableListOf<DocumentReference>()

                                    // Chained fetching logic
                                    // 1. Fetch Posts
                                    db.collection("posts").get().addOnSuccessListener { postsSnap ->
                                        val postDocs = postsSnap.documents
                                        for (doc in postDocs) {
                                            refsToDelete.add(doc.reference)
                                        }

                                        var pendingPostComments = postDocs.size

                                        fun proceedToChatRooms() {
                                            // 2. Fetch ChatRooms
                                            db.collection("chatRooms").get().addOnSuccessListener { roomsSnap ->
                                                val roomDocs = roomsSnap.documents
                                                for (doc in roomDocs) {
                                                    refsToDelete.add(doc.reference)
                                                }

                                                var pendingRoomMessages = roomDocs.size

                                                fun proceedToUsers() {
                                                    // 3. Fetch Users
                                                    db.collection("users").get().addOnSuccessListener { usersSnap ->
                                                        val userDocs = usersSnap.documents
                                                        for (doc in userDocs) {
                                                            refsToDelete.add(doc.reference)
                                                        }

                                                        var pendingUserSubcollections = userDocs.size

                                                        fun commitDeletions() {
                                                            val chunks = refsToDelete.chunked(400)
                                                            if (chunks.isEmpty()) {
                                                                isLoading = false
                                                                Toast.makeText(context, "Base de dados limpa com sucesso!", Toast.LENGTH_LONG).show()
                                                                FirebaseManager.auth.signOut()
                                                                onBack()
                                                                return
                                                            }

                                                            var remainingChunks = chunks.size
                                                            var hasError = false
                                                            var errorMessage = ""

                                                            for (chunk in chunks) {
                                                                val batch = db.batch()
                                                                for (ref in chunk) {
                                                                    batch.delete(ref)
                                                                }
                                                                batch.commit().addOnSuccessListener {
                                                                    remainingChunks--
                                                                    if (remainingChunks == 0) {
                                                                        isLoading = false
                                                                        if (hasError) {
                                                                            Toast.makeText(context, "Limpeza concluída com alguns erros: $errorMessage", Toast.LENGTH_LONG).show()
                                                                        } else {
                                                                            Toast.makeText(context, "Base de dados limpa com sucesso!", Toast.LENGTH_LONG).show()
                                                                        }
                                                                        FirebaseManager.auth.signOut()
                                                                        onBack()
                                                                    }
                                                                }.addOnFailureListener { e ->
                                                                    hasError = true
                                                                    errorMessage = e.message ?: "Erro desconhecido"
                                                                    remainingChunks--
                                                                    if (remainingChunks == 0) {
                                                                        isLoading = false
                                                                        Toast.makeText(context, "Erro na limpeza: $errorMessage", Toast.LENGTH_LONG).show()
                                                                        FirebaseManager.auth.signOut()
                                                                        onBack()
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        if (userDocs.isEmpty()) {
                                                            commitDeletions()
                                                        } else {
                                                            for (userDoc in userDocs) {
                                                                val userId = userDoc.id
                                                                // Fetch routines subcollection
                                                                db.collection("users").document(userId).collection("routines").get().addOnSuccessListener { routinesSnap ->
                                                                    for (doc in routinesSnap.documents) {
                                                                        refsToDelete.add(doc.reference)
                                                                    }

                                                                    // Fetch sessions subcollection
                                                                    db.collection("users").document(userId).collection("sessions").get().addOnSuccessListener { sessionsSnap ->
                                                                        for (doc in sessionsSnap.documents) {
                                                                            refsToDelete.add(doc.reference)
                                                                        }

                                                                        // Fetch custom_exercises subcollection
                                                                        db.collection("users").document(userId).collection("custom_exercises").get().addOnSuccessListener { exercisesSnap ->
                                                                            for (doc in exercisesSnap.documents) {
                                                                                refsToDelete.add(doc.reference)
                                                                            }

                                                                            pendingUserSubcollections--
                                                                            if (pendingUserSubcollections == 0) {
                                                                                commitDeletions()
                                                                            }
                                                                        }.addOnFailureListener {
                                                                            pendingUserSubcollections--
                                                                            if (pendingUserSubcollections == 0) {
                                                                                commitDeletions()
                                                                            }
                                                                        }
                                                                    }.addOnFailureListener {
                                                                        pendingUserSubcollections--
                                                                        if (pendingUserSubcollections == 0) {
                                                                            commitDeletions()
                                                                        }
                                                                    }
                                                                }.addOnFailureListener {
                                                                    pendingUserSubcollections--
                                                                    if (pendingUserSubcollections == 0) {
                                                                        commitDeletions()
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }.addOnFailureListener { e ->
                                                        isLoading = false
                                                        Toast.makeText(context, "Erro ao listar utilizadores: ${e.message}", Toast.LENGTH_SHORT).show()
                                                    }
                                                }

                                                if (roomDocs.isEmpty()) {
                                                    proceedToUsers()
                                                } else {
                                                    for (roomDoc in roomDocs) {
                                                        val roomId = roomDoc.id
                                                        db.collection("chatRooms").document(roomId).collection("messages").get().addOnSuccessListener { msgsSnap ->
                                                            for (doc in msgsSnap.documents) {
                                                                refsToDelete.add(doc.reference)
                                                            }
                                                            pendingRoomMessages--
                                                            if (pendingRoomMessages == 0) {
                                                                proceedToUsers()
                                                            }
                                                        }.addOnFailureListener {
                                                            pendingRoomMessages--
                                                            if (pendingRoomMessages == 0) {
                                                                proceedToUsers()
                                                            }
                                                        }
                                                    }
                                                }
                                            }.addOnFailureListener { e ->
                                                isLoading = false
                                                Toast.makeText(context, "Erro ao listar salas de chat: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }

                                        if (postDocs.isEmpty()) {
                                            proceedToChatRooms()
                                        } else {
                                            for (postDoc in postDocs) {
                                                val postId = postDoc.id
                                                db.collection("posts").document(postId).collection("comments").get().addOnSuccessListener { commentsSnap ->
                                                    for (doc in commentsSnap.documents) {
                                                        refsToDelete.add(doc.reference)
                                                    }
                                                    pendingPostComments--
                                                    if (pendingPostComments == 0) {
                                                        proceedToChatRooms()
                                                    }
                                                }.addOnFailureListener {
                                                    pendingPostComments--
                                                    if (pendingPostComments == 0) {
                                                        proceedToChatRooms()
                                                    }
                                                }
                                            }
                                        }
                                    }.addOnFailureListener { e ->
                                        isLoading = false
                                        Toast.makeText(context, "Erro ao listar publicações: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text("SIM, APAGAR TUDO", color = Color.Red, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirmDialog = false }) {
                                Text("CANCELAR", color = AccentBlue)
                            }
                        },
                        containerColor = SurfaceLevel1
                    )
                }

                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = true },
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("LIMPAR TODOS OS DADOS (DEV)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
