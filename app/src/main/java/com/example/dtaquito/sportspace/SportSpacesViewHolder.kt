package com.example.dtaquito.sportspace

import Beans.sportspaces.SportSpace
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import com.squareup.picasso.Picasso

class SportSpacesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val sportSpaceId: TextView = view.findViewById(R.id.idSport)
    private val sportSpaceType: TextView = view.findViewById(R.id.txtSportType)
    private val sportSpaceDistrict: TextView = view.findViewById(R.id.txtDistrict)
    private val sportSpaceImage: ImageView = view.findViewById(R.id.imgSportSpace)
    private val sportSpaceDescription: TextView = view.findViewById(R.id.txtDescription)
    private val sportSpaceStartTime: TextView = view.findViewById(R.id.txtStartTime)
    private val sportSpaceGameMode: TextView = view.findViewById(R.id.txtGameMode)
    private val sportSpaceAmount: TextView = view.findViewById(R.id.txtAmount)

    fun render(sportSpace: SportSpace) {

        val typeface = ResourcesCompat.getFont(itemView.context, R.font.righteous)

        sportSpaceId.typeface = typeface
        sportSpaceType.typeface = typeface
        sportSpaceDistrict.typeface = typeface
        sportSpaceDescription.typeface = typeface
        sportSpaceStartTime.typeface = typeface
        sportSpaceGameMode.typeface = typeface
        sportSpaceAmount.typeface = typeface

        sportSpaceId.text = "ID: ${sportSpace.id}"
        sportSpaceType.text = "Tipo de deporte: ${sportSpace.sportType}"
        sportSpaceDistrict.text = "Distrito: ${sportSpace.district}"
        sportSpaceDescription.text = "Descripción: ${sportSpace.description}"
        sportSpaceStartTime.text = "Hora de inicio: ${sportSpace.StartTime}"
        sportSpaceGameMode.text = "Modo de juego: ${sportSpace.gamemode}"
        sportSpaceAmount.text = "Adelanto: ${sportSpace.amount}"
        Picasso.get().load(sportSpace.imageUrl).into(sportSpaceImage)
    }
}