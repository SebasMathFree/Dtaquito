package com.example.dtaquito.profile

import Beans.update.UpdateEmailRequest
import Beans.update.UpdateNameRequest
import Beans.update.UpdatePasswordRequest
import Beans.userProfile.UserProfile
import Interface.PlaceHolder
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.dtaquito.R
import com.example.dtaquito.login.LoginActivity
import com.example.dtaquito.player.PlayerBase
import com.example.dtaquito.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import network.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class ProfileActivity : PlayerBase() {

    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var creditInput: EditText
    private lateinit var updateBtn: Button
    private lateinit var addCreditBtn: Button
    private lateinit var logoutBtn: Button
    private val service by lazy { RetrofitClient.instance.create(PlaceHolder::class.java) }

    private var initialCreditAmount: Double = 0.0
    private var initialName: String = ""
    private var initialEmail: String = ""
    private var initialPassword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        setupBottomNavigation(R.id.navigation_profile)

        initializeViews()
        setupListeners()
        fetchUserProfile()
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

    private fun fetchUserProfile() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { service.getUserId() }
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        populateUserFields(user)
                    } ?: showToast("User not found")
                } else {
                    showToast("Failed to fetch user data: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Error: ${e.message}", e)
                showToast("Error: ${e.message}")
            }
        }
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
}