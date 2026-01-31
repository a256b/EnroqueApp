package com.example.apptorneosajedrez.data

import com.example.apptorneosajedrez.model.EstadoTorneo
import com.example.apptorneosajedrez.model.Torneo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FieldValue

class TorneoRepository {

    private val db = FirebaseFirestore.getInstance()

    fun escucharTorneos(cuandoCambia: (List<Torneo>) -> Unit): ListenerRegistration {
        return db.collection("torneos").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                cuandoCambia(emptyList())
                return@addSnapshotListener
            }
            val torneos = snapshot.documents.mapNotNull { doc ->
                doc.toObject<Torneo>()?.copy(idTorneo = doc.id)
            }
            cuandoCambia(torneos)
        }
    }

    fun agregarTorneo(torneo: Torneo, onComplete: (Boolean, String?) -> Unit) {
        // Genero doc con ID automático
        val docRef = db.collection("torneos").document()
        val idGenerado = docRef.id

        // Inserto el ID dentro del objeto
        val torneoConId = torneo.copy(idTorneo = idGenerado)

        // Guardo
        docRef.set(torneoConId)
            .addOnSuccessListener { onComplete(true, idGenerado) }
            .addOnFailureListener { onComplete(false, null) }
    }

    fun actualizarEstadoTorneo(idTorneo: String, nuevoEstado: EstadoTorneo, onComplete: (Boolean) -> Unit) {
        if (idTorneo.isEmpty()) {
            onComplete(false)
            return
        }
        db.collection("torneos").document(idTorneo)
            .update("estado", nuevoEstado.name)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Agrega el ID de un jugador a la lista de participantes del torneo.
     */
    fun agregarJugadorATorneo(idTorneo: String, idJugador: String, onComplete: (Boolean) -> Unit) {
        if (idTorneo.isEmpty() || idJugador.isEmpty()) {
            onComplete(false)
            return
        }
        db.collection("torneos").document(idTorneo)
            .update("jugadores", FieldValue.arrayUnion(idJugador))
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
