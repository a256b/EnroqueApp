package com.example.apptorneosajedrez.ui.compose_theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = Background,
    surface = Surface,
    error = ErrorRed
)

@Composable
fun AppTorneosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
//        typography = AppTypography,
//        shapes = AppShapes,
        content = content
    )
}
