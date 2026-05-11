package com.shurrikann.jd7.activity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.shurrikann.jd7.R
import com.shurrikann.jd7.base.BaseActivity
import com.shurrikann.jd7.databinding.BluetoothsetingActivityBinding
import com.shurrikann.jd7.server.BluetoothService


class BluetoothSettingActivity : BaseActivity() {
    private lateinit var bluetoothService: BluetoothService
    override lateinit var binding: BluetoothsetingActivityBinding
    private var isBound = false//判断service是否绑定成功
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            bluetoothService = (service as BluetoothService.BluetoothBinder).getService()
            isBound = true
            Log.d("serviceConnection", "$isBound")
            // 观察蓝牙连接状态变化
            bluetoothService.bluetoothConnectionState.observe(
                this@BluetoothSettingActivity,
                Observer { isConnected ->
                    Log.d(
                        "serviceConnection",
                        "$isConnected,${bluetoothService.getConnectedDeviceName()}"
                    )
                    if (isConnected) {
                        UpBluetoothConnect(isConnected, bluetoothService.getConnectedDeviceName()!!)
                        binding.bluetoothSwitch.isChecked = true
//                        DataManager.setDataBool("ble_state", isConnected)
//                        DataManager.setDataString(
//                            "device_name",
//                            bluetoothService.getConnectedDeviceName()!!
//                        )
                    } else {
                        UpBluetoothConnect(isConnected, "设备名称")
                        binding.bluetoothSwitch.isChecked = false
//                        DataManager.setDataBool("ble_state", false)
//                        DataManager.setDataString("device_name", "设备名称")
                    }
                })
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            Log.d("serviceConnection", "$isBound")
        }
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun UpBluetoothConnect(state: Boolean, deviceName: String) {
        if (state) {
            binding.bluetoothSwitch.isChecked = true
            binding.loadingLayout.visibility = View.GONE
            binding.bluetoothLayout.visibility = View.VISIBLE
            binding.bluetoothNameText.text = deviceName
            Glide.with(this).load(resources.getDrawable(R.drawable.connect_img))
                .into(binding.connectStateImg)
        } else {
            binding.bluetoothSwitch.isChecked = false
            binding.loadingLayout.visibility = View.GONE
            binding.bluetoothLayout.visibility = View.GONE
            binding.bluetoothNameText.text = deviceName
            Glide.with(this).load(resources.getDrawable(R.drawable.unconnect_img))
                .into(binding.connectStateImg)
        }
    }

    override fun getLayoutResId(): Int {
        return R.layout.bluetoothseting_activity
    }

    override fun init(savedInstanceState: Bundle?) {
        binding = BluetoothsetingActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        UpBluetoothConnect(
//            DataManager.getDataBool("ble_state"),
//            DataManager.getDataString("device_name")
//        )
        initService()
        initView()
        initClick()

    }

    override fun onResume() {
        super.onResume()
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        // 判断蓝牙是否支持或是否开启
        if (bluetoothAdapter == null) {
            // 蓝牙不支持
            binding.bluetoothSwitch.isEnabled = false
            ToastUtils.showShort("设备不支持蓝牙")
            // 你可以在这里给用户提示设备不支持蓝牙
        } else if (!bluetoothAdapter.isEnabled) {
            // 蓝牙未开启
            binding.bluetoothSwitch.isEnabled = false
            ToastUtils.showShort("蓝牙未开启，请前往设置开启蓝牙后尝试")
        } else {
            binding.bluetoothSwitch.isEnabled = true
        }
    }


    private fun initService() {
        val intent = Intent(this, BluetoothService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
//        registerReceiver(
//            connectionReceiver,
//            IntentFilter(BluetoothService.ACTION_CONNECTION_STATE_CHANGED)
//        )
    }

    private fun initView() {
        Glide.with(this).load(R.drawable.loading1_img).into(binding.loadingImg)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initClick() {
        binding.bluetoothSwitch.setOnClickListener {
            Log.d("cehck", "${binding.bluetoothSwitch.isChecked}")
            // 获取蓝牙适配器
            if (binding.bluetoothSwitch.isChecked) {
                binding.loadingLayout.visibility = View.VISIBLE
                binding.bluetoothLayout.visibility = View.GONE
                if (isBound) {
                    bluetoothService.startScan()
                }
            } else {
                bluetoothService.StopService()
                binding.loadingLayout.visibility = View.GONE
                binding.bluetoothLayout.visibility = View.GONE
                Glide.with(this).load(resources.getDrawable(R.drawable.unconnect_img))
                    .into(binding.connectStateImg)
//                DataManager.setDataBool("ble_state", false)
//                DataManager.setDataString("device_name", "设备名称")
                binding.bluetoothNameText.text = "设备名称"
                bluetoothService.stopScan()
            }
        }
        binding.backImg.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("BluetoothSettingActivity", "onDestroy")
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
//        unregisterReceiver(connectionReceiver) // 确保广播接收器被注销
    }

}