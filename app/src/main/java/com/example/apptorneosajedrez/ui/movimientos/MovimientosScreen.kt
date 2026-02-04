package com.example.apptorneosajedrez.ui.movimientos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.apptorneosajedrez.R
import com.example.apptorneosajedrez.model.MovimientoFila
import com.example.apptorneosajedrez.ui._theme.AppTorneosTheme

@Composable
fun MovimientosScreen(
    uiState: MovimientosUiState,
    modifier: Modifier = Modifier,
    onMovimientoChange: (String) -> Unit,
    onBorrarTextoClick: () -> Unit,
    onDeshacerMovimientoClick: () -> Unit,
    onEnviarMovimientoClick: () -> Unit,
) {
    val movimientos = uiState.filasMovimientos
    val usuarioPuedeEditar = uiState.usuarioPuedeCargarMovimientos

    val colors = rememberMovimientosColors()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        MovimientosListCard(
            movimientos = movimientos,
            colors = colors,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        TurnIndicator(
            esTurnoBlancas = uiState.colorTurno == "BLANCAS",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (usuarioPuedeEditar) {
            MovimientoEditorSection(
                uiState = uiState,
                colors = colors,
                onMovimientoChange = onMovimientoChange,
                onBorrarTextoClick = onBorrarTextoClick,
                onDeshacerMovimientoClick = onDeshacerMovimientoClick,
                onEnviarMovimientoClick = onEnviarMovimientoClick
            )
        }
    }
}

/**
 * Agrupa los colores usados en la pantalla para evitar repetir llamadas al theme.
 */
private data class MovimientosColors(
    val fondoRectangulo: Color,
    val filaClara: Color,
    val filaOscura: Color,
    val titulo: Color,
    val texto: Color
)

@Composable
private fun rememberMovimientosColors(): MovimientosColors = MovimientosColors(
    fondoRectangulo = MaterialTheme.colorScheme.surface,
    filaClara = MaterialTheme.colorScheme.surface,
    filaOscura = MaterialTheme.colorScheme.surfaceVariant,
    titulo = MaterialTheme.colorScheme.primary,
    texto = MaterialTheme.colorScheme.onSurface
)

/**
 * Card que contiene la lista de movimientos.
 */
@Composable
private fun MovimientosListCard(
    movimientos: List<MovimientoFila>,
    colors: MovimientosColors,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.fondoRectangulo
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            EncabezadoMovimientos(colors.titulo)
            HorizontalDivider()
            ListaMovimientos(
                movimientos = movimientos,
                colorFilaClara = colors.filaClara,
                colorFilaOscura = colors.filaOscura,
                colorTexto = colors.texto
            )
        }
    }
}

/**
 * Sección inferior para carga de movimientos (solo organizador).
 */
@Composable
private fun MovimientoEditorSection(
    uiState: MovimientosUiState,
    colors: MovimientosColors,
    onBorrarTextoClick: () -> Unit,
    onDeshacerMovimientoClick: () -> Unit,
    onEnviarMovimientoClick: () -> Unit,
    onMovimientoChange: (String) -> Unit
) {

    MovimientoInputRow(
        movimiento = uiState.nuevoMovimiento,
        onClearMovimiento = onBorrarTextoClick
    )

    Spacer(modifier = Modifier.height(8.dp))

    ChessKeyboard(
        onKeyClick = { token ->
            val base = uiState.nuevoMovimiento
            val newText = if (base.isBlank()) token else base + token
            onMovimientoChange(newText)
        }
    )

    Spacer(modifier = Modifier.height(8.dp))

    BottomActionBar(
        onDeshacerClick = onDeshacerMovimientoClick,
        onEnviarClick = onEnviarMovimientoClick,
        enviarEnabled = uiState.botonEnviarMovimientoHabilitado
    )
}


/**
 * Barra con Deshacer (izquierda) y Enviar (derecha).
 */
