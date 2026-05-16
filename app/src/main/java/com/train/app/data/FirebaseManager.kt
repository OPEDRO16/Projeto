package com.train.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseManager {
    // Autenticação
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // Firestore (Posts, Rotinas, Perfil)
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // Realtime DB (WebSockets para Chat)
    val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance().apply {
            // Permite persistência offline para chat rápido
            setPersistenceEnabled(true)
        }
    }
}