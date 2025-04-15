package Beans.rooms

import Beans.userProfile.UserProfile
import Beans.playerList.Player
import Beans.sportspaces.SportSpace

data class GameRoom(
    val id:Int,
    val creatorId:Int,
    val creator: UserProfile?,
    val sportSpace: SportSpace?,
    val openingDate: String,
    val day: String,
    val players: MutableList<Player> = mutableListOf(),
    val roomName: String
)