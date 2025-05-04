package com.example.dtaquito.sportspace

import Beans.sportspaces.SportSpace
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R

class SportSpacesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val sportSpaceName: TextView = view.findViewById(R.id.name)
    private val sportSpaceDistrict: TextView = view.findViewById(R.id.txtDistrict)
    private val sportSpaceImage: ImageView = view.findViewById(R.id.imgSportSpace)
    private val sportSpaceGameMode: TextView = view.findViewById(R.id.txtGameMode)
    private val sportSpacePrice: TextView = view.findViewById(R.id.txtPrice)

    fun render(sportSpace: SportSpace) {
        val typeface = ResourcesCompat.getFont(itemView.context, R.font.righteous)

        sportSpaceName.typeface = typeface
        sportSpaceDistrict.typeface = typeface
        sportSpaceGameMode.typeface = typeface
        sportSpacePrice.typeface = typeface

        sportSpaceName.text = "Nombre: ${sportSpace.name}"
        sportSpaceDistrict.text = "Distrito: ${sportSpace.districtType.replace("_", " ")}"
        sportSpaceGameMode.text = "Modo de juego: ${sportSpace.gamemodeType.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}"
        sportSpacePrice.text = "Precio: S/. ${sportSpace.price.toInt()}"
        decodeAndSetImage(sportSpace.image, sportSpaceImage)
    }
}

fun decodeAndSetImage(base64Image: String, imageView: ImageView) {
    try {
        // Decodificar el string Base64 a un array de bytes
        val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)

        // Convertir los bytes en un Bitmap
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

        // Establecer el Bitmap en el ImageView
        imageView.setImageBitmap(bitmap)
    } catch (e: Exception) {
        e.printStackTrace()
        // Manejar errores si la decodificación falla
    }
}