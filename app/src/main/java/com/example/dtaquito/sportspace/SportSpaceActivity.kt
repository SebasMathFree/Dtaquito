package com.example.dtaquito.sportspace//package com.example.dtaquito
//
//import Beans.sportspaces.SportSpace
//import Interface.PlaceHolder
//import android.content.Intent
//import android.os.Bundle
//import android.view.View
//import android.widget.Button
//import android.widget.Toast
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
//class SportSpaceActivity : PlayerBase() {
//
//    private lateinit var service: PlaceHolder
//    private lateinit var tokenManager: TokenManager
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var adapter: SportSpaceAdapter
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_sport_space)
//
//        tokenManager = TokenManager(this)
//        service = createRetrofit().create(PlaceHolder::class.java)
//        recyclerView = findViewById(R.id.recyclerView)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//
//        val userId = tokenManager.getUserId()
//        fetchSportSpaces(userId)
//
//        setupCreateSportSpaceButton()
//    }
//
//    private fun setupCreateSportSpaceButton() {
//        val createSportSpaceBtn = findViewById<Button>(R.id.create_sport_space_btn)
//        val userRoleType = getUserRoleType()
//        if (userRoleType == "P") {
//            createSportSpaceBtn.visibility = View.VISIBLE
//            setupBottomNavigation(R.id.navigation_sportspaces_prop)
//            createSportSpaceBtn.setOnClickListener {
//                startActivity(Intent(this, CreateSportSpaceActivity::class.java))
//            }
//        } else {
//            setupBottomNavigation(R.id.navigation_sportspaces)
//            createSportSpaceBtn.visibility = View.GONE
//        }
//    }
//
//    private fun getUserRoleType(): String? {
//        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
//        return sharedPreferences.getString("role_type", null)
//    }
//
//    private fun createRetrofit(): Retrofit {
//        val logging = HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
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
//    private fun fetchSportSpaces(userId: Int) {
//        val roleType = getUserRoleType() ?: "default"
//        service.getAllSportSpaces().enqueue(object : Callback<List<SportSpace>> {
//            override fun onResponse(call: Call<List<SportSpace>>, response: Response<List<SportSpace>>) {
//                if (response.isSuccessful) {
//                    val sportSpaces = response.body()?.filter {
//                        roleType == "R" || (roleType == "P" && it.user?.id == userId)
//                    }
//                    if (sportSpaces != null) {
//                        adapter = SportSpaceAdapter(sportSpaces)
//                        recyclerView.adapter = adapter
//                    } else {
//                        showToast("No sport spaces found")
//                    }
//                } else {
//                    showToast("Failed to fetch sport spaces")
//                }
//            }
//
//            override fun onFailure(call: Call<List<SportSpace>>, t: Throwable) {
//                showToast("Error: ${t.message}")
//            }
//        })
//    }
//
//    private fun showToast(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }
//}