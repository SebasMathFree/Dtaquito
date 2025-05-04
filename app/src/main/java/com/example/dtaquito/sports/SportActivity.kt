package com.example.dtaquito.sports

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import com.example.dtaquito.R
import com.example.dtaquito.player.PlayerBase
import com.example.dtaquito.profile.ProfileActivity

class SportActivity : PlayerBase() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sport)
        setupBottomNavigation(R.id.navigation_home)
    }
    fun soccer(view: View) {
        val texto = view.contentDescription.toString()
        val soccerString = getString(R.string.soccer)
        if (texto == soccerString) {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("SPORT_ID", "1")
            startActivity(intent)
        }
    }

    fun pool(view: View) {
        val texto = view.contentDescription.toString()
        val poolString = getString(R.string.pool)
        if (texto == poolString) {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("SPORT_ID", "2")
            startActivity(intent)
        }
    }

}