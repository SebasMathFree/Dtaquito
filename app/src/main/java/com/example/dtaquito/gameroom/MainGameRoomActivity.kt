package com.example.dtaquito.gameroom//package com.example.dtaquito
//
//import Beans.rooms.GameRoom
//import Beans.playerList.PlayerList
//import Interface.PlaceHolder
//import android.content.Intent
//import android.graphics.Typeface
//import android.os.Bundle
//import android.text.SpannableString
//import android.text.style.StyleSpan
//import android.util.Log
//import android.view.View
//import android.widget.Button
//import android.widget.TextView
//import android.widget.Toast
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
//import java.text.SimpleDateFormat
//import java.util.Locale
//import java.util.TimeZone
//
//class MainGameRoomActivity : PlayerBase() {
//
//    private lateinit var service: PlaceHolder
//    private lateinit var tokenManager: TokenManager
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var roomNameTextView: TextView
//    private lateinit var districtTextView: TextView
//    private lateinit var dateTextView: TextView
//    private lateinit var timeTextView: TextView
//    private lateinit var formatTextView: TextView
//    private lateinit var joinBtn: Button
//    private lateinit var openChatButton: Button
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main_game_room)
//        setupBottomNavigation(R.id.navigation_home)
//        tokenManager = TokenManager(this)
//
//        service = createRetrofit().create(PlaceHolder::class.java)
//
//        initializeViews()
//        setupRecyclerViews()
//
//        val gameRoomId = intent.getIntExtra("GAME_ROOM_ID", -1)
//        val userId = tokenManager.getUserId()
//
//        if (gameRoomId != -1) {
//            fetchGameRoomDetails(gameRoomId)
//            fetchPlayerListByRoomId(gameRoomId)
//        } else {
//            showToast("Invalid game room ID")
//        }
//
//        joinBtn.setOnClickListener {
//            Log.d("MainGameRoomActivity", "joinButton clicked: userId = $userId, gameRoomId = $gameRoomId")
//            if (userId != -1 && gameRoomId != -1) {
//                joinRoom(userId, gameRoomId)
//            } else {
//                showToast("Invalid user ID or game room ID")
//            }
//        }
//        openChatButton.setOnClickListener {
//            val intent = Intent(this, ChatRoomActivity::class.java)
//            intent.putExtra("GAME_ROOM_ID", gameRoomId)
//            intent.putExtra("USER_ID", userId)
//            startActivity(intent)
//        }
//
//    }
//
//    private fun createRetrofit(): Retrofit {
//        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
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
//    private fun initializeViews() {
//        roomNameTextView = findViewById(R.id.roomName)
//        districtTextView = findViewById(R.id.district)
//        dateTextView = findViewById(R.id.date)
//        timeTextView = findViewById(R.id.time)
//        formatTextView = findViewById(R.id.format)
//        joinBtn = findViewById(R.id.joinButton)
//        openChatButton = findViewById(R.id.openChatButton)
//    }
//    private fun setupRecyclerViews() {
//        recyclerView = findViewById(R.id.playerList)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//    }
//    private fun fetchGameRoomDetails(gameRoomId: Int) {
//        service.getRoomById(gameRoomId).enqueue(object : Callback<GameRoom> {
//            override fun onResponse(call: Call<GameRoom>, response: Response<GameRoom>) {
//                if (response.isSuccessful) {
//                    response.body()?.let { gameRoom ->
//                        updateGameRoomDetails(gameRoom)
//                    } ?: run {
//                        logAndShowError("No game rooms found")
//                    }
//                } else {
//                    logAndShowError("Failed to fetch game rooms: HTTP ${response.code()}")
//                }
//            }
//
//            override fun onFailure(call: Call<GameRoom>, t: Throwable) {
//                logAndShowError("Error: ${t.message}")
//            }
//        })
//    }
//    private fun updateGameRoomDetails(gameRoom: GameRoom) {
//        roomNameTextView.text = gameRoom.roomName
//        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()).apply {
//            timeZone = TimeZone.getTimeZone("UTC")
//        }
//        val date = inputFormat.parse(gameRoom.openingDate)
//        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
//        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
//        val districtText = "Distrito: ${gameRoom.sportSpace?.district}"
//        val districtSpannable = SpannableString(districtText)
//        districtSpannable.setSpan(StyleSpan(Typeface.BOLD), 0, 9, 0)
//        districtTextView.text = districtSpannable
//
//        val dateText = "Fecha: ${dateFormat.format(date)}"
//        val dateSpannable = SpannableString(dateText)
//        dateSpannable.setSpan(StyleSpan(Typeface.BOLD), 0, 6, 0)
//        dateTextView.text = dateSpannable
//
//        val timeText = "Hora: ${timeFormat.format(date)}"
//        val timeSpannable = SpannableString(timeText)
//        timeSpannable.setSpan(StyleSpan(Typeface.BOLD), 0, 5, 0)
//        timeTextView.text = timeSpannable
//
//        val formatText = "Formato: ${gameRoom.sportSpace?.gamemode}"
//        val formatSpannable = SpannableString(formatText)
//        formatSpannable.setSpan(StyleSpan(Typeface.BOLD), 0, 8, 0)
//        formatTextView.text = formatSpannable
//    }
//    private fun fetchPlayerListByRoomId(roomId: Int) {
//        service.getPlayerListByRoomId(roomId).enqueue(object : Callback<List<PlayerList>> {
//            override fun onResponse(call: Call<List<PlayerList>>, response: Response<List<PlayerList>>) {
//                if (response.isSuccessful) {
//                    response.body()?.let { playerList ->
//                        recyclerView.adapter = PlayerListAdapter(playerList, service)
//                        val isUserInList = playerList.any { it.userId == intent.getIntExtra("USER_ID", -1) }
//                        joinBtn.visibility = if (isUserInList) View.GONE else View.VISIBLE
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<List<PlayerList>>, t: Throwable) {
//                logAndShowError("Error: ${t.message}")
//            }
//        })
//    }
//    private fun refreshView() {
//        val gameRoomId = intent.getIntExtra("GAME_ROOM_ID", -1)
//        if (gameRoomId != -1) {
//            fetchGameRoomDetails(gameRoomId)
//            fetchPlayerListByRoomId(gameRoomId)
//        } else {
//            showToast("Invalid game room ID")
//        }
//    }
//    private fun joinRoom(userId: Int, roomId: Int) {
//        service.joinRoom(userId, roomId).enqueue(object : Callback<Void> {
//            override fun onResponse(call: Call<Void>, response: Response<Void>) {
//                if (response.isSuccessful) {
//                    showToast("Joined room successfully")
//                    refreshView()
//                } else {
//                    showToast("Failed to join room")
//                }
//            }
//
//            override fun onFailure(call: Call<Void>, t: Throwable) {
//                logAndShowError("Error: ${t.message}")
//            }
//        })
//    }
//    private fun showToast(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }
//    private fun logAndShowError(message: String) {
//        Log.e("MainGameRoomActivity", message)
//        showToast(message)
//    }
//}