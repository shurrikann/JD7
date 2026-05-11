package com.shurrikann.jd7.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shurrikann.jd7.R
import com.shurrikann.jd7.adapter.TransmitListAdapter
import com.shurrikann.jd7.base.BaseActivity
import com.shurrikann.jd7.bean.DataManager
import com.shurrikann.jd7.bean.EventMessage
import com.shurrikann.jd7.bean.LocationData
import com.shurrikann.jd7.databinding.TransmitListActivityBinding
import com.shurrikann.jd7.myinterface.ListInterface
import com.shurrikann.jd7.utils.DataUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MyTransmitListActivity : BaseActivity(),ListInterface {
    override lateinit var binding: TransmitListActivityBinding
    private lateinit var transmitListAdapter: TransmitListAdapter
    private var list = mutableListOf<LocationData>()
    override fun getLayoutResId(): Int {
        return R.layout.transmit_list_activity
    }

    override fun init(savedInstanceState: Bundle?) {
        binding = TransmitListActivityBinding.inflate(layoutInflater)
        // 注册 EventBus
        EventBus.getDefault().register(this)
        setContentView(binding.root)
        initAdapter()
        initData()
        initListener()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initData() {
        list.clear()
        val data = DataManager.readDataString("data")
        Log.d("MyTransmitListActivity", "data:" + data)
        if (!data.isNullOrEmpty()) {
            val loclistType = object : TypeToken<MutableList<LocationData>>() {}.type
            val loclist: MutableList<LocationData> = Gson().fromJson(data, loclistType)
            list.addAll(loclist)
            val toRemove = mutableListOf<LocationData>()
            for (item in list) {
                if (!DataUtils.judgmentDate(item.cjTime.toLongOrNull()!!)) {
                    toRemove.add(item)
                }
            }
            list.removeAll(toRemove)
            DataManager.saveDataString("data", DataUtils.jsonToString(list))
            transmitListAdapter.notifyDataSetChanged()
        }
        if (list.size > 0) {
            binding.noDataLayout.visibility = View.GONE
            binding.transmitRecy.visibility = View.VISIBLE
        } else {
            binding.noDataLayout.visibility = View.VISIBLE
            binding.transmitRecy.visibility = View.GONE
        }
    }

    private fun initAdapter() {
        transmitListAdapter = TransmitListAdapter(this)
        binding.transmitRecy.layoutManager = LinearLayoutManager(this)
        binding.transmitRecy.adapter = transmitListAdapter
        transmitListAdapter.submitList(list)
    }

    private fun initListener() {
        binding.backImg.setOnClickListener {
            finish()
        }
        transmitListAdapter.setOnItemClickListener { bluetoothlistAdapter, view, pos ->
            val data = list.get(pos)
            val intent = Intent()
            intent.setClass(this@MyTransmitListActivity, TransmitInfoActivity::class.java)
            intent.putExtra("data", data)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 解注册 EventBus
        EventBus.getDefault().unregister(this)
    }


    // 事件接收方法
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EventMessage) {
        // 处理事件
        initData()
    }

    override fun deleteItem(pos: Int) {
        list.clear()
        val data = DataManager.readDataString("data")
        Log.d("deleteItem", "data:" + data)
        if (!data.isNullOrEmpty()) {
            val loclistType = object : TypeToken<MutableList<LocationData>>() {}.type
            val loclist: MutableList<LocationData> = Gson().fromJson(data, loclistType)
            list.addAll(loclist)
            list.removeAt(pos)
            DataManager.saveDataString("data", DataUtils.jsonToString(list))
            transmitListAdapter.notifyItemRemoved(pos)
        }
    }
}