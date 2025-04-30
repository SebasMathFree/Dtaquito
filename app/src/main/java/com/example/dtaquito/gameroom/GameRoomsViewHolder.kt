package com.example.dtaquito.gameroom

import Beans.rooms.GameRoom
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class GameRoomsViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private val imgSportSpace: ImageView = view.findViewById(R.id.imgSportSpace)
    private val roomName: TextView = view.findViewById(R.id.txtName)
    private val roomUser: TextView = view.findViewById(R.id.txtUser)
    private val roomDate: TextView = view.findViewById(R.id.txtDate)
    private val roomPrice: TextView = view.findViewById(R.id.txtPrice)
    private val roomDistrict: TextView = view.findViewById(R.id.txtDistrict)
    private val roomDescription: TextView = view.findViewById(R.id.txtDescription)
    private val roomGameMode: TextView = view.findViewById(R.id.txtGameMode)
    private val roomAmount: TextView = view.findViewById(R.id.txtAmount)
    private val joinButton: Button = itemView.findViewById(R.id.login_btn)

    private lateinit var currentGameRoom: GameRoom

//    init {
//        joinButton.setOnClickListener {
//            val context = itemView.context
//            //val intent = Intent(context, MainGameRoomActivity::class.java)
//            intent.putExtra("GAME_ROOM_ID", currentGameRoom.id)
//
//            // Retrieve USER_ID from the context
//            val userId = (context as? AppCompatActivity)?.intent?.getIntExtra("USER_ID", -1) ?: -1
//            if (userId != -1) {
//                intent.putExtra("USER_ID", userId)
//            }
//
//            context.startActivity(intent)
//        }
//    }

    fun renderRoom(gameRoom: GameRoom) {

        currentGameRoom = gameRoom
        val typeface = ResourcesCompat.getFont(itemView.context, R.font.righteous)
        roomName.typeface = typeface
        roomUser.typeface = typeface
        roomDate.typeface = typeface
        roomPrice.typeface = typeface
        roomDistrict.typeface = typeface
        roomDescription.typeface = typeface
        roomGameMode.typeface = typeface
        roomAmount.typeface = typeface

        roomName.text = "${gameRoom.roomName}"
        roomUser.text = "Creado por: ${gameRoom.creator?.name ?: "Unknown"}"

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val date = inputFormat.parse(gameRoom.openingDate)
        val formattedDate = outputFormat.format(date)
        roomDate.text = "Fecha: ${formattedDate}"

        roomPrice.text = "Precio: ${gameRoom.sportSpace?.price.toString()}"
        roomDistrict.text = "Distrito: ${gameRoom.sportSpace?.district ?: "Unknown"}"
        roomDescription.text = "Descripcion: ${gameRoom.sportSpace?.description ?: " No description"}"
        roomGameMode.text = "Modo de juego: ${gameRoom.sportSpace?.gamemode ?: "N/A"}"
        roomAmount.text = "Costo de ingreso: ${gameRoom.sportSpace?.amount.toString()}"
        Picasso.get().load(gameRoom.sportSpace?.imageUrl).into(imgSportSpace)

    }
}