//package com.example.dtaquito.sportspace
//
//import Beans.sportspaces.SportSpace2
//import Interface.PlaceHolder
//import android.content.Context
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
//import com.example.dtaquito.R
//import com.example.dtaquito.auth.CookieInterceptor
//import com.example.dtaquito.auth.SaveCookieInterceptor
//import com.example.dtaquito.player.PlayerBase
//import com.example.dtaquito.time.TimePickerFragment
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
//    private var userId: Int = 0
//
//    companion object {
//        private const val BASE_URL = "http://10.0.2.2:8080/"
//        private const val JWT_COOKIE_NAME = "JWT_TOKEN"
//    }
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_create_sport_space)
//        setupBottomNavigation(R.id.navigation_sportspaces_prop)
//
//        service = createRetrofitService(this)
//
//        //Obtenemos el ID del usuario desde el perfil
//        fillUserProfile { roleType ->
//            userId = getUserIdFromProfile() // Metodo para obtener el ID del usuario
//        }
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
//        createSportSpaceButton.setOnClickListener {
//            val name = nameInput.text.toString()
//            val description = descriptionInput.text.toString()
//            val openTime = startTimeInput.text.toString()
//            val closeTime = endTimeInput.text.toString()
//            val sportType = spinnerSport.selectedItem.toString()
//            val district = spinnerLocations.selectedItem.toString()
//            val address = findViewById<EditText>(R.id.address_input).text.toString()
//            val price = findViewById<EditText>(R.id.price_input).text.toString().toDoubleOrNull() ?: 0.0
//            val gamemode = spinnerFormat.selectedItem.toString()
//            val sportId = when (sportType) {
//                "FUTBOL" -> 1
//                "BILLAR" -> 2
//                else -> 0
//            }
//            val districtId = when (district) {
//                "San_Miguel" -> 1
//                "San_Borja" -> 2
//                "San_Isidro" -> 3
//                "Surco" -> 4
//                "Magdalena" -> 5
//                "Pueblo_Libre" -> 6
//                "Miraflores" -> 7
//                "Barranco" -> 8
//                "La_Molina" -> 9
//                "Jesus_Maria" -> 10
//                "Lince" -> 11
//                "Cercado_de_Lima" -> 12
//                "Chorrillos" -> 13
//                else -> 0
//            }
//            val gamemodeId = when (gamemode) {
//                "FUTBOL_11" -> 1
//                "FUTBOL_7" -> 2
//                "FUTBOL_8" -> 3
//                "FUTBOL_5" -> 4
//                "BILLAR_3" -> 5
//                else -> 0
//            }
//            val image = findViewById<EditText>(R.id.img_url).text.toString()
//
//            val sportSpace = hashMapOf(
//                "name" to name,
//                "sportId" to sportId,
//                "image" to image,
//                "price" to price,
//                "districtId" to districtId,
//                "address" to address,
//                "description" to description,
//                "openTime" to openTime,
//                "closeTime" to closeTime,
//                "gamemodeId" to gamemodeId
//            )
//
//            service.createSportSpace(sportSpace).enqueue(object : retrofit2.Callback<Void> {
//                override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
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
//                override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
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
//    private fun createRetrofitService(context: Context): PlaceHolder {
//        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
//        val client = OkHttpClient.Builder()
//            .addInterceptor(logging)
//            .addInterceptor(SaveCookieInterceptor(context))
//            .addInterceptor(CookieInterceptor(context))
//            .build()
//
//        return Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(client)
//            .build()
//            .create(PlaceHolder::class.java)
//    }
//
//
//}