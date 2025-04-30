//package com.example.dtaquito
//
//import Beans.rooms.GameRoom
//import Beans.playerList.Player
//import Beans.sportspaces.SportSpace
//import Beans.userProfile.UserProfile
//import Interface.PlaceHolder
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.widget.Button
//import android.widget.EditText
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import com.example.dtaquito.auth.TokenManager
//import okhttp3.Interceptor
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import java.text.SimpleDateFormat
//import java.util.Locale
//
//class CreateRoomActivity : PlayerBase() {
//
//    lateinit var service: PlaceHolder
//    lateinit var tokenManager: TokenManager
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_create_room)
//        setupBottomNavigation(R.id.navigation_home)
//
//        tokenManager = TokenManager(this)
//
//
//        val retrofit = createRetrofit()
//        service = retrofit.create(PlaceHolder::class.java)
//
//        val roomNameInput = findViewById<EditText>(R.id.room_name_input)
//        val idSportSpace = findViewById<EditText>(R.id.sport_space_id)
//        val dateInput = findViewById<EditText>(R.id.date_input)
//        dateInput.setOnClickListener { showDatePickerDialog() }
//        val timeInput = findViewById<EditText>(R.id.time_input)
//        timeInput.setOnClickListener { showTimePickerDialog() }
//
//        val createBtn = findViewById<Button>(R.id.create_btn)
//
//        createBtn.setOnClickListener {
//            val roomName = roomNameInput.text.toString().trim()
//            val idSS = idSportSpace.text.toString().toLongOrNull()
//            val date = dateInput.text.toString()
//            val time = timeInput.text.toString()
//
//            if (roomName.isEmpty() || idSS == null || date.isEmpty() || time.isEmpty()) {
//                Toast.makeText(this, "Please fill all the fields correctly", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            val creatorId = tokenManager.getUserId()
//
//            getSportSpaceDetails(idSS.toInt()) { sportSpace ->
//                if (sportSpace != null) {
//                    val inputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
//                    val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//                    val dateTime = inputFormat.parse("$date $time")
//                    val formattedDate = dateTime?.let { it1 -> outputFormat.format(it1) }
//                    if (formattedDate != null) {
//                        getUserDetails(creatorId) { creator ->
//                            if (creator != null) {
//                                createRoom(creator, sportSpace, time, formattedDate, roomName)
//                            } else {
//                                Toast.makeText(this, "Creator not found", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    }
//                } else {
//                    Toast.makeText(this, "SportSpace not found", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//
//
//    }
//
//    private fun createRetrofit(): Retrofit {
//        val logging = HttpLoggingInterceptor()
//        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
//
//        val urlLoggingInterceptor = Interceptor { chain ->
//            val request: Request = chain.request()
//            Log.d("URLInterceptor", "Request URL: ${request.url}")
//            chain.proceed(request)
//        }
//
//        val client = OkHttpClient.Builder()
//            .addInterceptor(logging)
//            .addInterceptor(urlLoggingInterceptor)
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
//    private fun getUserDetails(userId: Int, callback: (UserProfile?) -> Unit) {
//        service.getUserId(userId).enqueue(object : Callback<UserProfile> {
//            override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
//                if (response.isSuccessful) {
//                    callback(response.body())
//                } else {
//                    callback(null)
//                }
//            }
//
//            override fun onFailure(call: Call<UserProfile>, t: Throwable) {
//                t.printStackTrace()
//                callback(null)
//            }
//        })
//    }
//
//    private fun getSportSpaceDetails(id: Int, callback: (SportSpace?) -> Unit) {
//        service.getSportSpaceById(id).enqueue(object : Callback<SportSpace> {
//            override fun onResponse(call: Call<SportSpace>, response: Response<SportSpace>) {
//                if (response.isSuccessful) {
//                    callback(response.body())
//                } else {
//                    callback(null)
//                }
//            }
//
//            override fun onFailure(call: Call<SportSpace>, t: Throwable) {
//                t.printStackTrace()
//                callback(null)
//            }
//        })
//    }
//
//    private fun createRoom(creator: UserProfile, sportSpace: SportSpace, day: String, openingDate: String, roomName: String) {
//        val players = mutableListOf<Player>()
//        val creatorPlayer = Player(creator.id?:0,creator.name)
//        players.add(creatorPlayer)
//        service.createRoom(creatorId = tokenManager.getUserId().toLong(), sportSpaceId = sportSpace.id.toLong(),
//            day = day, openingDate = openingDate, roomName = roomName).enqueue(object : Callback<GameRoom> {
//            override fun onResponse(call: Call<GameRoom>, response: Response<GameRoom>) {
//                if (response.isSuccessful) {
//                    val createdRoom = response.body()
//                    Log.d("CreateRoomActivity", "Room created: $createdRoom")
//                    Toast.makeText(this@CreateRoomActivity, "Room created successfully", Toast.LENGTH_SHORT).show()
//                    val intent = Intent(this@CreateRoomActivity, GameRoomActivity::class.java)
//                    startActivity(intent)
//                    finish()
//                } else {
//                    val errorCode = response.code()
//                    Log.e("CreateRoomActivity", "Failed to create room: HTTP $errorCode")
//                    Toast.makeText(this@CreateRoomActivity, "Failed to create room: HTTP $errorCode", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<GameRoom>, t: Throwable) {
//                t.printStackTrace()
//                Log.e("CreateRoomActivity", "Error: ${t.message}")
//                Toast.makeText(this@CreateRoomActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    private fun showTimePickerDialog() {
//        val timePicker = TimePickerFragment { onTimeSelected(it) }
//        timePicker.show(supportFragmentManager, "timePicker")
//    }
//
//    private fun onTimeSelected(time: String) {
//        val timeInput = findViewById<EditText>(R.id.time_input)
//        timeInput.setText(time)
//    }
//
//    private fun showDatePickerDialog() {
//        val datePicker = DatePickerFragment { day, month, year -> onDateSelected(day, month, year) }
//        datePicker.show(supportFragmentManager, "datePicker")
//    }
//
//    private fun onDateSelected(day: Int, month: Int, year: Int) {
//        val dateInput = findViewById<EditText>(R.id.date_input)
//        dateInput.setText("$day/$month/$year")
//    }
//}