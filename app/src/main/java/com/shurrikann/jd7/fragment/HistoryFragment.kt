package com.shurrikann.jd7.fragment

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.shurrikann.jd7.R
import com.shurrikann.jd7.adapter.HistoryViewPageAdapter
import com.shurrikann.jd7.databinding.HistoryFragmentBinding
import com.shurrikann.jd7.viewmodel.SharedViewModel

class HistoryFragment : Fragment(R.layout.history_fragment) {
    private lateinit var historyViewPageAdapter: HistoryViewPageAdapter
    private lateinit var sharedViewModel: SharedViewModel
    private var _binding: HistoryFragmentBinding? = null
    private val binding get() = _binding!!
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = HistoryFragmentBinding.bind(view)
        // 获取 ViewModel
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        initAnimator()
        initAdapter()
        // 监听滑动禁用的状态变化
        sharedViewModel.isSwipeDisabled.observe(viewLifecycleOwner, Observer { isDisabled ->
            // 根据状态禁用或启用 ViewPager2 的滑动
            Log.d("HistoryFragment-ViewPager2","${isDisabled}")
            binding.historyViewpage.isUserInputEnabled = !isDisabled
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initAnimator() {
        val animator = ObjectAnimator.ofFloat(binding.img2, "translationY", 0f, 40f).apply {
            duration = 1000 // 动画时长
            interpolator = LinearInterpolator() // 线性插值器
            repeatCount = ObjectAnimator.INFINITE // 无限循环
            repeatMode = ObjectAnimator.REVERSE // 来回移动
        }
        animator.start()
    }

    @SuppressLint("MissingInflatedId", "ResourceAsColor", "UseRequireInsteadOfGet")
    private fun initAdapter() {
        historyViewPageAdapter = HistoryViewPageAdapter(this)
        binding.historyViewpage.adapter = historyViewPageAdapter
        TabLayoutMediator(binding.historyTab, binding.historyViewpage) { tab, position ->
            val customView = LayoutInflater.from(context).inflate(R.layout.history_tab, null)
            val tabImg = customView.findViewById<ImageView>(R.id.tab_img)
            val tabText = customView?.findViewById<TextView>(R.id.tab_text)
            when (position) {
                0 -> {
                    tabText?.text = "巷道查询"
                    tabText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                    tabText?.setTextColor(context?.resources?.getColor(R.color.white,null)!!)
                    tabImg.visibility = View.VISIBLE

                }

                1 -> {
                    tabText?.text = "时间查询"
                    tabText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                    tabText?.setTextColor(context?.resources?.getColor(R.color.spark_gray,null)!!)
                    tabImg.visibility = View.GONE
                }
            }
            tab.customView = customView
        }.attach()
        binding.historyTab.selectTab(binding.historyTab.getTabAt(0))
        // 设置 Tab 之间的间隔
        for (i in 0 until binding.historyTab.tabCount) {
            val tab = binding.historyTab.getTabAt(i)
            val params = tab?.view?.layoutParams as? LinearLayout.LayoutParams
            params?.let {
                it.marginStart = -5 // 设置左间距
                it.marginEnd = -5   // 设置右间距
                tab.view.layoutParams = it
            }
        }
        binding.historyTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            @SuppressLint("ResourceAsColor")
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    val tabImg = it.customView?.findViewById<ImageView>(R.id.tab_img)
                    val tabText = it.customView?.findViewById<TextView>(R.id.tab_text)
                    when (it.position) {
                        0 -> {
                            tabText?.text = "巷道查询"
                            tabText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                            tabText?.setTextColor(context?.resources?.getColor(R.color.white,null)!!)
                            tabImg?.visibility = View.VISIBLE
                        }

                        1 -> {
                            tabText?.text = "时间查询"
                            tabText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                            tabText?.setTextColor(context?.resources?.getColor(R.color.white,null)!!)
                            tabImg?.visibility = View.VISIBLE
                        }

                        else -> {}
                    }
                }
            }

            @SuppressLint("ResourceAsColor")
            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.let {
                    val tabImg = it.customView?.findViewById<ImageView>(R.id.tab_img)
                    val tabText = it.customView?.findViewById<TextView>(R.id.tab_text)
                    when (it.position) {
                        0 -> {
                            tabText?.text = "巷道查询"
                            tabText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                            tabText?.setTextColor(context?.resources?.getColor(R.color.spark_gray,null)!!)
                            tabImg?.visibility = View.GONE
                        }

                        1 -> {
                            tabText?.text = "时间查询"
                            tabText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                            tabText?.setTextColor(context?.resources?.getColor(R.color.spark_gray,null)!!)
                            tabImg?.visibility = View.GONE
                        }

                        else -> {}
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }
}