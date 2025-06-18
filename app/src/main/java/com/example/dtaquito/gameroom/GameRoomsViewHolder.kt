package com.example.dtaquito.gameroom

import Beans.playerList.PlayerList
import Beans.rooms.GameRoom
import Interface.PlaceHolder
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import androidx.fragment.app.FragmentActivity
import com.example.dtaquito.utils.loadImageFromUrl
import network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.dtaquito.utils.showToast

class GameRoomsViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private val imgSportSpace: ImageView = view.findViewById(R.id.imgSportSpace)
    private val roomName: TextView = view.findViewById(R.id.txtName)
    private val roomUser: TextView = view.findViewById(R.id.txtUser)
    private val roomDate: TextView = view.findViewById(R.id.txtDate)
    private val roomPrice: TextView = view.findViewById(R.id.txtPrice)
    private val roomDistrict: TextView = view.findViewById(R.id.txtDistrict)
    private val roomGameMode: TextView = view.findViewById(R.id.txtGameMode)
    private val roomAmount: TextView = view.findViewById(R.id.txtAmount)
    private val roomStartTime: TextView = view.findViewById(R.id.txtStartTime)
    private val roomEndTime: TextView = view.findViewById(R.id.txtEndTime)
    private val joinButton: Button = itemView.findViewById(R.id.login_btn)

    private lateinit var currentGameRoom: GameRoom
    private val service = RetrofitClient.instance.create(PlaceHolder::class.java)


    init {
        joinButton.setOnClickListener {
            val context = itemView.context
            val activity = context as? FragmentActivity
            if (activity != null) {
                checkUserRoomStatusAndJoinOrNavigate(currentGameRoom.id, activity)
            }
        }
    }

    private fun checkUserRoomStatusAndJoinOrNavigate(roomId: Int, activity: FragmentActivity) {
        service.getUserRoomStatus(roomId).enqueue(object : Callback<PlayerList> {
            override fun onResponse(call: Call<PlayerList>, response: Response<PlayerList>) {
                if (response.isSuccessful && response.body() != null) {
                    val playerStatus = response.body()!!
                    if (playerStatus.isRoomCreator || playerStatus.isMember) {
                        navigateToRoom(roomId, activity)
                    } else {
                        joinRoom(roomId, activity)
                    }
                } else {
                    joinRoom(roomId, activity)
                }
            }

            override fun onFailure(call: Call<PlayerList>, t: Throwable) {
                itemView.context.showToast("Error: ${t.message}")
            }
        })
    }

    private fun joinRoom(roomId: Int, activity: FragmentActivity) {
        service.joinRoom(roomId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    itemView.context.showToast("Te has unido a la sala")
                    navigateToRoom(roomId, activity)
                } else {
                    itemView.context.showToast("No se pudo unir a la sala")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                itemView.context.showToast("Error: ${t.message}")
            }
        })
    }

    private fun navigateToRoom(roomId: Int, activity: FragmentActivity) {
        val fragment = MainGameRoomFragment()
        val args = android.os.Bundle()
        args.putInt("GAME_ROOM_ID", roomId)
        fragment.arguments = args
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun renderRoom(gameRoom: GameRoom) {
        currentGameRoom = gameRoom
        val typeface = ResourcesCompat.getFont(itemView.context, R.font.righteous)
        val reservation = gameRoom.reservation
        val sportSpace = reservation?.sportSpace
        roomName.typeface = typeface
        roomUser.typeface = typeface
        roomDate.typeface = typeface
        roomPrice.typeface = typeface
        roomDistrict.typeface = typeface
        roomGameMode.typeface = typeface
        roomAmount.typeface = typeface
        roomStartTime.typeface = typeface
        roomEndTime.typeface = typeface

        roomName.text = reservation?.reservationName ?: "Sin nombre"
        roomUser.text = "Creado por: ${reservation?.userName ?: "Desconocido"}"
        roomAmount.text = "Costo de ingreso: ${sportSpace?.amount ?: 0}"
        roomDate.text = "Fecha: ${reservation?.gameDay ?: ""}"
        roomDistrict.text = "Dirección: ${sportSpace?.address ?: ""}"
        roomPrice.text = "Precio: ${sportSpace?.price ?: 0}"
        roomGameMode.text = "Modo de juego: ${sportSpace?.gamemode ?: "N/A"}"
        roomStartTime.text = "Hora de inicio: ${reservation?.startTime ?: ""}"
        roomEndTime.text = "Hora de fin: ${reservation?.endTime ?: ""}"

        loadImageFromUrl(sportSpace?.imageUrl, imgSportSpace)
    }
}