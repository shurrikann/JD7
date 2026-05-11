package com.shurrikann.jd7.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    // 使用 MutableLiveData 来表示是否禁用滑动
    private val _isSwipeDisabled = MutableLiveData<Boolean>()
    val isSwipeDisabled: LiveData<Boolean> get() = _isSwipeDisabled

    // 更新滑动禁用的状态
    fun setSwipeDisabled(isDisabled: Boolean) {
        _isSwipeDisabled.value = isDisabled
    }
}