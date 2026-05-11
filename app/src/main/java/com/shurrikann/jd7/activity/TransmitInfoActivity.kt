package com.shurrikann.jd7.activity

import android.os.Bundle
import com.shurrikann.jd7.R
import com.shurrikann.jd7.base.BaseActivity
import com.shurrikann.jd7.bean.LocationData
import com.shurrikann.jd7.databinding.TransmitInfoActivityBinding
import com.shurrikann.jd7.utils.DateUtils

class TransmitInfoActivity : BaseActivity() {
    override lateinit var binding: TransmitInfoActivityBinding
    override fun getLayoutResId(): Int {
        return R.layout.transmit_info_activity
    }

    override fun init(savedInstanceState: Bundle?) {
        binding = TransmitInfoActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val data = intent.getParcelableExtra<LocationData>("data")
        initData(data!!)
        initLinister()
    }


    private fun initData(data: LocationData) {
        binding.nameText.text = data.name
        binding.jcTime.text = DateUtils.convertMillisToDate((data.cjTime.toLongOrNull()!!) * 1000)
        if(!data.upTime.isNullOrEmpty()){
            binding.upTime.text = DateUtils.convertMillisToDate((data.upTime.toLongOrNull()!!) * 1000)
        }else{
            binding.upTime.text = "暂无"
        }
        binding.fsText.text = data.speedValue
        binding.qyText.text = data.presValue
        binding.wdText.text = data.heatValue
        binding.ch4Text.text = data.ch4Value
        binding.sdText.text = data.humValue
        binding.o2Text.text = data.o2Value
        binding.coText.text = data.coValue
    }

    private fun initLinister() {
        binding.backImg.setOnClickListener { finish() }
    }
}