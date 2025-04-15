package com.example.dtaquito.auth

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import androidx.core.content.edit

class SaveCookieInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        val cookies = response.headers("Set-Cookie")
        for (cookie in cookies) {
            if (cookie.startsWith("JWT_TOKEN=")) {
                val token = cookie.substringAfter("JWT_TOKEN=").substringBefore(";")
                context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    .edit() {
                        putString("jwt_token", token)
                    }
                break
            }
        }
        return response
    }
}