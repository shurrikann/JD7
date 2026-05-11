package com.shurrikann.jd7.network

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.shurrikann.jd7.bean.DataManager

class UploadWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {
        // 获取保存的待上传数据
//        val mmkv = MMKV.defaultMMKV()
//        val json = mmkv?.getString("pending_data", null)
        val json = DataManager.readDataString("")
        Log.d("UploadWorker", json.toString())
        // 如果缓存中有待上传的数据
        if (json != null) {
//            val pendingData = Gson().fromJson(json.toString(), Array<String>::class.java).toList()
            // 模拟上传操作
//            for (data in pendingData) {
            val uploadSuccess = uploadData(json.toString())
            if (!uploadSuccess) {
                return Result.retry()  // 如果上传失败，重试该任务
            }
//            }
            return Result.success()  // 成功
        }
        return Result.failure()  // 如果没有待上传的数据，任务失败
    }

    private fun uploadData(data: String): Boolean {
        // 在此处实现数据上传的逻辑
        println("Uploading data: $data")
//        // 使用 TypeToken 来获取 MutableList<UpDataBean> 类型
//        val listType = object : TypeToken<MutableList<UpDataBean>>() {}.type
//        // 将 JSON 字符串转换为 MutableList<UpDataBean>
//        val list: MutableList<UpDataBean> = Gson().fromJson(data, listType)
//        println("Uploading data: ${list.size}")
        // 模拟上传操作，返回成功/失败
        return true  // 上传成功
    }
}