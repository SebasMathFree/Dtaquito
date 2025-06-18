package com.example.dtaquito.sportspace

import Beans.sportspaces.SportSpace
import com.bumptech.glide.Glide
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import com.example.dtaquito.utils.loadImageFromUrl

class SportSpacesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val sportSpaceName: TextView = view.findViewById(R.id.name)
    private val sportSpaceImage: ImageView = view.findViewById(R.id.imgSportSpace)
    private val sportSpaceGameMode: TextView = view.findViewById(R.id.txtGameMode)
    private val sportSpacePrice: TextView = view.findViewById(R.id.txtPrice)

    fun render(sportSpace: SportSpace) {
        val context = itemView.context
        val typeface = ResourcesCompat.getFont(context, R.font.righteous)

        sportSpaceName.typeface = typeface
        sportSpaceGameMode.typeface = typeface
        sportSpacePrice.typeface = typeface

        sportSpaceName.text = sportSpace.name
        sportSpaceGameMode.text = context.getString(
            R.string.game_mode_label,
            sportSpace.gamemodeType.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
        )
        sportSpacePrice.text = context.getString(
            R.string.price_label,
            sportSpace.price.toInt()
        )
        loadImageFromUrl(sportSpace.imageUrl, sportSpaceImage)
    }


}

