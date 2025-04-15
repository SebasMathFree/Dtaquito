package com.example.dtaquito.register

import Beans.auth.register.RegisterRequest
import Beans.userProfile.UserProfile
import Interface.PlaceHolder
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.dtaquito.R
import com.example.dtaquito.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegisterActivity : AppCompatActivity() {

    private var isRoleSelected = false
    private var selectedRolePosition = 0
    private lateinit var selectedRole: String
    private lateinit var service: PlaceHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)


        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(PlaceHolder::class.java)

        val spinner = findViewById<Spinner>(R.id.rol_input)
        val items = listOf("Rol", "PLAYER", "OWNER")

        val adapter = ArrayAdapter(this, R.layout.spinner_items, items)
        adapter.setDropDownViewResource(R.layout.spinner_items)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {
                selectedRolePosition = position
                isRoleSelected = position != 0
                selectedRole = when (position) {
                    1 -> "PLAYER" // Jugador
                    2 -> "OWNER" // Propietario
                    else -> ""
                }

                Toast.makeText(this@RegisterActivity, items[position], Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                isRoleSelected = false
            }
        }

        val nameInput = findViewById<EditText>(R.id.name_input)
        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val registerBtn = findViewById<Button>(R.id.register_btn)

        registerBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa tu nombre.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Por favor, ingresa un email válido.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            if (password.length < 16) {
                Toast.makeText(
                    this,
                    "La contraseña debe tener al menos 16 caracteres.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (!isRoleSelected) {
                Toast.makeText(this, "Por favor, selecciona un rol.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedRolePosition == 0) {
                Toast.makeText(
                    this,
                    "Rol no válido. Por favor, selecciona un rol.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val registerRequest = RegisterRequest(
                name = name,
                email = email,
                password = password,
                role = selectedRole
            )

            service.createUser(registerRequest).enqueue(object : Callback<UserProfile> {
                override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Usuario registrado correctamente.",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Error al registrar usuario.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, "Error de red.", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        }
    }
}