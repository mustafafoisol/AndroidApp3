package com.mustafafoisol.androidapp3.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// The design is a single light treatment, so there is no dark variant to swap in.
private val AppColors = lightColorScheme(
    primary = Orange,
    onPrimary = Surface,
    secondary = Ink,
    onSecondary = Surface,
    background = Canvas,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    outline = BorderLine
)

@Composable
fun AndroidApp3Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColors,
        typography = AppTypography,
        content = content
    )
}
