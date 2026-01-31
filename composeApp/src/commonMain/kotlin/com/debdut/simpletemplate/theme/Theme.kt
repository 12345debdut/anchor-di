package com.debdut.simpletemplate.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Primary brand color — deep teal. */
private val Teal700 = Color(0xFF00897B)

/** Lighter primary for containers and chips. */
private val Teal50 = Color(0xFFE0F2F1)

/** Dark accent for secondary actions. */
private val Amber700 = Color(0xFFFF8F00)

/** Surface and background — warm off-white. */
private val SurfaceLight = Color(0xFFF5F5F5)

/** Card and elevated surface. */
private val SurfaceVariantLight = Color(0xFFFFFFFF)

private val ProductAppColorScheme = lightColorScheme(
    primary = Teal700,
    onPrimary = Color.White,
    primaryContainer = Teal50,
    onPrimaryContainer = Teal700,
    secondary = Amber700,
    onSecondary = Color.White,
    surface = SurfaceLight,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFE7E0EC),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
)

/**
 * App theme with a custom [ColorScheme] (teal primary, warm surfaces).
 * Use this as the root theme in [App].
 */
@Composable
fun ProductAppTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ProductAppColorScheme,
        content = content,
    )
}
