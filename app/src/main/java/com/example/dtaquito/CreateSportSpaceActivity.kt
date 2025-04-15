//package com.example.dtaquito
//
//import Beans.sportspaces.SportSpace2
//import Interface.PlaceHolder
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.widget.AdapterView
//import android.widget.ArrayAdapter
//import android.widget.Button
//import android.widget.EditText
//import android.widget.Spinner
//import androidx.activity.enableEdgeToEdge
//import com.example.dtaquito.auth.TokenManager
//import okhttp3.Interceptor
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
//class CreateSportSpaceActivity : PlayerBase() {
//
//    lateinit var service: PlaceHolder
//    lateinit var tokenManager: TokenManager
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_create_sport_space)
//        setupBottomNavigation(R.id.navigation_sportspaces_prop)
//
//        tokenManager = TokenManager(this)
//        val retrofit = createRetrofit()
//        service = retrofit.create(PlaceHolder::class.java)
//
//        val spinnerSport = findViewById<Spinner>(R.id.sport_input)
//        val spinnerLocations = findViewById<Spinner>(R.id.location_input)
//        val spinnerFormat = findViewById<Spinner>(R.id.format_input)
//        setupSpinners(spinnerSport, spinnerLocations, spinnerFormat)
//        val nameInput = findViewById<EditText>(R.id.name_input)
//        val descriptionInput = findViewById<EditText>(R.id.description_input)
//
//        val startTimeInput = findViewById<EditText>(R.id.start_time_input)
//        startTimeInput.setOnClickListener { showTimePickerDialog(startTimeInput) }
//
//        val endTimeInput = findViewById<EditText>(R.id.end_time_input)
//        endTimeInput.setOnClickListener { showTimePickerDialog(endTimeInput) }
//
//        val createSportSpaceButton = findViewById<Button>(R.id.create_space_btn)
//        createSportSpaceButton.setOnClickListener{
//            val name = nameInput.text.toString()
//            val description = descriptionInput.text.toString()
//            val startTime = startTimeInput.text.toString()
//            val endTime = endTimeInput.text.toString()
//            val sportType = spinnerSport.selectedItem.toString()
//            val district = spinnerLocations.selectedItem.toString()
//            val gamemode = spinnerFormat.selectedItem.toString()
//            val price = findViewById<EditText>(R.id.price_input).text.toString().toDoubleOrNull() ?: 0.0
//            val numberOfPlayers = when (gamemode) {
//                "BILLAR_3" -> 3
//                "FUTBOL_5" -> 10
//                "FUTBOL_7" -> 14
//                "FUTBOL_8" -> 16
//                "FUTBOL_11" -> 22
//                else -> 1
//            }
//            val amount = (price / 2 / numberOfPlayers).toInt()
//            val sportId = when (sportType) {
//                "Futbol" -> 1
//                "Billar" -> 2
//                else -> 0
//            }
//            val userid = tokenManager.getUserId()
//            val imageUrl = findViewById<EditText>(R.id.img_url).text.toString() // Retrieve the image URL
//
//            val sportSpace = SportSpace2(
//                name = name,
//                sportId = sportId,
//                imageUrl = imageUrl,
//                price = price,
//                district = district,
//                description = description,
//                userId = userid,
//                startTime = startTime,
//                endTime = endTime,
//                rating = 0, // Set rating as needed
//                gamemode = gamemode,
//                amount = amount
//            )
//
//            service.createSportSpace(sportSpace).enqueue(object : retrofit2.Callback<SportSpace2> {
//                override fun onResponse(
//                    call: retrofit2.Call<SportSpace2>,
//                    response: retrofit2.Response<SportSpace2>
//                ) {
//                    if (response.isSuccessful) {
//                        Log.d("CreateSportSpaceActivity", "Sport space created successfully")
//                        val intent = Intent(this@CreateSportSpaceActivity, SportSpaceActivity::class.java)
//                        startActivity(intent)
//                        finish()
//                    } else {
//                        Log.e("CreateSportSpaceActivity", "Failed to create sport space")
//                    }
//                }
//
//                override fun onFailure(call: retrofit2.Call<SportSpace2>, t: Throwable) {
//                    Log.e("CreateSportSpaceActivity", "Error: ${t.message}")
//                }
//            })
//        }
//
//    }
//
//    private fun setupSpinners(
//        spinnerSport: Spinner,
//        spinnerLocations: Spinner,
//        spinnerFormat: Spinner
//    ) {
//        val itemsSport = listOf("Deporte", "Futbol", "Billar")
//        val itemsLocations = listOf(
//            "Distrito",
//            "Ancón",
//            "Ate",
//            "Barranco",
//            "Breña",
//            "Carabayllo",
//            "Cercado de Lima",
//            "Chaclacayo",
//            "Chorrillos",
//            "Cieneguilla",
//            "Comas",
//            "El agustino",
//            "Independencia",
//            "Jesús maría",
//            "La molina",
//            "La victoria",
//            "Lince",
//            "Los olivos",
//            "Lurigancho",
//            "Lurín",
//            "Magdalena del mar",
//            "Miraflores",
//            "Pachacámac",
//            "Pucusana",
//            "Pueblo libre",
//            "Puente piedra",
//            "Punta hermosa",
//            "Punta negra",
//            "Rímac",
//            "San bartolo",
//            "San borja",
//            "San isidro",
//            "San Juan de Lurigancho",
//            "San Juan de Miraflores",
//            "San Luis",
//            "San Martin de Porres",
//            "San Miguel",
//            "Santa Anita",
//            "Santa María del Mar",
//            "Santa Rosa",
//            "Santiago de Surco",
//            "Surquillo",
//            "Villa el Salvador",
//            "Villa Maria del Triunfo"
//        )
//        val itemsFormatBillar = listOf("Formato", "BILLAR_3")
//        val itemsFormatFutbol = listOf("Formato", "FUTBOL_5", "FUTBOL_7", "FUTBOL_8", "FUTBOL_11")
//
//        val adapterSport = ArrayAdapter(this, R.layout.spinner_items, itemsSport)
//        adapterSport.setDropDownViewResource(R.layout.spinner_items)
//        spinnerSport.adapter = adapterSport
//        spinnerSport.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {
//                if (position != 0) {
//                    val selectedSport = itemsSport[position]
//                    Log.d("CreateSportSpaceActivity", "Selected Sport: $selectedSport")
//                    updateFormatSpinner(
//                        spinnerFormat,
//                        selectedSport,
//                        itemsFormatBillar,
//                        itemsFormatFutbol
//                    )
//                }
//            }
//
//            override fun onNothingSelected(p0: AdapterView<*>?) {
//                Log.d("CreateSportSpaceActivity", "Nothing selected")
//            }
//        }
//
//        val adapterLocations = ArrayAdapter(this, R.layout.spinner_items, itemsLocations)
//        adapterLocations.setDropDownViewResource(R.layout.spinner_items)
//        spinnerLocations.adapter = adapterLocations
//        spinnerLocations.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {
//                if (position != 0) {
//                    val selectedLocation = itemsLocations[position]
//                    Log.d("CreateSportSpaceActivity", "Selected Location: $selectedLocation")
//                }
//            }
//
//            override fun onNothingSelected(p0: AdapterView<*>?) {
//                Log.d("CreateSportSpaceActivity", "Nothing selected")
//            }
//        }
//    }
//
//    private fun updateFormatSpinner(
//        spinnerFormat: Spinner,
//        selectedSport: String,
//        itemsFormatBillar: List<String>,
//        itemsFormatFutbol: List<String>
//    ) {
//        val adapterFormat = when (selectedSport) {
//            "Billar" -> ArrayAdapter(this, R.layout.spinner_items, itemsFormatBillar)
//            "Futbol" -> ArrayAdapter(this, R.layout.spinner_items, itemsFormatFutbol)
//            else -> ArrayAdapter(this, R.layout.spinner_items, emptyList<String>())
//        }
//        adapterFormat.setDropDownViewResource(R.layout.spinner_items)
//        spinnerFormat.adapter = adapterFormat
//    }
//
//    private fun showTimePickerDialog(editText: EditText) {
//        val timePicker = TimePickerFragment { onTimeSelected(editText, it) }
//        timePicker.show(supportFragmentManager, "timePicker")
//    }
//
//    private fun onTimeSelected(editText: EditText, time: String) {
//        editText.setText(time)
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
//
//}