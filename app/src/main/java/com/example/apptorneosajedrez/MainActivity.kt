package com.example.apptorneosajedrez

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.apptorneosajedrez.data.AuthRepository
import com.example.apptorneosajedrez.databinding.ActivityMainBinding
import com.example.apptorneosajedrez.ui.login.LoginActivity
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    // Repositorio de autenticación (puede usar getInstance() si lo definiste así)
    private val authRepository: AuthRepository by lazy { AuthRepository.Companion.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeViewBinding()
        configureToolbar()
        setupNavigationSystem()
    }

    // ---------- Inicialización de vistas ----------

    private fun initializeViewBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun configureToolbar() {
        setSupportActionBar(binding.mainContent.toolbar)
    }

    // ---------- Navegación con NavHost + Drawer ----------

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

        setupAppBarConfiguration(drawerLayout)
        connectActionBarWithNavigation(navController)
        connectDrawerWithNavigation(navigationView, navController)
    }

    /**
     * Define qué destinos se consideran "de primer nivel".
     * Esto afecta al comportamiento del botón de back / icono de hamburguesa,
     * pero NO define qué destinos existen ni qué opciones tiene el drawer.
     */
    private fun setupAppBarConfiguration(drawerLayout: DrawerLayout) {
        appBarConfiguration = AppBarConfiguration(
            getTopLevelDestinations(),
            drawerLayout
        )
    }

    private fun getTopLevelDestinations(): Set<Int> {
        return setOf(
            R.id.nav_home,
        )
    }

    private fun connectActionBarWithNavigation(navController: NavController) {
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun connectDrawerWithNavigation(
        navigationView: NavigationView,
        navController: NavController
    ) {
        // Esto vincula el estado del menú (check, título, etc.) con el NavController
        navigationView.setupWithNavController(navController)

        // Y ahora sobreescribimos el listener para poder interceptar nav_logout
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    // 1) Cerrar sesión en Firebase / auth
                    authRepository.logout()

                    // 2) Ir a pantalla de login
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()

                    // 3) Cerrar el drawer
                    binding.drawerLayout.closeDrawers()

                    true
                }

                else -> {
                    // Para el resto de ítems, usamos la navegación normal del NavController
                    val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
                    if (handled) {
                        binding.drawerLayout.closeDrawers()
                    }
                    handled
                }
            }
        }
    }

    // ---------- Menú de la Toolbar (si lo usás) ----------

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // ---------- Manejo del botón de back / up ----------

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}