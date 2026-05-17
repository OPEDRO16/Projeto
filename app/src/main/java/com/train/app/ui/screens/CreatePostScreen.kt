package com.train.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ArrowBack
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
import com.train.app.data.FirebaseManager
import com.train.app.data.models.Post
import com.train.app.data.models.WorkoutSession
import com.train.app.data.models.UserProfile
import com.train.app.ui.components.TrainAdDialog
import com.train.app.ui.components.TrainSubscriptionDialog
import com.train.app.ui.components.TrainInput
import com.train.app.ui.components.TrainPrimaryButton
import com.train.app.ui.components.TrainSecondaryButton
import com.train.app.ui.theme.*
import java.util.UUID
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore

@Composable
fun CreatePostScreen(
    sessionId: String,
    onBack: () -> Unit,
    onPostCreated: () -> Unit
) {
    val context = LocalContext.current
    var description by remember { mutableStateOf("") }
    var workoutNameInput by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf("public") } // "public" or "friends"
    var isPosting by remember { mutableStateOf(false) }
    
    var session by remember { mutableStateOf<WorkoutSession?>(null) }
    var isLoadingSession by remember { mutableStateOf(true) }

    val currentUser = FirebaseManager.auth.currentUser
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var showAdInterstitial by remember { mutableStateOf(false) }
    var showSubscriptionPaywall by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            FirebaseManager.firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        userProfile = doc.toObject(UserProfile::class.java)?.apply { id = doc.id }
                    }
                }
        }
    }


    var imageUrl by remember { mutableStateOf("") }
    var bitmapState by remember(imageUrl) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(imageUrl) {
        if (!imageUrl.isNullOrBlank() && (imageUrl.startsWith("content://") || imageUrl.startsWith("file://"))) {
            val loadedBitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val uri = Uri.parse(imageUrl)
                com.train.app.ui.components.decodeUriSafely(context, uri, maxDimension = 1080)
            }
            bitmapState = loadedBitmap
        } else {
            bitmapState = null
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val postFile = java.io.File(context.filesDir, "post_pic_${UUID.randomUUID()}.jpg")
                    val outputStream = java.io.FileOutputStream(postFile)
                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    imageUrl = Uri.fromFile(postFile).toString() + "?t=${System.currentTimeMillis()}"
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao guardar foto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(sessionId) {
        if (currentUser != null) {
            FirebaseManager.firestore
                .collection("users")
                .document(currentUser.uid)
                .collection("sessions")
                .document(sessionId)
                .get()
                .addOnSuccessListener { snapshot ->
                    val s = snapshot.toObject(WorkoutSession::class.java)
                    session = s
                    workoutNameInput = s?.routineName?.ifBlank { "Treinamento" } ?: "Treinamento"
                    isLoadingSession = false
                }
                .addOnFailureListener {
                    isLoadingSession = false
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Nova Partilha",
                style = AppTypography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoadingSession) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentBlue)
            }
        } else if (session == null) {
            Text("Erro ao carregar os dados do treino.", color = AccentYellow)
            Spacer(modifier = Modifier.height(16.dp))
            TrainSecondaryButton(text = "VOLTAR", onClick = onBack, modifier = Modifier.fillMaxWidth())
        } else {
            // Nome do Treino
            Text("NOME DO TREINO", style = AppTypography.labelMedium, color = OutlineBorder)
            Spacer(modifier = Modifier.height(8.dp))
            TrainInput(
                value = workoutNameInput,
                onValueChange = { workoutNameInput = it },
                placeholder = "Ex: Treino de Perna, Fullbody, etc."
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Descrição
            Text("O que tens a dizer sobre este treino?", style = AppTypography.labelMedium, color = OutlineBorder)
            Spacer(modifier = Modifier.height(8.dp))
            TrainInput(
                value = description,
                onValueChange = { description = it },
                placeholder = "Grande pump hoje! 💪"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Real Image Selection
            Text("IMAGEM (Opcional)", style = AppTypography.labelMedium, color = OutlineBorder)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceLevel1)
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val currentBitmap = bitmapState
                if (currentBitmap != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            bitmap = currentBitmap.asImageBitmap(),
                            contentDescription = "Foto selecionada",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .clickable { imageUrl = "" }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("REMOVER", color = Color.Red, style = AppTypography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Image, contentDescription = "Adicionar Imagem", tint = AccentBlue, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Adicionar foto do treino", style = AppTypography.labelSmall, color = AccentBlue)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Visibilidade
            Text("VISIBILIDADE", style = AppTypography.labelMedium, color = OutlineBorder)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                VisibilityOption(
                    title = "Público",
                    icon = Icons.Default.Public,
                    isSelected = visibility == "public",
                    onClick = { visibility = "public" },
                    modifier = Modifier.weight(1f)
                )
                VisibilityOption(
                    title = "Amigos",
                    icon = Icons.Default.People,
                    isSelected = visibility == "friends",
                    onClick = { visibility = "friends" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TrainSecondaryButton(
                    text = "CANCELAR",
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                )
                TrainPrimaryButton(
                    text = if (isPosting) "A PUBLICAR..." else "PARTILHAR",
                    onClick = {
                        if (!isPosting && currentUser != null) {
                            isPosting = true
                            val post = Post(
                                id = UUID.randomUUID().toString(),
                                userId = currentUser.uid,
                                userName = currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "Atleta",
                                description = description,
                                category = "Treino",
                                workoutSessionId = sessionId,
                                workoutName = workoutNameInput.ifBlank { "Treino" },
                                workoutVolume = session!!.totalVolume,
                                workoutDuration = session!!.durationMinutes,
                                visibility = visibility,
                                likedBy = emptyList(),
                                commentsCount = 0,
                                imageUrl = imageUrl
                            )

                            FirebaseManager.firestore.collection("posts").document(post.id).set(post)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Partilhado no Feed!", Toast.LENGTH_SHORT).show()
                                    val tier = userProfile?.subscriptionTier ?: "FREE"
                                    if (tier == "FREE") {
                                        showAdInterstitial = true
                                    } else {
                                        onPostCreated()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    isPosting = false
                                    Toast.makeText(context, "Erro ao partilhar: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showSubscriptionPaywall) {
        TrainSubscriptionDialog(
            onDismiss = { showSubscriptionPaywall = false },
            onSubscriptionSuccess = { newTier ->
                // Sincronizado automaticamente!
            }
        )
    }

    if (showAdInterstitial) {
        TrainAdDialog(
            onDismiss = {
                showAdInterstitial = false
                onPostCreated()
            },
            onUpgradeClick = {
                showSubscriptionPaywall = true
            }
        )
    }
}

@Composable
private fun VisibilityOption(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else SurfaceLevel1)
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = if (isSelected) AccentBlue else OutlineBorder)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                title, 
                style = AppTypography.labelMedium, 
                color = if (isSelected) AccentBlue else OutlineBorder
            )
        }
    }
}
