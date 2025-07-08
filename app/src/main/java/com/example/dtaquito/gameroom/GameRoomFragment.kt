
package com.example.dtaquito.gameroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import com.example.dtaquito.utils.showToast
import Beans.rooms.GameRoom
import Interface.PlaceHolder
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import network.RetrofitClient

class GameRoomFragment : Fragment() {

    private lateinit var service: PlaceHolder
    private lateinit var recycler: RecyclerView
    private var sportType: String? = null
    private lateinit var spinner: Spinner
    private var gameMode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializar Retrofit solo una vez por aplicación
        RetrofitClient.initialize(requireContext().applicationContext)
        service = RetrofitClient.instance.create(PlaceHolder::class.java)
        sportType = arguments?.getString("SPORT_TYPE")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.activity_soccer_room, container, false)

    // Resto del código existente...

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler = view.findViewById(R.id.recyclerView)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        // Configurar el Spinner
        configurarSpinner(view)

        // Configurar los botones de filtro
        configurarBotonesFiltro(view)

        getAllRooms()
    }

    private fun configurarSpinner(view: View) {
        spinner = view.findViewById(R.id.spinnerGameMode)
        val modosJuegoMostrar = arrayOf("Modo de juego", "Fútbol 11", "Fútbol 8", "Fútbol 7", "Fútbol 5", "Fútbol 3")
        val adaptador = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, modosJuegoMostrar)
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adaptador

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                gameMode = if (position == 0) null else convertirModoJuego(modosJuegoMostrar[position])
                getAllRooms() // Actualizar la lista con el filtro aplicado
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                gameMode = null
            }
        }
    }

    private fun convertirModoJuego(modoSeleccionado: String): String {
        return when (modoSeleccionado) {
            "Fútbol 11" -> "FUTBOL_11"
            "Fútbol 8" -> "FUTBOL_8"
            "Fútbol 7" -> "FUTBOL_7"
            "Fútbol 5" -> "FUTBOL_5"
            "Fútbol 3" -> "FUTBOL_3"
            else -> modoSeleccionado
        }
    }

    private fun configurarBotonesFiltro(view: View) {
        val btnBillar = view.findViewById<ImageButton>(R.id.btnPoolFilter)
        val btnFutbol = view.findViewById<ImageButton>(R.id.btnSoccerFilter)

        // Inicializar ambos botones con alpha 0.7f (deseleccionados)
        btnBillar.alpha = 0.7f
        btnFutbol.alpha = 0.7f

        btnBillar.setOnClickListener {
            if (sportType == "BILLAR") {
                // Si ya está seleccionado, desactivar el filtro
                sportType = null
                btnBillar.alpha = 0.7f
                spinner.isEnabled = true
            } else {
                // Activar el filtro de billar
                sportType = "BILLAR"
                actualizarEstadoBotones(btnBillar, btnFutbol)
                // Deshabilitar el spinner
                spinner.isEnabled = false
                spinner.setSelection(0)
                gameMode = null
            }
            getAllRooms()
        }

        btnFutbol.setOnClickListener {
            if (sportType == "FUTBOL") {
                // Si ya está seleccionado, desactivar el filtro
                sportType = null
                btnFutbol.alpha = 0.7f
                // Mantener el spinner habilitado
                spinner.isEnabled = true
            } else {
                // Activar el filtro de fútbol
                sportType = "FUTBOL"
                actualizarEstadoBotones(btnFutbol, btnBillar)
                // Habilitar el spinner
                spinner.isEnabled = true
            }
            getAllRooms()
        }
    }

    private fun actualizarEstadoBotones(botonSeleccionado: ImageButton, otroBoton: ImageButton) {
        // Resaltar el botón seleccionado
        botonSeleccionado.alpha = 1.0f
        // Atenuar el otro botón
        otroBoton.alpha = 0.7f
    }


    private fun getAllRooms() {
        service.getAllRooms().enqueue(object : retrofit2.Callback<List<GameRoom>> {
            override fun onResponse(
                call: retrofit2.Call<List<GameRoom>>,
                response: retrofit2.Response<List<GameRoom>>
            ) {
                if (!response.isSuccessful) {
                    requireContext().showToast("Error al obtener las salas")
                    return
                }

                val gameRooms = response.body()
                if (gameRooms.isNullOrEmpty()) {
                    requireContext().showToast("No se encontraron salas")
                    return
                }

                // Aplicar filtros
                var salasFiltradas = gameRooms

                // Filtrar por tipo de deporte
                if (sportType != null) {
                    salasFiltradas = salasFiltradas.filter {
                        it.reservation?.sportSpace?.sportType.equals(sportType, ignoreCase = true)
                    }
                    println("Filtrado por deporte: $sportType - Resultados: ${salasFiltradas.size}")
                }

                // Filtrar por modo de juego
                if (gameMode != null && gameMode != "Modo de juego") {
                    salasFiltradas = salasFiltradas.filter {
                        it.reservation?.sportSpace?.gamemode == gameMode
                    }
                    println("Filtrado por modo: $gameMode - Resultados finales: ${salasFiltradas.size}")
                }

                if (salasFiltradas.isNotEmpty()) {
                    recycler.adapter = GameRoomAdapter(salasFiltradas)
                } else {
                    requireContext().showToast("No hay salas disponibles con los filtros aplicados")
                    // Mostrar la lista vacía para indicar visualmente que no hay resultados
                    recycler.adapter = GameRoomAdapter(emptyList())
                }
            }

            override fun onFailure(call: retrofit2.Call<List<GameRoom>>, t: Throwable) {
                requireContext().showToast("Error de red: ${t.message}")
            }
        })
    }
}