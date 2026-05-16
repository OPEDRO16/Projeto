package com.train.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.train.app.data.models.Routine
import com.train.app.navigation.Screen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    var activeWorkoutRoutine by remember { mutableStateOf<Routine?>(null) }

    val bottomItems = listOf(
        BottomNavItem("home", "Home", Icons.Default.Home),
        BottomNavItem("routines", "Routines", Icons.Default.List),
        BottomNavItem(Screen.ExerciseLibrary.route, "Library", Icons.Default.FitnessCenter),
        BottomNavItem("evolution", "Progress", Icons.Default.BarChart),
        BottomNavItem("feed", "Feed", Icons.Default.AccountCircle),
        BottomNavItem("profile", "Profile", Icons.Default.Person),
        BottomNavItem("chat", "AI", Icons.Default.Chat)
    )

    Scaffold(
        bottomBar = {
            val showBottomBar = currentRoute in bottomItems.map { it.route }

            if (showBottomBar && activeWorkoutRoutine == null) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo("home") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") { HomeScreen() }

            composable("routines") {
                RoutinesScreen(
                    onNavigateToEditor = { navController.navigate("routine_editor") },
                    onStartWorkout = { routine: Routine -> activeWorkoutRoutine = routine }
                )
            }

            composable(Screen.ExerciseLibrary.route) {
                ExerciseLibraryScreen(
                    onOpenExercise = { exerciseId ->
                        navController.navigate(Screen.ExerciseLibraryDetail.createRoute(exerciseId))
                    }
                )
            }

            composable("evolution") {
                EvolutionScreen(
                    onOpenExercise = { exerciseName ->
                        val encodedName = URLEncoder.encode(exerciseName, StandardCharsets.UTF_8.toString())
                        navController.navigate(Screen.ExerciseDetail.createRoute(encodedName))
                    }
                )
            }

            composable("feed") { FeedScreen() }

            composable("profile") {
                ProfileScreen(
                    onOpenWorkoutDetail = { sessionId ->
                        navController.navigate(Screen.WorkoutDetail.createRoute(sessionId))
                    },
                    onOpenCalendar = {
                        navController.navigate("workout_calendar")
                    }
                )
            }

            composable("chat") { ChatScreen() }

            composable("routine_editor") {
                RoutineEditorScreen(
                    onSaveComplete = { navController.popBackStack() }
                )
            }

            composable("workout_calendar") {
                WorkoutCalendarScreen(
                    onBack = { navController.popBackStack() },
                    onOpenWorkoutDetail = { sessionId ->
                        navController.navigate(Screen.WorkoutDetail.createRoute(sessionId))
                    }
                )
            }

            composable(
                route = Screen.WorkoutDetail.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId").orEmpty()
                WorkoutDetailScreen(sessionId = sessionId, onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.WorkoutSummary.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId").orEmpty()
                WorkoutSummaryScreen(sessionId = sessionId, onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.ExerciseDetail.route,
                arguments = listOf(navArgument("exerciseName") { type = NavType.StringType })
            ) { backStackEntry ->
                val exerciseName = backStackEntry.arguments?.getString("exerciseName").orEmpty()
                ExerciseDetailScreen(
                    exerciseName = exerciseName,
                    onBack = { navController.popBackStack() },
                    onOpenWorkout = { sessionId ->
                        navController.navigate(Screen.WorkoutDetail.createRoute(sessionId))
                    }
                )
            }

            composable(
                route = Screen.ExerciseLibraryDetail.route,
                arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getString("exerciseId").orEmpty()
                ExerciseLibraryDetailScreen(
                    exerciseId = exerciseId,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        activeWorkoutRoutine?.let { routine ->
            WorkoutTrackerScreen(
                routine = routine,
                onFinish = { sessionId ->
                    activeWorkoutRoutine = null
                    navController.navigate(Screen.WorkoutSummary.createRoute(sessionId))
                }
            )
        }
    }
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)