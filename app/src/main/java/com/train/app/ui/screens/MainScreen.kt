package com.train.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            // Esconde a barra no Login e durante o Treino Ativo para foco total
            if (currentRoute != Screen.Login.route && activeWorkoutRoutine == null) {
                BottomNavBar(navController)
            }
        },
        containerColor = BackgroundDark
    ) { innerPadding ->
        NavHost(navController, Screen.Login.route, Modifier.padding(innerPadding)) {
            composable(Screen.Login.route) {
                LoginScreen { navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } } }
            }
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Feed.route) { FeedScreen() }
            composable(Screen.Routines.route) {
                RoutinesScreen(onStartWorkout = { routine ->
                    activeWorkoutRoutine = routine
                })
            }
            composable(Screen.Chat.route) { ChatScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
        }

        // Overlay do Workout Tracker (Inicia quando activeWorkoutRoutine não é nulo)
        activeWorkoutRoutine?.let { routine ->
            WorkoutTrackerScreen(
                routine = routine,
                onFinish = { activeWorkoutRoutine = null } // Volta ao estado normal
            )
        }
    }
}

@Composable
fun BottomNavBar(x0: NavHostController) {
    TODO("Not yet implemented")
}