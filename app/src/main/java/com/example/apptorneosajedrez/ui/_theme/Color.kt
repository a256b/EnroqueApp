package com.example.apptorneosajedrez.ui._theme

import androidx.compose.ui.graphics.Color

// =========================
// Paleta base (de colors.xml)
// =========================
val Marino      = Color(0xFF123458)  // @color/marino
val Noche       = Color(0xFF030303)  // @color/noche
val Niebla      = Color(0xFFF1EFEC)  // @color/niebla
val Arena       = Color(0xFFD4C9BE)  // @color/arena
val Oro         = Color(0xFFEFBF04)  // @color/oro
val Rojo        = Color(0xFF550000)  // @color/rojo

val Teal200     = Color(0xFF03DAC5)  // @color/teal_200
val Teal700     = Color(0xFF018786)  // @color/teal_700

val Blanco      = Color(0xFFFFFFFF)  // @color/white
val Negro       = Color(0xFF000000)  // @color/black

// =========================
// Colores principales (para el tema Material3)
// =========================

// Equivalentes a themes.xml
val Primary         = Marino          // colorPrimary
val PrimaryDark     = Noche           // colorPrimaryVariant
val OnPrimary       = Niebla          // colorOnPrimary

val Secondary       = Teal200         // colorSecondary
val SecondaryVariant= Teal700         // colorSecondaryVariant
val OnSecondary     = Negro           // colorOnSecondary

// Fondo y superficie (puedes ajustar a gusto)
val Background      = Blanco          // Fondo general (similar a @color/white)
val Surface         = Niebla          // Superficie de tarjetas / paneles

// Error
val ErrorRed        = Rojo            // Usamos @color/rojo como color de error

// =========================
// Colores de estado de torneo (de colors.xml)
// =========================
val EstadoTorneoActivo      = Color(0xFF4CAF50) // @color/estado_torneo_activo
val EstadoTorneoFinalizado  = Color(0xFF9E9E9E) // @color/estado_torneo_finalizado
val EstadoTorneoSuspendido  = Color(0xFFF44336) // @color/estado_torneo_suspendido
val EstadoTorneoProximo     = Color(0xFF00BCD4) // @color/estado_torneo_proximo

// =========================
// Colores por defecto de plantilla (si algo de Compose los sigue usando)
// =========================

val Purple80      = Color(0xFFD0BCFF)
val PurpleGrey80  = Color(0xFFCCC2DC)
val Pink80        = Color(0xFFEFB8C8)

val Purple40      = Color(0xFF6650A4)
val PurpleGrey40  = Color(0xFF625B71)
val Pink40        = Color(0xFF7D5260)
