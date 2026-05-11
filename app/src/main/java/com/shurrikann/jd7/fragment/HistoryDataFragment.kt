package com.shurrikann.jd7.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.applandeo.materialcalendarview.CalendarDay
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener
import com.blankj.utilcode.util.ToastUtils
import com.shurrikann.jd7.activity.HistoryDataTimeActivity
import com.shurrikann.jd7.base.BaseFragment
import com.shurrikann.jd7.databinding.HistoryDataFragmentBinding
import com.shurrikann.jd7.viewmodel.SharedViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@SuppressLint("NewApi")
class HistoryDataFragment : BaseFragment() {
    private lateinit var sharedViewModel: SharedViewModel
    override lateinit var binding: HistoryDataFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HistoryDataFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //获取 ViewModel
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        initView(view)
    }

    @SuppressLint("NewApi")
    private fun initView(view: View) {
        binding.selectDataText.setOnClickListener {
            if (binding.calendarLayout.visibility == View.GONE) {
                showViewWithObjectAnimator(binding.calendarLayout)
                // 禁用滑动
                sharedViewModel.setSwipeDisabled(true)
            } else {
                hideViewWithObjectAnimator(binding.calendarLayout)
                // 恢复滑动
                sharedViewModel.setSwipeDisabled(false)
            }
        }
        binding.cancelBtn.setOnClickListener {
            hideViewWithObjectAnimator(binding.calendarLayout)
            // 恢复滑动
            sharedViewModel.setSwipeDisabled(false)
        }
        initCalender()
        binding.todayBtn.setOnClickListener {
            val calendar = Calendar.getInstance()
            binding.calendarView.setDate(calendar);
        }
        binding.searchBtn.setOnClickListener {
            if (binding.selectDataText.text.isNullOrEmpty()) {
                ToastUtils.showShort("请选择查询的日期")
                return@setOnClickListener
            }
            val date = binding.selectDataText.text.toString().trim()
            val intent = Intent(context, HistoryDataTimeActivity::class.java)
            intent.putExtra("date", date)  // "key_name" 是键，"Hello, World!" 是值
            startActivity(intent)
        }
    }

    fun dateToString(date: Date, format: String = "yyyy-MM-dd"): String {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun initCalender() {
        binding.calendarView.setOnCalendarDayClickListener(object : OnCalendarDayClickListener {
            override fun onClick(calendarDay: CalendarDay) {
                binding.selectDataText.text = dateToString(calendarDay.calendar.time)
                hideViewWithObjectAnimator(binding.calendarLayout)
                // 恢复滑动
                sharedViewModel.setSwipeDisabled(false)
            }
        })
    }

    // 显示视图（VISIBLE）
    fun showViewWithObjectAnimator(view: View) {
        val fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f) // 透明度动画
        val slideIn =
            ObjectAnimator.ofFloat(view, "translationY", view.height.toFloat(), 0f) // 平移动画
        // 并行执行两个动画
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(fadeIn, slideIn)
        animatorSet.duration = 300 // 设置动画持续时间
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                view.visibility = View.VISIBLE // 动画开始时，确保视图可见
            }
        })
        animatorSet.start()
    }

    // 隐藏视图（GONE）
    fun hideViewWithObjectAnimator(view: View) {
        val fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f) // 透明度动画
        val slideOut =
            ObjectAnimator.ofFloat(view, "translationY", 0f, view.height.toFloat()) // 平移动画

        // 并行执行两个动画
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(fadeOut, slideOut)
        animatorSet.duration = 300 // 设置动画持续时间
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view.visibility = View.GONE // 动画结束后，设置视图为 GONE
            }
        })
        animatorSet.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}