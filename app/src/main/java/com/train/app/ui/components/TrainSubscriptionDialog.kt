package com.train.app.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.train.app.data.FirebaseManager
import com.train.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainSubscriptionDialog(
    onDismiss: () -> Unit,
    onSubscriptionSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser = FirebaseManager.auth.currentUser

    // Screen navigation inside subscription flow: 0 = Plan Selection, 1 = Card Checkout, 2 = Success Confetti, 3 = Manage Subscription Dashboard
    var currentStep by remember { mutableStateOf(0) }
    var selectedPlan by remember { mutableStateOf("PRO") } // "PRO" or "MASTER"
    var selectedBillingCycle by remember { mutableStateOf("ANNUAL") } // "MONTHLY" or "ANNUAL"

    // Checkout Fields
    var cardName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var userTier by remember { mutableStateOf("FREE") }

    val planPrice = remember(selectedPlan, selectedBillingCycle) {
        when (selectedPlan) {
            "PRO" -> if (selectedBillingCycle == "ANNUAL") "€29,99/ano" else "€4,99/mês"
            else -> if (selectedBillingCycle == "ANNUAL") "€59,99/ano" else "€9,99/mês"
        }
    }

    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            FirebaseManager.firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        userTier = doc.getString("subscriptionTier") ?: "FREE"
                        if (userTier != "FREE" && currentStep == 0) {
                            currentStep = 3
                        }
                    }
                }
        }
    }

    Dialog(
        onDismissRequest = {
            if (!isProcessing && currentStep != 2) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !isProcessing && currentStep != 2,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .systemBarsPadding()
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "SubscriptionSteps"
            ) { step ->
                when (step) {
                    0 -> PlanSelectionStep(
                        selectedPlan = selectedPlan,
                        onPlanSelect = { selectedPlan = it },
                        selectedCycle = selectedBillingCycle,
                        onCycleSelect = { selectedBillingCycle = it },
                        planPrice = planPrice,
                        onContinue = { currentStep = 1 }
                    )
                    1 -> CheckoutStep(
                        planName = selectedPlan,
                        price = planPrice,
                        cardName = cardName,
                        onCardNameChange = { cardName = it },
                        cardNumber = cardNumber,
                        onCardNumberChange = { cardNumber = it },
                        cardExpiry = cardExpiry,
                        onCardExpiryChange = { cardExpiry = it },
                        cardCvv = cardCvv,
                        onCardCvvChange = { cardCvv = it },
                        isProcessing = isProcessing,
                        onBack = { 
                            if (userTier != "FREE") {
                                currentStep = 3
                            } else {
                                currentStep = 0 
                            }
                        },
                        onPay = {
                            if (cardName.isBlank() || cardNumber.length < 16 || cardExpiry.length < 4 || cardCvv.length < 3) {
                                Toast.makeText(context, "Por favor, preencha os dados do cartão corretamente.", Toast.LENGTH_SHORT).show()
                                return@CheckoutStep
                            }
                            
                            isProcessing = true
                            coroutineScope.launch {
                                delay(2200) // Simulated secure payment gateway handshake
                                if (currentUser != null) {
                                    val updates = mapOf(
                                        "isPremium" to true,
                                        "subscriptionTier" to selectedPlan
                                    )
                                    FirebaseManager.firestore
                                        .collection("users")
                                        .document(currentUser.uid)
                                        .update(updates)
                                        .addOnSuccessListener {
                                            isProcessing = false
                                            currentStep = 2
                                        }
                                        .addOnFailureListener { e ->
                                            isProcessing = false
                                            Toast.makeText(context, "Erro ao gravar subscrição: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                } else {
                                    isProcessing = false
                                    currentStep = 2
                                }
                            }
                        }
                    )
                    2 -> SuccessStep(
                        planName = selectedPlan,
                        onFinish = {
                            onSubscriptionSuccess(selectedPlan)
                            onDismiss()
                        }
                    )
                    3 -> ManageSubscriptionStep(
                        currentTier = userTier,
                        onUpgrade = {
                            selectedPlan = "MASTER"
                            currentStep = 1
                        },
                        onDowngrade = {
                            isProcessing = true
                            FirebaseManager.firestore.collection("users").document(currentUser?.uid ?: "")
                                .update("subscriptionTier", "PRO")
                                .addOnSuccessListener {
                                    isProcessing = false
                                    userTier = "PRO"
                                    Toast.makeText(context, "Downgrade para o plano PRO concluído! ⚡", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    isProcessing = false
                                    Toast.makeText(context, "Erro ao alterar plano: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        },
                        onCancel = {
                            isProcessing = true
                            val updates = mapOf(
                                "isPremium" to false,
                                "subscriptionTier" to "FREE",
                                "appTheme" to "DARK" // Revert to default escuro theme
                            )
                            FirebaseManager.firestore.collection("users").document(currentUser?.uid ?: "")
                                .update(updates)
                                .addOnSuccessListener {
                                    isProcessing = false
                                    userTier = "FREE"
                                    currentThemeName = "DARK"
                                    Toast.makeText(context, "Assinatura cancelada com sucesso. Revertido para FREE.", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                }
                                .addOnFailureListener { e ->
                                    isProcessing = false
                                    Toast.makeText(context, "Erro ao cancelar assinatura: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        },
                        onDismiss = onDismiss
                    )
                }
            }

            // Close Button - Declared last for highest Z-index and perfect clickability
            if (currentStep != 2 && !isProcessing) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .clip(CircleShape)
                        .background(TextWhite.copy(alpha = 0.08f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = TextWhite
                    )
                }
            }
        }
    }
}

// STEP 0: PLAN SELECTION
@Composable
private fun PlanSelectionStep(
    selectedPlan: String,
    onPlanSelect: (String) -> Unit,
    selectedCycle: String,
    onCycleSelect: (String) -> Unit,
    planPrice: String,
    onContinue: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(60.dp)) }

        // Header Title
        item {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AccentYellow.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "MEMBRESIA TRAIN APP",
                    color = AccentYellow,
                    fontWeight = FontWeight.Bold,
                    style = AppTypography.labelSmall
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Desbloqueia o teu Potencial",
                style = AppTypography.headlineLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Black),
                color = TextWhite,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Escolhe o plano científico ideal para o teu progresso.",
                style = AppTypography.bodyLarge,
                color = OutlineBorder,
                textAlign = TextAlign.Center
            )
        }

        // Billing Cycle Selector Toggle
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(SurfaceLevel1)
                    .padding(4.dp)
            ) {
                val isAnnual = selectedCycle == "ANNUAL"
                Surface(
                    color = if (!isAnnual) AccentBlue else Color.Transparent,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.clickable { onCycleSelect("MONTHLY") }
                ) {
                    Text(
                        text = "Mensal",
                        color = if (!isAnnual) TextWhite else OutlineBorder,
                        style = AppTypography.labelMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                Surface(
                    color = if (isAnnual) AccentBlue else Color.Transparent,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.clickable { onCycleSelect("ANNUAL") }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Anual",
                            color = if (isAnnual) TextWhite else OutlineBorder,
                            style = AppTypography.labelMedium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AccentYellow)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "-50%",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // PLANS SELECTION CARD LIST
        item {
            Spacer(modifier = Modifier.height(16.dp))
            // PRO PLAN CARD
            val isPro = selectedPlan == "PRO"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlanSelect("PRO") }
                    .border(
                        width = if (isPro) 2.dp else 1.dp,
                        color = if (isPro) AccentYellow else OutlineBorder,
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPro) SurfaceLevel1 else BackgroundDark
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AccentYellow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PLANO PRO 🥈",
                                style = AppTypography.headlineLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                color = TextWhite
                            )
                        }
                        Text(
                            text = if (selectedCycle == "ANNUAL") "€2.50/mês" else "€4.99/mês",
                            style = AppTypography.labelMedium.copy(fontSize = 15.sp),
                            color = AccentYellow,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Faturado anualmente (€29,99/ano). Ideal para atletas intermédios.",
                        style = AppTypography.bodyMedium,
                        color = OutlineBorder
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Bullet benefits
                    BenefitRow(text = "Anúncios Interstitiais 100% Removidos 🚫")
                    BenefitRow(text = "Até 8 Rotinas na Biblioteca 📋")
                    BenefitRow(text = "Até 3 Rotinas Geradas com IA Guardadas 🤖")
                    BenefitRow(text = "Badge Social Dourado PRO 🌟")
                }
            }
        }

        item {
            // MASTER PLAN CARD
            val isMaster = selectedPlan == "MASTER"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlanSelect("MASTER") }
                    .border(
                        width = if (isMaster) 2.dp else 1.dp,
                        color = if (isMaster) AccentPurple else OutlineBorder,
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMaster) SurfaceLevel1 else BackgroundDark
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AccentPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PLANO MASTER 🥇",
                                style = AppTypography.headlineLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                color = TextWhite
                            )
                        }
                        Text(
                            text = if (selectedCycle == "ANNUAL") "€5.00/mês" else "€9.99/mês",
                            style = AppTypography.labelMedium.copy(fontSize = 15.sp),
                            color = AccentPurple,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Faturado anualmente (€59,99/ano). Acesso total ilimitado de elite.",
                        style = AppTypography.bodyMedium,
                        color = OutlineBorder
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Bullet benefits
                    BenefitRow(text = "Totalmente Livre de Anúncios (Feed + Treinos) 🚫")
                    BenefitRow(text = "Gerações de IA 100% Ilimitadas 🤖")
                    BenefitRow(text = "Rotinas Guardadas Ilimitadas 📋")
                    BenefitRow(text = "Distintivo Cintilante de Mestre MASTER 👑")
                }
            }
        }

        // CTA Continue Button
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedPlan == "PRO") AccentYellow else AccentPurple
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "ASSINAR JÁ - $planPrice",
                    style = AppTypography.labelMedium,
                    color = if (selectedPlan == "PRO") Color.Black else TextWhite,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// STEP 1: CHECKOUT CARD SHEET
