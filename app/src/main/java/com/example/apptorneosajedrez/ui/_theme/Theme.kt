package com.example.apptorneosajedrez.ui._theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography


import androidx.compose.runtime.Composable

val Typography = Typography()

private val LightColors = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Tertiary,
    onPrimary = OnPrimary,
    onSecondary = OnSecondary,
    onTertiary = OnTertiary,
    onBackground = OnBackground,
    onSurface = OnSurface,
    onError = OnError,
    onPrimaryContainer = OnPrimaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = Background,
    surface = Surface,
    error = ErrorRed
)

@Composable
fun AppTorneosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
