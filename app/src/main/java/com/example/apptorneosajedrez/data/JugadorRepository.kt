package com.example.apptorneosajedrez.data

import com.example.apptorneosajedrez.model.Jugador
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject

class JugadorRepository {
    private val db = FirebaseFirestore.getInstance()

    fun escucharJugadores(onChange: (List<Jugador>) -> Unit): ListenerRegistration {
        return db.collection("jugadores")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onChange(emptyList())
                    return@addSnapshotListener
                }

                val jugadores = snapshot.documents.mapNotNull { doc ->
                    //val jugador = doc.toObject<Jugador>()
                    doc.toObject<Jugador>()

                    // Usar el campo 'id' cargado manualmente, no el document.id
                    //if (jugador != null && jugador.id.isNotBlank()) {
                    //    jugador
                    //} else {
                    //    null
                    //}
                }
                onChange(jugadores)
            }
    }

    fun crearJugador(uid: String, nombreCompleto: String, email: String){
        val jugador = Jugador(id=uid, nombre=nombreCompleto, email=email)
        db.collection("jugadores").document(uid).set(jugador)
    }

    //fun eliminarJugador(uid: String){
    //    db.collection("jugadores").document(uid).delete()
    //}

    fun actualizarEstadoJugador(id: String, nuevoEstado: String) {
        db.collection("jugadores")
            .whereEqualTo("id", id)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    doc.reference.update("estado", nuevoEstado)
                }
            }
    }

    fun eliminarJugador(id: String) {
        db.collection("jugadores")
            .whereEqualTo("id", id)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    doc.reference.delete()
                }
            }
    }
}
