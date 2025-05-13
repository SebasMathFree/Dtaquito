package com.example.dtaquito.player

import Interface.PlaceHolder
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dtaquito.R
import com.example.dtaquito.profile.ProfileActivity
import com.example.dtaquito.sports.SportActivity
import com.example.dtaquito.sportspace.SportSpaceActivity
import com.example.dtaquito.suscription.SubscriptionActivity
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

    protected fun setupBottomNavigation(selectedItemId: Int) {
        navigationMenu = findViewById(R.id.bottom_navigation)

        lifecycleScope.launch {
            try {
                val roleType = fillUserProfile()
                configureNavigationMenu(roleType, selectedItemId)
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun configureNavigationMenu(roleType: String, selectedItemId: Int) {
        val menuRes = if (roleType == "PLAYER") R.menu.menu_player else R.menu.menu_propietario
        navigationMenu.menu.clear()
        navigationMenu.inflateMenu(menuRes)
        navigationMenu.selectedItemId = selectedItemId

        navigationMenu.setOnNavigationItemSelectedListener { item ->
            val currentActivity = this::class.java
            when (item.itemId) {
                R.id.navigation_profile -> navigateToActivity(ProfileActivity::class.java, currentActivity)
                R.id.navigation_home -> navigateToActivity(SportActivity::class.java, currentActivity)
                R.id.navigation_sportspaces_prop -> navigateToActivity(SportSpaceActivity::class.java, currentActivity)
                R.id.navigation_sportspaces -> navigateToActivity(SportActivity::class.java, currentActivity)
                R.id.navigation_suscriptions -> navigateToActivity(SubscriptionActivity::class.java, currentActivity)
                else -> false
            }
        }
    }

    private fun navigateToActivity(targetActivity: Class<*>, currentActivity: Class<*>): Boolean {
        if (currentActivity != targetActivity) {
            startActivity(Intent(this, targetActivity))
        }
        return true
    }

    protected suspend fun fillUserProfile(): String {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.getUserId()
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        roleType = user.roleType
                        return@withContext roleType
                    } ?: throw Exception("User data is null")
                } else {
                    throw Exception("Failed to fetch user data: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("fillUserProfile", "Error: ${e.message}", e)
                throw e
            }
        }
    }

}