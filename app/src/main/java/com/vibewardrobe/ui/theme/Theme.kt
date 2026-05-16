package com.vibewardrobe.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColorScheme = lightColorScheme(
    primary = DeepLilac,
    onPrimary = Color.White,
    secondary = MistBlue,
    onSecondary = Charcoal,
    secondaryContainer = Mint,
    surface = Color.White,
    onSurface = Charcoal,
    background = OffWhite,
    onBackground = Charcoal,
    surfaceVariant = PastelPink,
    onSurfaceVariant = Charcoal
)

private val DarkColorScheme = darkColorScheme(
    primary = MistBlue,
    onPrimary = Charcoal,
    secondary = PastelPink,
    onSecondary = Charcoal,
    secondaryContainer = DeepLilac,
    surface = Color(0xFF1C1C24),
    onSurface = Color.White,
    background = Color(0xFF121217),
    onBackground = Color.White,
    surfaceVariant = Color(0xFF2C2C36),
    onSurfaceVariant = Mint
)

@Composable
fun VibeWardrobeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = vibeTypography,
        content = content
    )
}

private val vibeFontFamily = FontFamily.SansSerif

val vibeTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = vibeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 26.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = vibeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp
    ),
    titleLarge = TextStyle(
        fontFamily = vibeFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = vibeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = vibeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    labelLarge = TextStyle(
        fontFamily = vibeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
)
