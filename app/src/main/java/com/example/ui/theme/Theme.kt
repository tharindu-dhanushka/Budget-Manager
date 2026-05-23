package com.example.ui.theme

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

private val DarkColorScheme =
  darkColorScheme(
    primary = ElegantPurpleAccent,
    onPrimary = ElegantPurpleOnAccent,
    primaryContainer = ElegantPurpleDeep,
    secondary = ElegantPositiveAccent,
    secondaryContainer = ElegantPositiveSurface,
    background = ElegantDarkBg,
    surface = ElegantDarkSurface,
    onBackground = ElegantDarkOnSurface,
    onSurface = ElegantDarkOnSurface,
    outline = ElegantDarkBorder
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF2E7D32),
    secondaryContainer = Color(0xFFE8F5E9),
    background = Color(0xFFFAF9FD),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1D1F),
    onSurface = Color(0xFF1C1D1F),
    outline = Color(0xFF79747E)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme by default
  dynamicColor: Boolean = false, // Disable dynamic colors so our elegant dark theme shines
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      else -> if (darkTheme) DarkColorScheme else LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
