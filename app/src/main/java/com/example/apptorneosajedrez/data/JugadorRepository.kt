package com.example.apptorneosajedrez.data

import com.example.apptorneosajedrez.model.Jugador
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class JugadorRepository(
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val jugadoresRef = db.collection("jugadores")
    private val usuariosRef = db.collection("usuarios")

    /**
     * Escucha en tiempo real la colección de jugadores.
     */
    fun escucharJugadores(onChange: (List<Jugador>) -> Unit): ListenerRegistration {
        return jugadoresRef
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onChange(emptyList())
                    return@addSnapshotListener
                }

                val jugadores = snapshot.documents.mapNotNull { doc ->
                    doc.toObject<Jugador>()
                }
                onChange(jugadores)
            }
    }

    /**
     * Crea el documento del jugador en la colección "jugadores"
     * y actualiza el campo "tipoUsuario" en la colección "usuarios".
     *
     * IMPORTANTE: es suspend, se debe llamar desde una corrutina.
     */
    suspend fun crearJugador(
        uid: String,
        nombreCompleto: String,
        email: String
    ) {
        val jugador = Jugador(
            id = uid,
            nombre = nombreCompleto,
            email = email
        )

        // Crea/actualiza el documento del jugador
        jugadoresRef
            .document(uid)
            .set(jugador)
            .await()

        // Actualiza el tipoUsuario del documento en "usuarios"
        usuariosRef
            .document(uid)
            .update("tipoUsuario", "JUGADOR")
            .await()
    }

    /**
     * Obtiene un jugador por id (lectura única).
     */
    suspend fun obtenerJugadorPorId(idJugador: String): Jugador? {
        val snapshot = jugadoresRef
            .document(idJugador)
            .get()
            .await()

        return snapshot.toObject<Jugador>()
    }
/*
    *//**
     * Guarda (crea/actualiza) un jugador completo.
     *//*
    suspend fun guardarJugador(jugador: Jugador) {
        jugadoresRef
            .document(jugador.id)
            .set(jugador)
            .await()
    }

    *//**
     * Actualiza sólo el nombre del jugador.
     *//*
    suspend fun actualizarNombreJugador(
        idJugador: String,
        nombreCompleto: String
    ) {
        jugadoresRef
            .document(idJugador)
            .update("nombre", nombreCompleto)
            .await()
    }

    *//**
     * Actualiza el estado de un jugador.
     *//*
    fun actualizarEstadoJugador(id: String, nuevoEstado: String) {
        jugadoresRef
            .document(id)
            .update("estado", nuevoEstado)
    }

    *//**
     * Elimina un jugador por id.
     *//*
    fun eliminarJugador(id: String) {
        jugadoresRef
            .document(id)
            .delete()
    }*/
}
