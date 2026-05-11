package com.shurrikann.jd7.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.blankj.utilcode.util.ActivityUtils
import com.shurrikann.jd7.R
import com.shurrikann.jd7.base.BaseActivity
import com.shurrikann.jd7.bean.DataManager
import com.shurrikann.jd7.databinding.MyActivityBinding
import com.shurrikann.jd7.utils.DataUtils

class MyActivity : BaseActivity() {
    override lateinit var binding: MyActivityBinding
    override fun getLayoutResId(): Int {
        return R.layout.my_activity
    }

    @SuppressLint("SetTextI18n")
    override fun init(savedInstanceState: Bundle?) {
        binding = MyActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListeren()
        if (DataManager.getDataBool("login_state")) {
            val name = DataManager.getDataString("name")
            if (!name.isNullOrEmpty()){
                val substring = name.substring(0, 1)
                binding.firstNameText.text = substring
                binding.myLoginImg.visibility = View.GONE
                binding.firstNameText.visibility = View.VISIBLE
                binding.nameText.text = name
            }
        } else {
            binding.myLoginImg.visibility = View.VISIBLE
            binding.firstNameText.visibility = View.GONE
        }
        binding.verText.text = "v." + DataManager.getDataString("version")
    }

    private fun initListeren() {
        binding.bluetoothLayout.setOnClickListener {
            val intent = Intent()
            intent.setClass(this@MyActivity, BluetoothSettingActivity::class.java)
            startActivity(intent)
        }
        binding.backImg.setOnClickListener {
            finish()
        }
        binding.transmitLayout.setOnClickListener {
            val intent = Intent()
            intent.setClass(this@MyActivity, MyTransmitListActivity::class.java)
            startActivity(intent)
        }
        binding.myLoginImg.setOnClickListener {
            val intent = Intent()
            intent.setClass(this@MyActivity, LoginActivity::class.java)
            startActivity(intent)
//            finish()
        }
        binding.logoutLayout.setOnClickListener {
//            DataManager.cLearMMKV("token")
//            DataManager.setDataBool("login_state", false)
//            DataManager.setDataBool("ble_state", false)
//            DataManager.setDataString("device_name","设备名称")
//            DataManager.cLearMMKV("data")
            DataManager.clearAllMMKV()
            ActivityUtils.finishAllActivitiesExceptNewest()
            val intent = Intent()
            intent.setClass(this@MyActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.userinfoLayout.setOnClickListener {
            val data = DataUtils.getUpData()
            Log.d("MyActivity", "上传的数据:$data")
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}