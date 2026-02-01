package com.example.apptorneosajedrez.data

import com.example.apptorneosajedrez.model.EstadoTorneo
import com.example.apptorneosajedrez.model.Torneo
import com.example.apptorneosajedrez.model.Partida
import com.example.apptorneosajedrez.model.Fase
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

    /**
     * Función para auto generar las partidas.
     * Obtiene la cantidad de jugadores a partir del idTorneo, crea partidas en base a la cantidad
     */
    fun autoGenerarPartidas(idTorneo: String, onComplete: (Boolean) -> Unit) {
        if (idTorneo.isEmpty()) {
            onComplete(false)
            return
        }

        db.collection("torneos").document(idTorneo).get()
            .addOnSuccessListener { snapshot ->
                val torneo = snapshot.toObject<Torneo>()
                val numJugadores = torneo?.jugadores?.size ?: 0
                
                val partidas = mutableListOf<Partida>()

                when (numJugadores) {
                    2 -> {
                        partidas.add(Partida(fase = Fase.FINAL))
                    }
                    3 -> {
                        partidas.add(Partida(fase = Fase.SEMI))
                        partidas.add(Partida(fase = Fase.FINAL))
                    }
                    4 -> {
                        partidas.add(Partida(fase = Fase.SEMI))
                        partidas.add(Partida(fase = Fase.SEMI))
                        partidas.add(Partida(fase = Fase.FINAL))
                    }
                    5 -> {
                        partidas.add(Partida(fase = Fase.CUARTOS))
                        partidas.add(Partida(fase = Fase.CUARTOS))
                        partidas.add(Partida(fase = Fase.SEMI))
                        partidas.add(Partida(fase = Fase.FINAL))
                    }
                    6 -> {
                        partidas.add(Partida(fase = Fase.CUARTOS))
                        partidas.add(Partida(fase = Fase.CUARTOS))
                        partidas.add(Partida(fase = Fase.SEMI))
                        partidas.add(Partida(fase = Fase.SEMI))
                        partidas.add(Partida(fase = Fase.FINAL))
                    }
                    7 -> {
                        partidas.add(Partida(fase = Fase.CUARTOS))
                        partidas.add(Partida(fase = Fase.CUARTOS))
                        partidas.add(Partida(fase = Fase.CUARTOS))
                        partidas.add(Partida(fase = Fase.SEMI))
                        partidas.add(Partida(fase = Fase.SEMI))
                        partidas.add(Partida(fase = Fase.FINAL))
                    }
                    8 -> {
                        partidas.add(Partida(fase = Fase.CUARTOS))
                        partidas.add(Partida(fase = Fase.CUARTOS))
                        partidas.add(Partida(fase = Fase.CUARTOS))
                        partidas.add(Partida(fase = Fase.CUARTOS))
                        partidas.add(Partida(fase = Fase.SEMI))
                        partidas.add(Partida(fase = Fase.SEMI))
                        partidas.add(Partida(fase = Fase.FINAL))
                    }
                }

                if (partidas.isEmpty()) {
                    onComplete(false)
                    return@addOnSuccessListener
                }

                val batch = db.batch()
                val partidasCol = db.collection("torneos").document(idTorneo).collection("partidas")

                partidas.forEach { partida ->
                    val docRef = partidasCol.document()
                    val partidaConId = partida.copy(idPartida = docRef.id)
                    batch.set(docRef, partidaConId)
                }

                batch.commit()
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }
}
