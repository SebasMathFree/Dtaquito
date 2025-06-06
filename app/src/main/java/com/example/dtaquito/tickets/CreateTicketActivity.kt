package com.example.dtaquito.tickets

import Beans.tickets.CreateTicketRequest
import Interface.PlaceHolder
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.graphics.toColorInt
import com.example.dtaquito.R
import com.example.dtaquito.player.PlayerBase
import com.example.dtaquito.utils.showToast
import kotlinx.coroutines.*
import network.RetrofitClient

class CreateTicketActivity : PlayerBase() {

    private val service = RetrofitClient.instance.create(PlaceHolder::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_ticket)
        setupBottomNavigation(R.id.navigation_tickets)

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val bankNameSpinner = findViewById<Spinner>(R.id.bankNameTextView_spinner)
        val bankNameEditText = findViewById<EditText>(R.id.bankNameTextView_input)
        val accountNumberInput = findViewById<EditText>(R.id.account_number_input)
        val nameInput = findViewById<EditText>(R.id.name_input)
        val submitButton = findViewById<Button>(R.id.submit_button)
        val amountInput = findViewById<TextView>(R.id.amount_input)

        setupAmountInput(amountInput)
        setupRadioGroup(radioGroup, bankNameSpinner, bankNameEditText, accountNumberInput)
        setupBankNameSpinner(bankNameSpinner)
        setupSubmitButton(submitButton, nameInput, accountNumberInput, radioGroup, bankNameSpinner, bankNameEditText)
    }

    private fun setupAmountInput(amountInput: TextView) {
        val userCredits = intent.getDoubleExtra("USER_CREDITS", 0.0)
        amountInput.text = userCredits.toString()
    }

    private fun setupRadioGroup(
        radioGroup: RadioGroup,
        bankNameSpinner: Spinner,
        bankNameEditText: EditText,
        accountNumberInput: EditText
    ) {
        radioGroup.setOnCheckedChangeListener { _, _ ->
            accountNumberInput.text.clear()
            bankNameEditText.text.clear()
            when (radioGroup.checkedRadioButtonId) {
                R.id.option1 -> {
                    bankNameSpinner.visibility = View.VISIBLE
                    bankNameEditText.visibility = View.INVISIBLE
                    bankNameSpinner.setSelection(0) // Reset spinner selection
                }
                R.id.option2 -> {
                    bankNameSpinner.visibility = View.INVISIBLE
                    bankNameEditText.visibility = View.VISIBLE
                }
            }
            updateAccountNumberLimit()
        }
    }

    private fun setupBankNameSpinner(bankNameSpinner: Spinner) {
        val items = listOf("Choose a bank:", "Interbank", "BCP", "BBVA")
        val adapter = object : ArrayAdapter<String>(this, R.layout.spinner_items, items) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(if (position == 0) "#4D4D4D".toColorInt() else android.graphics.Color.WHITE)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(if (position == 0) "#4D4D4D".toColorInt() else android.graphics.Color.WHITE)
                return view
            }
        }
        bankNameSpinner.adapter = adapter

        bankNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateAccountNumberLimit()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSubmitButton(
        submitButton: Button,
        nameInput: EditText,
        accountNumberInput: EditText,
        radioGroup: RadioGroup,
        bankNameSpinner: Spinner,
        bankNameEditText: EditText
    ) {
        submitButton.setOnClickListener {
            val fullName = nameInput.text.toString()
            val accountNumber = accountNumberInput.text.toString()
            val transferType = when (radioGroup.checkedRadioButtonId) {
                R.id.option1 -> "CC"
                R.id.option2 -> "CCI"
                else -> null
            }
            val bankName = if (transferType == "CC") {
                bankNameSpinner.selectedItem.toString()
            } else {
                bankNameEditText.text.toString()
            }

            if (fullName.isEmpty() || accountNumber.isEmpty() || transferType == null || bankName.isEmpty()) {
                showToast("Por favor, completa todos los campos.")
                return@setOnClickListener
            }

            val ticket = CreateTicketRequest(fullName, transferType, bankName, accountNumber)
            Log.d("CreateTicketActivity", "Datos recopilados: $ticket")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = service.createBankTransfer(ticket)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            showToast("Transferencia creada exitosamente.")
                            Log.d("CreateTicketActivity", "Respuesta exitosa: ${response.body()}")
                            val intent = Intent(this@CreateTicketActivity, TicketActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            showToast("Error al crear la transferencia: ${response.code()}")
                            Log.e("CreateTicketActivity", "Error: ${response.errorBody()?.string()}")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        showToast("Error de red: ${e.message}")
                        Log.e("CreateTicketActivity", "Fallo en la llamada: ${e.message}")
                    }
                }
            }
        }
    }

    private fun updateAccountNumberLimit() {
        val accountNumberInput = findViewById<EditText>(R.id.account_number_input)
        val bankNameSpinner = findViewById<Spinner>(R.id.bankNameTextView_spinner)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)

        val selectedBank = bankNameSpinner.selectedItem.toString()
        val transferType = when (radioGroup.checkedRadioButtonId) {
            R.id.option1 -> "CC"
            R.id.option2 -> "CCI"
            else -> null
        }

        val maxLength = when {
            transferType == "CC" && selectedBank == "Interbank" -> 13
            transferType == "CC" && selectedBank == "BCP" -> 14
            transferType == "CC" && selectedBank == "BBVA" -> 18
            transferType == "CCI" -> 18
            else -> 0
        }

        Log.d("CreateTicketActivity", "Banco: $selectedBank, Tipo: $transferType, maxLength: $maxLength")
        setAccountNumberLimit(accountNumberInput, maxLength)
    }

    private fun setAccountNumberLimit(editText: EditText, maxLength: Int) {
        Log.d("CreateTicketActivity", "setAccountNumberLimit llamado con maxLength: $maxLength")
        editText.filters = arrayOf(android.text.InputFilter.LengthFilter(maxLength))
    }
}