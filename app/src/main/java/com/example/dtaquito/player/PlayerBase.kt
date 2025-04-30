package com.example.dtaquito.player

import MyCookieJar
import android.content.Intent
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import com.example.dtaquito.R
import com.example.dtaquito.profile.ProfileActivity
import com.example.dtaquito.suscription.SuscriptionActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject

open class PlayerBase : AppCompatActivity() {

    private lateinit var navigationMenu: BottomNavigationView

    private val cookieJar = MyCookieJar()

    protected fun setupBottomNavigation(selectedItemId: Int) {
        navigationMenu = findViewById(R.id.bottom_navigation)
        val roleType = getRoleFromCookie()
        val menuRes = if (roleType == "PLAYER") {
            R.menu.menu_player
        } else {
            R.menu.menu_propietario
        }
        navigationMenu.menu.clear()
        navigationMenu.inflateMenu(menuRes)
        navigationMenu.selectedItemId = selectedItemId

        navigationMenu.setOnNavigationItemSelectedListener { item ->
            val currentActivity = this::class.java
            when (item.itemId) {
                R.id.navigation_profile -> {
                    if (currentActivity != ProfileActivity::class.java) {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    }
                    true
                }
                R.id.navigation_home -> {
                    if (currentActivity != ProfileActivity::class.java) {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    }
                    true
                }
                R.id.navigation_sportspaces_prop -> {
                    if (currentActivity != ProfileActivity::class.java) {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    }
                    true
                }
                R.id.navigation_sportspaces -> {
                    if (currentActivity != ProfileActivity::class.java) {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    }
                    true
                }
                R.id.navigation_suscriptions -> {
                    if (currentActivity != SuscriptionActivity::class.java) {
                        startActivity(Intent(this, SuscriptionActivity::class.java))
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun getRoleFromCookie(): String {
        val cookies: List<Cookie> = cookieJar.loadForRequest("http://10.0.2.2:8080".toHttpUrlOrNull()!!)
        for (cookie in cookies) {
            if (cookie.name == "JWT_TOKEN") {
                return parseRoleFromJwt(cookie.value)
            }
        }
        return "default"
    }

    private fun parseRoleFromJwt(jwt: String): String {
        val segments = jwt.split(".")
        if (segments.size == 3) {
            val payload = segments[1]
            val decodedPayload = String(Base64.decode(payload, Base64.URL_SAFE))

            val jsonObject = JSONObject(decodedPayload)
            return jsonObject.getString("role")
        }
        return "default"
    }
}
