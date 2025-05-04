package com.example.dtaquito.suscription

import Beans.subscription.Subscriptions
import Interface.PlaceHolder
import MyCookieJar
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import com.example.dtaquito.auth.CookieInterceptor
import com.example.dtaquito.auth.SaveCookieInterceptor
import com.example.dtaquito.player.PlayerBase
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearSnapHelper
import environment.Environment

class SuscriptionActivity : PlayerBase() {

    private lateinit var service: PlaceHolder
    private lateinit var currentSubscriptionView: TextView
    private val cookieJar = MyCookieJar()

    companion object {
        private const val JWT_COOKIE_NAME = "JWT_TOKEN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suscription)
        setupBottomNavigation(R.id.navigation_suscriptions)

        initializeViews()
        service = createRetrofitService(this)

        val subscriptions = createSubscriptionList()
        fetchCurrentSubscription(subscriptions)
    }

    private fun initializeViews() {
        currentSubscriptionView = findViewById(R.id.currentSuscription)
    }

    private fun createRetrofitService(context: Context): PlaceHolder {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(SaveCookieInterceptor(context))
            .addInterceptor(CookieInterceptor(context))
            .build()

        return Retrofit.Builder()
            .baseUrl(Environment.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(PlaceHolder::class.java)
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
        service.getCurrentSubscription().enqueue(object : Callback<Subscriptions> {
            override fun onResponse(call: Call<Subscriptions>, response: Response<Subscriptions>) {
                if (response.isSuccessful) {
                    response.body()?.let { subscription ->
                        displayCurrentSubscription(subscription)
                        setupRecyclerView(subscriptions, subscription.planType)
                    }
                } else {
                    showToast("No se pudo obtener la suscripción actual.")
                }
            }

            override fun onFailure(call: Call<Subscriptions>, t: Throwable) {
                Log.e("SuscriptionActivity", "Error: ${t.message}")
                showToast("Error al obtener la suscripción: ${t.message}")
            }
        })
    }

    private fun setupRecyclerView(subscriptions: List<Subscriptions>, currentPlanType: String) {
        val recyclerView: RecyclerView = findViewById(R.id.suscriptionPackages)
        val adapter = SuscriptionAdapter(subscriptions, currentPlanType)
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
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(approvalUrl)))
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