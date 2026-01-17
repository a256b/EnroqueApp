package com.example.apptorneosajedrez.data

import com.example.apptorneosajedrez.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore
) {

    companion object {
        private const val USERS_COLLECTION = "usuarios"

        // Singleton
        @Volatile private var INSTANCE: AuthRepository? = null

        fun getInstance(): AuthRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = AuthRepository(
                    FirebaseAuth.getInstance(),
                    FirebaseFirestore.getInstance()
                )
                INSTANCE = instance
                instance
            }
        }
    }

    // ----------------------------
    // Sesión en memoria (StateFlow)
    // ----------------------------
    private val _currentUser = MutableStateFlow<Usuario?>(null)
    val currentUser: StateFlow<Usuario?> = _currentUser.asStateFlow()

    // Firebase user (solo auth)
    fun getCurrentFirebaseUser(): FirebaseUser? = firebaseAuth.currentUser

    /**
     * Inicializa la sesión al arrancar la app.
     * - Si NO hay usuario autenticado: limpia sesión en memoria.
     * - Si hay: carga/asegura documento en Firestore y lo guarda en memoria.
     *
     * Llamar una vez al inicio (por ejemplo en MainActivity/Splash).
     */
    suspend fun initSession(): Usuario? {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            _currentUser.value = null
            return null
        }

        val appUser = ensureUserDocument(firebaseUser)
        _currentUser.value = appUser
        return appUser
    }

    /**
     * Limpia sesión (Firebase + memoria).
     */
    fun logout() {
        firebaseAuth.signOut()
        _currentUser.value = null
    }

    /**
     * Login con email y contraseña.
     * Actualiza sesión en memoria.
     */
    suspend fun loginWithEmail(email: String, password: String): Usuario {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw IllegalStateException("Usuario no disponible")

        val appUser = ensureUserDocument(user)
        _currentUser.value = appUser
        return appUser
    }

    /**
     * Login con Google usando el idToken obtenido con Credential Manager.
     * Actualiza sesión en memoria.
     */
    suspend fun loginWithGoogle(idToken: String): Usuario {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        val user = result.user ?: throw IllegalStateException("Usuario no disponible")

        val appUser = ensureUserDocument(user)
        _currentUser.value = appUser
        return appUser
    }

    /**
     * Registro con email + creación de documento en Firestore.
     * Actualiza sesión en memoria.
     */
    suspend fun registerWithEmail(
        fullName: String,
        email: String,
        password: String
    ): Usuario {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw IllegalStateException("Usuario no disponible")

        val appUser = Usuario(
            uid = user.uid,
            email = email,
            nombreCompleto = fullName
            // Si tu modelo tiene defaults para tipo/estado, no hace falta setearlos acá
        )

        firebaseFirestore.collection(USERS_COLLECTION)
            .document(user.uid)
            .set(appUser)
            .await()

        _currentUser.value = appUser
        return appUser
    }

    /**
     * Devuelve el usuario de sesión actual en memoria (puede ser null).
     * Útil si estás en un lugar donde no querés coleccionar Flow.
     */
    fun getCurrentUserInMemory(): Usuario? = _currentUser.value

    /**
     * Refresca manualmente desde Firestore (por ejemplo desde "Perfil" → botón "Actualizar").
     * Actualiza la sesión en memoria.
     */
    suspend fun refreshCurrentUser(): Usuario? {
        val firebaseUser = firebaseAuth.currentUser ?: run {
            _currentUser.value = null
            return null
        }

        val snapshot = firebaseFirestore.collection(USERS_COLLECTION)
            .document(firebaseUser.uid)
            .get()
            .await()

        val user = if (snapshot.exists()) {
            snapshot.toObject(Usuario::class.java)?.copy(uid = firebaseUser.uid)
        } else {
            null
        }

        // Si no existe, lo creamos (misma política que ensureUserDocument)
        val finalUser = user ?: ensureUserDocument(firebaseUser)

        _currentUser.value = finalUser
        return finalUser
    }

    /**
     * Si el documento no existe, lo crea usando los datos del FirebaseUser.
     * Si existe, lo devuelve.
     */
    private suspend fun ensureUserDocument(firebaseUser: FirebaseUser): Usuario {
        val docRef = firebaseFirestore.collection(USERS_COLLECTION).document(firebaseUser.uid)
        val snapshot = docRef.get().await()

        return if (snapshot.exists()) {
            snapshot.toObject(Usuario::class.java)?.copy(uid = firebaseUser.uid)
                ?: defaultFromFirebaseUser(firebaseUser).also { docRef.set(it).await() }
        } else {
            val appUser = defaultFromFirebaseUser(firebaseUser)
            docRef.set(appUser).await()
            appUser
        }
    }

    /**
     * Obtiene todos los usuarios de Firebase
     */
    suspend fun obtenerTodosLosUsuarios(): List<Usuario> {
        val snapshot = firebaseFirestore.collection(USERS_COLLECTION)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Usuario::class.java)?.copy(uid = doc.id)
        }
    }

    /**
     * Actualiza el estadoComoJugador de un Usuario dado
     */
    suspend fun actualizarEstadoUsuario(
        uid: String,
        nuevoEstado: com.example.apptorneosajedrez.model.EstadoComoJugador
    ) {
        firebaseFirestore.collection(USERS_COLLECTION)
            .document(uid)
            .update("estadoComoJugador", nuevoEstado.name)
            .await()
    }

    private fun defaultFromFirebaseUser(user: FirebaseUser): Usuario =
        Usuario(
            uid = user.uid,
            email = user.email ?: "",
            nombreCompleto = user.displayName
        )
}
