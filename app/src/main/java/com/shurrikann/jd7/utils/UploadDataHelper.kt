package com.shurrikann.jd7.utils

import android.util.Log
import com.google.gson.Gson
import com.shurrikann.jd7.bean.CommonRequestBody
import com.shurrikann.jd7.bean.UpDataBody
import com.shurrikann.jd7.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ConnectException
import com.shurrikann.jd7.network.Result

object UploadDataHelper {

    // 上传数据的公共方法
    fun uploadData(data: String, onResult: (Boolean, String, Int) -> Unit) {
        // 创建请求体，这里将 data 合并到固定请求体中
        val jsonarray = UpDataBody(data)
        val jsonData = Gson().toJson(jsonarray)
        val body = CommonRequestBody(jsonData)
        // 使用后台线程执行上传操作
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = RetrofitClient.makeRequest {
                    RetrofitClient.apiService.updata(body) // 通过接口上传数据
                }
                Log.d("UploadDataHelper", result.toString())
                // 根据请求结果执行回调
                withContext(Dispatchers.Main) {
                    when (result) {
                        is Result.Success -> {
                            if (result.data.code == 0) {
                                onResult(true, result.data.info, result.data.code)
                            }else if(result.data.code==5){
                                onResult(true, result.data.info, result.data.code)
                            }else{
                                onResult(true, result.data.info, result.data.code)
                            }
                        }

                        is Result.Error -> {
                            onResult(false, "上传失败: ${result.exception.message}",-1)

                        }
                    }
                }

            } catch (e: ConnectException) {
                withContext(Dispatchers.Main) {
                    onResult(false, "网络异常，请稍后再试",-2)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, "发生异常: ${e.message}",-3)
                }
            }
        }
    }
}