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
                val jugadores = torneo?.jugadores ?: emptyList()
                
                val partidas = mutableListOf<Partida>()

                /**
                 *      F
                 *   ╔══╩══╗
                 *   S1    S2
                 * ╔═╩═╗  ╔═╩═╗
                 * C1  C2 C3  C4
                 */

                when (numJugadores) {
                    2 -> {
                        partidas.add(Partida(fase = Fase.FINAL))    //F
                    }
                    3 -> {
                        partidas.add(Partida(fase = Fase.SEMI))     //S1
                        partidas.add(Partida(fase = Fase.FINAL))    //F
                    }
                    4 -> {
                        partidas.add(Partida(fase = Fase.SEMI))     //S1
                        partidas.add(Partida(fase = Fase.SEMI))     //S2
                        partidas.add(Partida(fase = Fase.FINAL))    //F
                    }
                    5 -> {
                        partidas.add(Partida(fase = Fase.CUARTOS))  //C1
                        partidas.add(Partida(fase = Fase.CUARTOS))  //C2
                        partidas.add(Partida(fase = Fase.SEMI))     //S1
                        partidas.add(Partida(fase = Fase.FINAL))    //F
                    }
                    6 -> {
                        partidas.add(Partida(fase = Fase.CUARTOS))  //C1
                        partidas.add(Partida(fase = Fase.CUARTOS))  //C2
                        partidas.add(Partida(fase = Fase.SEMI))     //S1
                        partidas.add(Partida(fase = Fase.SEMI))     //S2
                        partidas.add(Partida(fase = Fase.FINAL))    //F
                    }
                    7 -> {
                        partidas.add(Partida(fase = Fase.CUARTOS))  //C1
                        partidas.add(Partida(fase = Fase.CUARTOS))  //C2
                        partidas.add(Partida(fase = Fase.CUARTOS))  //C3
                        partidas.add(Partida(fase = Fase.SEMI))     //S1
                        partidas.add(Partida(fase = Fase.SEMI))     //S2
                        partidas.add(Partida(fase = Fase.FINAL))    //F
                    }
                    8 -> {
                        partidas.add(Partida(fase = Fase.CUARTOS))  //C1
                        partidas.add(Partida(fase = Fase.CUARTOS))  //C2
                        partidas.add(Partida(fase = Fase.CUARTOS))  //C3
                        partidas.add(Partida(fase = Fase.CUARTOS))  //C4
                        partidas.add(Partida(fase = Fase.SEMI))     //S1
                        partidas.add(Partida(fase = Fase.SEMI))     //S2
                        partidas.add(Partida(fase = Fase.FINAL))    //F
                    }
                }

                if (partidas.isEmpty()) {
                    onComplete(false)
                    return@addOnSuccessListener
                }

                // Asignamos los jugadores a la lista de partidas antes de guardarlas en Firestore
                asignarJugadores(jugadores, partidas)

                val batch = db.batch()
                val partidasCol = db.collection("torneos").document(idTorneo).collection("partidas")

                partidas.forEach { partida ->
                    val docRef = partidasCol.document()
                    val partidaConId = partida.copy(idPartida = docRef.id)
                    batch.set(docRef, partidaConId)
                }

                /**
                 * Llamada a función:
                 * fun asignarJugadores(idTorneo: String)
                 */

                batch.commit()
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Función para obtener si un torneo ya tiene partidas creadas.
     */
    fun tienePartidasGeneradas(idTorneo: String, onResult: (Boolean) -> Unit) {
        db.collection("torneos").document(idTorneo).collection("partidas").limit(1).get()
            .addOnSuccessListener { snapshot ->
                onResult(!snapshot.isEmpty)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    /**
     * Obtiene todas las partidas de un torneo.
     */
    fun obtenerPartidas(idTorneo: String, onComplete: (List<Partida>) -> Unit) {
        db.collection("torneos").document(idTorneo).collection("partidas").get()
            .addOnSuccessListener { snapshot ->
                val partidas = snapshot.documents.mapNotNull { it.toObject<Partida>()?.copy(idPartida = it.id) }
                onComplete(partidas)
            }
            .addOnFailureListener {
                onComplete(emptyList())
            }
    }

    /**
     * Función para asignar jugadores a las partidas creadas.
     * Obtiene el idTorneo, comprueba la cantidad de jugadores que tiene y los asigna a las partidas creadas.
     * La asignación de jugadores NO SE PUEDE REPETIR, cada jugador se asigna una sola vez a una partida.
     * Se comienza a asignar de izquierda a derecha (Primero se asigna fichas blancas -idJugador1- y luego fichas negras -idJugador2-)
     *
     * 2 JUGADORES - 1 FINAL
     * A los 2 jugadores se les asigna en la partida FINAL F primero idJugador1 en blancas y luego idJugador2 en negras.
     *
     * 3 JUGADORES - 1 SEMI Y 1 FINAL
     * Primer y segundo jugador se los asigna a SEMI S1. El tercer jugador va an el lugar de idJugador2 en negras de la FINAL F.
     *
     * 4 JUGADORES - 2 SEMI Y 1 FINAL
     * Primer y segundo jugador se los asigna a SEMI S1. El tercer y cuarto jugador van en la otra SEMI S2
     *
     * 5 JUGADORES - 2 CUARTOS, 1 SEMI Y 1 FINAL
     * Primer y segundo jugador se los asigna a CUARTOS C1. El tercer y cuarto jugador van en la otra CUARTOS C2. El quinto jugador va en idJugador2 en negras en la de FINAL F
     *
     * 6 JUGADORES - 2 CUARTOS, 2 SEMI Y 1 FINAL
     * Primer y segundo jugador se los asigna a CUARTOS C1. El tercer y cuarto jugador se los asigna a otra de CUARTOS C2. El quinto y sexto jugador se asigna a una de SEMI S2.
     *
     * 7 JUGADORES - 3 CUARTOS, 2 SEMI Y 1 FINAL
     * Primer y segundo jugador se los asigna a CUARTOS C1. El tercer y cuarto jugador se los asigna a otra de CUARTOS C2. El quinto y sexto jugador se los asigna a la última de CUARTOS C3. El séptimo jugador se lo asigna a idJugador2 en negras de la SEMI S2.
     *
     * 8 JUGADORES - 4 CUARTOS, 2 SEMI Y 1 FINAL
     * Primer y segundo jugador van en CUARTOS C1. El tercero y cuarto jugador van en CUARTOS C2. El quinto y sexto van en CUARTOS C3. El séptimo y octavo van en CUARTOS C4.
     *
     * fun asignarJugadores(idTorneo: String){}
     */
    private fun asignarJugadores(jugadores: List<String>, partidas: MutableList<Partida>) {
        val numJugadores = jugadores.size
        if (numJugadores < 2) return

        when (numJugadores) {
            2 -> {
                // FINAL F (index 0)
                partidas[0] = partidas[0].copy(idJugador1 = jugadores[0], idJugador2 = jugadores[1])
            }
            3 -> {
                // SEMI S1 (index 0), FINAL F (index 1)
                partidas[0] = partidas[0].copy(idJugador1 = jugadores[0], idJugador2 = jugadores[1])
                partidas[1] = partidas[1].copy(idJugador2 = jugadores[2])
            }
            4 -> {
                // SEMI S1 (index 0), SEMI S2 (index 1), FINAL F (index 2)
                partidas[0] = partidas[0].copy(idJugador1 = jugadores[0], idJugador2 = jugadores[1])
                partidas[1] = partidas[1].copy(idJugador1 = jugadores[2], idJugador2 = jugadores[3])
            }
            5 -> {
                // CUARTOS C1 (0), CUARTOS C2 (1), SEMI S1 (2), FINAL F (3)
                partidas[0] = partidas[0].copy(idJugador1 = jugadores[0], idJugador2 = jugadores[1])
                partidas[1] = partidas[1].copy(idJugador1 = jugadores[2], idJugador2 = jugadores[3])
                partidas[3] = partidas[3].copy(idJugador2 = jugadores[4])
            }
            6 -> {
                // CUARTOS C1 (0), CUARTOS C2 (1), SEMI S1 (2), SEMI S2 (3), FINAL F (4)
                partidas[0] = partidas[0].copy(idJugador1 = jugadores[0], idJugador2 = jugadores[1])
                partidas[1] = partidas[1].copy(idJugador1 = jugadores[2], idJugador2 = jugadores[3])
                partidas[3] = partidas[3].copy(idJugador1 = jugadores[4], idJugador2 = jugadores[5])
            }
            7 -> {
                // CUARTOS C1 (0), CUARTOS C2 (1), CUARTOS C3 (2), SEMI S1 (3), SEMI S2 (4), FINAL F (5)
                partidas[0] = partidas[0].copy(idJugador1 = jugadores[0], idJugador2 = jugadores[1])
                partidas[1] = partidas[1].copy(idJugador1 = jugadores[2], idJugador2 = jugadores[3])
                partidas[2] = partidas[2].copy(idJugador1 = jugadores[4], idJugador2 = jugadores[5])
                partidas[4] = partidas[4].copy(idJugador2 = jugadores[6])
            }
            8 -> {
                // CUARTOS C1 (0), CUARTOS C2 (1), CUARTOS C3 (2), CUARTOS C4 (3), SEMI S1 (4), SEMI S2 (5), FINAL F (6)
                partidas[0] = partidas[0].copy(idJugador1 = jugadores[0], idJugador2 = jugadores[1])
                partidas[1] = partidas[1].copy(idJugador1 = jugadores[2], idJugador2 = jugadores[3])
                partidas[2] = partidas[2].copy(idJugador1 = jugadores[4], idJugador2 = jugadores[5])
                partidas[3] = partidas[3].copy(idJugador1 = jugadores[6], idJugador2 = jugadores[7])
            }
        }
    }
}
