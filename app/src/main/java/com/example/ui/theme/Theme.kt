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

private val LightColorScheme = DarkColorScheme // Force dark theme for the elegant dark vibe!

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

      else -> DarkColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
