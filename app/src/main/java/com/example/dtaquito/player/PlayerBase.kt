package com.example.dtaquito.player

import Beans.userProfile.UserProfile
import Interface.PlaceHolder
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dtaquito.R
import com.example.dtaquito.auth.CookieInterceptor
import com.example.dtaquito.auth.SaveCookieInterceptor
import com.example.dtaquito.profile.ProfileActivity
import com.example.dtaquito.sports.SportActivity
import com.example.dtaquito.sportspace.SportSpaceActivity
import com.example.dtaquito.suscription.SuscriptionActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

open class PlayerBase : AppCompatActivity() {

    private lateinit var navigationMenu: BottomNavigationView
    private lateinit var service: PlaceHolder
    private var roleType: String = "default"

    protected fun setupBottomNavigation(selectedItemId: Int) {
        navigationMenu = findViewById(R.id.bottom_navigation)
        service = createRetrofitService(this)

        fillUserProfile { roleType ->
            Log.d("setupBottomNavigation", "Role Type: $roleType")
            configureNavigationMenu(roleType, selectedItemId)
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
                R.id.navigation_suscriptions -> navigateToActivity(SuscriptionActivity::class.java, currentActivity)
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

    protected fun fillUserProfile(onRoleFetched: (String) -> Unit) {
        service.getUserId().enqueue(object : Callback<UserProfile> {
            override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        roleType = user.roleType
                        onRoleFetched(roleType)
                    }
                } else {
                    showToast("Failed to fetch user data")
                }
            }

            override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                Log.e("fillUserProfile", "Error: ${t.message}", t)
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun createRetrofitService(context: Context): PlaceHolder {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(SaveCookieInterceptor(context))
            .addInterceptor(CookieInterceptor(context))
            .build()

        return Retrofit.Builder()
            .baseUrl("https://dtaquito-backend.azurewebsites.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(PlaceHolder::class.java)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}