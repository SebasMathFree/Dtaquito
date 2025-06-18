package com.example.dtaquito.reservation

import Beans.reservations.Reservation
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.dtaquito.utils.loadImageFromUrl

class ReservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val imgSportSpace: ImageView = itemView.findViewById(R.id.imgSportSpace)
    val txtName: TextView = itemView.findViewById(R.id.txtName)
    val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
    val txtType: TextView = itemView.findViewById(R.id.txtType)
    val txtDate: TextView = itemView.findViewById(R.id.txtDate)
    val txtSportSpaceName: TextView = itemView.findViewById(R.id.txtSportSpaceName)
    val txtStartTime: TextView = itemView.findViewById(R.id.txtStartTime)
    val txtEndTime: TextView = itemView.findViewById(R.id.txtEndTime)
    val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
    val txtGameMode: TextView = itemView.findViewById(R.id.txtGameMode)
    val qrBtn: Button = itemView.findViewById(R.id.qr_btn)

    fun render(reservation: Reservation) {
        // Usar operadores de navegación segura y valores por defecto
        txtName.text = reservation.name ?: "Sin nombre"
        txtStatus.text = "Estado: ${reservation.status ?: "Desconocido"}"
        txtType.text = "Tipo: ${reservation.type ?: "Desconocido"}"
        txtDate.text = "Fecha: ${reservation.gameDay ?: "No disponible"}"
        txtStartTime.text = "Hora inicio: ${reservation.startTime ?: "No disponible"}"
        txtEndTime.text = "Hora fin: ${reservation.endTime ?: "No disponible"}"

        // Manejar sportSpaces
        if (reservation.sportSpaces != null) {
            txtSportSpaceName.text = "Espacio: ${reservation.sportSpaces.name ?: "Desconocido"}"
            txtPrice.text = "Precio: S/.${reservation.sportSpaces.price ?: 0.0}"
            txtGameMode.text = "Modo: ${reservation.sportSpaces.gamemode ?: "Desconocido"}"

            // Cargar imagen
            val image = reservation.sportSpaces.image
            Log.d("ReservationViewHolder", "Image URL: $image")
            if (image != null && image.isNotEmpty()) {
                    loadImageFromUrl(image, imgSportSpace)
            } else {
                imgSportSpace.setImageResource(R.drawable.default_image)
            }
        } else {
            txtSportSpaceName.text = "Espacio: No disponible"
            txtPrice.text = "Precio: No disponible"
            txtGameMode.text = "Modo: No disponible"
            imgSportSpace.setImageResource(R.drawable.default_image)
        }
    }
}