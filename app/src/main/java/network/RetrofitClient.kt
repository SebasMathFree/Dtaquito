package network

import MyCookieJar
import android.content.Context
import com.example.dtaquito.auth.SaveCookieInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://dtaquito-backend.azurewebsites.net/"
    //private const val CHAT_URL = "ws://dtaquito-backend.azurewebsites.net/ws/chat"
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private val client: OkHttpClient by lazy {
        val context = appContext ?: throw IllegalStateException("RetrofitClient not initialized.")
        OkHttpClient.Builder()
            .addInterceptor(SaveCookieInterceptor(context))
            .cookieJar(MyCookieJar())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}