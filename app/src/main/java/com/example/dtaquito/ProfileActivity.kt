package com.example.dtaquito

import Beans.userProfile.UserProfile
import Interface.PlaceHolder
import MyCookieJar
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dtaquito.auth.CookieInterceptor
import com.example.dtaquito.auth.SaveCookieInterceptor
import com.example.dtaquito.login.LoginActivity
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : PlayerBase() {

    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var creditInput: EditText
    private lateinit var updateBtn: Button
    private lateinit var addCreditBtn: Button
    private lateinit var logoutBtn: Button
    private lateinit var service: PlaceHolder

    private fun initializeViews() {
        nameInput = findViewById(R.id.name_input)
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        creditInput = findViewById(R.id.credit_input)
        updateBtn = findViewById(R.id.update_btn)
        addCreditBtn = findViewById(R.id.add_credit)
        logoutBtn = findViewById(R.id.logout_btn)

        logoutBtn.setOnClickListener {
            logout()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        setupBottomNavigation(R.id.navigation_profile)

        initializeViews()

        service = createRetrofit(this).create(PlaceHolder::class.java)

        fillUserProfile()
    }

    private fun createRetrofit(context: Context): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val saveCookieInterceptor = SaveCookieInterceptor(context)
        val cookieInterceptor = CookieInterceptor(context)

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(saveCookieInterceptor)
            .addInterceptor(cookieInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    private fun fillUserProfile() {
        Log.d("ProfileActivity", "Solicitud para obtener el perfil del usuario iniciada.")

        service.getUserId().enqueue(object : Callback<UserProfile> {
            override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                Log.d("ProfileActivity", "Respuesta recibida: ${response.code()}")

                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        Log.d("ProfileActivity", "Perfil del usuario recibido: ${user.name}, ${user.email}, ${user.roleType}, ${user.credits}")

                        nameInput.setText(user.name)
                        emailInput.setText(user.email)
                        passwordInput.setText("")

                        if (user.roleType == "PLAYER") {
                            creditInput.setText(user.credits.toString())
                            creditInput.visibility = View.VISIBLE
                            addCreditBtn.visibility = View.VISIBLE
                        } else {
                            creditInput.visibility = View.GONE
                            addCreditBtn.visibility = View.GONE
                        }
                    } ?: run {
                        Log.e("ProfileActivity", "Error: User not found")
                        Toast.makeText(this@ProfileActivity, "User not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("ProfileActivity", "Error: Failed to fetch user data, Response code: ${response.code()}")
                    Toast.makeText(this@ProfileActivity, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                t.printStackTrace()
                Log.e("ProfileActivity", "Error: ${t.message}", t)
                Toast.makeText(this@ProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

//    private fun updateUserProfile() {
//        val updatedName = nameInput.text.toString()
//        val updatedEmail = emailInput.text.toString()
//        val updatedPassword = passwordInput.text.toString()
//
//        if (updatedName.isEmpty() || updatedEmail.isEmpty() || updatedPassword.isEmpty()) {
//            showToast("Please fill in all fields")
//            return
//        }
//
//        val userId = tokenManager.getUserId()
//        service.getUserId(userId).enqueue(object : Callback<UserProfile> {
//            override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
//                if (response.isSuccessful) {
//                    response.body()?.let { existingUser ->
//                        val updatedUser = existingUser.copy(
//                            name = updatedName,
//                            email = updatedEmail,
//                            password = updatedPassword,
//                            roleType =
//                        )
//                        service.updateUser(userId, updatedUser).enqueue(object : Callback<UserProfile> {
//                            override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
//                                if (response.isSuccessful) {
//                                    showToast("Profile updated successfully")
//                                } else {
//                                    showToast("Failed to update profile")
//                                }
//                            }
//
//                            override fun onFailure(call: Call<UserProfile>, t: Throwable) {
//                                t.printStackTrace()
//                                showToast("Error: ${t.message}")
//                            }
//                        })
//                    } ?: showToast("User not found")
//                } else {
//                    showToast("Failed to fetch user data")
//                }
//            }
//
//            override fun onFailure(call: Call<UserProfile>, t: Throwable) {
//                t.printStackTrace()
//                showToast("Error: ${t.message}")
//            }
//        })
//    }

//    private fun showAddCreditDialog() {
//        val input = EditText(this).apply { inputType = InputType.TYPE_CLASS_NUMBER }
//        AlertDialog.Builder(this)
//            .setTitle("Añadir Crédito")
//            .setMessage("Ingrese la cantidad de crédito que desea añadir:")
//            .setView(input)
//            .setPositiveButton("Añadir") { _, _ ->
//                val creditAmount = input.text.toString().toIntOrNull()
//                if (creditAmount != null) {
//                    addCredit(creditAmount)
//                } else {
//                    showToast("Por favor, ingrese un número válido")
//                }
//            }
//            .setNegativeButton("Cancelar", null)
//            .show()
//    }
//
//    private fun addCredit(amount: Int) {
//        service.createDeposit(amount).enqueue(object : Callback<ResponseBody> {
//            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                if (response.isSuccessful) {
//                    response.body()?.string()?.let { responseBody ->
//                        val approvalUrl = extractApprovalUrl(responseBody)
//                        if (approvalUrl != null) {
//                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(approvalUrl)))
//                        } else {
//                            showToast("Approval URL not found.")
//                        }
//                    } ?: showToast("Response body is null.")
//                } else {
//                    showToast("Error al añadir crédito")
//                }
//            }
//
//            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                t.printStackTrace()
//                showToast("Error: ${t.message}")
//            }
//        })
//    }
//
//    private fun extractApprovalUrl(responseBody: String): String? {
//        Log.d("extractApprovalUrl", "Response Body: $responseBody")
//        return responseBody
//    }
//
    private fun showToast(message: String) {
        Toast.makeText(this@ProfileActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun clearCookies() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies { isSuccess ->
            Log.d("ProfileActivity", "Cookies eliminadas: $isSuccess")
        }
        cookieManager.flush()
    }


    private fun logout() {
        // Primero, borra las cookies de la sesión
        clearCookies()

        // Luego, realiza la solicitud para cerrar sesión en el servidor
        service.logOutUser().enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Redirigir al LoginActivity
                    redirectToLoginActivity()
                } else {
                    Log.e("ProfileActivity", "Error al cerrar sesión: ${response.code()}")
                    Toast.makeText(this@ProfileActivity, "Error al cerrar sesión", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("ProfileActivity", "Error de red al cerrar sesión", t)
                Toast.makeText(this@ProfileActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun redirectToLoginActivity() {
        val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}