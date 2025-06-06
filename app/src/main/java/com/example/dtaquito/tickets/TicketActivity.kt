package com.example.dtaquito.tickets

import Interface.PlaceHolder
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import com.example.dtaquito.player.PlayerBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import network.RetrofitClient

class TicketActivity : PlayerBase() {

    private val service = RetrofitClient.instance.create(PlaceHolder::class.java)
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TicketAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d("TicketActivity", "Inicializando TicketActivity")
        setContentView(R.layout.activity_ticket)
        setupBottomNavigation(R.id.navigation_tickets)

        setupRecyclerView()
        val createTicketButton = findViewById<Button>(R.id.create_ticket_btn)
        createTicketButton.setOnClickListener {
            val userCredits = intent.getDoubleExtra("USER_CREDITS", 0.0)
            Log.d("TicketActivity", "USER_CREDITS recibido: $userCredits")
            val intent = Intent(this, CreateTicketActivity::class.java)
            intent.putExtra("USER_CREDITS", userCredits)
            Log.d("TicketActivity", "Navegando a CreateTicketActivity con USER_CREDITS: $userCredits")
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        Log.d("TicketActivity", "Configurando RecyclerView")
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("TicketActivity", "Obteniendo lista de tickets desde el servicio")
                val tickets = service.getBankTransfers()
                val sortedTickets = tickets.sortedByDescending { it.createdAt }
                Log.d("TicketActivity", "Tickets obtenidos: $tickets")
                withContext(Dispatchers.Main) {
                    adapter = TicketAdapter(sortedTickets)
                    recyclerView.adapter = adapter
                    Log.d("TicketActivity", "RecyclerView configurado con tickets ordenados")
                }
            } catch (e: Exception) {
                Log.e("TicketActivity", "Error al obtener tickets: ${e.message}", e)
            }
        }
    }
}