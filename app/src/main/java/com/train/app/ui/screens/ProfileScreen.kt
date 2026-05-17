package com.train.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.train.app.data.FirebaseManager
import com.train.app.data.models.UserProfile
import com.train.app.data.models.WorkoutSession
import com.train.app.ui.components.UserAvatar
import androidx.compose.ui.graphics.Brush
import com.train.app.ui.theme.AccentBlue
import com.train.app.ui.theme.AccentYellow
import com.train.app.ui.theme.AccentPurple
import com.train.app.ui.theme.AppTypography
import com.train.app.ui.theme.BackgroundDark
import com.train.app.ui.theme.OutlineBorder
import com.train.app.ui.theme.SurfaceLevel1
import com.train.app.ui.theme.TextPrimary
import com.train.app.ui.theme.TextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onOpenCalendar: () -> Unit = {},
    onOpenWorkoutDetail: (String) -> Unit = {},
    onOpenEditProfile: () -> Unit = {},
    onOpenFriends: () -> Unit = {},
    onOpenExerciseLibrary: () -> Unit = {},
    onOpenSubscriptionPaywall: () -> Unit = {}
) {
    val currentUser = FirebaseManager.auth.currentUser
    var sessions by remember { mutableStateOf<List<WorkoutSession>>(emptyList()) }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(currentUser != null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser?.uid) {
        if (currentUser == null) {
            isLoading = false
            return@LaunchedEffect
        }

        // Load profile + sessions
        FirebaseManager.firestore
            .collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { doc ->
                userProfile = doc.toObject(UserProfile::class.java)?.apply { id = doc.id }
                
                FirebaseManager.firestore
                    .collection("users")
                    .document(currentUser.uid)
                    .collection("sessions")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        sessions = snapshot.toObjects(WorkoutSession::class.java)
                            .sortedByDescending { it.startTime }
                        isLoading = false
                    }
                    .addOnFailureListener { error ->
                        errorMessage = error.message ?: "Erro ao carregar treinos"
                        isLoading = false
                    }
            }
            .addOnFailureListener { error ->
                errorMessage = error.message ?: "Erro ao carregar perfil"
                isLoading = false
            }
    }

    if (currentUser == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Text("Sem utilizador autenticado", color = AccentYellow)
        }
        return
    }

    val username = userProfile?.name?.ifBlank { null } 
        ?: currentUser.displayName 
        ?: currentUser.email?.substringBefore("@")?.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        } ?: "Atleta"

    val emailNick = currentUser.email?.substringBefore("@") ?: "user"

    val totalWorkouts = sessions.size
    val seguindoCount = userProfile?.friends?.size ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(emailNick, style = AppTypography.headlineLarge.copy(fontSize = 20.sp)) },
                actions = {
                    IconButton(onClick = onOpenFriends) {
                        BadgedBox(
                            badge = {
                                val requestsCount = userProfile?.friendRequests?.size ?: 0
                                if (requestsCount > 0) {
                                    Badge(
                                        containerColor = Color.Red,
                                        contentColor = TextWhite
                                    ) {
                                        Text("$requestsCount")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.People, contentDescription = "Amigos e Pedidos", tint = TextWhite)
                        }
                    }
                    IconButton(onClick = onOpenEditProfile) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = TextWhite)
                    }
                    IconButton(onClick = { FirebaseManager.auth.signOut() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Encerrar Sessão", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextWhite
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        photoUrl = userProfile?.photoUrl,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(username, style = AppTypography.headlineLarge.copy(fontSize = 20.sp), color = TextWhite)
                            
                            val tier = userProfile?.subscriptionTier ?: "FREE"
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
                                        color = TextWhite,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally)
                        ) {
                            ProfileStatItem("Treinamentos", totalWorkouts.toString())
                            ProfileStatItem("Amigos", seguindoCount.toString(), onClick = onOpenFriends)
                        }
                    }
                }
            }

            // Seja Premium Card (Upsell Card)
            val currentTier = userProfile?.subscriptionTier ?: "FREE"
            if (currentTier != "MASTER") {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable { onOpenSubscriptionPaywall() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                        border = BorderStroke(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(AccentYellow, AccentPurple)
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SEJA PREMIUM 👑",
                                    color = AccentYellow,
                                    style = AppTypography.headlineLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Black)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (currentTier == "FREE") 
                                        "Desbloqueia IA ilimitada, até 8 rotinas, remove anúncios e ganha um badge PRO dourado!"
                                        else "Faz upgrade para MASTER! IA e rotinas 100% ilimitadas com zero anúncios!",
                                    color = TextWhite.copy(alpha = 0.8f),
                                    style = AppTypography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentYellow)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "VER PLANOS",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    style = AppTypography.labelSmall
                                )
                            }
                        }
                    }
                }
            }


            // Painel
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PanelCard(
                        icon = Icons.Default.FitnessCenter,
                        title = "Exercícios",
                        modifier = Modifier.weight(1f),
                        onClick = onOpenExerciseLibrary
                    )
                    PanelCard(
                        icon = Icons.Default.CalendarMonth,
                        title = "Calendário",
                        modifier = Modifier.weight(1f),
                        onClick = onOpenCalendar
                    )
                }
            }

            // Treinamentos
            item {
                Text(
                    text = "Treinamentos", 
                    style = AppTypography.labelLarge, 
                    color = OutlineBorder,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            when {
                isLoading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AccentBlue)
                        }
                    }
                }
                sessions.isEmpty() -> {
                    item {
                        Text(
                            text = "Ainda não existem treinos concluídos.", 
                            color = OutlineBorder,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                else -> {
                    items(sessions) { session ->
                        HevyWorkoutHistoryCard(
                            session = session,
                            username = emailNick,
                            photoUrl = userProfile?.photoUrl,
                            onClick = { onOpenWorkoutDetail(session.id) }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ProfileStatItem(label: String, value: String, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.clickable(enabled = onClick != {}) { onClick() }
    ) {
        Text(label, style = AppTypography.labelSmall, color = OutlineBorder)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = TextWhite
        )
    }
}

@Composable
private fun PanelCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceLevel1)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = TextWhite)
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, style = AppTypography.bodyLarge, color = TextWhite)
        }
    }
}

@Composable
private fun HevyWorkoutHistoryCard(
    session: WorkoutSession,
    username: String,
    photoUrl: String?,
    onClick: () -> Unit
) {
    val date = remember(session.startTime) {
        SimpleDateFormat("EEEE, MMM dd, yyyy", Locale("pt", "PT")).format(Date(session.startTime)).replaceFirstChar { it.titlecase() }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header (Avatar + Name + Date)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(
                    photoUrl = photoUrl,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = username, style = AppTypography.bodyLarge, color = TextWhite)
                    Text(text = date, style = AppTypography.labelSmall, color = OutlineBorder)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = session.routineName.ifBlank { "Treino" },
            style = AppTypography.headlineLarge.copy(fontSize = 18.sp),
            color = TextWhite
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Column {
                Text("Tempo", style = AppTypography.labelSmall, color = OutlineBorder)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${session.durationMinutes}m", style = AppTypography.bodyLarge, color = TextWhite)
            }
            Column {
                Text("Volume", style = AppTypography.labelSmall, color = OutlineBorder)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${session.totalVolume.toInt()} kg", style = AppTypography.bodyLarge, color = TextWhite)
            }
            Column {
                Text("Recordes", style = AppTypography.labelSmall, color = OutlineBorder)
                Spacer(modifier = Modifier.height(4.dp))
                Text("0", style = AppTypography.bodyLarge, color = TextWhite) // Mock PRs for now
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = SurfaceLevel1)
    }
}