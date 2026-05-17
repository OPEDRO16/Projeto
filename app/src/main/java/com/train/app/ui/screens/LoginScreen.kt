package com.train.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.train.app.data.FirebaseManager
import com.train.app.data.models.UserProfile
import com.train.app.ui.components.*
import com.train.app.ui.theme.*

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("TRAIN", style = AppTypography.displayLarge, color = AccentBlue)
        Spacer(Modifier.height(32.dp))

        TrainInput(value = email, onValueChange = { email = it }, placeholder = "Email")
        Spacer(Modifier.height(16.dp))
        TrainInput(value = password, onValueChange = { password = it }, placeholder = "Palavra-passe")

        if (errorMessage != null) {
            Text(errorMessage!!, color = AccentYellow, style = AppTypography.labelSmall, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator(color = AccentBlue)
        } else {
            TrainPrimaryButton(
                text = "ENTRAR",
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Por favor, preencha o email e a palavra-passe."
                    } else {
                        isLoading = true
                        errorMessage = null
                        FirebaseManager.auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                val user = FirebaseManager.auth.currentUser
                                if (user != null) {
                                    val userRef = FirebaseManager.firestore.collection("users").document(user.uid)
                                    userRef.get().addOnSuccessListener { doc ->
                                        if (!doc.exists()) {
                                            val newProfile = UserProfile(
                                                id = user.uid,
                                                name = user.displayName ?: user.email?.substringBefore("@") ?: "Atleta",
                                                email = user.email ?: ""
                                            )
                                            userRef.set(newProfile).addOnCompleteListener {
                                                isLoading = false
                                                onLoginSuccess()
                                            }
                                        } else {
                                            isLoading = false
                                            onLoginSuccess()
                                        }
                                    }.addOnFailureListener {
                                        isLoading = false
                                        onLoginSuccess() // Failsafe
                                    }
                                } else {
                                    isLoading = false
                                    onLoginSuccess()
                                }
                            }
                            .addOnFailureListener {
                                isLoading = false
                                errorMessage = "Erro: ${it.localizedMessage}"
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            TrainSecondaryButton(
                text = "CRIAR CONTA",
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Por favor, preencha o email e a palavra-passe."
                    } else if (password.length < 6) {
                        errorMessage = "A palavra-passe deve ter pelo menos 6 caracteres."
                    } else {
                        isLoading = true
                        errorMessage = null
                        FirebaseManager.auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                val user = FirebaseManager.auth.currentUser
                                if (user != null) {
                                    val newProfile = UserProfile(
                                        id = user.uid,
                                        name = user.email?.substringBefore("@") ?: "Atleta",
                                        email = user.email ?: ""
                                    )
                                    FirebaseManager.firestore.collection("users").document(user.uid)
                                        .set(newProfile)
                                        .addOnCompleteListener {
                                            isLoading = false
                                            onLoginSuccess()
                                        }
                                } else {
                                    isLoading = false
                                    onLoginSuccess()
                                }
                            }
                            .addOnFailureListener {
                                isLoading = false
                                errorMessage = "Erro ao criar: ${it.localizedMessage}"
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}