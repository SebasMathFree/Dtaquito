package com.example.dtaquito.gameroom

import Beans.reservations.ReservationRequest
import Interface.PlaceHolder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.dtaquito.R
import com.example.dtaquito.time.DatePickerFragment
import com.example.dtaquito.time.TimePickerFragment
import network.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateRoomFragment : Fragment() {

    lateinit var service: PlaceHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.initialize(requireContext().applicationContext)
        service = RetrofitClient.instance.create(PlaceHolder::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_create_room, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val typeReservationSpinner = view.findViewById<Spinner>(R.id.typeReservation_spinner)
        val typeOptions = listOf("Personal", "Comunidad")
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_items2, typeOptions)
        adapter.setDropDownViewResource(R.layout.spinner_items2)
        typeReservationSpinner.adapter = adapter

        val roomNameInput = view.findViewById<EditText>(R.id.room_name_input)
        val idSportSpace = view.findViewById<EditText>(R.id.sport_space_id)
        val dateInput = view.findViewById<EditText>(R.id.date_input)
        val timeInput = view.findViewById<EditText>(R.id.time_input)
        val endTimeInput = view.findViewById<EditText>(R.id.endTime_input)
        val createBtn = view.findViewById<Button>(R.id.create_btn)

        dateInput.setOnClickListener { showDatePickerDialog(dateInput) }
        timeInput.setOnClickListener { showTimePickerDialog(timeInput) }
        endTimeInput.setOnClickListener { showTimePickerDialog(endTimeInput) }

        createBtn.setOnClickListener {
            val roomName = roomNameInput.text.toString().trim()
            val sportSpaceId = idSportSpace.text.toString().toIntOrNull()
            val gameDay = dateInput.text.toString().trim()
            val startTime = timeInput.text.toString().trim()
            val endTime = endTimeInput.text.toString().trim()
            val type = when (typeReservationSpinner.selectedItem.toString().uppercase()) {
                "PERSONAL" -> "PERSONAL"
                "COMUNIDAD", "COMMUNITY" -> "COMMUNITY"
                else -> ""
            }

            Log.d("CreateReservation", "roomName: $roomName")
            Log.d("CreateReservation", "sportSpaceId: $sportSpaceId")
            Log.d("CreateReservation", "gameDay: $gameDay")
            Log.d("CreateReservation", "startTime: $startTime")
            Log.d("CreateReservation", "endTime: $endTime")
            Log.d("CreateReservation", "type: $type")

            if (roomName.isEmpty() || sportSpaceId == null || gameDay.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || type.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val reservation = ReservationRequest(
                gameDay = gameDay,
                startTime = startTime,
                endTime = endTime,
                sportSpacesId = sportSpaceId,
                type = type,
                reservationName = roomName
            )

            service.createReservation(reservation).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Reserva creada correctamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Error al crear la reserva", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun showTimePickerDialog(targetEditText: EditText) {
        val timePicker = TimePickerFragment { selectedTime ->
            targetEditText.setText(selectedTime)
        }
        timePicker.show(parentFragmentManager, "timePicker")
    }

    private fun showDatePickerDialog(targetEditText: EditText) {
        val datePicker = DatePickerFragment { day, month, year ->
            val formattedDate = String.format("%d-%02d-%02d", year, month, day)
            targetEditText.setText(formattedDate)
        }
        datePicker.show(parentFragmentManager, "datePicker")
    }
}