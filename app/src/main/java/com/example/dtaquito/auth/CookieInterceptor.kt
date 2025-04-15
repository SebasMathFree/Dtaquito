package com.example.dtaquito.auth

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class CookieInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getString("jwt_token", null)

        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Cookie", "JWT_TOKEN=$token")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}