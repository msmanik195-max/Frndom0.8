package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.repository.AppSettingsRepository

private val LightColorScheme =
  lightColorScheme(
    primary = FrndomPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7F3FF),
    onPrimaryContainer = Color(0xFF003087),
    secondary = FrndomSecondary,
    onSecondary = Color.White,
    tertiary = FrndomTertiary,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    outline = BorderLight,
    outlineVariant = Color(0xFFE4E6EB),
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = FrndomPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = Color(0xFF90CAF9),
    secondary = FrndomSecondary,
    onSecondary = Color.White,
    tertiary = FrndomTertiary,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2C2C2C),
    outline = Color(0xFF3E3E3E),
    outlineVariant = Color(0xFF333333),
    onBackground = Color(0xFFEDEDED),
    onSurface = Color(0xFFEDEDED),
    onSurfaceVariant = Color(0xFFB0B3B8),
  )

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  val context = LocalContext.current
  val appSettingsRepo = remember { AppSettingsRepository.getInstance(context) }
  val isDarkMode by appSettingsRepo.isDarkMode.collectAsState()

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = !isDarkMode
        insetsController.isAppearanceLightNavigationBars = !isDarkMode
      }
    }
  }

  MaterialTheme(
    colorScheme = if (isDarkMode) DarkColorScheme else LightColorScheme,
    typography = Typography,
    content = content
  )
}
