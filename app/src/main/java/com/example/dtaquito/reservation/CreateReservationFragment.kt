package com.example.dtaquito.reservation

import Beans.availability.AvailabilityResponse
import Beans.reservations.ReservationRequest
import Interface.PlaceHolder
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.dtaquito.R
import network.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateReservationFragment : Fragment() {

    private lateinit var service: PlaceHolder
    private lateinit var roomNameInput: EditText
    private lateinit var dateInput: EditText
    private lateinit var timeInput: EditText
    private lateinit var endTimeInput: EditText

    private var availableDates: List<String> = emptyList()
    private var dateAvailabilityMap: Map<String, List<String>> = emptyMap()
    private var selectedDate: String = ""
    private var availableHours: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inicializar el servicio Retrofit
        service = RetrofitClient.instance.create(PlaceHolder::class.java)

        // Inflar el layout para este fragmento
        return inflater.inflate(R.layout.activity_create_room, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar vistas
        roomNameInput = view.findViewById(R.id.room_name_input)
        dateInput = view.findViewById(R.id.date_input)
        timeInput = view.findViewById(R.id.time_input)
        endTimeInput = view.findViewById(R.id.endTime_input)

        // Configurar spinner
        val typeReservationSpinner = view.findViewById<Spinner>(R.id.typeReservation_spinner)
        val typeOptions = listOf("Personal", "Comunidad")
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_items2, typeOptions)
        adapter.setDropDownViewResource(R.layout.spinner_items2)
        typeReservationSpinner.adapter = adapter

        // Obtener el ID del espacio deportivo
        val sportSpaceId = arguments?.getInt("sportSpaceId", -1) ?: -1
        Log.d("sportSpaceId", "Received sportSpaceId: $sportSpaceId")

        // Deshabilitar inputs hasta cargar disponibilidad
        dateInput.isEnabled = false
        timeInput.isEnabled = false
        endTimeInput.isEnabled = false

        // Cargar disponibilidad del espacio deportivo
        if (sportSpaceId != -1) {
            loadAvailability(sportSpaceId)
        } else {
            Toast.makeText(requireContext(), "ID de espacio deportivo no válido", Toast.LENGTH_SHORT).show()
        }

        // Configurar listeners
        dateInput.setOnClickListener { showDatePickerDialog() }
        timeInput.setOnClickListener { showStartTimeDialog() }
        endTimeInput.setOnClickListener { showEndTimeDialog() }

        // Configurar botón crear
        val createBtn = view.findViewById<Button>(R.id.create_btn)
        createBtn.setOnClickListener {
            createReservation(sportSpaceId, typeReservationSpinner.selectedItem.toString())
        }
    }

    private fun createReservation(sportSpaceId: Int, reservationType: String) {
        // Validar que todos los campos estén completos
        val roomName = roomNameInput.text.toString()
        val gameDay = dateInput.text.toString()
        val startTime = timeInput.text.toString()
        val endTime = endTimeInput.text.toString()

        if (roomName.isEmpty() || gameDay.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Convertir el tipo de reserva al formato esperado por el API
        val type = when(reservationType.lowercase()) {
            "comunidad" -> "COMMUNITY"
            "personal" -> "PERSONAL"
            else -> reservationType.uppercase()
        }

        // Crear objeto de solicitud
        val reservationRequest = ReservationRequest(
            gameDay = gameDay,
            startTime = startTime,
            endTime = endTime,
            sportSpacesId = sportSpaceId,
            type = type,
            reservationName = roomName
        )

        // Enviar solicitud al servidor
        service.createReservation(reservationRequest).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Reserva creada con éxito", Toast.LENGTH_SHORT).show()
                    // Regresar al fragmento anterior
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Error al crear la reserva: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e("CreateReservation", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(requireContext(), "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("CreateReservation", "Error: ${t.message}")
            }
        })
    }

    private fun loadAvailability(sportSpaceId: Int) {
        service.getSportSpaceAvailability(sportSpaceId).enqueue(object :
            Callback<AvailabilityResponse> {
            override fun onResponse(call: Call<AvailabilityResponse>, response: Response<AvailabilityResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val availability = response.body()!!
                    dateAvailabilityMap = availability.weeklyAvailability
                    availableDates = dateAvailabilityMap.keys.toList()

                    // Habilitar selección de fecha
                    dateInput.isEnabled = true
                } else {
                    Toast.makeText(requireContext(), "Error al cargar disponibilidad", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AvailabilityResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDatePickerDialog() {
        if (availableDates.isEmpty()) {
            Toast.makeText(requireContext(), "No hay fechas disponibles", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar fecha disponible")
            .setItems(availableDates.toTypedArray()) { _, which ->
                selectedDate = availableDates[which]
                dateInput.setText(selectedDate)

                // Actualizar horas disponibles para esta fecha
                availableHours = dateAvailabilityMap[selectedDate] ?: emptyList()

                // Habilitar selección de hora
                timeInput.isEnabled = true
                timeInput.setText("")
                endTimeInput.isEnabled = false
                endTimeInput.setText("")
            }
            .show()
    }

    private fun showStartTimeDialog() {
        if (availableHours.isEmpty()) {
            Toast.makeText(requireContext(), "No hay horarios disponibles para esta fecha", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar hora de inicio")
            .setItems(availableHours.toTypedArray()) { _, which ->
                val selectedTime = availableHours[which]
                timeInput.setText(selectedTime)
                endTimeInput.isEnabled = true
                endTimeInput.setText("")
            }
            .show()
    }

    private fun showEndTimeDialog() {
        val startTime = timeInput.text.toString()
        if (startTime.isEmpty()) {
            Toast.makeText(requireContext(), "Primero selecciona la hora de inicio", Toast.LENGTH_SHORT).show()
            return
        }

        // Filtrar solo horas posteriores a la hora de inicio
        val availableEndHours = availableHours.filter { it > startTime }

        if (availableEndHours.isEmpty()) {
            Toast.makeText(requireContext(), "No hay horas disponibles después de $startTime", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar hora de fin")
            .setItems(availableEndHours.toTypedArray()) { _, which ->
                val selectedEndTime = availableEndHours[which]
                endTimeInput.setText(selectedEndTime)
            }
            .show()
    }
}