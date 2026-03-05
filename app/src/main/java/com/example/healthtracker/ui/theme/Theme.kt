package com.example.healthtracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = Color(0xFF003A12),
    primaryContainer = Color(0xFF005319),
    onPrimaryContainer = Green80,
    secondary = GreenGrey80,
    onSecondary = Color(0xFF213529),
    secondaryContainer = Color(0xFF374B3F),
    onSecondaryContainer = Color(0xFFBFCFBC),
    tertiary = Teal80,
    onTertiary = Color(0xFF003733),
    tertiaryContainer = Color(0xFF004F4A),
    onTertiaryContainer = Teal80,
    error = Error80,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Error80,
    background = Color(0xFF191C1A),
    onBackground = Color(0xFFE1E3DE),
    surface = Color(0xFF191C1A),
    onSurface = Color(0xFFE1E3DE),
    surfaceVariant = Color(0xFF414942),
    onSurfaceVariant = Color(0xFFC1C9BE),
    outline = Color(0xFF8B9389)
)

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8F5A8),
    onPrimaryContainer = Color(0xFF002203),
    secondary = GreenGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4E8C8),
    onSecondaryContainer = Color(0xFF0F2618),
    tertiary = Teal40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFA4F3EC),
    onTertiaryContainer = Color(0xFF00201D),
    error = Error40,
    onError = Color.White,
    errorContainer = Error80,
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFCFDF7),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFFCFDF7),
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFDEE5D9),
    onSurfaceVariant = Color(0xFF414942),
    outline = Color(0xFF727970)
)

@Composable
fun HealthTrackerTheme(
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}