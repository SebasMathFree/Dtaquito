package Beans.reservations

data class ReservationRequest(
val gameDay: String,
val startTime: String,
val endTime: String,
val sportSpacesId: Int,
val type: String,
val reservationName: String,)
