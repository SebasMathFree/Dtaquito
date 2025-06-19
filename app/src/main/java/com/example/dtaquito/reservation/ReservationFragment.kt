package com.example.dtaquito.reservation

import Beans.reservations.Reservation
import Interface.PlaceHolder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import com.example.dtaquito.qr.QrFragment
import network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReservationFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReservationAdapter
    private val apiService = RetrofitClient.instance.create(PlaceHolder::class.java)

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

        // Cargar las reservaciones
        loadReservations()
    }

    private fun loadReservations() {
        apiService.getMyReservations().enqueue(object : Callback<List<Reservation>> {
            override fun onResponse(call: Call<List<Reservation>>, response: Response<List<Reservation>>) {
                if (response.isSuccessful) {
                    val reservations = response.body() ?: emptyList()
                    Log.d("ReservationData", "Recibidas ${reservations.size} reservaciones")

                    // Depurar el contenido de cada reserva
                    reservations.forEach { reservation ->
                        Log.d("ReservationData", "Reserva: id=${reservation.id}, " +
                                "name=${reservation.name}, " +
                                "status=${reservation.status}, " +
                                "type=${reservation.type}, " +
                                "gameDay=${reservation.gameDay}, " +
                                "sportSpaces=${reservation.sportSpaces != null}")

                        // Si sportSpaces no es nulo, depurar sus campos
                        reservation.sportSpaces?.let { space ->
                            Log.d("ReservationData", "SportSpace: name=${space.name}, " +
                                    "price=${space.price}, " +
                                    "gamemode=${space.gamemode}")
                        }
                    }

                    adapter.updateReservations(reservations)
                } else {
                    Log.e("ReservationData", "Error: ${response.code()} - ${response.message()}")
                    if(isAdded) Toast.makeText(context, "Error al cargar reservaciones", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Reservation>>, t: Throwable) {
                Log.e("ReservationData", "Error de conexión: ${t.message}", t)
                if(isAdded) Toast.makeText(context, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}