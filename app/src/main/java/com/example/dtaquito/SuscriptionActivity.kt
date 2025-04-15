//package com.example.dtaquito
//
//import Beans.subscription.Subscriptions
//import Interface.PlaceHolder
//import android.content.Intent
//import android.net.Uri
//import android.os.Bundle
//import android.util.Log
//import android.widget.TextView
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import android.widget.Toast
//import com.example.dtaquito.auth.TokenManager
//import okhttp3.ResponseBody
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import org.json.JSONObject
//
//class SuscriptionActivity : PlayerBase() {
//
//    private lateinit var service: PlaceHolder
//    private lateinit var tokenManager: TokenManager
//    private lateinit var currentSubscriptionView: TextView
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_suscription)
//        setupBottomNavigation(R.id.navigation_suscriptions)
//        tokenManager = TokenManager(this)
//        service = createRetrofit().create(PlaceHolder::class.java)
//        currentSubscriptionView = findViewById(R.id.currentSuscription)
//
//        val recyclerView: RecyclerView = findViewById(R.id.suscriptionPackages)
//        val subscriptions = createSubscriptionList()
//        val userId = tokenManager.getUserId()
//
//        if (userId != -1) {
//            fetchCurrentSubscription(userId, subscriptions, recyclerView)
//        } else {
//            showToast("User ID not found.")
//        }
//    }
//
//    private fun createSubscriptionList(): List<Subscriptions> {
//        return listOf(
//            Subscriptions(
//                "Plan BRONCE",
//                "S/ 29.99/mes",
//                "Agregar 1 espacio deportivo\n" +
//                        "Personalización de tus espacios deportivos\n" +
//                        "Mayor visibilidad de tus espacios deportivos\n" +
//                        "Servicio al cliente 24 horas",
//                ContextCompat.getColor(this, R.color.bronze),
//                "bronze"
//            ),
//            Subscriptions(
//                "Plan PLATA",
//                "S/ 49.99/mes",
//                "Agregar 2 espacios deportivos\n" +
//                        "Personalización de tus espacios deportivos\n" +
//                        "Mayor visibilidad de tus espacios deportivos\n" +
//                        "Servicio al cliente 24 horas",
//                ContextCompat.getColor(this, R.color.silver),
//                "silver"
//            ),
//            Subscriptions(
//                "Plan ORO",
//                "S/ 69.99/mes",
//                "Agregar 3 espacios deportivos\n" +
//                        "Personalización de tus espacios deportivos\n" +
//                        "Mayor visibilidad de tus espacios deportivos\n" +
//                        "Servicio al cliente 24 horas",
//                ContextCompat.getColor(this, R.color.gold),
//                "gold"
//            )
//        )
//    }
//
//    private fun fetchCurrentSubscription(userId: Int, subscriptions: List<Subscriptions>, recyclerView: RecyclerView) {
//        service.getCurrentSubscription(userId).enqueue(object : Callback<Subscriptions> {
//            override fun onResponse(call: Call<Subscriptions>, response: Response<Subscriptions>) {
//                if (response.isSuccessful) {
//                    response.body()?.let {
//                        displayCurrentSubscription(it)
//                        setupRecyclerView(subscriptions, it.planType, recyclerView, userId)
//                    }
//                } else {
//                    showToast("Failed to fetch current subscription.")
//                }
//            }
//
//            override fun onFailure(call: Call<Subscriptions>, t: Throwable) {
//                showToast("Error: ${t.message}")
//            }
//        })
//    }
//
//    private fun setupRecyclerView(subscriptions: List<Subscriptions>, currentPlanType: String, recyclerView: RecyclerView, userId: Int) {
//        val adapter = SuscriptionAdapter(subscriptions, currentPlanType)
//        recyclerView.adapter = adapter
//        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        adapter.setOnItemClickListener { subscription -> upgradeSubscription(userId, subscription) }
//    }
//
//    private fun upgradeSubscription(userId: Int, subscription: Subscriptions) {
//        service.upgradeSubscription(userId, subscription.planType).enqueue(object : Callback<ResponseBody> {
//            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                if (response.isSuccessful) {
//                    response.body()?.string()?.let { extractApprovalUrl(it) }?.let { approvalUrl ->
//                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(approvalUrl)))
//                    } ?: showToast("Approval URL not found.")
//                } else {
//                    showToast("Failed to upgrade subscription.")
//                }
//            }
//
//            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                showToast("Error: ${t.message}")
//            }
//        })
//    }
//
//    private fun extractApprovalUrl(responseBody: String): String? {
//        return try {
//            JSONObject(responseBody).getString("approval_url")
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//    private fun createRetrofit(): Retrofit {
//        val logging = HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
//        val client = OkHttpClient.Builder()
//            .addInterceptor(logging)
//            .addInterceptor { chain ->
//                val request: Request = chain.request()
//                Log.d("URLInterceptor", "Request URL: ${request.url}")
//                chain.proceed(request)
//            }
//            .addInterceptor(AuthInterceptor(tokenManager))
//            .build()
//
//        return Retrofit.Builder()
//            .baseUrl("https://dtaquito-backend.azurewebsites.net/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(client)
//            .build()
//    }
//
//    private fun displayCurrentSubscription(subscription: Subscriptions) {
//        val planType = subscription.planType.lowercase()
//        Log.d("Subscription", "Current plan type: $planType")
//        currentSubscriptionView.text = "TU SUSCRIPCIÓN ACTUAL ES: ${planType.uppercase()}"
//    }
//
//    private fun showToast(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }
//}