package com.example.dtaquito.profile

import Beans.update.UpdateEmailRequest
import Beans.update.UpdateNameRequest
import Beans.update.UpdatePasswordRequest
import Beans.userProfile.UserProfile
import Interface.PlaceHolder
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.dtaquito.R
import com.example.dtaquito.login.LoginActivity
import com.example.dtaquito.utils.showToast
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import network.RetrofitClient
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class ProfileFragment : Fragment() {

    private lateinit var header: TextView
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var creditView: TextView
    private lateinit var updateBtn: Button
    private lateinit var addCreditBtn: Button
    private lateinit var logoutBtn: Button
    private lateinit var selectLanguageBtn: ImageButton
    private lateinit var darkModeBtn: SwitchMaterial
    private val service by lazy { RetrofitClient.instance.create(PlaceHolder::class.java) }

    private var initialCreditAmount: Double = 0.0
    private var initialName: String = ""
    private var initialEmail: String = ""
    private var initialPassword: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        setupListeners()
        fetchUserProfile()
        selectLanguageBtn.setOnClickListener { showLanguageDialog() }
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
                view.clearFocus()
                view.performClick()
            }
            false
        }
        updateTexts()
    }

    private fun showLanguageDialog() {
        val idiomas = arrayOf(
            getString(R.string.spanish),
            getString(R.string.english)
        )
        val codigos = arrayOf("es", "en")
        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_1,
            idiomas
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(ContextCompat.getColor(context, R.color.white))
                return view
            }
        }

        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setTitle(getString(R.string.language))
            .setAdapter(adapter) { _, which ->
                setLocale(requireContext(), codigos[which])
                (activity as? com.example.dtaquito.MainActivity)?.updateAllFragmentsTexts()
            }
            .show()
    }

    private fun setLocale(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit { putString("app_lang", language) }
    }
    fun updateTexts() {
        header.text = getString(R.string.profile)
        updateBtn.text = getString(R.string.edit_profile)
        addCreditBtn.text = getString(R.string.add_credits)
        logoutBtn.text = getString(R.string.log_out)
        nameInput.hint = getString(R.string.name)
        emailInput.hint = getString(R.string.email)
        passwordInput.hint = getString(R.string.password)
        creditView.hint = getString(R.string.credits)
    }

    private fun initializeViews(view: View) {
        header = view.findViewById(R.id.header)
        nameInput = view.findViewById(R.id.name_input)
        emailInput = view.findViewById(R.id.email_input)
        passwordInput = view.findViewById(R.id.password_input)
        creditView = view.findViewById(R.id.credit_input)
        updateBtn = view.findViewById(R.id.update_btn)
        addCreditBtn = view.findViewById(R.id.add_credit)
        logoutBtn = view.findViewById(R.id.logout_btn)
        selectLanguageBtn = view.findViewById(R.id.btn_select_language)
        darkModeBtn = view.findViewById(R.id.dark_mode_switch)

        // Configurar el switch de modo oscuro
        setupDarkModeSwitch()
    }

    private fun setupDarkModeSwitch() {
        // Obtener el estado actual del tema
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)

        // Configurar estado inicial sin activar el listener
        darkModeBtn.setOnCheckedChangeListener(null)
        darkModeBtn.isChecked = isDarkMode

        // Usar post para configurar el listener después de que la vista esté completamente inicializada
        darkModeBtn.post {
            darkModeBtn.setOnCheckedChangeListener { _, isChecked ->
                // Guardar preferencia inmediatamente
                prefs.edit {
                    putBoolean("dark_mode", isChecked)
                }

                // Guardar el estado actual del fragment en MainActivity
                (activity as? com.example.dtaquito.MainActivity)?.saveCurrentFragmentState()

                // Aplicar el tema inmediatamente
                val newMode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                AppCompatDelegate.setDefaultNightMode(newMode)
            }
        }
    }

    private fun fetchUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { service.getUserId() }
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        populateUserFields(user)
                    } ?: context?.showToast("User not found")
                } else {
                    context?.showToast("Failed to fetch user data: ${response.code()}")
                }
            } catch (e: Exception) {
                context?.showToast("Error: ${e.message}")
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
        initialCreditAmount = user.credits
        creditView.text = user.credits.toString()
        addCreditBtn.visibility = if (user.roleType == "PLAYER") View.VISIBLE else View.GONE
    }

    private fun handleProfileUpdate() {
        val currentName = nameInput.text.toString()
        val currentEmail = emailInput.text.toString()
        val currentPassword = passwordInput.text.toString()

        if (currentName != initialName) updateName(currentName)
        if (currentEmail != initialEmail) updateEmail(currentEmail)
        if (currentPassword.isNotEmpty() && currentPassword != initialPassword) updatePassword(currentPassword)

        if (currentName == initialName && currentEmail == initialEmail && currentPassword.isEmpty()) {
            requireContext().showToast("No changes detected")
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
        // Crear un EditText para el input del usuario con estilo mejorado
        val editText = EditText(requireContext())
        editText.hint = getString(R.string.enter_credit_amount)
        editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

        // Aplicar estilo al EditText
        editText.background = ContextCompat.getDrawable(requireContext(), R.drawable.input_text)
        editText.setPadding(40, 24, 40, 24)
        editText.setTextColor(ContextCompat.getColor(requireContext(), R.color.input_text_color))
        editText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.input_text_color))

        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setTitle(getString(R.string.add_credits))
            .setMessage(getString(R.string.enter_amount_to_add))
            .setView(editText)
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                val creditAmount = editText.text.toString().toDoubleOrNull()

                if (creditAmount == null || creditAmount <= 0.0) {
                    requireContext().showToast(getString(R.string.enter_valid_amount))
                } else {
                    processDeposit(creditAmount)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun processDeposit(creditAmount: Double) {
        service.createDeposit(creditAmount.toInt()).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    response.body()?.string()?.let { responseBody ->
                        val approvalUrl = extractApprovalUrl(responseBody)
                        if (approvalUrl != null) {
                            val intent = Intent(Intent.ACTION_VIEW, approvalUrl.toUri())
                            startActivity(intent)
                        } else {
                            requireContext().showToast("Approval URL not found.")
                        }
                    } ?: requireContext().showToast("Response body is null.")
                } else {
                    requireContext().showToast("Failed to add credit")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                requireContext().showToast("Error: ${t.message}")
            }
        })
    }

    private fun logout() {
        // Limpiar TODOS los datos de SharedPreferences
        clearAllUserData()
        clearCookies()
        service.logOutUser().enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    redirectToLoginActivity()
                } else {
                    requireContext().showToast("Error al cerrar sesión")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                requireContext().showToast("Error: ${t.message}")
            }
        })
    }
    
    private fun clearAllUserData() {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            // Limpiar todos los datos de sesión
            remove("user_id")
            remove("user_name")
            remove("user_email")
            remove("role_type")
            remove("credits")
            remove("jwt_token")
            
            // Limpiar datos de "Remember me"
            remove("remember_me")
            remove("saved_email")
            remove("saved_password")
            
            // Limpiar flags de sesión temporal
            remove("is_temporary_session")
            remove("app_was_closed")
            
            apply()
        }
    }

    private fun clearCookies() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }

    private fun redirectToLoginActivity() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun createUpdateCallback(successMessage: String): Callback<ResponseBody> {
        return object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    requireContext().showToast(successMessage)
                } else {
                    requireContext().showToast("Failed to update")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                requireContext().showToast("Error: ${t.message}")
            }
        }
    }

    private fun extractApprovalUrl(responseBody: String): String? {
        return try {
            val jsonObject = JSONObject(responseBody)
            jsonObject.getString("approval_url")
        } catch (_: Exception) {
            null
        }
    }

    private fun setupListeners() {
        updateBtn.setOnClickListener { handleProfileUpdate() }
        addCreditBtn.setOnClickListener { addCredit() }
        logoutBtn.setOnClickListener { logout() }
    }
}