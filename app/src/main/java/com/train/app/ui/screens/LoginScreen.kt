package com.train.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.train.app.ui.components.*
import com.train.app.ui.theme.*
import com.train.app.data.FirebaseManager

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TRAIN",
            style = AppTypography.displayLarge,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(48.dp))

        TrainInput(
            value = email,
            onValueChange = { email = it },
            placeholder = "EMAIL"
        )

        Spacer(modifier = Modifier.height(16.dp))

        TrainInput(
            value = password,
            onValueChange = { password = it },
            placeholder = "PASSWORD"
        )

        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = error, color = AccentYellow, style = AppTypography.labelMedium)
        }

        Spacer(modifier = Modifier.height(32.dp))

        TrainPrimaryButton(
            text = "LOGIN",
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    FirebaseManager.auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener { onLoginSuccess() }
                        .addOnFailureListener { error = it.message ?: "Erro" }
                } else {
                    error = "Dados vazios"
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TrainSecondaryButton(
            text = "REGISTER",
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    FirebaseManager.auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { onLoginSuccess() }
                        .addOnFailureListener { error = it.message ?: "Erro" }
                } else {
                    error = "Dados vazios"
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}