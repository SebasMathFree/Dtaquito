package com.example.dtaquito.chat

import Beans.chat.ChatMessage
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import java.text.SimpleDateFormat
import java.util.Locale

class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
    private val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
    private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)

    fun bind(message: ChatMessage) {
        userNameTextView.text = "${message.userName.uppercase()}:"
        messageTextView.text = message.content
        timestampTextView.text = formatDate(message.createdAt)
    }
}
fun formatDate(dateString: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
    val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val date = inputFormat.parse(dateString)
    return outputFormat.format(date)
}