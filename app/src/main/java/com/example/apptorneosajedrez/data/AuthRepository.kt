package com.example.apptorneosajedrez.data

import com.example.apptorneosajedrez.model.EstadoComoJugador
import com.example.apptorneosajedrez.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore,
    private val jugadorRepository: JugadorRepository = JugadorRepository()
) {

    companion object {
        private const val USERS_COLLECTION = "usuarios"

        // Singleton
        @Volatile
        private var INSTANCE: AuthRepository? = null

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

    // ----------------------------
    // Sincronización automática (listener)
    // ----------------------------
    private var userListenerRegistration: ListenerRegistration? = null
    private var listeningUid: String? = null

    // Scope interno para tareas desde callbacks no-suspend (listener)
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Inicializa la sesión al arrancar la app.
     * - Si NO hay usuario autenticado: limpia sesión en memoria y detiene el listener.
     * - Si hay: asegura documento en Firestore, lo guarda en memoria y deja un listener
     *   activo sobre usuarios/{uid} para mantener sincronizado el StateFlow.
     *
     * Llamar una vez al inicio (por ejemplo en MainActivity/Splash).
     */
    suspend fun initSession(): Usuario? {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            stopUserListener()
            _currentUser.value = null
            return null
        }

        val appUser = ensureUserDocument(firebaseUser)
        _currentUser.value = appUser

        // Mantener sincronizado en segundo plano
        startUserListener(firebaseUser.uid)

        return appUser
    }

    /**
     * Limpia sesión (Firebase + memoria) y detiene el listener.
     */
    fun logout() {
        stopUserListener()
        firebaseAuth.signOut()
        _currentUser.value = null
    }

    /**
     * Login con email y contraseña.
     * Actualiza sesión en memoria y arranca listener.
     */
    suspend fun loginWithEmail(email: String, password: String): Usuario {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw IllegalStateException("Usuario no disponible")

        val appUser = ensureUserDocument(user)
        _currentUser.value = appUser
        startUserListener(user.uid)

        return appUser
    }

    /**
     * Login con Google usando el idToken obtenido con Credential Manager.
     * Actualiza sesión en memoria y arranca listener.
     */
    suspend fun loginWithGoogle(idToken: String): Usuario {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        val user = result.user ?: throw IllegalStateException("Usuario no disponible")

        val appUser = ensureUserDocument(user)
        _currentUser.value = appUser
        startUserListener(user.uid)

        return appUser
    }

    /**
     * Registro con email + creación de documento en Firestore.
     * Actualiza sesión en memoria y arranca listener.
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
        )

        firebaseFirestore.collection(USERS_COLLECTION)
            .document(user.uid)
            .set(appUser)
            .await()

        _currentUser.value = appUser
        startUserListener(user.uid)

        return appUser
    }

    /**
     * Devuelve el usuario de sesión actual en memoria (puede ser null).
     */
    fun getCurrentUserInMemory(): Usuario? = _currentUser.value


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
     * Mantiene _currentUser sincronizado con el documento usuarios/{uid}.
     * - No duplica listeners.
     * - Si cambia el uid (otro login), reemplaza el listener anterior.
     */
    private fun startUserListener(uid: String) {
        if (listeningUid == uid && userListenerRegistration != null) return

        // Si había un listener de otro usuario, lo reemplazamos
        stopUserListener()

        listeningUid = uid
        userListenerRegistration = firebaseFirestore.collection(USERS_COLLECTION)
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // No cortamos sesión por un error transitorio. Mantenemos el último valor en memoria.
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val updated = snapshot.toObject(Usuario::class.java)?.copy(uid = uid)
                    if (updated != null) {
                        _currentUser.value = updated
                    }
                    return@addSnapshotListener
                }

                // Si el doc no existe por algún motivo, intentamos recrearlo (política actual del repo)
                val firebaseUser = firebaseAuth.currentUser
                if (firebaseUser != null && firebaseUser.uid == uid) {
                    repoScope.launch {
                        val recreated = ensureUserDocument(firebaseUser)
                        _currentUser.value = recreated
                    }
                } else {
                    _currentUser.value = null
                }
            }
    }

    private fun stopUserListener() {
        userListenerRegistration?.remove()
        userListenerRegistration = null
        listeningUid = null
    }

    /**
     * Obtiene todos los usuarios.
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
     * Actualiza el estadoComoJugador de un Usuario dado.
     * Si el nuevo estado es ACEPTADO, crea el documento correspondiente en "jugadores"
     * y actualiza el tipoUsuario a JUGADOR (lógica en JugadorRepository).
     */
    suspend fun actualizarEstadoUsuario(
        uid: String,
        nuevoEstado: EstadoComoJugador
    ) {
        val usuariosRef = firebaseFirestore.collection(USERS_COLLECTION)
        val userDocRef = usuariosRef.document(uid)

        // 1) Actualizar el estado en el documento de usuario
        userDocRef
            .update("estadoComoJugador", nuevoEstado.name)
            .await()

        // 2) Si pasó a ACEPTADO, crear el jugador correspondiente
        if (nuevoEstado == EstadoComoJugador.ACEPTADO) {
            val userSnapshot = userDocRef.get().await()
            val usuario = userSnapshot.toObject(Usuario::class.java)?.copy(uid = uid) ?: return

            jugadorRepository.crearJugador(
                uid = usuario.uid,
                nombreCompleto = usuario.nombreCompleto.orEmpty(),
                email = usuario.email
            )
        }
    }

    private fun defaultFromFirebaseUser(user: FirebaseUser): Usuario =
        Usuario(
            uid = user.uid,
            email = user.email ?: "",
            nombreCompleto = user.displayName
        )
}
