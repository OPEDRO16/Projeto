package com.train.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.train.app.data.FirebaseManager
import com.train.app.data.models.Routine
import com.train.app.navigation.Screen
import com.train.app.ui.theme.AccentBlue
import com.train.app.ui.theme.BackgroundDark
import com.train.app.ui.theme.OutlineBorder
import com.train.app.ui.theme.SurfaceLevel1

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var activeWorkoutRoutine by remember { mutableStateOf<Routine?>(null) }
    var currentUser by remember { mutableStateOf(FirebaseManager.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        FirebaseManager.auth.addAuthStateListener(listener)
        onDispose {
            FirebaseManager.auth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            activeWorkoutRoutine = null
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

            val showBottomBar = currentUser != null &&
                    currentRoute != Screen.Login.route &&
                    currentRoute != Screen.RoutineEditor.route &&
                    activeWorkoutRoutine == null

            if (showBottomBar) {
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
                        launchSingleTop = true
                    }
                }
            }

            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Feed.route) { FeedScreen() }
            composable(Screen.Evolution.route) { EvolutionScreen() }
            composable(Screen.Chat.route) { ChatScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }

            composable(Screen.Routines.route) {
                RoutinesScreen(
                    onStartWorkout = { routine ->
                        activeWorkoutRoutine = routine
                    },
                    onNavigateToEditor = {
                        navController.navigate(Screen.RoutineEditor.route)
                    }
                )
            }

            composable(Screen.RoutineEditor.route) {
                RoutineEditorScreen(
                    onSaveComplete = {
                        navController.popBackStack()
                    }
                )
            }
        }

        activeWorkoutRoutine?.let { routine ->
            WorkoutTrackerScreen(
                routine = routine,
                onFinish = {
                    activeWorkoutRoutine = null
                }
            )
        }
    }
}

@Composable
private fun BottomNavBar(navController: NavHostController) {
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
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(item.title)
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

private data class NavItem(
    val route: String,
    val icon: ImageVector,
    val title: String
)