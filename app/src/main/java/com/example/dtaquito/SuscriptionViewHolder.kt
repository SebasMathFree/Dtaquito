package com.example.dtaquito

import Beans.subscription.Subscriptions
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SuscriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val title: TextView = itemView.findViewById(R.id.subscription_title)
    private val price: TextView = itemView.findViewById(R.id.subscription_price)
    private val details: TextView = itemView.findViewById(R.id.subscription_details)
    private val button: Button = itemView.findViewById(R.id.subscribe_button)

    private fun mapPlanType(planType: String): String {
        return when (planType.lowercase()) {
            "bronce" -> "bronze"
            "plata" -> "silver"
            "oro" -> "gold"
            else -> planType
        }
    }

    fun renderSubscription(subscription: Subscriptions, currentPlanType: String, onItemClickListener: ((Subscriptions) -> Unit)?) {
        title.text = subscription.title
        price.text = subscription.price
        details.text = subscription.details
        itemView.setBackgroundColor(subscription.backgroundColor)

        val mappedCurrentPlanType = mapPlanType(currentPlanType)

        if (subscription.planType == mappedCurrentPlanType) {
            button.text = "Current Plan"
            button.isEnabled = false
            button.visibility = View.VISIBLE
        } else {
            button.text = "Update"
            button.isEnabled = true
            button.setOnClickListener {
                onItemClickListener?.invoke(subscription)
            }

            button.visibility = when (mappedCurrentPlanType) {
                "silver" -> if (subscription.planType == "bronze") View.GONE else View.VISIBLE
                "gold" -> if (subscription.planType == "bronze" || subscription.planType == "silver") View.GONE else View.VISIBLE
                else -> View.VISIBLE
            }
        }
    }
}