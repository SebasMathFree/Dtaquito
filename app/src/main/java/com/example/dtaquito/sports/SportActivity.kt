//package com.example.dtaquito
//
//import android.content.Intent
//import android.os.Bundle
//import android.view.View
//import androidx.activity.enableEdgeToEdge
//import com.example.dtaquito.auth.TokenManager
//
//class SportActivity : PlayerBase() {
//    lateinit var tokenManager: TokenManager
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_sport)
//        setupBottomNavigation(R.id.navigation_home)
//        tokenManager = TokenManager(this)
//
//    }
//    fun soccer(view: View) {
//        val texto = view.contentDescription.toString()
//        val soccerString = getString(R.string.soccer)
//        if (texto == soccerString) {
//            val userId = tokenManager.getUserId()
//            val intent = Intent(this, GameRoomActivity::class.java)
//            intent.putExtra("USER_ID", userId)
//            intent.putExtra("SPORT_ID", "1")
//            startActivity(intent)
//        }
//    }
//
//    fun pool(view: View) {
//        val texto = view.contentDescription.toString()
//        val poolString = getString(R.string.pool)
//        if (texto == poolString) {
//            val userId = tokenManager.getUserId()
//            val intent = Intent(this, GameRoomActivity::class.java)
//            intent.putExtra("USER_ID", userId)
//            intent.putExtra("SPORT_ID", "2")
//            startActivity(intent)
//        }
//    }
//
//}