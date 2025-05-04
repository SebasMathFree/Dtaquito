package com.example.dtaquito.sportspace

import Beans.sportspaces.SportSpace
import Interface.PlaceHolder
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import com.example.dtaquito.auth.CookieInterceptor
import com.example.dtaquito.auth.SaveCookieInterceptor
import com.example.dtaquito.player.PlayerBase
import environment.Environment
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SportSpaceActivity : PlayerBase() {

    // Variables
    private lateinit var service: PlaceHolder
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SportSpaceAdapter


    // Ciclo de vida
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sport_space)
        setupBottomNavigation(R.id.navigation_sportspaces_prop)

        initializeService()
        setupRecyclerView()
        fetchUserRoleAndLoadSportSpaces()
        setupCreateSportSpaceButton()
    }

    // Inicialización
    private fun initializeService() {
        service = createRetrofitService(this)
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    // Configuración del botón
    private fun setupCreateSportSpaceButton() {
        val createSportSpaceBtn = findViewById<Button>(R.id.create_sport_space_btn)

        fillUserProfile { userRoleType ->
            if (userRoleType == "OWNER") {
                createSportSpaceBtn.visibility = View.VISIBLE
                setupBottomNavigation(R.id.navigation_sportspaces_prop)
                createSportSpaceBtn.setOnClickListener {
                    //startActivity(Intent(this, CreateSportSpaceActivity::class.java))
                }
            } else {
                createSportSpaceBtn.visibility = View.GONE
                setupBottomNavigation(R.id.navigation_sportspaces)
            }
        }
    }

    // Carga de datos
    private fun fetchUserRoleAndLoadSportSpaces() {
        fillUserProfile { role ->
            Log.d("SportSpaceActivity", "User role: $role")
            fetchSportSpaces(role)
        }
    }

    private fun fetchSportSpaces(userRole: String) {
        val call = if (userRole == "OWNER") {
            service.getSportSpacesByUserId()
        } else {
            service.getAllSportSpaces()
        }

        call.enqueue(object : Callback<List<SportSpace>> {
            override fun onResponse(call: Call<List<SportSpace>>, response: Response<List<SportSpace>>) {
                if (response.isSuccessful) {
                    handleSuccessfulResponse(response.body())
                } else {
                    handleErrorResponse(response.code())
                }
            }

            override fun onFailure(call: Call<List<SportSpace>>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    // Manejo de respuestas
    private fun handleSuccessfulResponse(sportSpaces: List<SportSpace>?) {
        if (sportSpaces != null) {
            adapter = SportSpaceAdapter(sportSpaces)
            recyclerView.adapter = adapter
        } else {
            showToast("No se encontraron espacios deportivos")
        }
    }

    private fun handleErrorResponse(responseCode: Int) {
        when (responseCode) {
            403 -> showToast("Acceso denegado: no tienes permiso para acceder a esta funcionalidad")
            404 -> showToast("No se encontraron espacios deportivos para este usuario")
            else -> showToast("Error al obtener los espacios deportivos")
        }
    }

    // Utilidades
    private fun createRetrofitService(context: Context): PlaceHolder {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(SaveCookieInterceptor(context))
            .addInterceptor(CookieInterceptor(context))
            .build()

        return Retrofit.Builder()
            .baseUrl(Environment.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(PlaceHolder::class.java)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}