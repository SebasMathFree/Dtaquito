package com.example.dtaquito

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.example.dtaquito.gameroom.GameRoomFragment
import com.example.dtaquito.profile.ProfileFragment
import com.example.dtaquito.reservation.ReservationFragment
import com.example.dtaquito.sportspace.SportSpaceFragment
import com.example.dtaquito.subscription.SubscriptionFragment
import com.example.dtaquito.tickets.TicketFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    var userRoleType: String = "PLAYER"

    // Variable para guardar el estado del fragment actual
    private var savedFragmentTag: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplica el idioma guardado antes de setContentView
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val lang = prefs.getString("app_lang", "es") ?: "es"
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        super.onCreate(savedInstanceState)
        CoroutineScope(Dispatchers.Main).launch {
            userRoleType = readUserRoleTypeFromPrefs()
            setContentView(R.layout.activity_main)
            bottomNav = findViewById(R.id.bottom_navigation)
            val menuRes = if (userRoleType == "PLAYER") R.menu.menu_player else R.menu.menu_propietario
            bottomNav.menu.clear()
            bottomNav.inflateMenu(menuRes)
            bottomNav.selectedItemId = when(userRoleType){
                "PLAYER" -> R.id.navigation_home
                else -> R.id.navigation_subscriptions
            }
            setupNavigation()

            // Manejar la restauración del fragment después del cambio de tema
            val goToProfile = prefs.getBoolean("go_to_profile", false)
            if (goToProfile) {
                bottomNav.selectedItemId = R.id.navigation_profile
                loadFragment(ProfileFragment())
                prefs.edit { remove("go_to_profile") }
            } else if (savedInstanceState == null && supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
                // Solo cargar fragment por defecto si no hay savedInstanceState y no hay fragment cargado
                if (userRoleType == "PLAYER") {
                    loadFragment(GameRoomFragment())
                } else {
                    loadFragment(SubscriptionFragment())
                }
            }
        }
    }

    private fun setupNavigation() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> loadFragment(GameRoomFragment())
                R.id.navigation_sportspaces -> loadFragment(SportSpaceFragment())
                R.id.navigation_reservations -> loadFragment(ReservationFragment())
                R.id.navigation_profile -> loadFragment(ProfileFragment())
                R.id.navigation_sportspaces_prop -> loadFragment(SportSpaceFragment())
                R.id.navigation_subscriptions -> loadFragment(SubscriptionFragment())
                R.id.navigation_tickets -> loadFragment(TicketFragment())
                else -> false
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        // Verifica si el fragmento ya está cargado
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment != null && currentFragment::class.java == fragment::class.java) {
            // Si el fragmento ya está cargado, no hacemos nada
            return
        }
        // Carga el fragmento en el contenedor
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun readUserRoleTypeFromPrefs(): String {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return prefs.getString("role_type", "PLAYER") ?: "PLAYER"
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    // --- Métodos para actualización instantánea de textos ---
    
    fun updateAllFragmentsTexts() {
        // Actualiza los textos de todos los fragments visibles
        supportFragmentManager.fragments.forEach { fragment ->
            when (fragment) {
                is ProfileFragment -> fragment.updateTexts()
            }
        }
        updateBottomNavTexts()
    }

    fun updateBottomNavTexts() {
        val menu = bottomNav.menu
        when (userRoleType) {
            "PLAYER" -> {
                menu.findItem(R.id.navigation_home)?.title = getString(R.string.communityrooms)
                menu.findItem(R.id.navigation_sportspaces)?.title = getString(R.string.sport_spaces)
                menu.findItem(R.id.navigation_profile)?.title = getString(R.string.profile)
                // Agrega aquí los demás ítems si los tienes
            }
            else -> {
                menu.findItem(R.id.navigation_subscriptions)?.title = getString(R.string.subscriptions)
                menu.findItem(R.id.navigation_sportspaces_prop)?.title = getString(R.string.sport_spaces)
                menu.findItem(R.id.navigation_tickets)?.title = getString(R.string.tickets)
                menu.findItem(R.id.navigation_profile)?.title = getString(R.string.profile)
                // Agrega aquí los demás ítems si los tienes
            }
        }
    }

    fun saveCurrentFragmentState() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        when (currentFragment) {
            is ProfileFragment -> {
                savedFragmentTag = "PROFILE"
                val prefs = getSharedPreferences("settings", MODE_PRIVATE)
                prefs.edit { putBoolean("go_to_profile", true) }
            }
            is GameRoomFragment -> savedFragmentTag = "GAMEROOM"
            is SportSpaceFragment -> savedFragmentTag = "SPORTSPACE"
            is ReservationFragment -> savedFragmentTag = "RESERVATION"
            is SubscriptionFragment -> savedFragmentTag = "SUBSCRIPTION"
            is TicketFragment -> savedFragmentTag = "TICKET"
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveCurrentFragmentState()
        savedFragmentTag?.let { tag ->
            outState.putString("CURRENT_FRAGMENT", tag)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val fragmentTag = savedInstanceState.getString("CURRENT_FRAGMENT")
        if (fragmentTag != null) {
            // Usar un handler para ejecutar después de que onCreate() termine completamente
            findViewById<View>(android.R.id.content).post {
                if (::bottomNav.isInitialized) {
                    restoreFragmentFromTag(fragmentTag)
                }
            }
        }
    }

    private fun restoreFragmentFromTag(tag: String) {
        // Verificar que bottomNav esté inicializado antes de usarlo
        if (!::bottomNav.isInitialized) {
            return
        }

        val fragment = when (tag) {
            "PROFILE" -> {
                bottomNav.selectedItemId = R.id.navigation_profile
                ProfileFragment()
            }
            "GAMEROOM" -> {
                bottomNav.selectedItemId = R.id.navigation_home
                GameRoomFragment()
            }
            "SPORTSPACE" -> {
                bottomNav.selectedItemId = if (userRoleType == "PLAYER") R.id.navigation_sportspaces else R.id.navigation_sportspaces_prop
                SportSpaceFragment()
            }
            "RESERVATION" -> {
                bottomNav.selectedItemId = R.id.navigation_reservations
                ReservationFragment()
            }
            "SUBSCRIPTION" -> {
                bottomNav.selectedItemId = R.id.navigation_subscriptions
                SubscriptionFragment()
            }
            "TICKET" -> {
                bottomNav.selectedItemId = R.id.navigation_tickets
                TicketFragment()
            }
            else -> null
        }

        fragment?.let {
            // Forzar la carga del fragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, it)
                .commitNow() // Usar commitNow para asegurar que se ejecute inmediatamente
        }
    }
}