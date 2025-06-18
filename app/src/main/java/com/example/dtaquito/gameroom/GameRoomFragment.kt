package com.example.dtaquito.gameroom

import Beans.rooms.GameRoom
import Interface.PlaceHolder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import com.example.dtaquito.utils.showToast
import network.RetrofitClient

class GameRoomFragment : Fragment() {

    private lateinit var service: PlaceHolder
    private lateinit var recycler: RecyclerView
    private lateinit var createRoomBtn: Button
    private var sportType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.initialize(requireContext().applicationContext)
        service = RetrofitClient.instance.create(PlaceHolder::class.java)

        arguments?.let {
            sportType = it.getString("SPORT_TYPE")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_soccer_room, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler = view.findViewById(R.id.recyclerView)
        createRoomBtn = view.findViewById(R.id.create_room_btn)

        recycler.layoutManager = LinearLayoutManager(requireContext())
        getAllRooms()

        createRoomBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateRoomFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun getAllRooms() {
        service.getAllRooms().enqueue(object : retrofit2.Callback<List<GameRoom>> {
            override fun onResponse(
                call: retrofit2.Call<List<GameRoom>>,
                response: retrofit2.Response<List<GameRoom>>
            ) {
                if (response.isSuccessful) {
                    val gameRooms = response.body()
                    if (gameRooms != null) {
                        // Filtrar las salas por tipo de deporte si hay un tipo seleccionado
                        val filteredRooms = if (sportType != null) {
                            gameRooms.filter { room ->
                                room.reservation?.sportSpace?.sportType == sportType
                            }
                        } else {
                            gameRooms
                        }

                        if (filteredRooms.isNotEmpty()) {
                            recycler.adapter = GameRoomAdapter(filteredRooms)
                        } else {
                            requireContext().showToast("No se encontraron salas para $sportType")
                        }
                    } else {
                        requireContext().showToast("No se encontraron salas")
                    }
                } else {
                    requireContext().showToast("Error al obtener las salas")
                }
            }

            override fun onFailure(call: retrofit2.Call<List<GameRoom>>, t: Throwable) {
                requireContext().showToast("Error de red")
            }
        })
    }
}