package com.train.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.train.app.navigation.Screen
import com.train.app.data.FirebaseManager
import com.train.app.ui.theme.*
import com.train.app.data.models.Routine

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var activeWorkoutRoutine by remember { mutableStateOf<Routine?>(null) }
    var currentUser by remember { mutableStateOf(FirebaseManager.auth.currentUser) }

    // Sincronização do estado de autenticação para redirecionamento e UI
    DisposableEffect(Unit) {
        val listener = { auth: com.google.firebase.auth.FirebaseAuth ->
            currentUser = auth.currentUser
        }
        FirebaseManager.auth.addAuthStateListener(listener)
        onDispose { FirebaseManager.auth.removeAuthStateListener(listener) }
    }

    // Monitorização global do Logout
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // Oculta a barra de navegação no Login, no Editor e durante o Treino Ativo
            if (currentUser != null &&
                currentRoute != Screen.Login.route &&
                currentRoute != Screen.RoutineEditor.route &&
                activeWorkoutRoutine == null) {
                BottomNavBar(navController)
            }
        },
        containerColor = BackgroundDark
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (currentUser != null) Screen.Home.route else Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Feed.route) { FeedScreen() }
            composable(Screen.Routines.route) {
                RoutinesScreen(
                    onStartWorkout = { routine -> activeWorkoutRoutine = routine },
                    onNavigateToEditor = { navController.navigate(Screen.RoutineEditor.route) }
                )
            }
            composable(Screen.RoutineEditor.route) {
                RoutineEditorScreen(onSaveComplete = {
                    navController.popBackStack()
                })
            }
            composable(Screen.Evolution.route) { EvolutionScreen() }
            composable(Screen.Chat.route) { ChatScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
        }

        // Overlay do Workout Tracker (HUD de Performance)
        activeWorkoutRoutine?.let { routine ->
            WorkoutTrackerScreen(
                routine = routine,
                onFinish = { activeWorkoutRoutine = null }
            )
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(
        NavItem(Screen.Home.route, Icons.Default.Home, "Home"),
        NavItem(Screen.Evolution.route, Icons.Default.Timeline, "Evolução"),
        NavItem(Screen.Routines.route, Icons.Default.FitnessCenter, "Treinar"),
        NavItem(Screen.Chat.route, Icons.Default.ChatBubble, "Chat"),
        NavItem(Screen.Profile.route, Icons.Default.Person, "Perfil")
    )

    NavigationBar(containerColor = SurfaceLevel1) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentBlue,
                    unselectedIconColor = OutlineBorder,
                    selectedTextColor = AccentBlue,
                    unselectedTextColor = OutlineBorder,
                    indicatorColor = SurfaceLevel1
                )
            )
        }
    }
}

data class NavItem(val route: String, val icon: ImageVector, val title: String)