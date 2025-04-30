package com.example.dtaquito.gameroom//package com.example.dtaquito
//
//import Beans.rooms.GameRoom
//import Interface.PlaceHolder
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Button
//import androidx.activity.enableEdgeToEdge
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.dtaquito.auth.TokenManager
//import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
//class GameRoomActivity : PlayerBase() {
//
//    lateinit var service: PlaceHolder
//    lateinit var tokenManager: TokenManager
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_soccer_room)
//        setupBottomNavigation(R.id.navigation_home )
//
//        tokenManager = TokenManager(this)
//
//
//        val retrofit = createRetrofit()
//        service = retrofit.create<PlaceHolder>(PlaceHolder::class.java)
//
//        val sportId = intent.getStringExtra("SPORT_ID")?.toIntOrNull() ?: 0
//        getAllRooms(sportId)
//
//        val createRoomBtn = findViewById<Button>(R.id.create_room_btn)
//        createRoomBtn.setOnClickListener {
//            val intent = Intent(this, CreateRoomActivity::class.java)
//            startActivity(intent)
//        }
//
//    }
//
//    private fun createRetrofit(): Retrofit {
//        val logging = HttpLoggingInterceptor()
//        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
//
//        val client = OkHttpClient.Builder()
//            .addInterceptor(logging)
//            .addInterceptor(AuthInterceptor(tokenManager))
//            .build()
//
//        return Retrofit.Builder()
//            .baseUrl("https://dtaquito-backend.azurewebsites.net/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(client)
//            .build()
//    }
//
//    private fun getAllRooms(sportId: Int) {
//        val token = tokenManager.getToken()
//        if (token != null) {
//            service.getAllRooms().enqueue(object : Callback<List<GameRoom>> {
//                override fun onResponse(call: Call<List<GameRoom>>, response: Response<List<GameRoom>>) {
//                    if (response.isSuccessful) {
//                        val gameRooms = response.body()
//                        val filteredGameRooms = gameRooms?.filter { it.sportSpace?.sportId == sportId }
//                        if (filteredGameRooms != null) {
//                            val recycler = findViewById<RecyclerView>(R.id.recyclerView)
//                            recycler.layoutManager = LinearLayoutManager(applicationContext)
//                            recycler.adapter = GameRoomAdapter(filteredGameRooms)
//                        } else {
//                            println("Game rooms not found")
//                        }
//                    } else {
//                        println("Failed to get game rooms with status code: ${response.code()}")
//                    }
//                }
//
//                override fun onFailure(call: Call<List<GameRoom>>, t: Throwable) {
//                    t.printStackTrace()
//                }
//            })
//        } else {
//            println("Token not found")
//        }
//    }
//}