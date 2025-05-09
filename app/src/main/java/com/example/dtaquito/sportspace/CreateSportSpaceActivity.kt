package com.example.dtaquito.sportspace

import Interface.PlaceHolder
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.dtaquito.BuildConfig
import com.example.dtaquito.R
import com.example.dtaquito.auth.CookieInterceptor
import com.example.dtaquito.auth.SaveCookieInterceptor
import com.example.dtaquito.player.PlayerBase
import com.example.dtaquito.time.TimePickerFragment
import com.google.android.material.textfield.TextInputLayout
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException

class CreateSportSpaceActivity : PlayerBase() {

    companion object {
        private const val BASE_URL = "https://dtaquito-backend.azurewebsites.net/"
        private const val SELECT_SPORT = "Choose a sport:"
        private const val SELECT_FORMAT = "Choose a format:"
        private const val STORAGE_PERMISSION_CODE = 1001
    }

    private lateinit var service: PlaceHolder
    private lateinit var imageView: ImageView
    private lateinit var imgUrlEditText: TextView
    private lateinit var mapView: MapView
    private var selectedLatitude: Double = 0.0
    private var selectedLongitude: Double = 0.0

    // Ciclo de vida de la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this, BuildConfig.LOCATIONIQ_API_KEY, WellKnownTileServer.MapTiler)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_sport_space)
        initializeMap(savedInstanceState)
        initializeUI()
        service = createRetrofitService(this)
    }

    // Inicialización de la interfaz de usuario
    private fun initializeUI() {
        imageView = findViewById(R.id.image_view)
        imgUrlEditText = findViewById(R.id.img_url)
        val imgUrlLayout = findViewById<TextInputLayout>(R.id.img_url_layout)
        imgUrlLayout.setEndIconOnClickListener { checkStoragePermission() }
        val spinnerSport = findViewById<Spinner>(R.id.sport_input)
        val spinnerFormat = findViewById<Spinner>(R.id.format_input)
        val nameInput = findViewById<EditText>(R.id.name_input)
        val descriptionInput = findViewById<EditText>(R.id.description_input)
        val startTimeInput = findViewById<EditText>(R.id.start_time_input)
        val endTimeInput = findViewById<EditText>(R.id.end_time_input)
        val addressInput = findViewById<TextView>(R.id.address_input)
        val priceInput = findViewById<EditText>(R.id.price_input)
        val createSportSpaceButton = findViewById<Button>(R.id.create_space_btn)

        setupSpinners(spinnerSport, spinnerFormat)
        setupTimePickers(startTimeInput, endTimeInput)

        createSportSpaceButton.setOnClickListener {
            val price = priceInput.text.toString().toDoubleOrNull() ?: 0.0
            val imagePath = imgUrlEditText.text.toString()

            createSportSpace(
                nameInput.text.toString(),
                descriptionInput.text.toString(),
                startTimeInput.text.toString(),
                endTimeInput.text.toString(),
                spinnerSport.selectedItem.toString(),
                addressInput.text.toString(),
                price,
                spinnerFormat.selectedItem.toString(),
                imagePath,
                selectedLatitude,
                selectedLongitude
            )
        }
    }

    // Inicialización del mapa
    private fun initializeMap(savedInstanceState: Bundle?) {
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync { mapLibreMap ->
            mapLibreMap.setStyle("https://tiles.locationiq.com/v3/streets/vector.json?key=${BuildConfig.LOCATIONIQ_API_KEY}") {
                val limaLatLng = LatLng(-12.0464, -77.0428)
                mapLibreMap.cameraPosition = CameraPosition.Builder()
                    .target(limaLatLng)
                    .zoom(8.0)
                    .build()
                setupMapClickListener(mapLibreMap)
            }
            setupMapInteractions(mapLibreMap)
            setupZoomControls(mapLibreMap)
        }
    }

    private fun setupMapInteractions(mapLibreMap: MapLibreMap) {
        mapView.setOnGenericMotionListener { _, event ->
            if (event.action == MotionEvent.ACTION_SCROLL && event.isFromSource(InputDevice.SOURCE_CLASS_POINTER)) {
                val scrollDelta = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                val currentZoom = mapLibreMap.cameraPosition.zoom
                val newZoom = if (scrollDelta > 0) currentZoom + 1 else currentZoom - 1
                mapLibreMap.animateCamera(CameraUpdateFactory.zoomTo(newZoom))
                true
            } else {
                false
            }
        }
        mapView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> findViewById<ScrollView>(R.id.scrollView).requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP -> findViewById<ScrollView>(R.id.scrollView).requestDisallowInterceptTouchEvent(false)
            }
            mapView.onTouchEvent(event)
        }
        mapLibreMap.uiSettings.isZoomGesturesEnabled = true
        mapLibreMap.uiSettings.isRotateGesturesEnabled = true
        mapLibreMap.uiSettings.isScrollGesturesEnabled = true
    }

    private fun setupMapClickListener(mapLibreMap: MapLibreMap) {
        mapLibreMap.addOnMapClickListener { point ->
            selectedLatitude = point.latitude
            selectedLongitude = point.longitude

            mapLibreMap.clear()

            val drawable = ContextCompat.getDrawable(this, R.drawable.place)
            val bitmap = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
            val icon = bitmap?.let {
                org.maplibre.android.annotations.IconFactory.getInstance(this).fromBitmap(it)
            }
            mapLibreMap.addMarker(
                MarkerOptions()
                    .position(LatLng(selectedLatitude, selectedLongitude))
                    .title("Selected Location")
                    .icon(icon)
            )
            fetchAddressFromLocationIQ(selectedLatitude, selectedLongitude)

            true
        }
    }
    private fun setupZoomControls(mapLibreMap: MapLibreMap) {
        val zoomInButton = findViewById<Button>(R.id.zoom_in_button)
        val zoomOutButton = findViewById<Button>(R.id.zoom_out_button)

        zoomInButton.setOnClickListener {
            val currentZoom = mapLibreMap.cameraPosition.zoom
            mapLibreMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoom + 1))
        }

        zoomOutButton.setOnClickListener {
            val currentZoom = mapLibreMap.cameraPosition.zoom
            mapLibreMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoom - 1))
        }
    }

    private fun fetchAddressFromLocationIQ(latitude: Double, longitude: Double) {
        val apiKey = BuildConfig.LOCATIONIQ_API_KEY
        val url = "https://us1.locationiq.com/v1/reverse.php?key=$apiKey&lat=$latitude&lon=$longitude&format=json"

        val client = OkHttpClient()
        val request = okhttp3.Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CreateSportSpaceActivity, "Error al obtener la dirección", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        val jsonObject = org.json.JSONObject(responseBody)
                        val address = jsonObject.getString("display_name")

                        runOnUiThread {
                            val addressInput = findViewById<TextView>(R.id.address_input)
                            addressInput.text = address
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@CreateSportSpaceActivity, "No se pudo obtener la dirección", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    // Configuración de los spinners
    private fun setupSpinners(spinnerSport: Spinner, spinnerFormat: Spinner) {
        val sports = listOf(SELECT_SPORT, "Soccer", "Pool")
        val formatsSoccer = listOf(SELECT_FORMAT, "Soccer 5", "Soccer 7", "Soccer 11")
        val formatsPool = listOf(SELECT_FORMAT, "Pool 3")

        setupSpinner(spinnerSport, sports) { selectedSport ->
            val formats = if (selectedSport == "Pool") formatsPool else formatsSoccer
            setupSpinner(spinnerFormat, formats) {}
        }
    }

    private fun setupSpinner(spinner: Spinner, items: List<String>, onItemSelected: (String) -> Unit) {
        val adapter = ArrayAdapter(this, R.layout.spinner_items, items)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onItemSelected(items[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // Configuración de los selectores de tiempo
    private fun setupTimePickers(startTimeInput: EditText, endTimeInput: EditText) {
        startTimeInput.setOnClickListener { showTimePickerDialog(startTimeInput) }
        endTimeInput.setOnClickListener { showTimePickerDialog(endTimeInput) }
    }

    private fun showTimePickerDialog(editText: EditText) {
        val timePicker = TimePickerFragment { time -> editText.setText(time) }
        timePicker.show(supportFragmentManager, "timePicker")
    }

    // Verifica permisos de almacenamiento
    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            } else {
                openGallery()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )
            } else {
                openGallery()
            }
        }
    }

    // Maneja el resultado de la solicitud de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // Abre la galería para seleccionar una imagen
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    // Maneja el resultado de la selección de la imagen
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri = result.data?.data
            if (selectedImageUri != null) {
                imageView.setImageURI(selectedImageUri) // Muestra la imagen seleccionada
                val filePath = getRealPathFromURI(selectedImageUri) // Obtén la ruta absoluta
                if (filePath != null) {
                    imgUrlEditText.text = filePath // Muestra la ruta en el EditText
                    imgUrlEditText.hint = "" // Cambia el hint al nombre del archivo
                } else {
                    Toast.makeText(this, "No se pudo obtener la ruta del archivo", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Metodo para obtener la ruta absoluta desde la URI
    private fun getRealPathFromURI(uri: Uri): String? {
        var filePath: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                filePath = cursor.getString(columnIndex)
            }
        }
        return filePath
    }

    // Obtiene el nombre del archivo desde la URI
    private fun getFileName(uri: Uri): String {
        var fileName = "Unknown"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) {
                cursor.moveToFirst()
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName
    }

    // Creación del espacio deportivo
    private fun createSportSpace(
        name: String,
        description: String,
        openTime: String,
        closeTime: String,
        sportType: String,
        address: String,
        price: Double,
        gamemode: String,
        imagePath: String,
        latitude: Double,
        longitude: Double
    ) {
        val sportId = if (sportType == "Soccer") 1 else 2
        val gamemodeId = if (gamemode == "Soccer 11") 1 else 5

        val namePart = RequestBody.create("text/plain".toMediaTypeOrNull(), name)
        val descriptionPart = RequestBody.create("text/plain".toMediaTypeOrNull(), description)
        val openTimePart = RequestBody.create("text/plain".toMediaTypeOrNull(), openTime)
        val closeTimePart = RequestBody.create("text/plain".toMediaTypeOrNull(), closeTime)
        val sportIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), sportId.toString())
        val addressPart = RequestBody.create("text/plain".toMediaTypeOrNull(), address)
        val pricePart = RequestBody.create("text/plain".toMediaTypeOrNull(), price.toString())
        val gamemodeIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), gamemodeId.toString())
        val latitudePart = RequestBody.create("text/plain".toMediaTypeOrNull(), latitude.toString())
        val longitudePart = RequestBody.create("text/plain".toMediaTypeOrNull(), longitude.toString())

        val file = File(imagePath)
        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

        service.createSportSpace(
            namePart, sportIdPart, imagePart, pricePart, addressPart,
            descriptionPart, openTimePart, closeTimePart, gamemodeIdPart, latitudePart, longitudePart
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CreateSportSpaceActivity, "Sport space created successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@CreateSportSpaceActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@CreateSportSpaceActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Configuración de Retrofit
    private fun createRetrofitService(context: Context): PlaceHolder {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(SaveCookieInterceptor(context))
            .addInterceptor(CookieInterceptor(context))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(PlaceHolder::class.java)
    }

    // Métodos del ciclo de vida del MapView
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}