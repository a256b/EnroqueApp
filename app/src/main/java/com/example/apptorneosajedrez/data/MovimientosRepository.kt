package com.example.apptorneosajedrez.data

import com.example.apptorneosajedrez.model.Movimiento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MovimientosRepository {
    private val db = FirebaseFirestore.getInstance()
    private val movesRef = db.collection("moves")

    fun sendMove(notation: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val movimiento = Movimiento(
            notation = notation,
            timestamp = System.currentTimeMillis(),
            userId = user.uid
        )
        movesRef.add(movimiento)
    }

    fun listenMoves(onUpdate: (List<Movimiento>) -> Unit) {
        movesRef
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snaps, e ->
                if (e != null) return@addSnapshotListener
                val list = snaps
                    ?.documents
                    ?.mapNotNull { it.toObject(Movimiento::class.java) }
                    ?: emptyList()
                onUpdate(list)
            }
    }
}
