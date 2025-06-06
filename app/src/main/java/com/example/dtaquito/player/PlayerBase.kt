package com.example.dtaquito.player

import Interface.PlaceHolder
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dtaquito.R
import com.example.dtaquito.profile.ProfileActivity
import com.example.dtaquito.sports.SportActivity
import com.example.dtaquito.sportspace.SportSpaceActivity
import com.example.dtaquito.subscription.SubscriptionActivity
import com.example.dtaquito.tickets.TicketActivity
import com.example.dtaquito.utils.showToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import network.RetrofitClient

open class PlayerBase : AppCompatActivity() {

    private lateinit var navigationMenu: BottomNavigationView
    private val service = RetrofitClient.instance.create(PlaceHolder::class.java)
    private var roleType: String = "default"
    protected open var initialCreditAmount: Double = 0.0

    protected fun setupBottomNavigation(selectedItemId: Int) {
        navigationMenu = findViewById(R.id.bottom_navigation)
        Log.d("PlayerBase", "setupBottomNavigation iniciado con selectedItemId: $selectedItemId")

        lifecycleScope.launch {
            try {
                val roleType = fillUserProfile()
                Log.d("PlayerBase", "RoleType obtenido: $roleType")
                configureNavigationMenu(roleType, selectedItemId)
            } catch (e: Exception) {
                Log.e("PlayerBase", "Error en setupBottomNavigation: ${e.message}", e)
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun configureNavigationMenu(roleType: String, selectedItemId: Int) {
        Log.d("PlayerBase", "Configurando menú de navegación para roleType: $roleType")
        val menuRes = if (roleType == "PLAYER") R.menu.menu_player else R.menu.menu_propietario
        navigationMenu.menu.clear()
        navigationMenu.inflateMenu(menuRes)
        navigationMenu.selectedItemId = selectedItemId

        navigationMenu.setOnNavigationItemSelectedListener { item ->
            val currentActivity = this::class.java
            Log.d("PlayerBase", "Item seleccionado: ${item.itemId}")
            when (item.itemId) {
                R.id.navigation_profile -> {
                    Log.d("PlayerBase", "Navegando a ProfileActivity")
                    navigateToActivity(ProfileActivity::class.java, currentActivity)
                }
                R.id.navigation_home -> {
                    Log.d("PlayerBase", "Navegando a SportActivity")
                    navigateToActivity(SportActivity::class.java, currentActivity)
                }
                R.id.navigation_sportspaces_prop -> {
                    Log.d("PlayerBase", "Navegando a SportSpaceActivity")
                    navigateToActivity(SportSpaceActivity::class.java, currentActivity)
                }
                R.id.navigation_sportspaces -> {
                    Log.d("PlayerBase", "Navegando a SportActivity")
                    navigateToActivity(SportActivity::class.java, currentActivity)
                }
                R.id.navigation_suscriptions -> {
                    Log.d("PlayerBase", "Navegando a SubscriptionActivity")
                    navigateToActivity(SubscriptionActivity::class.java, currentActivity)
                }
                R.id.navigation_tickets -> {
                    Log.d("PlayerBase", "Navegando a TicketActivity con créditos: $initialCreditAmount")
                    val extras = Bundle()
                    extras.putDouble("USER_CREDITS", initialCreditAmount)
                    navigateToActivity(TicketActivity::class.java, currentActivity, extras)
                    true
                }
                else -> {
                    Log.d("PlayerBase", "Item no reconocido")
                    false
                }
            }
        }
    }

    private fun navigateToActivity(targetActivity: Class<*>, currentActivity: Class<*>, extras: Bundle? = null): Boolean {
        Log.d("PlayerBase", "Navegando de $currentActivity a $targetActivity")
        if (currentActivity != targetActivity) {
            val intent = Intent(this, targetActivity)
            extras?.let {
                Log.d("PlayerBase", "Extras añadidos al Intent: $it")
                intent.putExtras(it)
            }
            startActivity(intent)
        }
        return true
    }

    protected suspend fun fillUserProfile(): String {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("PlayerBase", "Obteniendo datos del usuario...")
                val response = service.getUserId()
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        roleType = user.roleType
                        Log.d("PlayerBase", "Datos del usuario obtenidos: $user")
                        return@withContext roleType
                    } ?: throw Exception("User data is null")
                } else {
                    throw Exception("Failed to fetch user data: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PlayerBase", "Error en fillUserProfile: ${e.message}", e)
                throw e
            }
        }
    }
}