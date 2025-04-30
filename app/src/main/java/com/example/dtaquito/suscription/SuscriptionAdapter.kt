package com.example.dtaquito.suscription

import Beans.subscription.Subscriptions
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R

class SuscriptionAdapter(
    private val subscriptions: List<Subscriptions>,
    private val currentPlanType: String
) : RecyclerView.Adapter<SuscriptionViewHolder>() {

    private var onItemClickListener: ((Subscriptions) -> Unit)? = null

    fun setOnItemClickListener(listener: (Subscriptions) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuscriptionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_suscription, parent, false)
        return SuscriptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuscriptionViewHolder, position: Int) {
        holder.renderSubscription(subscriptions[position], currentPlanType, onItemClickListener)
    }

    override fun getItemCount() = subscriptions.size
}