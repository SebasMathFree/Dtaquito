package com.example.dtaquito.login

import Beans.auth.login.LoginRequest
import Beans.auth.login.LoginResponse
import Interface.PlaceHolder
import MyCookieJar
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.dtaquito.ProfileActivity
import com.example.dtaquito.R
import com.example.dtaquito.auth.CookieInterceptor
import com.example.dtaquito.auth.SaveCookieInterceptor
import com.example.dtaquito.register.RegisterActivity
//import com.example.dtaquito.SportActivity
//import com.example.dtaquito.SuscriptionActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginBtn: Button
    private lateinit var registerBtn: Button
    private lateinit var service: PlaceHolder
    private lateinit var cookieJar: MyCookieJar
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        cookieJar = MyCookieJar()

        val retrofit = createRetrofit(this)
        service = retrofit.create(PlaceHolder::class.java)

        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        loginBtn = findViewById(R.id.login_btn)
        registerBtn = findViewById(R.id.register_btn)

        registerBtn.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        loginBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    "Por favor, ingresa tu email y contraseña.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            loginUser(email, password)
        }
    }


    private fun createRetrofit(context: Context): Retrofit {

        val cookieInterceptor = CookieInterceptor(context)
        val saveCookieInterceptor = SaveCookieInterceptor(context)


        val client = OkHttpClient.Builder()
            .addInterceptor(saveCookieInterceptor)
            .addInterceptor(cookieInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }


    private fun loginUser(email: String, password: String) {
        val loginRequest = LoginRequest(email, password)
        service.loginUser(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    loginResponse?.let {
                        userId = it.id
                        getUserInfo()
                    }

//                    val jwtToken = extractJwtToken()
//                    jwtToken?.let {
//                        Log.d("LoginActivity", "JWT_TOKEN recibido: $it")
//                    }

                    Toast.makeText(
                        this@LoginActivity,
                        "Inicio de sesión exitoso.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Usuario o contraseña incorrectos.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("LoginActivity", "Fallo login, código: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("LoginActivity", "Error en login", t)
            }
        })
    }

//    private fun extractJwtToken(): String? {
//        val url = "http://10.0.2.2:8080/".toHttpUrl()
//        val cookies: List<Cookie> = cookieJar.loadForRequest(url)
//        for (cookie in cookies) {
//            if (cookie.name == "JWT_TOKEN") {
//                return cookie.value
//            }
//        }
//        return null
//    }

    private fun getUserInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.getUserId().execute()
                if (response.isSuccessful) {
                    val user = response.body()
                    user?.let {
                        val jwtToken = response.headers()["Set-Cookie"]?.let { cookieHeader ->
                            extractJwtTokenFromCookie(cookieHeader)
                        }

                        jwtToken?.let {
                            saveJwtTokenCookie(it)
                        }

                        withContext(Dispatchers.Main) {
                            saveUserRoleType(it.roleType)
                            it.id?.let { id -> redirectToMainActivity(it.roleType) }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e("LoginActivity", "Error al obtener usuario: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("LoginActivity", "Error de red al obtener usuario", e)
                }
            }
        }
    }

    private fun extractJwtTokenFromCookie(cookieHeader: String): String? {
        val jwtRegex = "JWT_TOKEN=([^;]+)".toRegex()
        return jwtRegex.find(cookieHeader)?.groupValues?.get(1)
    }

    private fun saveJwtTokenCookie(jwtToken: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("jwt_token", jwtToken)
        editor.apply()
    }

    private fun redirectToMainActivity(roleType: String) {
        val intent = when (roleType) {
            "PLAYER" -> Intent(this@LoginActivity, ProfileActivity::class.java)
            "OWNER" -> Intent(this@LoginActivity, ProfileActivity::class.java)
            else -> Intent(this@LoginActivity, LoginActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun saveUserRoleType(roleType: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("role_type", roleType)
        editor.apply()
    }
}
