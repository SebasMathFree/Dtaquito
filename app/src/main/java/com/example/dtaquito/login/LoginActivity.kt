package com.example.dtaquito.login

import Beans.auth.login.LoginRequest
import Beans.auth.login.LoginResponse
import Interface.PlaceHolder
import MyCookieJar
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.dtaquito.R
import com.example.dtaquito.auth.CookieInterceptor
import com.example.dtaquito.auth.SaveCookieInterceptor
import com.example.dtaquito.profile.ProfileActivity
import com.example.dtaquito.register.RegisterActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {

    // Variables privadas
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginBtn: Button
    private lateinit var signUpBtn: TextView
    private lateinit var forgotPass: TextView
    private lateinit var service: PlaceHolder
    private lateinit var cookieJar: MyCookieJar
    private var userId: Int = -1

    companion object {
        private const val BASE_URL = "http://10.0.2.2:8080/"
        private const val SHARED_PREFS = "user_prefs"
        private const val JWT_TOKEN_KEY = "jwt_token"
        private const val ROLE_TYPE_KEY = "role_type"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        cookieJar = MyCookieJar()
        service = createRetrofitService(this)

        initializeUI()
        setupHyperlinks()
        setupListeners()
    }

    // Inicialización de vistas
    private fun initializeUI() {
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        loginBtn = findViewById(R.id.login_btn)
        signUpBtn = findViewById(R.id.newUser)
        forgotPass = findViewById(R.id.forgotPassword)
    }

    // Configuración de listeners
    private fun setupListeners() {
        loginBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                showToast("Por favor, ingresa tu email y contraseña.")
                return@setOnClickListener
            }
            loginUser(email, password)
        }
    }

    // Configuración de hipervínculos
    private fun setupHyperlinks() {
        // Hipervínculo de registro
        val signUpSpannable = SpannableString(signUpBtn.text)
        val signUpClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                navigateToRegister()
            }
        }
        val colorSpan = ForegroundColorSpan(ContextCompat.getColor(this, R.color.green))
        val underlineSpan = UnderlineSpan()

        signUpSpannable.setSpan(signUpClickableSpan, 10, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        signUpSpannable.setSpan(colorSpan, 10, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        signUpSpannable.setSpan(underlineSpan, 10, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        signUpBtn.text = signUpSpannable
        signUpBtn.movementMethod = LinkMovementMethod.getInstance()

        // Hipervínculo de olvido de contraseña
        val forgotPassSpannable = SpannableString(forgotPass.text)
        val forgotPassClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginActivity, ProfileActivity::class.java)
                startActivity(intent)
            }
        }
        forgotPassSpannable.setSpan(forgotPassClickableSpan, 0, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        forgotPass.text = forgotPassSpannable
        forgotPass.movementMethod = LinkMovementMethod.getInstance()
    }

    // Lógica de inicio de sesión
    private fun loginUser(email: String, password: String) {
        val loginRequest = LoginRequest(email, password)
        service.loginUser(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        userId = it.id
                        getUserInfo()
                    }
                    showToast("Inicio de sesión exitoso.")
                } else {
                    showToast("Usuario o contraseña incorrectos.")
                    Log.e("LoginActivity", "Fallo login, código: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("LoginActivity", "Error en login", t)
            }
        })
    }

    // Obtener información del usuario
    private fun getUserInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.getUserId().execute()
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        val jwtToken = response.headers()["Set-Cookie"]?.let { extractJwtToken(it) }
                        jwtToken?.let { saveToSharedPreferences(JWT_TOKEN_KEY, it) }

                        withContext(Dispatchers.Main) {
                            saveToSharedPreferences(ROLE_TYPE_KEY, user.roleType)
                            redirectToMainActivity(user.roleType)
                        }
                    }
                } else {
                    Log.e("LoginActivity", "Error al obtener usuario: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error de red al obtener usuario", e)
            }
        }
    }

    // Métodos auxiliares
    private fun extractJwtToken(cookieHeader: String): String? {
        val jwtRegex = "JWT_TOKEN=([^;]+)".toRegex()
        return jwtRegex.find(cookieHeader)?.groupValues?.get(1)
    }

    private fun saveToSharedPreferences(key: String, value: String) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        sharedPreferences.edit().putString(key, value).apply()
    }

    private fun redirectToMainActivity(roleType: String) {
        val intent = when (roleType) {
            "PLAYER", "OWNER" -> Intent(this, ProfileActivity::class.java)
            else -> Intent(this, LoginActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Configuración de Retrofit
    private fun createRetrofitService(context: Context): PlaceHolder {
        val client = OkHttpClient.Builder()
            .addInterceptor(SaveCookieInterceptor(context))
            .addInterceptor(CookieInterceptor(context))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(PlaceHolder::class.java)
    }
}