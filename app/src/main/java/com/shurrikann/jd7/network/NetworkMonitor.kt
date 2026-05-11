package com.shurrikann.jd7.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

// NetworkMonitor.kt
object NetworkMonitor {
    private lateinit var context: Context
    private lateinit var connectivityManager: ConnectivityManager
    private var listener: NetworkStateListener? = null

    // 初始化 NetworkMonitor
    fun init(applicationContext: Context) {
        context = applicationContext
        connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        startMonitoring()
    }

    // 启动网络监控
    private fun startMonitoring() {
        // 对于低版本设备，使用 BroadcastReceiver
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (isWiFiConnectedAndAvailable()) {
                    listener?.onNetworkAvailable()  // 确保调用回调
                    Log.d("NetworkMonitor", "网络正常${isWiFiConnectedAndAvailable()}")
                } else {
                    listener?.onNetworkLost()  // 确保调用回调
                    Log.d("NetworkMonitor", "网络断开${isWiFiConnectedAndAvailable()}")
                }
            }
        }, filter)
    }

    // 检查 Wi-Fi 是否连接并且是否有互联网访问
    fun isWiFiConnectedAndAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // 设置网络状态监听器
    fun setNetworkStateListener(networkStateListener: NetworkStateListener) {
        listener = networkStateListener
    }
}

interface NetworkStateListener {
    fun onNetworkAvailable()
    fun onNetworkLost()
}
