package com.example.dtaquito.register

import Beans.auth.register.RegisterRequest
import Beans.userProfile.UserProfile
import Interface.PlaceHolder
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.example.dtaquito.R
import com.example.dtaquito.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import environment.Environment

class RegisterActivity : AppCompatActivity() {

    // Variables privadas
    private var isRoleSelected = false
    private var selectedRolePosition = 0
    private lateinit var selectedRole: String
    private lateinit var signIn: TextView
    private lateinit var service: PlaceHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        initializeUI()
        setupRetrofit()
        setupSpinner()
        setupRegisterButton()
    }

    // Inicialización de vistas y configuración de hipervínculo
    private fun initializeUI() {
        signIn = findViewById(R.id.signIn)
        val signInSpannable = SpannableString(signIn.text)
        val signInClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                navigateToLogin()
            }
        }
        val colorSpan = ForegroundColorSpan(ContextCompat.getColor(this, R.color.green))
        val underlineSpan = UnderlineSpan()

        signInSpannable.setSpan(signInClickableSpan, 29, 36, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        signInSpannable.setSpan(colorSpan, 29, 36, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        signInSpannable.setSpan(underlineSpan, 29, 36, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        signIn.text = signInSpannable
        signIn.movementMethod = LinkMovementMethod.getInstance()
    }

    // Configuración de Retrofit
    private fun setupRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl(Environment.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(PlaceHolder::class.java)
    }

    // Configuración del Spinner
    private fun setupSpinner() {
        val spinner = findViewById<Spinner>(R.id.rol_input)
        val items = listOf("Choose a role: ","Player", "Owner")
        val adapter = object : ArrayAdapter<String>(this, R.layout.spinner_items, items) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                if (position == 0) {
                    textView.setTextColor("#4D4D4D".toColorInt()) // Color para la primera opción
                } else {
                    textView.setTextColor(Color.WHITE) // Color para las demás opciones
                }
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                if (position == 0) {
                    textView.setTextColor("#4D4D4D".toColorInt()) // Color para la primera opción
                } else {
                    textView.setTextColor(Color.WHITE) // Color para las demás opciones
                }
                return view
            }
        }
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    // No se seleccionó ninguna opción válida
                    isRoleSelected = false
                    selectedRole = ""

                } else {
                    // Se seleccionó una opción válida
                    isRoleSelected = true
                    selectedRole = items[position]
                    selectedRolePosition = position
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No se seleccionó nada
                isRoleSelected = false
                selectedRole = ""
            }
        }
    }

    // Configuración del botón de registro
    private fun setupRegisterButton() {
        val nameInput = findViewById<EditText>(R.id.name_input)
        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val registerBtn = findViewById<Button>(R.id.register_btn)

        registerBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (!validateInputs(name, email, password)) return@setOnClickListener

            val registerRequest = RegisterRequest(
                name = name,
                email = email,
                password = password,
                role = selectedRole.uppercase()
            )
            registerUser(registerRequest)
        }
    }

    // Validación de entradas
    private fun validateInputs(name: String, email: String, password: String): Boolean {
        if (name.isEmpty()) {
            showToast("Por favor, ingresa tu nombre.")
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Por favor, ingresa un email válido.")
            return false
        }
        if (password.length < 16) {
            showToast("La contraseña debe tener al menos 16 caracteres.")
            return false
        }
        if (!isRoleSelected || selectedRolePosition == 0) {
            showToast("Por favor, selecciona un rol válido.")
            return false
        }
        return true
    }

    // Registro del usuario
    private fun registerUser(registerRequest: RegisterRequest) {
        service.createUser(registerRequest).enqueue(object : Callback<UserProfile> {
            override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                if (response.isSuccessful) {
                    showToast("Usuario registrado correctamente.")
                    navigateToLogin()
                } else {
                    showToast("Error al registrar usuario.")
                }
            }

            override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                showToast("Error de red.")
            }
        })
    }

    // Navegación a la pantalla de inicio de sesión
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Mostrar mensajes de Toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}