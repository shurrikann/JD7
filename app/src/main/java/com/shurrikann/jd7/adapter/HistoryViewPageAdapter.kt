package com.shurrikann.jd7.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.shurrikann.jd7.fragment.HistoryDataFragment
import com.shurrikann.jd7.fragment.HistoryFragment
import com.shurrikann.jd7.fragment.SelectLanwayFragment

class HistoryViewPageAdapter(fragment: HistoryFragment): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> SelectLanwayFragment()
            1 -> HistoryDataFragment()
            else -> throw IllegalStateException("Invalid position")
        }
    }
}