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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser = FirebaseManager.auth.currentUser

    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    
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
                        val profile = doc.toObject(UserProfile::class.java)
                        if (profile != null) {
                            name = profile.name
                            bio = profile.bio ?: ""
                            photoUrl = profile.photoUrl ?: ""
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
                title = { Text("Editar Perfil", style = AppTypography.headlineLarge.copy(fontSize = 20.sp)) },
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
                }

                Spacer(modifier = Modifier.weight(1f))

                TrainPrimaryButton(
                    text = if (isSaving) "A GUARDAR..." else "GUARDAR ALTERAÇÕES",
                    onClick = {
                        if (currentUser != null && !isSaving) {
                            isSaving = true
                            val updates = mapOf(
                                "name" to name.trim(),
                                "bio" to bio.trim(),
                                "photoUrl" to photoUrl
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
                        title = { Text("Limpar Dados de Testes", color = Color.White, fontWeight = FontWeight.Bold) },
                        text = { Text("Tens a certeza? Esta ação vai apagar permanentemente todas as publicações (posts), utilizadores, rotinas e treinos da base de dados.", color = TextPrimary) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteConfirmDialog = false
                                    isLoading = true
                                    val db = FirebaseManager.firestore
                                    // Apagar posts
                                    db.collection("posts").get().addOnSuccessListener { postsSnapshot ->
                                        val batch = db.batch()
                                        for (postDoc in postsSnapshot.documents) {
                                            batch.delete(postDoc.reference)
                                        }
                                        // Apagar utilizadores
                                        db.collection("users").get().addOnSuccessListener { usersSnapshot ->
                                            val userDocs = usersSnapshot.documents
                                            if (userDocs.isEmpty()) {
                                                batch.commit().addOnSuccessListener {
                                                    isLoading = false
                                                    Toast.makeText(context, "Base de dados limpa com sucesso!", Toast.LENGTH_LONG).show()
                                                    FirebaseManager.auth.signOut()
                                                    onBack()
                                                }.addOnFailureListener { e ->
                                                    isLoading = false
                                                    Toast.makeText(context, "Erro ao limpar: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                var remainingUsersToClean = userDocs.size
                                                for (userDoc in userDocs) {
                                                    val userId = userDoc.id
                                                    batch.delete(userDoc.reference)
                                                    
                                                    // Apagar rotinas subcoleção
                                                    db.collection("users").document(userId).collection("routines").get().addOnSuccessListener { routinesSnapshot ->
                                                        for (routineDoc in routinesSnapshot.documents) {
                                                            batch.delete(routineDoc.reference)
                                                        }
                                                        
                                                        // Apagar treinos (sessions) subcoleção
                                                        db.collection("users").document(userId).collection("sessions").get().addOnSuccessListener { sessionsSnapshot ->
                                                            for (sessionDoc in sessionsSnapshot.documents) {
                                                                batch.delete(sessionDoc.reference)
                                                            }
                                                            
                                                            remainingUsersToClean--
                                                            if (remainingUsersToClean == 0) {
                                                                batch.commit().addOnSuccessListener {
                                                                    isLoading = false
                                                                    Toast.makeText(context, "Base de dados limpa com sucesso!", Toast.LENGTH_LONG).show()
                                                                    FirebaseManager.auth.signOut()
                                                                    onBack()
                                                                }.addOnFailureListener { e ->
                                                                    isLoading = false
                                                                    Toast.makeText(context, "Erro ao limpar: ${e.message}", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                        }.addOnFailureListener {
                                                            remainingUsersToClean--
                                                            if (remainingUsersToClean == 0) {
                                                                batch.commit().addOnSuccessListener {
                                                                    isLoading = false
                                                                    Toast.makeText(context, "Base de dados limpa com sucesso!", Toast.LENGTH_LONG).show()
                                                                    FirebaseManager.auth.signOut()
                                                                    onBack()
                                                                }
                                                            }
                                                        }
                                                    }.addOnFailureListener {
                                                        remainingUsersToClean--
                                                        if (remainingUsersToClean == 0) {
                                                            batch.commit().addOnSuccessListener {
                                                                isLoading = false
                                                                Toast.makeText(context, "Base de dados limpa com sucesso!", Toast.LENGTH_LONG).show()
                                                                FirebaseManager.auth.signOut()
                                                                onBack()
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }.addOnFailureListener {
                                            isLoading = false
                                        }
                                    }.addOnFailureListener {
                                        isLoading = false
                                    }
                                }
                            ) {
                                Text("SIM, APAGAR TUDO", color = Color.Red, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirmDialog = false }) {
                                Text("CANCELAR", color = Color.White)
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
