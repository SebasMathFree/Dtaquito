package com.example.dtaquito.sports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.dtaquito.R
import com.example.dtaquito.gameroom.GameRoomFragment

class SportFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_sport, container, false)
    }


    private fun openGameRoom(sportType: String) {
        val fragment = GameRoomFragment().apply {
            arguments = Bundle().apply {
                putString("SPORT_TYPE", sportType)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.soccer_img).setOnClickListener {
            openGameRoom("FUTBOL")
        }
        view.findViewById<View>(R.id.pool_img).setOnClickListener {
            openGameRoom("BILLAR")
        }
    }
}