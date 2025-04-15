package Beans.sportspaces

data class SportSpace2(
    val name: String,
    val sportId: Int,
    val imageUrl: String?,
    val price: Double,
    val district: String,
    val description: String?,
    val userId: Int,
    val startTime: String?,
    val endTime: String?,
    val rating: Int,
    val gamemode: String?,
    val amount: Int
)