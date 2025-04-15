package com.example.dtaquito

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlayerListViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private val userNameTextView: TextView = view.findViewById(R.id.userNameTextView)

    fun renderPlayer(userName: String) {
        userNameTextView.text = userName
    }
}