package com.train.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import com.train.app.ui.theme.*

@Composable
fun TrainPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = Color.White),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        Text(text = text, style = AppTypography.labelMedium)
    }
}

@Composable
fun TrainSecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(1.dp, TextPrimary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        Text(text = text, style = AppTypography.labelMedium)
    }
}

@Composable
fun TrainCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = SurfaceLevel1,
        border = BorderStroke(1.dp, OutlineBorder)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun TrainChip(text: String, isWarning: Boolean = false) {
    val bgColor = if (isWarning) ChipYellowBg else ChipPurpleBg
    val textColor = if (isWarning) AccentYellow else AccentPurple

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50),
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = AppTypography.labelMedium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun TrainInput(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = OutlineBorder) },
        textStyle = AppTypography.bodyLarge.copy(color = TextPrimary),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentBlue,
            unfocusedBorderColor = OutlineBorder,
            focusedContainerColor = BackgroundDark,
            unfocusedContainerColor = BackgroundDark,
            cursorColor = AccentBlue
        ),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
    )
}

@Composable
fun TrainProgressBar(progress: Float, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier.fillMaxWidth().height(4.dp),
        color = AccentBlue,
        trackColor = Color.White.copy(alpha = 0.1f)
    )
}