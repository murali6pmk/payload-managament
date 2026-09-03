package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val LocalThemeIsDark = compositionLocalOf { true }

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryIndigoLight,
    onPrimary = Color.White,
    primaryContainer = PrimaryIndigoDark,
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = SecondaryTeal,
    onSecondary = Color.White,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFF334155),
    onBackground = Slate50,
    onSurface = Slate50,
    onSurfaceVariant = Slate300,
    outline = Slate600,
    outlineVariant = Slate700
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryIndigo,
    onPrimary = Color.White,
    primaryContainer = PrimaryIndigoContainer,
    onPrimaryContainer = OnPrimaryIndigoContainer,
    secondary = SecondarySlate,
    onSecondary = Color.White,
    background = HighDensityBg, // #F3F6FA
    surface = Color.White,
    surfaceVariant = Slate50,
    onBackground = Slate900,
    onSurface = Slate900,
    onSurfaceVariant = Slate500,
    outline = Slate300,
    outlineVariant = Slate100
  )

@Composable
fun CloudAttendTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  CompositionLocalProvider(LocalThemeIsDark provides darkTheme) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  CloudAttendTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}


