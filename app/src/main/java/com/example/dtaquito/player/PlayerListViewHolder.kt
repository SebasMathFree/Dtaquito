package com.example.dtaquito.player

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R

class PlayerListViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val playerName: TextView = view.findViewById(R.id.playerName)
    val playerIcon: ImageView = view.findViewById(R.id.playerIcon)

    fun bind(name: String) {
        playerName.text = name
    }
}