package com.train.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.train.app.navigation.Screen
import com.train.app.data.FirebaseManager
import com.train.app.ui.theme.AccentBlue
import com.train.app.ui.theme.BackgroundDark
import com.train.app.ui.theme.OutlineBorder
import com.train.app.ui.theme.SurfaceLevel1
import com.train.app.ui.theme.TextPrimary

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    // Verifica se existe utilizador logado para definir ecrã inicial
    val currentUser = FirebaseManager.auth.currentUser
    val startDestination = if (currentUser != null) Screen.Home.route else Screen.Login.route

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // Oculta barra de navegação no ecrã de Login
            if (currentRoute != Screen.Login.route) {
                BottomNavBar(navController = navController)
            }
        },
        containerColor = BackgroundDark
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Feed.route) { FeedScreen() }
            composable(Screen.Routines.route) { RoutinesScreen() }
            composable(Screen.Chat.route) { ChatScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(
        NavItem(Screen.Home.route, Icons.Default.Home, "Home"),
        NavItem(Screen.Feed.route, Icons.Default.DynamicFeed, "Feed"),
        NavItem(Screen.Routines.route, Icons.Default.FitnessCenter, "Train"),
        NavItem(Screen.Chat.route, Icons.Default.ChatBubble, "Chat"),
        NavItem(Screen.Profile.route, Icons.Default.Person, "Profile")
    )

    NavigationBar(
        containerColor = SurfaceLevel1,
        contentColor = OutlineBorder
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
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