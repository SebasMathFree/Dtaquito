package com.example.dtaquito.player//package com.example.dtaquito
//
//import Beans.playerList.PlayerList
//import Beans.userProfile.UserProfile
//import Interface.PlaceHolder
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class PlayerListAdapter(
//    private val playerList: List<PlayerList>,
//    private val service: PlaceHolder
//) : RecyclerView.Adapter<PlayerListViewHolder>() {
//
//    private val userNames = mutableMapOf<Int, String>()
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerListViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.player_list_item, parent, false)
//        return PlayerListViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: PlayerListViewHolder, position: Int) {
//        val userId = playerList[position].userId
//        if (userNames.containsKey(userId)) {
//            holder.renderPlayer(userNames[userId] ?: "Unknown")
//        } else {
//            service.getUserId(userId).enqueue(object : Callback<UserProfile> {
//                override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
//                    if (response.isSuccessful) {
//                        val userName = response.body()?.name ?: "Unknown"
//                        userNames[userId] = userName
//                        holder.renderPlayer(userName)
//                    }
//                }
//
//                override fun onFailure(call: Call<UserProfile>, t: Throwable) {
//                    holder.renderPlayer("Unknown")
//                }
//            })
//        }
//    }
//
//    override fun getItemCount(): Int = playerList.size
//}