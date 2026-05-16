package com.train.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Usamos 'by lazy' para garantir que as instâncias só são criadas
 * quando chamadas, evitando crashes se o FirebaseApp ainda não estiver pronto.
 */
object FirebaseManager {
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance().apply {
            // Ativa persistência offline para evitar crashes sem net
            setPersistenceEnabled(true)
        }
    }
}