package com.train.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Chat
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

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    var activeWorkoutRoutine by remember { mutableStateOf<Routine?>(null) }

    val bottomItems = listOf(
        BottomNavItem("feed", "Início", Icons.Default.Home),
        BottomNavItem("treino", "Treino", Icons.Default.FitnessCenter),
        BottomNavItem("chat", "Chat", Icons.Default.Chat),
        BottomNavItem("profile", "Perfil", Icons.Default.Person)
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
                                        popUpTo("feed") { saveState = true }
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
            startDestination = "feed",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("feed") { 
                FeedScreen(
                    onOpenPostComments = { postId ->
                        navController.navigate(Screen.PostComments.createRoute(postId))
                    },
                    onOpenWorkoutDetail = { sessionId, postUserId ->
                        navController.navigate(Screen.WorkoutSummary.createRoute(sessionId, postUserId))
                    }
                ) 
            }

            composable("treino") {
                WorkoutDashboardScreen(
                    onStartWorkout = { routine ->
                        activeWorkoutRoutine = routine ?: Routine(name = "Treino Livre")
                    },
                    onNavigateToEditor = { navController.navigate("routine_editor") },
                    onNavigateToEditRoutine = { routineId ->
                        navController.navigate("routine_editor/$routineId")
                    }
                )
            }

            composable("chat") {
                ChatScreen(
                    onOpenChat = { roomId ->
                        navController.navigate("conversation/$roomId")
                    }
                )
            }


            composable("profile") {
                ProfileScreen(
                    onOpenWorkoutDetail = { sessionId ->
                        navController.navigate(Screen.WorkoutDetail.createRoute(sessionId))
                    },
                    onOpenCalendar = {
                        navController.navigate("workout_calendar")
                    },
                    onOpenEditProfile = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    onOpenFriends = {
                        navController.navigate(Screen.Friends.route)
                    },
                    onOpenExerciseLibrary = {
                        navController.navigate(Screen.ExerciseLibrary.route)
                    }
                )
            }

            composable("routine_editor") {
                RoutineEditorScreen(
                    onSaveComplete = { navController.popBackStack() }
                )
            }

            composable(
                route = "routine_editor/{routineId}",
                arguments = listOf(navArgument("routineId") { type = NavType.StringType })
            ) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getString("routineId")
                RoutineEditorScreen(
                    routineId = routineId,
                    onSaveComplete = { navController.popBackStack() }
                )
            }

            composable(Screen.ExerciseLibrary.route) {
                ExerciseLibraryScreen(
                    onOpenExercise = { exerciseId ->
                        navController.navigate(Screen.ExerciseLibraryDetail.createRoute(exerciseId))
                    },
                    onBack = { navController.popBackStack() }
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
                arguments = listOf(
                    navArgument("sessionId") { type = NavType.StringType },
                    navArgument("userId") { 
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId").orEmpty()
                val targetUserId = backStackEntry.arguments?.getString("userId")
                WorkoutSummaryScreen(
                    sessionId = sessionId, 
                    targetUserId = targetUserId,
                    onBack = {
                        val popped = navController.popBackStack("treino", inclusive = false)
                        if (!popped) {
                            navController.popBackStack()
                        }
                    },
                    onNavigateToCreatePost = { sId ->
                        navController.navigate(Screen.CreatePost.createRoute(sId))
                    }
                )
            }

            composable(
                route = Screen.CreatePost.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId").orEmpty()
                CreatePostScreen(
                    sessionId = sessionId,
                    onBack = { navController.popBackStack() },
                    onPostCreated = {
                        // Pop backstack to reset 'treino' tab to root dashboard
                        navController.popBackStack("treino", inclusive = false)
                        
                        // After posting, navigate to feed
                        navController.navigate("feed") {
                            popUpTo("feed") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(
                route = Screen.PostComments.route,
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId").orEmpty()
                PostCommentsScreen(
                    postId = postId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.EditProfile.route) {
                EditProfileScreen(onBack = { navController.popBackStack() })
            }

            composable(route = Screen.Friends.route) {
                FriendsScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = "conversation/{roomId}",
                arguments = listOf(navArgument("roomId") { type = NavType.StringType })
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getString("roomId").orEmpty()
                ConversationScreen(
                    roomId = roomId,
                    onBack = { navController.popBackStack() },
                    onOpenPostComments = { postId ->
                        navController.navigate(Screen.PostComments.createRoute(postId))
                    },
                    onOpenWorkoutDetail = { sessionId, userId ->
                        navController.navigate(Screen.WorkoutSummary.createRoute(sessionId, userId))
                    }
                )
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