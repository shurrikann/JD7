package com.shurrikann.jd7.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.shurrikann.jd7.R
import com.shurrikann.jd7.adapter.HistoryDataTimeAdapter
import com.shurrikann.jd7.base.BaseActivity
import com.shurrikann.jd7.bean.AllRequestBody
import com.shurrikann.jd7.bean.DataManager
import com.shurrikann.jd7.bean.HData
import com.shurrikann.jd7.bean.HistoryDataRequest
import com.shurrikann.jd7.databinding.HistoryDataTimeActivityBinding
import com.shurrikann.jd7.network.Result
import com.shurrikann.jd7.network.RetrofitClient
import com.shurrikann.jd7.utils.JsonUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.ConnectException

/**
 * 这是根据选择时间获取历史记录的activity
 * startTime为启示时间
 * endTime为结束时间
 * 查询间隔为24小时
 *
 */
class HistoryDataTimeActivity : BaseActivity() {
    override lateinit var binding: HistoryDataTimeActivityBinding
    private lateinit var historyDataTimeAdapter: HistoryDataTimeAdapter
    private var lanwaylist = mutableListOf<HData>()
    private var startTime = ""
    private var endTime = ""
    private var page = 1
    override fun getLayoutResId(): Int {
        return R.layout.history_data_lanway_activity
    }

    override fun init(savedInstanceState: Bundle?) {
        binding = HistoryDataTimeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getData()
        initAdapter()
        initListener()
        getLanwayData(startTime, endTime, page)
    }

    //获取传入的参数
    private fun getData() {
        val date = intent.getStringExtra("date")
        binding.titleText.text = date
        startTime = date + " 00:00:00"
        endTime = date + " 23:59:59"
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initAdapter() {
        historyDataTimeAdapter = HistoryDataTimeAdapter()
        binding.historyRecy.layoutManager = LinearLayoutManager(this)
        binding.historyRecy.adapter = historyDataTimeAdapter
        historyDataTimeAdapter.submitList(lanwaylist)
        historyDataTimeAdapter.notifyDataSetChanged()
    }

    private fun initListener() {
        binding.backImg.setOnClickListener {
            finish()
        }
        historyDataTimeAdapter.setOnItemClickListener { _, view, pos ->

        }
        binding.refreshLayout.setOnRefreshListener {
            page = 1
            Log.d("page", "$page")
            getLanwayData(startTime, endTime, page)
            binding.refreshLayout.finishRefresh(1000)
        }
        binding.refreshLayout.setOnLoadMoreListener {
            page++
            Log.d("page", "$page")
            getLanwayData(startTime, endTime, page)
            binding.refreshLayout.finishLoadMore(1000)
        }
    }

    private fun getLanwayData(startTime: String, endTime: String, page: Int) {
        val historyDataRequest = HistoryDataRequest(endTime, "", "10", "$page", startTime)
        val loginJson = JsonUtils.createJsonRequestBody(historyDataRequest)
        // 创建 RequestBody 实例
        val requestBody = AllRequestBody(loginJson, DataManager.getDataString("token"))
        CoroutineScope(Dispatchers.Main).launch {
            getHistoryData(requestBody, page)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun getHistoryData(allRequestBody: AllRequestBody, page: Int) {
        showLoadingDialog(this, "获取历史数据...")
        try {
            // 通过 RetrofitClient.makeRequest 统一处理请求
            val result = RetrofitClient.makeRequest {
                RetrofitClient.apiService.historydata(allRequestBody)// 将请求体传递给接口
            }
            // 根据 result 处理请求结果
            when (result) {
                is Result.Success -> {
                    Log.d("GetData", "获取成功")
                    if(result.data.code==0){
                        if (page == 1) {
                            lanwaylist.clear()
                        }
                        lanwaylist.addAll(result.data.data)
                        historyDataTimeAdapter.notifyDataSetChanged()
//                    val jsonString = Gson().toJson(lanwaylist)
                        if (result.data.data.isEmpty()) {
                            ToastUtils.showShort("没有新的数据了")
                        }
                    }else if(result.data.code == 5){
                        ToastUtils.showLong(result.data.info)
                        DataManager.clearAllMMKV()
                        ActivityUtils.finishAllActivitiesExceptNewest()
                        val intent = Intent()
                        intent.setClass(this@HistoryDataTimeActivity, WelcomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        ToastUtils.showLong(result.data.msg)
                    }
                }

                is Result.Error -> {
                    ToastUtils.showLong("获取失败,请稍后再试...")
                    Log.d("GetData", "获取失败:${result.exception.message}")
                }
            }
        } catch (e: ConnectException) {
            // 网络异常处理
            ToastUtils.showLong("网络异常,请稍后再试")
            Log.e("LoginError", "网络异常: ${e.message}")
        } catch (e: Exception) {
            // 其他异常处理
            ToastUtils.showLong("发生异常: ${e.message}")
            Log.e("LoginError", "发生异常: ${e.message}")
        } finally {
            // 不管成功与否，都关闭加载动画
            if (lanwaylist.size > 0) {
                binding.noDataLayout.visibility = View.GONE
                binding.refreshLayout.visibility = View.VISIBLE
            } else {
                binding.noDataLayout.visibility = View.VISIBLE
                binding.refreshLayout.visibility = View.GONE
            }
            dismissLoadingDialog()
        }
    }
}