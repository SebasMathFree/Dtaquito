package Beans.sportspaces

import Beans.userProfile.UserProfile


data class SportSpace(
    val id: Int,
    val name: String,
    val sportId: Int,
    val sportType: String,
    val image: String,
    val price: Double,
    val districtId: Int,
    val districtType: String,
    val address: String,
    val description: String,
    val user: UserProfile?,
    val openTime: String,
    val closeTime: String,
    val gamemodeId: Int,
    val gamemodeType: String,
    val amount: Int
)