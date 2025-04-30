package com.example.dtaquito.sportspace

import Beans.sportspaces.SportSpace
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R

class SportSpaceAdapter(val sportSpacesList: List<SportSpace>) : RecyclerView.Adapter<SportSpacesViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SportSpacesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return SportSpacesViewHolder(layoutInflater.inflate(R.layout.card_sport_space, parent, false))
    }

    override fun getItemCount(): Int = sportSpacesList.size

    override fun onBindViewHolder(holder: SportSpacesViewHolder, position: Int) {
        val item = sportSpacesList[position]
        holder.render(item)
    }
}