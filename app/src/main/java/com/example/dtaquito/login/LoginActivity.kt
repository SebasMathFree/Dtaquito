package com.example.dtaquito.login

import Beans.auth.login.LoginRequest
import Interface.PlaceHolder
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.dtaquito.MainActivity
import com.example.dtaquito.R
import com.example.dtaquito.forgotPassword.ForgotPasswordActivity
import com.example.dtaquito.register.RegisterActivity
import com.example.dtaquito.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import network.RetrofitClient
import java.util.Locale
import androidx.core.content.edit

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginBtn: Button
    private lateinit var signUpBtn: TextView
    private lateinit var forgotPass: TextView
    private lateinit var selectLanguageBtn: Button
    private var userId: Int = -1
    private val service = RetrofitClient.instance.create(PlaceHolder::class.java)

    companion object {
        private const val SHARED_PREFS = "user_prefs"
        private const val JWT_TOKEN_KEY = "jwt_token"
        private const val ROLE_TYPE_KEY = "role_type"
        private const val USER_CREDITS = "credits"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val lang = prefs.getString("app_lang", Locale.getDefault().language) ?: "es"
        setLocale(this, lang)
        setContentView(R.layout.activity_login)

        initializeUI()
        setupHyperlinks()
        setupListeners()

        selectLanguageBtn = findViewById(R.id.btn_select_language)
        selectLanguageBtn.setOnClickListener { showLanguageDialog() }

    }

    private fun showLanguageDialog() {
        val idiomas = arrayOf(
            getString(R.string.spanish),
            getString(R.string.english)
        )
        val codigos = arrayOf("es", "en")
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            idiomas
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(ContextCompat.getColor(context, R.color.white))
                return view
            }
        }

        AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setTitle(getString(R.string.language))
            .setAdapter(adapter) { _, which ->
                setLocale(this, codigos[which])
                recreate()
            }
            .show()
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun setLocale(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        val prefs = context.getSharedPreferences("settings", MODE_PRIVATE)
        prefs.edit { putString("app_lang", language) }
    }

    private fun initializeUI() {
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        loginBtn = findViewById(R.id.login_btn)
        signUpBtn = findViewById(R.id.newUser)
        forgotPass = findViewById(R.id.forgotPassword)
    }

    private fun setupListeners() {
        loginBtn.setOnClickListener {
            hideKeyboard()
            loginBtn.isEnabled = false
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                showToast("Por favor, ingresa tu email y contraseña.")
                loginBtn.isEnabled = true
                return@setOnClickListener
            }
            loginUser(email, password)
        }
    }

    private fun setupHyperlinks() {
        val signUpText = signUpBtn.text.toString()
        val signUpClickable = getString(R.string.sign_up_clickable)
        val signUpStart = signUpText.indexOf(signUpClickable)
        val signUpEnd = signUpStart + signUpClickable.length
        if (signUpStart >= 0 && signUpEnd <= signUpText.length) {
            val signUpSpannable = SpannableString(signUpText)
            val signUpClickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    navigateToRegister()
                }
            }
            val colorSpan = ForegroundColorSpan(ContextCompat.getColor(this, R.color.green))
            val underlineSpan = UnderlineSpan()

            signUpSpannable.setSpan(signUpClickableSpan, signUpStart, signUpEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            signUpSpannable.setSpan(colorSpan, signUpStart, signUpEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            signUpSpannable.setSpan(underlineSpan, signUpStart, signUpEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            signUpBtn.text = signUpSpannable
            signUpBtn.movementMethod = LinkMovementMethod.getInstance()
        }

        val forgotPassText = forgotPass.text.toString()
        val forgotPassClickable = getString(R.string.forgot_password_clickable)
        val forgotPassStart = forgotPassText.indexOf(forgotPassClickable)
        val forgotPassEnd = forgotPassStart + forgotPassClickable.length
        if (forgotPassStart >= 0 && forgotPassEnd <= forgotPassText.length) {
            val forgotPassSpannable = SpannableString(forgotPassText)
            val forgotPassClickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
                    startActivity(intent)
                }
            }
            forgotPassSpannable.setSpan(forgotPassClickableSpan, forgotPassStart, forgotPassEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            forgotPass.text = forgotPassSpannable
            forgotPass.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun loginUser(email: String, password: String) {
        val loginRequest = LoginRequest(email, password)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = service.loginUser(loginRequest)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let { user ->
                            userId = user.id
                            val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                            prefs.edit { putInt("user_id", userId) }
                            getUserInfo()
                        }
                        showToast("Inicio de sesión exitoso.")
                    } else {
                        showToast("Usuario o contraseña incorrectos.")
                        loginBtn.isEnabled = true
                        Log.e("LoginActivity", "Fallo login, código: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("LoginActivity", "Error en login: ${e.message}", e)
                    showToast("Error de red: ${e.message}")
                    loginBtn.isEnabled = true
                }
            }
        }
    }

    private fun getUserInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.getUserId()
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        val jwtToken = response.headers()["Set-Cookie"]?.let { extractJwtToken(it) }
                        jwtToken?.let { saveToSharedPreferences(JWT_TOKEN_KEY, it) }
                        withContext(Dispatchers.Main) {
                            saveToSharedPreferences("user_name", user.name)
                            saveToSharedPreferences("user_email", user.email)
                            saveToSharedPreferences(ROLE_TYPE_KEY, user.roleType)
                            Log.d("PasoDeCreditos", "LoginActivity - Créditos obtenidos: ${user.credits}")
                            saveToSharedPreferences(USER_CREDITS, user.credits.toString())
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

    private fun extractJwtToken(cookieHeader: String): String? {
        val jwtRegex = "JWT_TOKEN=([^;]+)".toRegex()
        return jwtRegex.find(cookieHeader)?.groupValues?.get(1)
    }

    private fun saveToSharedPreferences(key: String, value: String) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString(key, value)
            apply()
        }
    }

    private fun redirectToMainActivity(roleType: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("ROLE_TYPE", roleType)
        startActivity(intent)
        finish()
    }

    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}