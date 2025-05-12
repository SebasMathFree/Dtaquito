package com.example.dtaquito.forgotPassword

import Beans.auth.forgotPassword.ForgotPasswordRequest
import Interface.PlaceHolder
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.dtaquito.R
import com.example.dtaquito.databinding.ActivityForgotPasswordBinding
import com.example.dtaquito.login.LoginActivity
import environment.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val service: PlaceHolder by lazy {
        RetrofitClient.instance.create(PlaceHolder::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.recoverBtn.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            if (email.isNotEmpty()) {
                sendForgotPasswordRequest(email)
            } else {
                binding.emailInput.error = getString(R.string.enter_valid_email)
            }
        }

        val signInSpannable = SpannableString(binding.signIn.text).apply {
            setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    navigateToLogin()
                }
            }, 36, 43, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(ContextCompat.getColor(this@ForgotPasswordActivity, R.color.green)), 36, 43, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(UnderlineSpan(), 36, 43, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.signIn.text = signInSpannable
        binding.signIn.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun sendForgotPasswordRequest(email: String) {
        binding.recoverBtn.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        val request = ForgotPasswordRequest(email)
        service.forgotPassword(request).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                binding.progressBar.visibility = View.GONE
                binding.recoverBtn.isEnabled = true

                if (response.isSuccessful) {
                    Toast.makeText(this@ForgotPasswordActivity, getString(R.string.email_sent_success), Toast.LENGTH_SHORT).show()
                    navigateToLogin()
                } else {
                    Toast.makeText(this@ForgotPasswordActivity, getString(R.string.email_send_error), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                binding.recoverBtn.isEnabled = true
                Toast.makeText(this@ForgotPasswordActivity, getString(R.string.network_error, t.message), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}