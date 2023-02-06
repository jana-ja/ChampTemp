package de.janaja.champtemp.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import de.janaja.champtemp.ui.main_content.HomeFragment
import de.janaja.champtemp.ui.main_content.DayFragment
import de.janaja.champtemp.ui.main_content.WeekFragment

class MainViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> HomeFragment()
            1 -> DayFragment()
            else -> WeekFragment()
        }
    }
}