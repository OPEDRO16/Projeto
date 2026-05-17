package com.train.app.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.train.app.ui.theme.*
import android.graphics.ImageDecoder
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip

/**
 * Botão Principal: Azul (#0A62D0) com texto branco.
 * Raio de 4px conforme o DESIGN.md.
 */
@Composable
fun TrainPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
        shape = AppShapes.small,
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        Text(text = text.uppercase(), style = AppTypography.labelMedium, color = TextWhite)
    }
}

/**
 * Botão Secundário: Contorno branco/cinza.
 */
@Composable
fun TrainSecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(1.dp, TextPrimary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
        shape = AppShapes.small,
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        Text(text = text.uppercase(), style = AppTypography.labelMedium, color = TextPrimary)
    }
}

/**
 * Card do Sistema: Usa SurfaceLevel1 (#252324) e raio de 8px.
 */
@Composable
fun TrainCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier,
        shape = AppShapes.medium,
        color = SurfaceLevel1,
        border = BorderStroke(1.dp, OutlineBorder)
    ) {
        Box(Modifier.padding(16.dp)) { content() }
    }
}

/**
 * Chip de Categoria/Aviso: Cores Purple ou Yellow a 20% de opacidade.
 */
@Composable
fun TrainChip(text: String, isWarning: Boolean = false) {
    val bgColor = if (isWarning) ChipYellowBg else ChipPurpleBg
    val textColor = if (isWarning) AccentYellow else AccentPurple

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            style = AppTypography.labelSmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * Input de Texto: Fundo escuro (#191718) e borda azul no foco.
 */
@Composable
fun TrainInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = OutlineBorder) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentBlue,
            unfocusedBorderColor = OutlineBorder,
            cursorColor = AccentBlue,
            focusedContainerColor = BackgroundDark,
            unfocusedContainerColor = BackgroundDark,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        textStyle = AppTypography.bodyLarge,
        shape = AppShapes.small,
        modifier = modifier.heightIn(min = 48.dp)
    )
}

/**
 * Barra de Progresso: Altura de 4px, fundo 10% branco, preenchimento AccentBlue.
 */
@Composable
fun TrainProgressBar(progress: Float, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp),
        color = AccentBlue,
        trackColor = Color.White.copy(alpha = 0.1f),
        strokeCap = StrokeCap.Round
    )
}

/**
 * Reusable Avatar Loader that supports both ContentResolver (local picking) and Remote URLs.
 */
@Composable
fun UserAvatar(
    photoUrl: String?,
    modifier: Modifier = Modifier,
    placeholderIcon: ImageVector = Icons.Default.Person,
    iconSizePercent: Float = 0.5f
) {
    val context = LocalContext.current
    var bitmapState by remember(photoUrl) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(photoUrl) {
        if (!photoUrl.isNullOrBlank() && (photoUrl.startsWith("content://") || photoUrl.startsWith("file://"))) {
            val loadedBitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val uri = Uri.parse(photoUrl)
                decodeUriSafely(context, uri, maxDimension = 512)
            }
            bitmapState = loadedBitmap
        } else {
            bitmapState = null
        }
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(SurfaceLevel1),
        contentAlignment = Alignment.Center
    ) {
        val currentBitmap = bitmapState
        if (currentBitmap != null) {
            Image(
                bitmap = currentBitmap.asImageBitmap(),
                contentDescription = "Avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = placeholderIcon,
                contentDescription = "Avatar Placeholder",
                tint = OutlineBorder,
                modifier = Modifier.fillMaxSize(iconSizePercent)
            )
        }
    }
}

/**
 * Safely decodes a Uri to a Bitmap downsampled to max dimension to avoid OutOfMemory or Canvas too large exceptions.
 */
fun decodeUriSafely(context: android.content.Context, uri: Uri, maxDimension: Int = 1080): Bitmap? {
    return try {
        // Strip query parameters for opening streams
        val cleanUri = if (uri.toString().contains("?")) {
            Uri.parse(uri.toString().substringBefore("?"))
        } else {
            uri
        }

        // Read EXIF orientation
        val rotationDegrees = try {
            context.contentResolver.openInputStream(cleanUri)?.use { stream ->
                val exif = android.media.ExifInterface(stream)
                val orientation = exif.getAttributeInt(
                    android.media.ExifInterface.TAG_ORIENTATION,
                    android.media.ExifInterface.ORIENTATION_NORMAL
                )
                when (orientation) {
                    android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }

        val options = android.graphics.BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(cleanUri)?.use { stream ->
            android.graphics.BitmapFactory.decodeStream(stream, null, options)
        }

        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1
        if (width > maxDimension || height > maxDimension) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= maxDimension && (halfWidth / inSampleSize) >= maxDimension) {
                inSampleSize *= 2
            }
        }

        val decodeOptions = android.graphics.BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
            inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
        }
        
        val decodedBitmap = context.contentResolver.openInputStream(cleanUri)?.use { stream ->
            android.graphics.BitmapFactory.decodeStream(stream, null, decodeOptions)
        }

        if (decodedBitmap != null && rotationDegrees != 0) {
            try {
                val matrix = android.graphics.Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                val rotated = android.graphics.Bitmap.createBitmap(
                    decodedBitmap, 0, 0, decodedBitmap.width, decodedBitmap.height, matrix, true
                )
                if (rotated != decodedBitmap) {
                    decodedBitmap.recycle()
                }
                rotated
            } catch (e: Exception) {
                decodedBitmap
            }
        } else {
            decodedBitmap
        }
    } catch (e: Exception) {
        null
    }
}