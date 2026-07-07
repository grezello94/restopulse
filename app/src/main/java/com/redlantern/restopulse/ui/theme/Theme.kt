package com.redlantern.restopulse.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = LanternRed,
    secondary = Jade,
    tertiary = Gold,
    background = Rice,
    surface = Rice,
    surfaceVariant = Smoke,
    primaryContainer = Coral,
    onPrimaryContainer = LanternRedDark,
    onPrimary = Color.White,
    onBackground = Ink,
    onSurface = Ink,
    secondaryContainer = Mint,
    tertiaryContainer = Amber,
    outlineVariant = Smoke
)

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFFFB4AB),
    secondary = androidx.compose.ui.graphics.Color(0xFF7FDBB6),
    tertiary = androidx.compose.ui.graphics.Color(0xFFE8C36A),
    background = Color(0xFF171211),
    surface = Color(0xFF171211),
    surfaceVariant = Color(0xFF514442),
    primaryContainer = Color(0xFF7F1612),
    onPrimaryContainer = Color(0xFFFFDAD5)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp)
)

@Composable
fun RestoPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme: ColorScheme =
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
