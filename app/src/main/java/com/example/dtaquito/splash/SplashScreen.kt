package com.example.dtaquito.splash

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.dtaquito.MainActivity
import com.example.dtaquito.R
import com.example.dtaquito.login.LoginActivity

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setFlags(
            FLAG_FULLSCREEN,
            FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash_screen)

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val savedUserId = prefs.getInt("user_id", -1)

        android.os.Handler().postDelayed({
            val intent = if (savedUserId != -1) {
                Intent(this@SplashScreen, MainActivity::class.java)
            } else {
                Intent(this@SplashScreen, LoginActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, SPLASH_TIMER.toLong())
    }

    companion object {
        private const val SPLASH_TIMER = 2000
    }
}