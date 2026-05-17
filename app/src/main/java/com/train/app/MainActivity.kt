package com.train.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.google.firebase.FirebaseApp
import com.train.app.ui.screens.MainScreen
import com.train.app.ui.theme.BackgroundDark
import com.train.app.ui.theme.TrainTheme // Certifica-te que o nome do teu tema é este

import androidx.compose.runtime.*
import com.train.app.ui.screens.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. CRITICAL: Inicializar o Firebase explicitamente antes de carregar a UI
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            // Se falhar aqui, o problema é o google-services.json ausente
            e.printStackTrace()
        }

        setContent {
            // Usa o teu tema customizado definido no Theme.kt
            TrainTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    var isUserLoggedIn by remember { mutableStateOf(com.train.app.data.FirebaseManager.auth.currentUser != null) }
                    
                    // Listen to auth state changes to dynamically switch screens on logout
                    LaunchedEffect(Unit) {
                        com.train.app.data.FirebaseManager.auth.addAuthStateListener { auth ->
                            isUserLoggedIn = auth.currentUser != null
                        }
                    }

                    if (isUserLoggedIn) {
                        MainScreen()
                    } else {
                        LoginScreen(onLoginSuccess = { isUserLoggedIn = true })
                    }
                }
            }
        }
    }
}