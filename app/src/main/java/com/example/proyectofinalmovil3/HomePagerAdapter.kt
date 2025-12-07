package com.example.proyectofinalmovil3

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class HomePagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> MapaFragment()
            2 -> EventosFragment()
            3 -> StatsFragment()
            4 -> PerfilFragment()
            else -> HomeFragment()
        }
    }
}
