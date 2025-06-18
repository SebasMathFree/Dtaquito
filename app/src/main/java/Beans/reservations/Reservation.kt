package Beans.reservations

import Beans.blockChain.BlockChain
import Beans.sportspaces.SportSpaceView

data class Reservation(
    val id: Int? = null,
    val gameDay: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val sportSpacesId: Int? = null,
    val type: String? = null,
    val reservationName: String? = null,
    val sportSpace: SportSpaceView? = null,
    val userName: String? = null,
    val sportSpaces: SportSpaceView? = null,
    val name: String? = null,
    val status: String? = null,
    val blockChain: BlockChain? = null,
)