package com.example.apptorneosajedrez.data

import com.example.apptorneosajedrez.model.Inscripcion
import com.example.apptorneosajedrez.model.EstadoInscripcion
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject

class InscripcionRepository {
    private val db = FirebaseFirestore.getInstance()
    private val inscripcionesRef = db.collection("inscripciones")

    fun escucharInscripciones(onChange: (List<Inscripcion>) -> Unit): ListenerRegistration {
        return inscripcionesRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                onChange(emptyList())
                return@addSnapshotListener
            }

            val lista = snapshot.documents.mapNotNull {
                it.toObject<Inscripcion>()?.copy(id = it.id)
            }
            onChange(lista)
        }
    }

    fun agregarInscripcion(inscripcion: Inscripcion, onComplete: (Boolean) -> Unit) {
        val docRef = inscripcionesRef.document()
        val inscripcionConId = inscripcion.copy(id = docRef.id)
        
        docRef.set(inscripcionConId)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun actualizarEstadoInscripcion(id: String, nuevoEstado: EstadoInscripcion) {
        inscripcionesRef.document(id).update("estado", nuevoEstado.name)
    }

    fun eliminarInscripcion(id: String) {
        inscripcionesRef.document(id).delete()
    }
}
