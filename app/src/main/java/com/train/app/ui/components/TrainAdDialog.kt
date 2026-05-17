package com.train.app.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.train.app.ui.theme.*
import kotlinx.coroutines.delay

private data class MockAd(
    val brandName: String,
    val headline: String,
    val description: String,
    val ctaText: String,
    val gradientColors: List<Color>,
    val offerCode: String
)

private val MOCK_ADS = listOf(
    MockAd(
        brandName = "GYMSHARK 🦈",
        headline = "ELEVATE YOUR TRAINING",
        description = "Obtém até 30% de desconto em todo o vestuário de treino e calções premium. Tecido ultra-leve sem costuras.",
        ctaText = "COMPRAR AGORA",
        gradientColors = listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)),
        offerCode = "SHARK30"
    ),
    MockAd(
        brandName = "NIKE ⚡",
        headline = "JUST DO IT. FOREVER.",
        description = "Os novos ténis Nike Metcon estão desenhados para estabilidade máxima nos teus agachamentos e saltos de caixa.",
        ctaText = "VER MODELO",
        gradientColors = listOf(Color(0xFFE11D48), Color(0xFFFB7185)),
        offerCode = "METCON20"
    ),
    MockAd(
        brandName = "OPTIMUM NUTRITION 🎖️",
        headline = "GOLD STANDARD WHEY",
        description = "A proteína de soro de leite nº 1 do mundo. Alimenta a reconstrução muscular e acelera a tua recuperação.",
        ctaText = "COMPRAR PROTEÍNA",
        gradientColors = listOf(Color(0xFF047857), Color(0xFF34D399)),
        offerCode = "GOLDWHEY"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainAdDialog(
    onDismiss: () -> Unit,
    onUpgradeClick: () -> Unit
) {
    val context = LocalContext.current
    var secondsLeft by remember { mutableStateOf(5) }
    
    // Choose a random ad for variety on launch
    val ad = remember { MOCK_ADS.random() }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft -= 1
        }
    }

    Dialog(
        onDismissRequest = {
            if (secondsLeft == 0) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = secondsLeft == 0,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
        ) {
            // Background glow matching the brand colors
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                ad.gradientColors.first().copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .systemBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header (Ad indicator + close button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = TextWhite.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, TextWhite.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = "ANÚNCIO",
                            color = OutlineBorder,
                            style = AppTypography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (secondsLeft > 0) {
                        Surface(
                            color = AccentYellow.copy(alpha = 0.15f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "${secondsLeft}s",
                                color = AccentYellow,
                                fontWeight = FontWeight.Bold,
                                style = AppTypography.labelMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(TextWhite.copy(alpha = 0.12f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar Anúncio",
                                tint = TextWhite
                            )
                        }
                    }
                }

                // Middle Area (Mock Ad creative Card)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 32.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLevel1),
                    border = BorderStroke(1.dp, OutlineBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Brand Logo Icon Area
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        colors = ad.gradientColors
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = TextWhite,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = ad.brandName,
                                style = AppTypography.labelMedium.copy(fontSize = 13.sp),
                                color = AccentYellow,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = ad.headline,
                                style = AppTypography.headlineLarge.copy(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    lineHeight = 30.sp
                                ),
                                color = TextWhite,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = ad.description,
                                style = AppTypography.bodyLarge,
                                color = OutlineBorder,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }

                        // Promo Badge + CTA Button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = TextWhite.copy(alpha = 0.06f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, TextWhite.copy(alpha = 0.1f)),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Text(
                                    text = "CÓDIGO PROMO: ${ad.offerCode}",
                                    color = TextWhite,
                                    style = AppTypography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    Toast.makeText(
                                        context,
                                        "A redirecionar para o site oficial da ${ad.brandName.substringBefore(" ")}... 🌐",
                                        Toast.LENGTH_LONG
                                    ).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ad.gradientColors.first()
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = ad.ctaText,
                                    style = AppTypography.labelMedium,
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Footer (Paywall direct upgrade shortcut)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cansado de publicidade nos teus treinos?",
                        color = OutlineBorder,
                        style = AppTypography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "REMOVER ANÚNCIOS COM PREMIUM 👑",
                        color = AccentYellow,
                        fontWeight = FontWeight.Bold,
                        style = AppTypography.labelSmall,
                        modifier = Modifier
                            .clickable {
                                onDismiss()
                                onUpgradeClick()
                            }
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                }
            }
        }
    }
}