@Composable
private fun BottomActionBar(
    onDeshacerClick: () -> Unit,
    onEnviarClick: () -> Unit,
    enviarEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Button(
            modifier = Modifier.weight(1f),
            onClick = onDeshacerClick,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.undo),
                contentDescription = "Deshacer",
                modifier = Modifier.size(30.dp)
            )
        }

        Button(
            modifier = Modifier.weight(1f),
            onClick = onEnviarClick,
            enabled = enviarEnabled
        ) {
            Icon(
                painter = painterResource(id = R.drawable.send),
                contentDescription = "Enviar",
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun MovimientoInputRow(
    movimiento: String,
    onClearMovimiento: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Display del movimiento (no editable, no abre teclado)
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = MaterialTheme.shapes.small,
            tonalElevation = 2.dp,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                val estaVacio = movimiento.isBlank()

                Text(
                    text = if (estaVacio) "Ingresa el movimiento" else movimiento,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (estaVacio)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Botón de borrar todo el movimiento
        IconButton(
            onClick = onClearMovimiento,
            enabled = movimiento.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Borrar movimiento"
            )
        }
    }
}


/**
 * Indicador de a quién le toca mover.
 */
@Composable
private fun TurnIndicator(
    esTurnoBlancas: Boolean,
    modifier: Modifier = Modifier
) {
    val (bg, content) =
        if (esTurnoBlancas) {
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        }

    val texto = "Turno de " + if (esTurnoBlancas) "Blancas" else "Negras"
    val ficha = if (esTurnoBlancas) "♙" else "♟︎"

    Surface(
        color = bg,
        contentColor = content,
        shape = RoundedCornerShape(4.dp),
        tonalElevation = 1.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = ficha, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}


@Composable
private fun EncabezadoMovimientos(colorTitulo: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#",
            modifier = Modifier.weight(0.15f),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = colorTitulo
        )
        Text(
            text = "Blancas",
            modifier = Modifier.weight(0.425f),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = colorTitulo
        )
        Text(
            text = "Negras",
            modifier = Modifier.weight(0.425f),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = colorTitulo
        )
    }
}

@Composable
private fun ListaMovimientos(
    movimientos: List<MovimientoFila>,
    colorFilaClara: Color,
    colorFilaOscura: Color,
    colorTexto: Color
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(movimientos) { index, movimiento ->
            val fondo = if (index % 2 == 0) colorFilaClara else colorFilaOscura

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(fondo)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${movimiento.numero}.",
                    modifier = Modifier.weight(0.15f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorTexto
                )
                Text(
                    text = movimiento.blancas,
                    modifier = Modifier.weight(0.425f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorTexto
                )
                Text(
                    text = movimiento.negras.orEmpty(),
                    modifier = Modifier.weight(0.425f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorTexto,
                )
            }
        }
    }
}

@Composable
fun ChessKeyboard(
    onKeyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filaPiezas = listOf("R", "D", "T", "A", "C")
    //     val filaPiezas = listOf("K", "Q", "R", "B", "N")
    val filaColumnas = listOf("a", "b", "c", "d", "e", "f", "g", "h")
    val filaRenglones = listOf("1", "2", "3", "4", "5", "6", "7", "8")
    val filaEspeciales = listOf("x", "O-O", "O-O-O", "=", "+", "#")

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KeyboardRow(keys = filaPiezas, onKeyClick = onKeyClick)
        KeyboardRow(keys = filaColumnas, onKeyClick = onKeyClick)
        KeyboardRow(keys = filaRenglones, onKeyClick = onKeyClick)
        KeyboardRow(keys = filaEspeciales, onKeyClick = onKeyClick)
    }
}

@Composable
private fun KeyboardRow(
    keys: List<String>,
    onKeyClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        keys.forEach { key ->
            OutlinedButton(
                onClick = { onKeyClick(key) },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(
                    text = key,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ---------------------------------------------------------------------
// PREVIEW
// ---------------------------------------------------------------------
@Preview(showBackground = true, showSystemUi = true, name = "MovimientosScreen Preview")
@Composable
fun MovimientosScreenPreview() {
    AppTorneosTheme {
        MovimientosScreen(
            uiState = MovimientosUiState(),
            onMovimientoChange = {},
            onBorrarTextoClick = {},
            onDeshacerMovimientoClick = {},
            onEnviarMovimientoClick = {}
        )
    }
}