@Composable
private fun CheckoutStep(
    planName: String,
    price: String,
    cardName: String,
    onCardNameChange: (String) -> Unit,
    cardNumber: String,
    onCardNumberChange: (String) -> Unit,
    cardExpiry: String,
    onCardExpiryChange: (String) -> Unit,
    cardCvv: String,
    onCardCvvChange: (String) -> Unit,
    isProcessing: Boolean,
    onBack: () -> Unit,
    onPay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(40.dp))
            Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = null,
                tint = if (planName == "PRO") AccentYellow else AccentPurple,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Simulador de Pagamento Seguro",
                style = AppTypography.headlineLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Estás a subscrever o Plano $planName ($price). Insere os dados fictícios do teu cartão de crédito para concluir.",
                style = AppTypography.bodyMedium,
                color = OutlineBorder,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = cardName,
                onValueChange = onCardNameChange,
                label = { Text("Nome do Titular", color = OutlineBorder) },
                enabled = !isProcessing,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (planName == "PRO") AccentYellow else AccentPurple,
                    unfocusedBorderColor = OutlineBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = cardNumber,
                onValueChange = { if (it.length <= 16) onCardNumberChange(it) },
                label = { Text("Número do Cartão (16 dígitos)", color = OutlineBorder) },
                enabled = !isProcessing,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (planName == "PRO") AccentYellow else AccentPurple,
                    unfocusedBorderColor = OutlineBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = cardExpiry,
                    onValueChange = { if (it.length <= 4) onCardExpiryChange(it) },
                    label = { Text("Validade (MMAA)", color = OutlineBorder) },
                    enabled = !isProcessing,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (planName == "PRO") AccentYellow else AccentPurple,
                        unfocusedBorderColor = OutlineBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = cardCvv,
                    onValueChange = { if (it.length <= 3) onCardCvvChange(it) },
                    label = { Text("CVV", color = OutlineBorder) },
                    enabled = !isProcessing,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (planName == "PRO") AccentYellow else AccentPurple,
                        unfocusedBorderColor = OutlineBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Action buttons
        Column(modifier = Modifier.fillMaxWidth()) {
            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = if (planName == "PRO") AccentYellow else AccentPurple)
                }
            } else {
                Button(
                    onClick = onPay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (planName == "PRO") AccentYellow else AccentPurple
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "EFETUAR TRANSAÇÃO",
                        style = AppTypography.labelMedium,
                        color = if (planName == "PRO") Color.Black else TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "VOLTAR",
                    color = OutlineBorder,
                    fontWeight = FontWeight.Bold,
                    style = AppTypography.labelSmall,
                    modifier = Modifier
                        .clickable { onBack() }
                        .align(Alignment.CenterHorizontally)
                        .padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// STEP 2: CONFETTI SUCCESS STATE
@Composable
private fun SuccessStep(
    planName: String,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = if (planName == "PRO") listOf(Color(0xFFFFD700), Color(0xFFFFA500)) else listOf(AccentPurple, Color(0xFF8B5CF6))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = TextWhite,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Subscrição Ativa! 🎉",
            style = AppTypography.headlineLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Black),
            color = TextWhite,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Parabéns! Foste promovido ao Plano $planName. Todos os teus limites de treino e ferramentas de IA foram ajustados com sucesso.",
            style = AppTypography.bodyLarge,
            color = OutlineBorder,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "COMEÇAR A TREINAR",
                style = AppTypography.labelMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BenefitRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color(0xFF4ADE80),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = AppTypography.bodyMedium,
            color = TextPrimary
        )
    }
}

