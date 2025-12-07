package com.example.apptorneosajedrez.data

import com.example.apptorneosajedrez.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val USERS_COLLECTION = "usuarios"

        // Método de conveniencia para usar desde las ViewModelFactory
        fun getInstance(): AuthRepository {
            val auth = FirebaseAuth.getInstance()
            val firestore = FirebaseFirestore.getInstance()
            return AuthRepository(auth, firestore)
        }
    }

    fun getCurrentFirebaseUser(): FirebaseUser? = auth.currentUser

    fun logout() {
        auth.signOut()
    }

    /**
     * Login con email y contraseña.
     * Lanza excepción si falla.
     */
    suspend fun loginWithEmail(email: String, password: String): Usuario {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw IllegalStateException("Usuario no disponible")
        return ensureUserDocument(user)
    }

    /**
     * Login con Google usando el idToken obtenido con Credential Manager.
     * Lanza excepción si falla.
     */
    suspend fun loginWithGoogle(idToken: String): Usuario {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user ?: throw IllegalStateException("Usuario no disponible")
        return ensureUserDocument(user)
    }

    /**
     * Registro con email y contraseña + creación de documento en Firestore.
     * userType podría ser "admin", "player", etc.
     */
    suspend fun registerWithEmail(
        fullName: String,
        email: String,
        password: String
    ): Usuario {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw IllegalStateException("Usuario no disponible")

        val appUser = Usuario(
            uid = user.uid,
            email = email,
            nombreCompleto = fullName
        )

        firestore.collection(USERS_COLLECTION)
            .document(user.uid)
            .set(appUser)
            .await()

        return appUser
    }

    /**
     * Carga el documento de usuario desde Firestore, si existe.
     */
    suspend fun loadCurrentUserProfile(): Usuario? {
        val firebaseUser = auth.currentUser ?: return null
        val snapshot = firestore.collection(USERS_COLLECTION)
            .document(firebaseUser.uid)
            .get()
            .await()

        return if (snapshot.exists()) {
            snapshot.toObject(Usuario::class.java)?.copy(uid = firebaseUser.uid)
        } else {
            null
        }
    }

    /**
     * Si el documento no existe, lo crea usando los datos del FirebaseUser.
     */
    private suspend fun ensureUserDocument(firebaseUser: FirebaseUser): Usuario {
        val docRef = firestore.collection(USERS_COLLECTION).document(firebaseUser.uid)
        val snapshot = docRef.get().await()

        return if (snapshot.exists()) {
            snapshot.toObject(Usuario::class.java)?.copy(uid = firebaseUser.uid)
                ?: defaultFromFirebaseUser(firebaseUser).also {
                    docRef.set(it).await()
                }
        } else {
            val appUser = defaultFromFirebaseUser(firebaseUser)
            docRef.set(appUser).await()
            appUser
        }
    }

    private fun defaultFromFirebaseUser(user: FirebaseUser): Usuario =
        Usuario(
            uid = user.uid,
            email = user.email ?: "",
            nombreCompleto = user.displayName,
        )
}
