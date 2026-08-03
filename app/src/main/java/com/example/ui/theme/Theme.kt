package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryIndigo,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryCyan,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryAmber,
    tertiaryContainer = TertiaryContainerDark,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    onPrimary = OnPrimaryWhite,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = Color(0xFF312E81),
    secondary = SecondaryCyan,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF075985),
    tertiary = TertiaryAmber,
    tertiaryContainer = Color(0xFFFEF3C7),
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant
)

@Composable
fun NexusAiTheme(
    themeMode: String = "SYSTEM",
    accentColor: Color = Color(0xFF4F46E5),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val darkColorScheme = darkColorScheme(
        primary = accentColor,
        onPrimary = OnPrimaryWhite,
        primaryContainer = accentColor.copy(alpha = 0.25f),
        onPrimaryContainer = Color.White,
        secondary = SecondaryCyan,
        secondaryContainer = SecondaryContainerDark,
        onSecondaryContainer = OnSecondaryContainerLight,
        tertiary = TertiaryAmber,
        tertiaryContainer = TertiaryContainerDark,
        background = DarkBackground,
        surface = DarkSurface,
        surfaceVariant = DarkSurfaceVariant,
        onBackground = DarkOnSurface,
        onSurface = DarkOnSurface,
        onSurfaceVariant = DarkOnSurfaceVariant
    )

    val lightColorScheme = lightColorScheme(
        primary = accentColor,
        onPrimary = OnPrimaryWhite,
        primaryContainer = accentColor.copy(alpha = 0.12f),
        onPrimaryContainer = accentColor,
        secondary = SecondaryCyan,
        secondaryContainer = Color(0xFFE0F2FE),
        onSecondaryContainer = Color(0xFF075985),
        tertiary = TertiaryAmber,
        tertiaryContainer = Color(0xFFFEF3C7),
        background = LightBackground,
        surface = LightSurface,
        surfaceVariant = LightSurfaceVariant,
        onBackground = LightOnSurface,
        onSurface = LightOnSurface,
        onSurfaceVariant = LightOnSurfaceVariant
    )

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
