package com.shurrikann.jd7.application

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import androidx.multidex.MultiDexApplication
import com.blankj.utilcode.util.ActivityUtils
import com.shurrikann.jd7.bean.DataManager
import com.shurrikann.jd7.network.NetworkMonitor
import com.shurrikann.jd7.server.BluetoothService
import com.tencent.mmkv.MMKV


class JD7Application : MultiDexApplication() {

    lateinit var bluetoothService: BluetoothService
    private lateinit var wakeLock: PowerManager.WakeLock

    companion object {
        private lateinit var instance: JD7Application

        fun getInstance(): JD7Application {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        MMKV.initialize(this);
        // 初始化 BluetoothService
        Log.d("login","${DataManager.getDataBool("login_state")}")
        bluetoothService = BluetoothService()
        NetworkMonitor.init(applicationContext)
        myWakeLock()
    }

    private fun myWakeLock() {
        // 获取 PowerManager 实例
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        // 创建 WakeLock，保持屏幕常亮
        wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "App::ScreenWakeLock"
        )
        // 注册 ActivityLifecycleCallbacks 监听 Activity 的生命周期
        registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {
                // 应用进入前台时保持屏幕常亮
                wakeLock.acquire()
            }

            override fun onActivityResumed(activity: Activity) {
                // 应用在前台时保持屏幕常亮
                wakeLock.acquire()
            }

            override fun onActivityPaused(activity: Activity) {
                // 应用进入后台时释放 WakeLock，停止常亮
                wakeLock.release()
            }

            override fun onActivityStopped(activity: Activity) {
                // 应用进入后台时释放 WakeLock，停止常亮
                if (activity.isFinishing) {
                    wakeLock.release()
                }
            }

            override fun onActivityDestroyed(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        })
    }


    override fun onTerminate() {
        super.onTerminate()
        // 注销广播接收器

    }

    override fun onLowMemory() {
        super.onLowMemory()
        System.gc()
    }

    fun exit() {
        try {
            ActivityUtils.finishAllActivities()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            System.exit(0)
        }
    }
}