package com.example.dtaquito.subscription

import Beans.subscription.Subscriptions
import Interface.PlaceHolder
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import com.example.dtaquito.player.PlayerBase
import network.RetrofitClient
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubscriptionActivity : PlayerBase() {

    private val service = RetrofitClient.instance.create(PlaceHolder::class.java)
    private lateinit var currentSubscriptionView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suscription)
        setupBottomNavigation(R.id.navigation_suscriptions)

        initializeViews()

        val subscriptions = createSubscriptionList()
        fetchCurrentSubscription(subscriptions)
    }

    private fun initializeViews() {
        currentSubscriptionView = findViewById(R.id.currentSuscription)
    }

    private fun createSubscriptionList(): List<Subscriptions> {
        return listOf(
            Subscriptions(
                "BRONZE PLAN",
                "$ 4.99/mes",
                "Add 1 sports space\nCustomization of your sports spaces\nGreater visibility of your sports spaces\n24-hour customer service",
                ContextCompat.getColor(this, R.color.bronze),
                "BRONZE"
            ),
            Subscriptions(
                "SILVER PLAN",
                "$ 9.99/mes",
                "Add 2 sports spaces\nCustomization of your sports spaces\nGreater visibility of your sports spaces\n24-hour customer service",
                ContextCompat.getColor(this, R.color.silver),
                "SILVER"
            ),
            Subscriptions(
                "GOLD PLAN",
                "$ 14.99/mes",
                "Add 3 sports spaces\nCustomization of your sports spaces\nGreater visibility of your sports spaces\n24-hour customer service",
                ContextCompat.getColor(this, R.color.gold),
                "GOLD"
            )
        )
    }

    private fun fetchCurrentSubscription(subscriptions: List<Subscriptions>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.getCurrentSubscription().execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let { subscription ->
                            displayCurrentSubscription(subscription)
                            setupRecyclerView(subscriptions, subscription.planType)
                        }
                    } else {
                        showToast("No se pudo obtener la suscripción actual.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("SuscriptionActivity", "Error: ${e.message}")
                    showToast("Error al obtener la suscripción.")
                }
            }
        }
    }

    private fun setupRecyclerView(subscriptions: List<Subscriptions>, currentPlanType: String) {
        val recyclerView: RecyclerView = findViewById(R.id.suscriptionPackages)
        val adapter = SubscriptionAdapter(subscriptions, currentPlanType)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.setHasFixedSize(true)
        //Agregar LinearSnapHelper para hacer scroll horizontal
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)
        adapter.setOnItemClickListener { subscription -> upgradeSubscription(subscription) }
    }

    private fun upgradeSubscription(subscription: Subscriptions) {
        service.upgradeSubscription(subscription.planType).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.string()?.let { extractApprovalUrl(it) }?.let { approvalUrl ->
                        startActivity(Intent(Intent.ACTION_VIEW, approvalUrl.toUri()))
                    } ?: showToast("No se encontró la URL de aprobación.")
                } else {
                    showToast("No se pudo actualizar la suscripción.")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("SuscriptionActivity", "Error: ${t.message}")
                showToast("Error al actualizar la suscripción: ${t.message}")
            }
        })
    }

    private fun extractApprovalUrl(responseBody: String): String? {
        return try {
            JSONObject(responseBody).getString("approval_url")
        } catch (e: Exception) {
            Log.e("SuscriptionActivity", "Error al extraer la URL de aprobación: ${e.message}")
            null
        }
    }

    private fun displayCurrentSubscription(subscription: Subscriptions) {
        val planType = subscription.planType.uppercase()
        val spannable = SpannableString("Your current subscription is: $planType")

        val color = when (planType) {
            "BRONZE" -> ContextCompat.getColor(this, R.color.bronze)
            "SILVER" -> ContextCompat.getColor(this, R.color.silver)
            "GOLD" -> ContextCompat.getColor(this, R.color.gold)
            else -> "#FFFFFF".toColorInt()
        }

        val startIndex = spannable.indexOf(planType)
        val endIndex = startIndex + planType.length

        spannable.setSpan(
            ForegroundColorSpan(color),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        currentSubscriptionView.text = spannable
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}