package com.example.dtaquito.splash

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager.LayoutParams.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
        android.os.Handler().postDelayed({
            val intent = Intent(this@SplashScreen, LoginActivity::class.java)
            startActivity(intent)
            finish()
        },
        SPLASH_TIMER.toLong()
        )

    }
    companion object {
        private const val SPLASH_TIMER = 2000
    }
}