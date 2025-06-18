package Beans.sportspaces

import Beans.userProfile.UserProfile

data class SportSpaceView(
    val id: Int,
    val name: String,
    val sportId: Int,
    val sportType: String,
    val imageUrl: String,
    val image: String,
    val price: Double,
    val address: String,
    val description: String,
    val user: UserProfile?,
    val openTime: String,
    val closeTime: String,
    val gamemodeId: Int,
    val gamemode: String,
    val amount: Int,
    val latitude: Double,
    val longitude: Double
)
