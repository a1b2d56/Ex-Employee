package com.powergrid.exemployee.presentation.maintabs

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.powergrid.exemployee.presentation.dependants.DependantsFragment
import com.powergrid.exemployee.presentation.home.HomeFragment
import com.powergrid.exemployee.presentation.liveliness.LivelinessFragment
import com.powergrid.exemployee.presentation.noticeboard.NoticeboardFragment

class MainTabsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> NoticeboardFragment()
            2 -> DependantsFragment()
            3 -> LivelinessFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
}
