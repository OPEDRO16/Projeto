package com.train.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.train.app.ui.screens.MainScreen
import com.train.app.ui.theme.TrainTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrainTheme {
                MainScreen()
            }
        }
    }
}