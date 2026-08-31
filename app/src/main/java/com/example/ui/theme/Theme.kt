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

private val DarkColorScheme = darkColorScheme(
  primary = TechBluePrimaryDark,
  onPrimary = TechBlueOnPrimaryDark,
  primaryContainer = TechBlueContainerDark,
  onPrimaryContainer = TechBlueOnContainerDark,
  secondary = SlateSecondaryDark,
  onSecondary = SlateOnSecondaryDark,
  secondaryContainer = SlateSecondaryContainerDark,
  onSecondaryContainer = SlateOnSecondaryContainerDark,
  tertiary = CoralTertiaryDark,
  onTertiary = CoralOnTertiaryDark,
  tertiaryContainer = CoralTertiaryContainerDark,
  onTertiaryContainer = CoralOnTertiaryContainerDark,
  background = DarkBackground,
  surface = DarkSurface,
  surfaceVariant = DarkSurfaceVariant,
  outline = DarkOutline,
  onSurface = DarkOnSurface,
  onSurfaceVariant = DarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
  primary = TechBluePrimary,
  onPrimary = TechBlueOnPrimary,
  primaryContainer = TechBlueContainer,
  onPrimaryContainer = TechBlueOnContainer,
  secondary = SlateSecondary,
  onSecondary = SlateOnSecondary,
  secondaryContainer = SlateSecondaryContainer,
  onSecondaryContainer = SlateOnSecondaryContainer,
  tertiary = CoralTertiary,
  onTertiary = CoralOnTertiary,
  tertiaryContainer = CoralTertiaryContainer,
  onTertiaryContainer = CoralOnTertiaryContainer,
  background = LightBackground,
  surface = LightSurface,
  surfaceVariant = LightSurfaceVariant,
  outline = LightOutline,
  onSurface = LightOnSurface,
  onSurfaceVariant = LightOnSurfaceVariant
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep consistent custom refined palette
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
