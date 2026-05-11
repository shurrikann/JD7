package com.shurrikann.jd7.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.shurrikann.jd7.R
import com.shurrikann.jd7.base.BaseActivity
import com.shurrikann.jd7.bean.DataManager
import com.shurrikann.jd7.databinding.WelcomeActivityBinding

class WelcomeActivity : BaseActivity() {
    override lateinit var binding: WelcomeActivityBinding
    override fun getLayoutResId(): Int {
        return R.layout.welcome_activity
    }

    override fun init(savedInstanceState: Bundle?) {
        binding = WelcomeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initApp()
    }

    override fun getStatusBarColorResId(): Int {
        return R.color.black
    }


    private fun initApp() {
        Log.d("login_state","${DataManager.getDataBool("login_state")}")
        Handler().postDelayed({
            if (DataManager.getDataBool("login_state")) {
                val intent = Intent()
                intent.setClass(this@WelcomeActivity, MainActivity::class.java)
                startActivity(intent)
            } else if (!DataManager.getDataBool("login_state")) {
                val intent = Intent()
                intent.setClass(this@WelcomeActivity, LoginActivity::class.java)
                startActivity(intent)
            }
            finish()
        }, 1000)
    }
}