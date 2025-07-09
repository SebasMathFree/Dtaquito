package com.example.dtaquito.reservation

import Beans.reservations.Reservation
import Interface.PlaceHolder
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class ReservationFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReservationAdapter
    private lateinit var btnPersonal: Button
    private lateinit var btnCommunity: Button
    private val apiService = RetrofitClient.instance.create(PlaceHolder::class.java)

    // Lista para almacenar todas las reservaciones
    private var allReservations = listOf<Reservation>()
    // Tipo de filtro actual
    private var currentFilter: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reservation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar RecyclerView y adaptador
        recyclerView = view.findViewById(R.id.recyclerView2)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ReservationAdapter(emptyList(), parentFragmentManager)
        recyclerView.adapter = adapter

        // Inicializar botones
        btnPersonal = view.findViewById(R.id.btn_personal)
        btnCommunity = view.findViewById(R.id.btn_community)

        // Configurar listeners de botones
        setupButtonListeners()

        // Establecer estilo inicial sin filtro activo
        updateButtonStyles()

        // Cargar las reservaciones
        loadReservations()
    }

    private fun setupButtonListeners() {
        btnPersonal.setOnClickListener {
            if (currentFilter == "PERSONAL") {
                // Si ya está seleccionado, quitar el filtro
                currentFilter = null
                updateButtonStyles()
                applyFilter()
            } else {
                currentFilter = "PERSONAL"
                updateButtonStyles()
                applyFilter()
            }
        }

        btnCommunity.setOnClickListener {
            if (currentFilter == "COMMUNITY") {
                // Si ya está seleccionado, quitar el filtro
                currentFilter = null
                updateButtonStyles()
                applyFilter()
            } else {
                currentFilter = "COMMUNITY"
                updateButtonStyles()
                applyFilter()
            }
        }
    }

    private fun updateButtonStyles() {
        when (currentFilter) {
            null -> {
                // Ningún filtro seleccionado - ambos botones con fondo activo
                btnPersonal.setBackgroundResource(R.drawable.tab_button_active_background)
                btnPersonal.setTextColor(resources.getColor(android.R.color.white))

                btnCommunity.setBackgroundResource(R.drawable.tab_button_active_background)
                btnCommunity.setTextColor(resources.getColor(android.R.color.white))
            }
            "PERSONAL" -> {
                // El botón seleccionado tiene fondo inactivo y texto verde
                btnPersonal.setBackgroundResource(R.drawable.tab_button_inactive_background)
                btnPersonal.setTextColor(Color.parseColor("#266b12"))

                // El botón no seleccionado tiene fondo activo y texto blanco
                btnCommunity.setBackgroundResource(R.drawable.tab_button_active_background)
                btnCommunity.setTextColor(resources.getColor(android.R.color.white))
            }
            "COMMUNITY" -> {
                // El botón seleccionado tiene fondo inactivo y texto verde
                btnCommunity.setBackgroundResource(R.drawable.tab_button_inactive_background)
                btnCommunity.setTextColor(Color.parseColor("#266b12"))

                // El botón no seleccionado tiene fondo activo y texto blanco
                btnPersonal.setBackgroundResource(R.drawable.tab_button_active_background)
                btnPersonal.setTextColor(resources.getColor(android.R.color.white))
            }
        }
    }

    private fun applyFilter() {
        val filteredReservations = if (currentFilter == null) {
            // Sin filtro, mostrar todas las reservaciones
            allReservations
        } else {
            // Aplicar filtro
            allReservations.filter { it.type == currentFilter }
        }
        adapter.updateReservations(filteredReservations)
    }

    private fun loadReservations() {
        apiService.getMyReservations().enqueue(object : Callback<List<Reservation>> {
            override fun onResponse(call: Call<List<Reservation>>, response: Response<List<Reservation>>) {
                if (response.isSuccessful) {
                    allReservations = response.body() ?: emptyList()
                    applyFilter() // Aplicar el filtro actual a las reservaciones cargadas
                } else {
                    if(isAdded) Toast.makeText(context, "Error al cargar reservaciones", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Reservation>>, t: Throwable) {
                if(isAdded) Toast.makeText(context, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}