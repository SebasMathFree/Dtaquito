package Beans.rooms

import Beans.userProfile.UserProfile
import Beans.playerList.Player
import Beans.reservations.Reservation
import Beans.sportspaces.SportSpaceView

data class GameRoom(
    val id:Int,
    val creatorId:Int,
    val creator: UserProfile?,
    val sportSpace: SportSpaceView?,
    val reservation: Reservation?,
    val openingDate: String,
    val day: String,
    val players: MutableList<Player> = mutableListOf(),
    val roomName: String
)