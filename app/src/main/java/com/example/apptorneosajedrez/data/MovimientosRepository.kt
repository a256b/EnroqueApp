package com.example.apptorneosajedrez.data

import android.util.Log
import com.example.apptorneosajedrez.model.Movimiento
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class MovimientosRepository(
    private val firestore: FirebaseFirestore
) {
    private companion object {
        const val TAG = "MovimientosRepo"
    }

    // -------------------------------------------------------------------------
    // Helpers de colección
    // -------------------------------------------------------------------------

    private fun partidaRef(
        torneoId: String,
        partidaId: String
    ) = firestore
        .collection("torneos")
        .document(torneoId)
        .collection("partidas")
        .document(partidaId)

    private fun movimientosCollection(
        torneoId: String,
        partidaId: String
    ): CollectionReference = partidaRef(torneoId, partidaId)
        .collection("movimientos")

    // -------------------------------------------------------------------------
    // Escuchar movimientos en tiempo real
    // -------------------------------------------------------------------------

    fun escucharMovimientos(
        torneoId: String,
        partidaId: String,
        onResult: (List<Movimiento>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration {

        Log.d(TAG, "Iniciando escucha de movimientos. torneoId=$torneoId, partidaId=$partidaId")

        return movimientosCollection(torneoId, partidaId)
            // Ahora ordenamos por INDICE (orden lógico de carga),
            // no por numeroMovimiento
            .orderBy("indice", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e(TAG, "Error en snapshotListener", error)
                    onError(error)
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    Log.d(TAG, "Snapshot vacío para partidaId=$partidaId")
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                Log.d(TAG, "Snapshot recibido. size=${snapshot.size()}")

                val movimientosIndividuales = snapshot.toMovimientos()
                Log.d(TAG, "Movimientos mapeados correctamente: ${movimientosIndividuales.size}")

                onResult(movimientosIndividuales)
            }
    }

    /**
     * Convierte un snapshot de Firestore en una lista de Movimiento,
     * logueando posibles problemas de mapeo.
     *
     * Asegurate de que data class Movimiento tenga los campos:
     *  - id: String = ""
     *  - indice: Long = 0
     *  - numeroMovimientoCompleto: Int = 0
     *  - color: String = "BLANCAS"
     *  - notacion: String = ""
     *  - creadoEn: Timestamp? = null
     */
    private fun QuerySnapshot.toMovimientos(): List<Movimiento> {
        return documents.mapNotNull { doc ->
            Log.d(TAG, "Doc ${doc.id} => ${doc.data}")

            val movimiento = doc.toObject(Movimiento::class.java)

            if (movimiento == null) {
                Log.w(TAG, "No se pudo mapear el documento ${doc.id} a Movimiento")
                null
            } else {
                movimiento.copy(id = doc.id)
            }
        }
    }

    // -------------------------------------------------------------------------
    // Agregar movimiento
    // -------------------------------------------------------------------------

    /**
     * Agrega un movimiento nuevo a Firestore actualizando también el estado
     * de la PARTIDA (ultimoIndiceMov, ultimoNumeroMovimientoCompleto, ultimoColorMovimiento).
     */
    fun agregarMovimiento(
        torneoId: String,
        partidaId: String,
        notacion: String,
        onComplete: (Boolean) -> Unit
    ) {
        val partidaRef = partidaRef(torneoId, partidaId)
        val movimientosRef = partidaRef.collection("movimientos")

        firestore.runTransaction { transaction ->

            // 1) Leer el estado actual de la PARTIDA
            val partidaSnapshot = transaction.get(partidaRef)

            val ultimoIndice = partidaSnapshot.getLong("ultimoIndiceMov") ?: 0L
            val ultimoNumeroCompleto =
                partidaSnapshot.getLong("ultimoNumeroMovimientoCompleto")?.toInt() ?: 0
            val ultimoColor = partidaSnapshot.getString("ultimoColorMovimiento") ?: "NEGRAS"
            // Si nunca se jugó nada, arrancamos suponiendo que el último fue NEGRAS,
            // para que el primero nuevo sea BLANCAS.

            // 2) Calcular el nuevo estado
            val nuevoIndice = ultimoIndice + 1

            val nuevoColor = if (ultimoColor == "BLANCAS") "NEGRAS" else "BLANCAS"

            val nuevoNumeroCompleto =
                if (nuevoColor == "BLANCAS") {
                    // Cada vez que vuelve a BLANCAS, incrementa el número completo de jugada
                    ultimoNumeroCompleto + 1
                } else {
                    ultimoNumeroCompleto
                }

            // 3) Crear el documento de movimiento
            val nuevoMovimiento = mapOf(
                "indice" to nuevoIndice,
                "numeroMovimientoCompleto" to nuevoNumeroCompleto,
                "color" to nuevoColor,
                "notacion" to notacion,
                "creadoEn" to FieldValue.serverTimestamp()
            )

            val nuevoMovimientoRef = movimientosRef.document()
            transaction.set(nuevoMovimientoRef, nuevoMovimiento)

            // 4) Actualizar el estado de la PARTIDA con los últimos datos
            val nuevosCamposPartida = mapOf(
                "ultimoIndiceMov" to nuevoIndice,
                "ultimoNumeroMovimientoCompleto" to nuevoNumeroCompleto,
                "ultimoColorMovimiento" to nuevoColor
            )

            transaction.update(partidaRef, nuevosCamposPartida)

            null
        }
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error en agregarMovimiento", e)
                onComplete(false)
            }
    }

    // -------------------------------------------------------------------------
    // Borrar último movimiento (Deshacer)
    // -------------------------------------------------------------------------

    /**
     * Borra el último movimiento (según 'indice' más alto) y actualiza
     * el estado de la PARTIDA para que apunte al penúltimo, o a estado
     * inicial si ya no quedan movimientos.
     */
    fun borrarUltimoMovimiento(
        torneoId: String,
        partidaId: String,
        onComplete: (Boolean) -> Unit
    ) {
        val partidaRef = partidaRef(torneoId, partidaId)
        val movimientosRef = movimientosCollection(torneoId, partidaId)

        // Obtenemos el último y el penúltimo movimiento (si existe), ordenados por INDICE
        movimientosRef
            .orderBy("indice", Query.Direction.DESCENDING)
            .limit(2)
            .get()
            .addOnSuccessListener { snapshot ->
                val docs = snapshot.documents
                val ultimoDoc = docs.getOrNull(0)
                val penultimoDoc = docs.getOrNull(1)

                Log.d(TAG, "Entró a borrarUltimoMovimiento(). docs=${snapshot.size()}")

                if (ultimoDoc == null) {
                    // No hay movimientos para borrar
                    onComplete(false)
                    return@addOnSuccessListener
                }

                // 1) Borrar el último movimiento
                ultimoDoc.reference
                    .delete()
                    .addOnSuccessListener {
                        // 2) Actualizar el estado de la PARTIDA
                        val nuevosCamposPartida = if (penultimoDoc != null) {
                            // Hay al menos un movimiento previo, lo usamos como "nuevo último"
                            val nuevoIndice =
                                penultimoDoc.getLong("indice") ?: 0L
                            val nuevoNumeroCompleto =
                                (penultimoDoc.getLong("numeroMovimientoCompleto") ?: 0L).toInt()
                            val nuevoColor =
                                penultimoDoc.getString("color") ?: "NEGRAS"

                            mapOf(
                                "ultimoIndiceMov" to nuevoIndice,
                                "ultimoNumeroMovimientoCompleto" to nuevoNumeroCompleto,
                                "ultimoColorMovimiento" to nuevoColor
                            )
                        } else {
                            // Se borró el único movimiento que había: la partida vuelve a "estado inicial"
                            mapOf(
                                "ultimoIndiceMov" to 0L,
                                "ultimoNumeroMovimientoCompleto" to 0,
                                // Elegimos NEGRAS para que el próximo en agregarMovimiento sea BLANCAS
                                "ultimoColorMovimiento" to "NEGRAS"
                            )
                        }

                        partidaRef
                            .update(nuevosCamposPartida)
                            .addOnSuccessListener {
                                onComplete(true)
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error al actualizar estado de partida al deshacer", e)
                                onComplete(false)
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error al borrar último movimiento", e)
                        onComplete(false)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al consultar último movimiento", e)
                onComplete(false)
            }
    }
}
