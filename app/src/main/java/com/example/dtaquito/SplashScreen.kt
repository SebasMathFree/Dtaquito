package com.example.dtaquito

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.dtaquito.login.LoginActivity

class SplashScreen : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private val splashRunnable = Runnable {
        val intent = Intent(this@SplashScreen, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setFlags(
            FLAG_FULLSCREEN,
            FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash_screen)

        // Inicia el retraso para abrir la LoginActivity
        handler.postDelayed(splashRunnable, SPLASH_TIMER.toLong())
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancela el Runnable si la actividad se destruye
        handler.removeCallbacks(splashRunnable)
    }

    companion object {
        private const val SPLASH_TIMER = 2000
    }
}