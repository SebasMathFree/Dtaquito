package Beans.sportspaces

import Beans.userProfile.UserProfile


data class SportSpace(
    val id: Int,
    val name: String,
    val sportId: Int,
    val sportType: String,
    val imageUrl: String?,
    val price: Double,
    val district: String,
    val description: String?,
    val user: UserProfile?,
    val StartTime: String?,
    val endTime: String?,
    val gamemode: String?,
    val amount: Int
)