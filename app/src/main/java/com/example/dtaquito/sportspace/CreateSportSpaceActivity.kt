package com.example.dtaquito.sportspace

import Interface.PlaceHolder
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.TypedValue
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.dtaquito.BuildConfig
import com.example.dtaquito.R
import com.example.dtaquito.player.PlayerBase
import com.example.dtaquito.time.TimePickerFragment
import com.example.dtaquito.utils.showToast
import com.google.android.material.textfield.TextInputLayout
import network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri

class CreateSportSpaceActivity : PlayerBase() {

    companion object {
        private const val SELECT_SPORT = "Choose a sport:"
        private const val SELECT_FORMAT = "Choose a format:"
        private const val STORAGE_PERMISSION_CODE = 1001
    }

    private lateinit var imageView: ImageView
    private lateinit var imgUrlEditText: TextView
    private lateinit var mapView: MapView
    private val service by lazy { RetrofitClient.instance.create(PlaceHolder::class.java) }
    private var selectedLatitude: Double = 0.0
    private var selectedLongitude: Double = 0.0

    // Ciclo de vida de la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this, BuildConfig.LOCATIONIQ_API_KEY, WellKnownTileServer.MapTiler)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_sport_space)
        setupBottomNavigation(R.id.navigation_sportspaces_prop)
        initializeMap(savedInstanceState)
        initializeUI()
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
                    .zoom(10.0)
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
                    showToast("Error al obtener la dirección")
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
                        showToast("No se pudo obtener la dirección")
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
        val adapter = object : ArrayAdapter<String>(this, R.layout.spinner_items, items) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view as TextView
                textView.setTextColor(
                    if (position == 0) "#4D4D4D".toColorInt()
                    else "#FFFFFF".toColorInt()
                )
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view as TextView
                textView.setTextColor(
                    if (position == 0) "#4D4D4D".toColorInt()
                    else "#FFFFFF".toColorInt()
                )
                return view
            }
        }
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
                intent.data = "package:$packageName".toUri()
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
            showToast("Permiso denegado")
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
                val fileName = getFileName(selectedImageUri) // Obtén el nombre del archivo
                if (fileName != null) {
                    imgUrlEditText.text = fileName // Muestra la ruta en el EditText
                    imgUrlEditText.hint = "" // Cambia el hint al nombre del archivo
                    val heightInPixels = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        300f,
                        resources.displayMetrics
                    ).toInt()
                    imageView.layoutParams.height = heightInPixels // Cambia la altura de la imagen
                    imageView.visibility= View.VISIBLE // Muestra la imagen
                    imageView.requestLayout()
                } else {
                    showToast("No se pudo obtener la ruta del archivo")
                }
            } else {
                showToast("No se pudo cargar la imagen")
            }
        }
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

        val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val openTimePart = openTime.toRequestBody("text/plain".toMediaTypeOrNull())
        val closeTimePart = closeTime.toRequestBody("text/plain".toMediaTypeOrNull())
        val sportIdPart = sportId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val addressPart = address.toRequestBody("text/plain".toMediaTypeOrNull())
        val pricePart = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val gamemodeIdPart = gamemodeId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val latitudePart = latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val longitudePart = longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val file = File(imagePath)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

        service.createSportSpace(
            namePart, sportIdPart, imagePart, pricePart, addressPart,
            descriptionPart, openTimePart, closeTimePart, gamemodeIdPart, latitudePart, longitudePart
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    showToast("Sport space created successfully")
                    finish()
                } else {
                    showToast("Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showToast("Network error: ${t.message}")
            }
        })
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