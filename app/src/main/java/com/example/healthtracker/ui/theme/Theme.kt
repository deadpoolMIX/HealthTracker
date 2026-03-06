package com.example.healthtracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 主题颜色枚举
 */
enum class ThemeColor(val index: Int, val displayName: String) {
    GREEN(0, "绿色"),
    BLUE(1, "蓝色"),
    PURPLE(2, "紫色"),
    ORANGE(3, "橙色"),
    RED(4, "红色")
}

/**
 * 主题模式枚举
 */
enum class ThemeMode(val index: Int, val displayName: String) {
    SYSTEM(0, "跟随系统"),
    LIGHT(1, "浅色"),
    DARK(2, "深色")
}

// Green Color Schemes
private val GreenDarkColorScheme = darkColorScheme(
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

private val GreenLightColorScheme = lightColorScheme(
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

// Blue Color Schemes
private val BlueDarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Color(0xFF003065),
    primaryContainer = Color(0xFF00478F),
    onPrimaryContainer = Blue80,
    secondary = BlueGrey80,
    onSecondary = Color(0xFF1F2F42),
    secondaryContainer = Color(0xFF354559),
    onSecondaryContainer = Color(0xFFBBC8DC),
    tertiary = Teal80,
    onTertiary = Color(0xFF003733),
    tertiaryContainer = Color(0xFF004F4A),
    onTertiaryContainer = Teal80,
    error = Error80,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Error80,
    background = Color(0xFF1A1B20),
    onBackground = Color(0xFFE2E2E8),
    surface = Color(0xFF1A1B20),
    onSurface = Color(0xFFE2E2E8),
    surfaceVariant = Color(0xFF414952),
    onSurfaceVariant = Color(0xFFC1C9D4),
    outline = Color(0xFF8B939E)
)

private val BlueLightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001A3E),
    secondary = BlueGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8E2F0),
    onSecondaryContainer = Color(0xFF111C2B),
    tertiary = Teal40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFA4F3EC),
    onTertiaryContainer = Color(0xFF00201D),
    error = Error40,
    onError = Color.White,
    errorContainer = Error80,
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFAFCFF),
    onBackground = Color(0xFF1A1B20),
    surface = Color(0xFFFAFCFF),
    onSurface = Color(0xFF1A1B20),
    surfaceVariant = Color(0xFFE0E2EC),
    onSurfaceVariant = Color(0xFF414952),
    outline = Color(0xFF71787E)
)

// Purple Color Schemes
private val PurpleDarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = PurpleGrey80,
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Pink80,
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    error = Error80,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Error80,
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99)
)

private val PurpleLightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = PurpleGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Pink40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    error = Error40,
    onError = Color.White,
    errorContainer = Error80,
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E)
)

// Orange Color Schemes
private val OrangeDarkColorScheme = darkColorScheme(
    primary = Orange80,
    onPrimary = Color(0xFF562100),
    primaryContainer = Color(0xFF7A3000),
    onPrimaryContainer = Orange80,
    secondary = OrangeGrey80,
    onSecondary = Color(0xFF2F261D),
    secondaryContainer = Color(0xFF463C31),
    onSecondaryContainer = Color(0xFFDCC8BB),
    tertiary = Teal80,
    onTertiary = Color(0xFF003733),
    tertiaryContainer = Color(0xFF004F4A),
    onTertiaryContainer = Teal80,
    error = Error80,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Error80,
    background = Color(0xFF1C1B1A),
    onBackground = Color(0xFFE6E2E0),
    surface = Color(0xFF1C1B1A),
    onSurface = Color(0xFFE6E2E0),
    surfaceVariant = Color(0xFF504740),
    onSurfaceVariant = Color(0xFFD4C4B7),
    outline = Color(0xFF9C8E83)
)

private val OrangeLightColorScheme = lightColorScheme(
    primary = Orange40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBC9),
    onPrimaryContainer = Color(0xFF2F1500),
    secondary = OrangeGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DDD4),
    onSecondaryContainer = Color(0xFF1F1B17),
    tertiary = Teal40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFA4F3EC),
    onTertiaryContainer = Color(0xFF00201D),
    error = Error40,
    onError = Color.White,
    errorContainer = Error80,
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFCFCFC),
    onBackground = Color(0xFF1C1B1A),
    surface = Color(0xFFFCFCFC),
    onSurface = Color(0xFF1C1B1A),
    surfaceVariant = Color(0xFFF3DFD1),
    onSurfaceVariant = Color(0xFF504740),
    outline = Color(0xFF83786F)
)

// Red Color Schemes
private val RedDarkColorScheme = darkColorScheme(
    primary = Red80,
    onPrimary = Color(0xFF680012),
    primaryContainer = Color(0xFF92001A),
    onPrimaryContainer = Red80,
    secondary = RedGrey80,
    onSecondary = Color(0xFF2F2626),
    secondaryContainer = Color(0xFF463C3C),
    onSecondaryContainer = Color(0xFFDCBBBC),
    tertiary = Teal80,
    onTertiary = Color(0xFF003733),
    tertiaryContainer = Color(0xFF004F4A),
    onTertiaryContainer = Teal80,
    error = Error80,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Error80,
    background = Color(0xFF1C1B1A),
    onBackground = Color(0xFFE6E2E1),
    surface = Color(0xFF1C1B1A),
    onSurface = Color(0xFFE6E2E1),
    surfaceVariant = Color(0xFF524343),
    onSurfaceVariant = Color(0xFFD8C0C0),
    outline = Color(0xFF9E8B8B)
)

private val RedLightColorScheme = lightColorScheme(
    primary = Red40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = RedGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEDE),
    onSecondaryContainer = Color(0xFF1F1B1B),
    tertiary = Teal40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFA4F3EC),
    onTertiaryContainer = Color(0xFF00201D),
    error = Error40,
    onError = Color.White,
    errorContainer = Error80,
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFCFCFC),
    onBackground = Color(0xFF1C1B1A),
    surface = Color(0xFFFCFCFC),
    onSurface = Color(0xFF1C1B1A),
    surfaceVariant = Color(0xFFF4DDDD),
    onSurfaceVariant = Color(0xFF524343),
    outline = Color(0xFF857373)
)

@Composable
fun HealthTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColorIndex: Int = 0,
    content: @Composable () -> Unit
) {
    val darkMode = when {
        darkTheme -> true
        else -> false
    }

    val colorScheme = when (themeColorIndex) {
        ThemeColor.BLUE.index -> if (darkMode) BlueDarkColorScheme else BlueLightColorScheme
        ThemeColor.PURPLE.index -> if (darkMode) PurpleDarkColorScheme else PurpleLightColorScheme
        ThemeColor.ORANGE.index -> if (darkMode) OrangeDarkColorScheme else OrangeLightColorScheme
        ThemeColor.RED.index -> if (darkMode) RedDarkColorScheme else RedLightColorScheme
        else -> if (darkMode) GreenDarkColorScheme else GreenLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkMode
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}