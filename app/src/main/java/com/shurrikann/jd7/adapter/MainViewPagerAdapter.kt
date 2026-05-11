package com.shurrikann.jd7.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.shurrikann.jd7.fragment.DetectionFragment
import com.shurrikann.jd7.fragment.HistoryFragment

class MainViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> DetectionFragment()
            1 -> HistoryFragment()
            else -> throw IllegalStateException("Invalid position")
        }
    }

}