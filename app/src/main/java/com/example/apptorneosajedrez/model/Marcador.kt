package com.example.apptorneosajedrez.model

enum class Categoria {
    COMERCIO,
    TORNEO
}
data class Marcador(
    val id: String = "",
    val nombre: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val categoria: Categoria = Categoria.COMERCIO,
    val descripcion: String? = null,
    val descuento: Int? = null
)


