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
                    MainScreen()
                }
            }
        }
    }
}