@Composable
private fun ManageSubscriptionStep(
    currentTier: String,
    onUpgrade: () -> Unit,
    onDowngrade: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(60.dp)) }

        // Header Title
        item {
            Text(
                text = "Gerir Plano Ativo",
                style = AppTypography.headlineLarge,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Controla e altera a tua subscrição premium",
                style = AppTypography.bodyMedium,
                color = OutlineBorder,
                textAlign = TextAlign.Center
            )
        }

        // Subscription Card
        item {
            val cardBrush = if (currentTier == "MASTER") {
                Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF3B82F6)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500)))
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, TextWhite.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .background(cardBrush)
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PLANO ATIVO",
                                style = AppTypography.labelSmall,
                                color = if (currentTier == "MASTER") TextWhite.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(TextWhite.copy(alpha = 0.2f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (currentTier == "MASTER") "👑 MASTER" else "★ PRO",
                                    style = AppTypography.labelMedium,
                                    color = if (currentTier == "MASTER") TextWhite else Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (currentTier == "MASTER") "Membro Mestre do Fitness" else "Membro de Elite PRO",
                            style = AppTypography.headlineMedium,
                            color = if (currentTier == "MASTER") TextWhite else Color.Black,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Acesso completo a todas as funcionalidades do teu plano. Obrigado por treinares connosco!",
                            style = AppTypography.bodySmall,
                            color = if (currentTier == "MASTER") TextWhite.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Action Buttons
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentTier == "PRO") {
                    TrainPrimaryButton(
                        text = "FAZER UPGRADE PARA MASTER 👑",
                        onClick = onUpgrade,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (currentTier == "MASTER") {
                    Button(
                        onClick = onDowngrade,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceLevel1,
                            contentColor = TextWhite
                        ),
                        border = BorderStroke(1.dp, OutlineBorder.copy(alpha = 0.3f))
                    ) {
                        Text("REBAIXAR PLANO PARA PRO ⚡", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Text("CANCELAR ASSINATURA (REVERT FREE) ❌", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Voltar ao Aplicativo", color = OutlineBorder, style = AppTypography.labelLarge)
                }
            }
        }
    }
}
