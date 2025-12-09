package com.example.apptorneosajedrez.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AlertDialog
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
import com.example.apptorneosajedrez.R
import com.example.apptorneosajedrez.data.AuthRepository
import com.example.apptorneosajedrez.databinding.ActivityMainBinding
import com.example.apptorneosajedrez.ui.login.LoginActivity
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val authRepository: AuthRepository by lazy { AuthRepository.Companion.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeViewBinding()
        configureToolbar()
        setupNavigationSystem()
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

        setupAppBarConfiguration(drawerLayout)
        connectActionBarWithNavigation(navController)
        connectDrawerWithNavigation(navigationView, navController)
    }

    private fun setupAppBarConfiguration(drawerLayout: DrawerLayout) {
        appBarConfiguration = AppBarConfiguration(
            getTopLevelDestinations(),
            drawerLayout
        )
    }

    private fun getTopLevelDestinations(): Set<Int> {
        return setOf(
            R.id.nav_home,
            R.id.nav_torneos,
            R.id.nav_jugadores,
            R.id.nav_inscripciones,
            R.id.nav_mapa
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

    private fun performLogout() {
        authRepository.logout()

        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}