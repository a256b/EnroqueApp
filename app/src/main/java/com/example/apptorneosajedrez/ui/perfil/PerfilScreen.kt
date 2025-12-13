package com.example.apptorneosajedrez.ui.perfil

import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun PerfilScreen() {
    Text(
        "Hola Mundo desde Jetpack compose",
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.wrapContentWidth(
            Alignment.CenterHorizontally
        )
    )
}
