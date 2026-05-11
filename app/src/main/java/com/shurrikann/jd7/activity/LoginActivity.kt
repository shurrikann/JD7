package com.shurrikann.jd7.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.blankj.utilcode.util.ToastUtils
import com.shurrikann.jd7.R
import com.shurrikann.jd7.base.BaseActivity
import com.shurrikann.jd7.bean.CommonRequestBody
import com.shurrikann.jd7.bean.DataManager
import com.shurrikann.jd7.bean.LoginRequest
import com.shurrikann.jd7.databinding.LoginActivityBinding
import com.shurrikann.jd7.network.Result
import com.shurrikann.jd7.network.RetrofitClient
import com.shurrikann.jd7.utils.JsonUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.annotations.TestOnly
import java.net.ConnectException

class LoginActivity : BaseActivity() {
    override lateinit var binding: LoginActivityBinding
    override fun getLayoutResId(): Int {
        return R.layout.login_activity
    }

    override fun init(savedInstanceState: Bundle?) {
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initClick()
    }

    private fun initClick() {
        binding.loginBtn.setOnClickListener {
            if (binding.usernameEdit.text.isNullOrEmpty() || binding.pwdEdit.text.isNullOrEmpty()) {
                ToastUtils.showShort("请输入用户名或密码")
                return@setOnClickListener
            }
            showLoadingDialog(this, "正在登陆...")
            val loginRequest = LoginRequest(
                binding.usernameEdit.text.toString().trim(),
                binding.pwdEdit.text.toString().trim()
            )
            val loginJson = JsonUtils.createJsonRequestBody(loginRequest)
            // 创建 RequestBody 实例
            val requestBody = CommonRequestBody(loginJson)
            CoroutineScope(Dispatchers.Main).launch {
                userLogin(requestBody)
            }
        }
    }

    private suspend fun userLogin(loginRequest: CommonRequestBody) {
        try {
            // 通过 RetrofitClient.makeRequest 统一处理请求
            val result = RetrofitClient.makeRequest {
                RetrofitClient.apiService.login(loginRequest)// 将请求体传递给接口
            }
            Log.d("result","$result")
            // 根据 result 处理请求结果
            when (result) {
                is Result.Success -> {
                    if (result.data.code == 0) {
                        val loginResponse = result.data
                        DataManager.setDataString("token", loginResponse.data.curSysUser.token)
                        DataManager.setDataBool("login_state", true)
                        DataManager.setDataString("name", loginResponse.data.curSysUser.name!!)
                        DataManager.setDataString(
                            "role",
                            loginResponse.data.curSysUser.role_list_name
                        )
                        Log.d("Login", "登录成功: ${loginResponse.info}")
                        // 在此处理登录成功后的逻辑，比如保存 Token 等
//                    ToastUtils.showShort("登录成功")
                        val intent = Intent()
                        intent.setClass(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        ToastUtils.showShort(result.data.info)
                    }
                }

                is Result.Error -> {
                    Log.e("LoginError1", "请求失败: ${result.exception.message}")
                    ToastUtils.showShort("登录失败: ${result.exception.message}")
                }
            }
        } catch (e: ConnectException) {
            // 网络异常处理
            ToastUtils.showShort("网络异常,请稍后再试")
            Log.e("LoginError2", "网络异常: ${e.message}")
        } catch (e: Exception) {
            // 其他异常处理
            ToastUtils.showShort("发生异常: ${e.localizedMessage}")
            Log.e("LoginError3", "发生异常: ${e.message}")
        } finally {
            // 不管成功与否，都关闭加载动画
            dismissLoadingDialog()
        }
    }

}