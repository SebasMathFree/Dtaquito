package com.example.dtaquito.register

import Beans.auth.register.RegisterRequest
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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.example.dtaquito.R
import com.example.dtaquito.login.LoginActivity
import com.example.dtaquito.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import network.RetrofitClient

class RegisterActivity : AppCompatActivity() {

    // Variables privadas
    private var isRoleSelected = false
    private var selectedRolePosition = 0
    private lateinit var selectedRole: String
    private lateinit var signIn: TextView
    private val service by lazy { RetrofitClient.instance.create(PlaceHolder::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        initializeUI()
        setupSpinner()
        setupRegisterButton()
    }

    private fun initializeUI() {
        signIn = findViewById(R.id.signIn)
        val signInText = signIn.text.toString()
        val signInClickable = getString(R.string.sign_in_clickable)
        val signInStart = signInText.indexOf(signInClickable)
        val signInEnd = signInStart + signInClickable.length

        if (signInStart >= 0 && signInEnd <= signInText.length) {
            val signInSpannable = SpannableString(signInText)
            val signInClickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    navigateToLogin()
                }
            }
            val colorSpan = ForegroundColorSpan(ContextCompat.getColor(this, R.color.green))
            val underlineSpan = UnderlineSpan()

            signInSpannable.setSpan(signInClickableSpan, signInStart, signInEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            signInSpannable.setSpan(colorSpan, signInStart, signInEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            signInSpannable.setSpan(underlineSpan, signInStart, signInEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            signIn.text = signInSpannable
            signIn.movementMethod = LinkMovementMethod.getInstance()
        }
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
            registerBtn.isEnabled = false // Deshabilitar el botón para evitar múltiples clics
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (!validateInputs(name, email, password)) {
                registerBtn.isEnabled = true // Rehabilitar el botón si hay un error
                return@setOnClickListener
            }

            val registerRequest = RegisterRequest(
                name = name,
                email = email,
                password = password,
                role = selectedRole.uppercase()
            )
            registerUser(registerRequest, registerBtn)
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
    private fun registerUser(registerRequest: RegisterRequest, registerBtn: Button) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { service.createUser(registerRequest) }
                if (response.isSuccessful) {
                    showToast("Usuario registrado correctamente.")
                    navigateToLogin()
                } else {
                    showToast("Error al registrar usuario.")
                    registerBtn.isEnabled = true // Rehabilitar el botón en caso de error
                }
            } catch (_: Exception) {
                showToast("Error de red.")
                registerBtn.isEnabled = true // Rehabilitar el botón en caso de error
            }
        }
    }

    // Navegación a la pantalla de inicio de sesión
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

}