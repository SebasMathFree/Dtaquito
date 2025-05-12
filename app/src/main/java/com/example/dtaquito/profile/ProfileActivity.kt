package com.example.dtaquito.profile

import Beans.update.UpdateEmailRequest
import Beans.update.UpdateNameRequest
import Beans.update.UpdatePasswordRequest
import Beans.userProfile.UserProfile
import Interface.PlaceHolder
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import com.example.dtaquito.R
import com.example.dtaquito.auth.CookieInterceptor
import com.example.dtaquito.auth.SaveCookieInterceptor
import com.example.dtaquito.login.LoginActivity
import com.example.dtaquito.player.PlayerBase
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class ProfileActivity : PlayerBase() {

    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var creditInput: EditText
    private lateinit var updateBtn: Button
    private lateinit var addCreditBtn: Button
    private lateinit var logoutBtn: Button
    private lateinit var service: PlaceHolder

    private var initialCreditAmount: Double = 0.0
    private var initialName: String = ""
    private var initialEmail: String = ""
    private var initialPassword: String = ""

    companion object {
        private const val BASE_URL = "https://dtaquito-backend.azurewebsites.net/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        setupBottomNavigation(R.id.navigation_profile)

        initializeViews()
        setupListeners()
        service = createRetrofitService(this)
        fillUserProfile()
    }

    private fun initializeViews() {
        nameInput = findViewById(R.id.name_input)
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        creditInput = findViewById(R.id.credit_input)
        updateBtn = findViewById(R.id.update_btn)
        addCreditBtn = findViewById(R.id.add_credit)
        logoutBtn = findViewById(R.id.logout_btn)
    }

    private fun setupListeners() {
        updateBtn.setOnClickListener { handleProfileUpdate() }
        addCreditBtn.setOnClickListener { addCredit() }
        logoutBtn.setOnClickListener { logout() }
    }

    private fun createRetrofitService(context: Context): PlaceHolder {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(SaveCookieInterceptor(context))
            .addInterceptor(CookieInterceptor(context))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(PlaceHolder::class.java)
    }

    private fun fillUserProfile() {
        service.getUserId().enqueue(object : Callback<UserProfile> {
            override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        populateUserFields(user)
                    } ?: showToast("User not found")
                } else {
                    showToast("Failed to fetch user data")
                }
            }

            override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                Log.e("ProfileActivity", "Error: ${t.message}", t)
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun populateUserFields(user: UserProfile) {
        initialName = user.name
        initialEmail = user.email
        initialPassword = ""
        nameInput.setText(user.name)
        emailInput.setText(user.email)
        passwordInput.setText("")

        if (user.roleType == "PLAYER") {
            initialCreditAmount = user.credits
            creditInput.setText(String.format(Locale.getDefault(), "%.2f", user.credits))
            creditInput.visibility = View.VISIBLE
            addCreditBtn.visibility = View.VISIBLE
        } else {
            creditInput.visibility = View.GONE
            addCreditBtn.visibility = View.GONE
        }
    }

    private fun handleProfileUpdate() {
        val currentName = nameInput.text.toString()
        val currentEmail = emailInput.text.toString()
        val currentPassword = passwordInput.text.toString()

        if (currentName != initialName) updateName(currentName)
        if (currentEmail != initialEmail) updateEmail(currentEmail)
        if (currentPassword.isNotEmpty() && currentPassword != initialPassword) updatePassword(currentPassword)

        if (currentName == initialName && currentEmail == initialEmail && currentPassword.isEmpty()) {
            showToast("No changes detected")
        }
    }

    private fun updateName(newName: String) {
        val nameRequest = UpdateNameRequest(newName)
        service.updateName(nameRequest).enqueue(createUpdateCallback("Name updated successfully"))
    }

    private fun updateEmail(newEmail: String) {
        val emailRequest = UpdateEmailRequest(newEmail)
        service.updateEmail(emailRequest).enqueue(createUpdateCallback("Email updated successfully"))
    }

    private fun updatePassword(newPassword: String) {
        val passwordRequest = UpdatePasswordRequest(newPassword)
        service.updatePassword(passwordRequest).enqueue(createUpdateCallback("Password updated successfully"))
    }

    private fun addCredit() {
        val creditAmount = creditInput.text.toString().toDoubleOrNull()
        if (creditAmount == null || creditAmount <= 0.0) {
            showToast("Please enter a valid credit amount")
            return
        }

//        service.createDeposit(creditAmount.toInt()).enqueue(object : Callback<ResponseBody> {
//            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                if (response.isSuccessful) {
//                    response.body()?.string()?.let { responseBody ->
//                        val approvalUrl = extractApprovalUrl(responseBody)
//                        approvalUrl?.let { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
//                            ?: showToast("Approval URL not found.")
//                    } ?: showToast("Response body is null.")
//                } else {
//                    showToast("Failed to add credit")
//                }
//            }
//
//            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                Log.e("ProfileActivity", "Error: ${t.message}")
//                showToast("Error: ${t.message}")
//            }
//        })
    }

    private fun extractApprovalUrl(responseBody: String): String? {
        return try {
            JSONObject(responseBody).getString("approval_url")
        } catch (e: Exception) {
            Log.e("ProfileActivity", "Error parsing approval URL: ${e.message}")
            null
        }
    }

    private fun logout() {
        clearCookies()
        service.logOutUser().enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) redirectToLoginActivity()
                else showToast("Error al cerrar sesión")
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("ProfileActivity", "Error: ${t.message}")
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun clearCookies() {
        CookieManager.getInstance().removeAllCookies { isSuccess ->
            Log.d("ProfileActivity", "Cookies eliminadas: $isSuccess")
        }
        CookieManager.getInstance().flush()
    }

    private fun redirectToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun createUpdateCallback(successMessage: String): Callback<ResponseBody> {
        return object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) showToast(successMessage)
                else showToast("Failed to update")
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("ProfileActivity", "Error: ${t.message}")
                showToast("Error: ${t.message}")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}