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

private val LightColorScheme = lightColorScheme(
    primary = LibraryForestGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4E8DC),
    onPrimaryContainer = LibraryForestGreen,
    secondary = LibraryAmber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFECCF),
    onSecondaryContainer = Color(0xFF4C2A00),
    tertiary = LibrarySage,
    onTertiary = Color.White,
    background = LibraryCream,
    onBackground = TextPrimary,
    surface = LibraryParchment,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFE5DFD4),
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFFB3ADA3)
)

private val DarkColorScheme = darkColorScheme(
    primary = LibraryMint,
    onPrimary = Color.White,
    primaryContainer = LibraryEmerald,
    onPrimaryContainer = Color(0xFFE8F5E9),
    secondary = LibraryGold,
    onSecondary = Color(0xFF3E2000),
    secondaryContainer = LibraryTerracotta,
    onSecondaryContainer = Color(0xFFFFECCF),
    tertiary = LibrarySage,
    onTertiary = Color.White,
    background = LibraryDarkBg,
    onBackground = Color(0xFFE5EDE8),
    surface = LibraryDarkSurface,
    onSurface = Color(0xFFE5EDE8),
    surfaceVariant = LibraryDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBAC7C0),
    outline = Color(0xFF4C5D54)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep our crafted literary aesthetic
    content: @Composable () -> Unit,
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
        typography = Typography,
        content = content
    )
}
