package com.example.dtaquito.sportspace

import Beans.sportspaces.SportSpace
import Interface.PlaceHolder
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import com.example.dtaquito.player.PlayerBase
import com.example.dtaquito.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import network.RetrofitClient

class SportSpaceActivity : PlayerBase() {

    // Variables
    private val service = RetrofitClient.instance.create(PlaceHolder::class.java)
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SportSpaceAdapter

    // Ciclo de vida
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sport_space)
        setupBottomNavigation(R.id.navigation_sportspaces_prop)

        setupRecyclerView()
        fetchUserRoleAndLoadSportSpaces()
        setupCreateSportSpaceButton()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    // Configuración del botón
    private fun setupCreateSportSpaceButton() {
        val createSportSpaceBtn = findViewById<Button>(R.id.create_sport_space_btn)

        lifecycleScope.launch {
            try {
                val userRoleType = withContext(Dispatchers.IO) { fillUserProfile() }
                if (userRoleType == "OWNER") {
                    createSportSpaceBtn.visibility = View.VISIBLE
                    setupBottomNavigation(R.id.navigation_sportspaces_prop)
                    createSportSpaceBtn.setOnClickListener {
                        startActivity(Intent(this@SportSpaceActivity, CreateSportSpaceActivity::class.java))
                    }
                } else {
                    createSportSpaceBtn.visibility = View.GONE
                    setupBottomNavigation(R.id.navigation_sportspaces)
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun fetchUserRoleAndLoadSportSpaces() {
        lifecycleScope.launch {
            try {
                val role = withContext(Dispatchers.IO) { fillUserProfile() }
                Log.d("SportSpaceActivity", "User role: $role")
                fetchSportSpaces(role)
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun fetchSportSpaces(userRole: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = if (userRole == "OWNER") {
                    service.getSportSpacesByUserId().execute()
                } else {
                    service.getAllSportSpaces().execute()
                }

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        handleSuccessfulResponse(response.body())
                    } else {
                        handleErrorResponse(response.code())
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error: ${e.message}")
                }
            }
        }
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

}