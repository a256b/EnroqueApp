package com.example.apptorneosajedrez.data

import com.example.apptorneosajedrez.model.Partida
import com.google.firebase.firestore.FirebaseFirestore

class PartidasRepository {
    private val firebaseFirestore = FirebaseFirestore.getInstance()

    fun crearPartida(torneoId: String, partida: Partida) {
        firebaseFirestore.collection("torneos")
            .document(torneoId)
            .collection("partidas")
            .add(partida)
    }

}
