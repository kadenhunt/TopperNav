package edu.wku.toppernav.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = WKURed,
    onPrimary = WKUWhite,
    secondary = WKUGrey,
    onSecondary = WKUWhite,
    background = WKUBlack,
    onBackground = WKUWhite,
    surface = Color(0xFF1C1C1C),
    onSurface = WKUWhite,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = WKUGreyLight
)

private val LightColorScheme = lightColorScheme(
    primary = WKURed,
    onPrimary = WKUWhite,
    secondary = WKUGrey,
    onSecondary = WKUWhite,
    background = WKUWhite,
    onBackground = Color(0xFF1C1B1F),
    surface = WKUWhite,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = WKUGreyLight,
    onSurfaceVariant = WKUGrey
)

@Composable
fun ToppernavTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep brand colors consistent across Android versions
    dynamicColor: Boolean = false,
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
        typography = Typography,
        content = content
    )
}