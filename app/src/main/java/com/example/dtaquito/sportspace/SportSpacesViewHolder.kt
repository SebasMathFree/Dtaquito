package com.example.dtaquito.sportspace

import Beans.sportspaces.SportSpace
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import com.example.dtaquito.reservation.CreateReservationFragment
import com.example.dtaquito.utils.loadImageFromUrl

class SportSpacesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val sportSpaceName: TextView = view.findViewById(R.id.name)
    private val sportSpaceImage: ImageView = view.findViewById(R.id.imgSportSpace)
    private val sportSpaceGameMode: TextView = view.findViewById(R.id.txtGameMode)
    private val sportSpacePrice: TextView = view.findViewById(R.id.txtPrice)
    private val btnReservar: Button = view.findViewById(R.id.btnReservar)

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

        // Mostrar/ocultar botón según el rol del usuario
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val role = sharedPreferences.getString("role_type", null)

        if (role == "PLAYER") {
            btnReservar.visibility = View.VISIBLE
            btnReservar.setOnClickListener {
                val createReservationFragment = CreateReservationFragment()

                // Pasar el ID del espacio deportivo al fragmento de crear reserva
                val bundle = Bundle()
                bundle.putInt("sportSpaceId", sportSpace.id)
                createReservationFragment.arguments = bundle

                // Navegar al fragmento de crear reserva
                val activity = context as FragmentActivity
                activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, createReservationFragment)
                    .addToBackStack(null)
                    .commit()
            }
        } else {
            btnReservar.visibility = View.GONE
        }
    }
}