package com.example.dtaquito.tickets

import Beans.tickets.Tickets
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import java.text.SimpleDateFormat
import java.util.Locale

class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val fullNameTextView: TextView = itemView.findViewById(R.id.fullNameTextView)
    private val bankNameTextView: TextView = itemView.findViewById(R.id.bankNameTextView)
    private val transferTypeTextView: TextView = itemView.findViewById(R.id.transferTypeTextView)
    private val accountNumberTextView: TextView = itemView.findViewById(R.id.accountNumberTextView)
    private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
    private val ticketNumberTextView: TextView = itemView.findViewById(R.id.ticketNumberTextView)
    private val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
    private val createdTextView: TextView = itemView.findViewById(R.id.createdTextView)

    fun bind(ticket: Tickets) {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm dd/MM/yy", Locale.getDefault())

        val formattedDate = if (!ticket.createdAt.isNullOrEmpty()) {
            try {
                val createdDate = inputFormat.parse(ticket.createdAt)
                outputFormat.format(createdDate)
            } catch (e: Exception) {
                "Fecha inválida"
            }
        } else {
            "Sin fecha"
        }
        fullNameTextView.text = "Nombre completo: ${ticket.fullName}"
        bankNameTextView.text = "Banco: ${ticket.bankName}"
        transferTypeTextView.text = "Tipo de transferencia: ${ticket.transferType}"
        accountNumberTextView.text = "Número de cuenta: ${ticket.accountNumber}"
        statusTextView.text = ticket.status.toString()
        ticketNumberTextView.text = "Ticket #${ticket.ticketNumber}"
        amountTextView.text = "Monto: ${ticket.amount}"
        createdTextView.text = "Creado: $formattedDate"
    }
}