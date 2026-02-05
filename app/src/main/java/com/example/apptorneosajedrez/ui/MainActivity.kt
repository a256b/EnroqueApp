package com.example.apptorneosajedrez.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.apptorneosajedrez.R
import com.example.apptorneosajedrez.data.AuthRepository
import com.example.apptorneosajedrez.databinding.ActivityMainBinding
import com.example.apptorneosajedrez.model.TipoUsuario
import com.example.apptorneosajedrez.model.Usuario
import com.example.apptorneosajedrez.ui.login.LoginActivity
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val authRepository: AuthRepository by lazy { AuthRepository.Companion.getInstance() }

    companion object {
        const val EXTRA_NOMBRE_USUARIO = "EXTRA_NOMBRE_USUARIO"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeViewBinding()

        lifecycleScope.launch {
            val hasSession = ensureSessionOrRedirect()
            if (!hasSession) return@launch

            if (isFinishing || isDestroyed) return@launch

            mostrarToastBienvenidaSiCorresponde()
            configureToolbar()
            setupNavigationSystem()
            observarUsuario()
        }
    }

    private fun observarUsuario() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
               val usuarioLogueado = authRepository.getCurrentUserInMemory()
                actualizarMenuDrawer(usuarioLogueado)
            }
        }
    }

    private fun actualizarMenuDrawer(usuario: Usuario?) {
        val esOrganizador = usuario?.tipoUsuario == TipoUsuario.ORGANIZADOR
        val esJugador = usuario?.tipoUsuario == TipoUsuario.JUGADOR
        val esAficionado = usuario?.tipoUsuario == TipoUsuario.AFICIONADO
        val estadoComoJugador = usuario?.estadoComoJugador?.name
        val esJugadorSinEstado = estadoComoJugador == "NINGUNO"
        val esJugadorRechazado = estadoComoJugador == "RECHAZADO"

        // Opciones que ve solo un Organizador
        binding.navigationView.menu
            .findItem(R.id.nav_nuevosJugadoresFragment)
            .isVisible = esOrganizador
        binding.navigationView.menu
            .findItem(R.id.nav_inscripciones)
            .isVisible = esOrganizador


        // Opciones que puede ver solo un Jugador
        binding.navigationView.menu
            .findItem(R.id.nav_mis_inscripciones)
            .isVisible = esJugador


       // Opciones que puede ver solo un Aficionado
        binding.navigationView.menu
            .findItem(R.id.nav_quiero_ser_jugador)
            .isVisible = esAficionado && (esJugadorSinEstado || esJugadorRechazado)

    }
    private fun mostrarToastBienvenidaSiCorresponde() {
        val nombreUsuario = intent.getStringExtra(EXTRA_NOMBRE_USUARIO)

        if (!nombreUsuario.isNullOrBlank()) {
            Toast.makeText(
                this,
                "Bienvenido $nombreUsuario",
                Toast.LENGTH_LONG
            ).show()

            // Opcional: para que no se vuelva a mostrar si se recrea la Activity
            intent.removeExtra(EXTRA_NOMBRE_USUARIO)
        }
    }

    private suspend fun ensureSessionOrRedirect(): Boolean {
        val user = authRepository.initSession()
        if (user != null) return true

        navigateToLoginAndFinish()
        return false
    }

    private fun navigateToLoginAndFinish() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun initializeViewBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun configureToolbar() {
        setSupportActionBar(binding.mainContent.toolbar)
    }

    private fun setupNavigationSystem() {
        val navHostFragment = getNavHostFragment() ?: return
        val navController = extractNavController(navHostFragment)

        configureAppBarAndDrawer(navController)
    }

    private fun getNavHostFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
    }

    private fun extractNavController(navHostFragment: Fragment): NavController {
        return (navHostFragment as NavHostFragment).navController
    }

    private fun configureAppBarAndDrawer(navController: NavController) {
        val drawerLayout = binding.drawerLayout
        val navigationView = binding.navigationView

        // Primer llamado al crear la activity
        checkEstadoComoJugadorVisibilidad(navigationView)

        // Listener para actualizar cada vez que se abre el drawer
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {
                checkEstadoComoJugadorVisibilidad(navigationView)
            }

            override fun onDrawerClosed(drawerView: View) {}

            override fun onDrawerStateChanged(newState: Int) {}
        })

        setupAppBarConfiguration(drawerLayout)
        connectActionBarWithNavigation(navController)
        connectDrawerWithNavigation(navigationView, navController)
    }

    private fun setupAppBarConfiguration(drawerLayout: DrawerLayout) {
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home),
            drawerLayout
        )
    }


    private fun connectActionBarWithNavigation(navController: NavController) {
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun connectDrawerWithNavigation(
        navigationView: NavigationView,
        navController: NavController
    ) {
        navigationView.setupWithNavController(navController)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

                // Captura de la opción Quiero ser jugador
                R.id.nav_quiero_ser_jugador ->{
                    showAltaJugadorDialog()
                    binding.drawerLayout.closeDrawers()
                    true
                }

                // Captura de la opción Logout
                R.id.nav_logout -> {
                    showLogoutConfirmationDialog()
                    binding.drawerLayout.closeDrawers()
                    true
                }

                else -> {
                    // NavigationUI maneja el resto de destinos
                    val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
                    if (handled) {
                        binding.drawerLayout.closeDrawers()
                    }
                    handled
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun showAltaJugadorDialog(){
        AlertDialog.Builder(this)
            .setTitle("Solicitar alta como jugador")
            .setMessage("¿Desea solicitar el alta como jugador?")
            .setPositiveButton("Aceptar"){_, _ ->
                actualizarEstadoJugadorPendiente()
            }
            .setNegativeButton(getString(R.string.cancel)){ dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun actualizarEstadoJugadorPendiente(){
        val currentUser = authRepository.getCurrentUserInMemory() ?: return
        val uid = currentUser.uid
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        db.collection("usuarios")
            .document(uid)
            .update("estadoComoJugador", "PENDIENTE")
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Solicitud enviada",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Error al enviar la solicitud",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun performLogout() {
        authRepository.logout()

        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.logout_message))
            .setPositiveButton(getString(R.string.logout_confirm)) { _, _ ->
                performLogout()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // TODO: mejorar en una sola consulta a la bd por sesión para guardar en caché la info
    private fun checkEstadoComoJugadorVisibilidad(navigationView: NavigationView){
        val currentUser = authRepository.getCurrentUserInMemory() ?: return
        val uid = currentUser.uid
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if(!document.exists()) return@addOnSuccessListener
                val estado = document.getString("estadoComoJugador") ?: "NINGUNO"
                val menu = navigationView.menu
                val item = menu.findItem(R.id.nav_quiero_ser_jugador)
                when (estado){
                    "NINGUNO", "RECHAZADO" -> item.isVisible = true
                    "PENDIENTE", "ACEPTADO" -> item.isVisible = false
                }
            }
    }
}