package com.example.dtaquito

import MyCookieJar
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Response
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
                else -> false
            }
        }
    }

    private fun getRoleFromCookie(): String {
        val cookies: List<Cookie> = cookieJar.loadForRequest("http://10.0.2.2:8080".toHttpUrlOrNull()!!) // Asegúrate de que la URL de tu backend esté correcta
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
