package com.shurrikann.jd7.server

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import java.util.UUID
import android.app.Notification
import android.content.Context
import android.content.pm.ServiceInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class BluetoothService : Service() {
    companion object {
        const val ACTION_CONNECTION_STATE_CHANGED =
            "com.example.bluetoothdemo.CONNECTION_STATE_CHANGED"
        const val ACTION_NOTIFICATION_RECEIVED = "com.example.bluetoothdemo.NOTIFICATION_RECEIVED"
        const val EXTRA_DEVICE_NAME = "device_name"
        const val EXTRA_IS_CONNECTED = "is_connected"
        const val EXTRA_NOTIFICATION_DATA = "notification_data"
        const val CHANNEL_ID = "BluetoothServiceChannel"
    }


    private val binder = BluetoothBinder()
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private var bluetoothGatt: BluetoothGatt? = null

    private val TAG = "BluetoothService"
    private var connectedDeviceName: String? = null
    private var scanCallback: ScanCallback? = null

    override fun onBind(intent: Intent?): IBinder = binder

    // 创建 LiveData 来保存蓝牙连接状态
    private val _bluetoothConnectionState = MutableLiveData<Boolean>()
    val bluetoothConnectionState: LiveData<Boolean> get() = _bluetoothConnectionState

    private val _dataConnectionState = MutableLiveData<String>()
    val dataConnectionState: LiveData<String> get() = _dataConnectionState
    inner class BluetoothBinder : Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }

    override fun onCreate() {
        super.onCreate()
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.e(TAG, "设备不支持蓝牙或蓝牙未开启")
            stopSelf()  // 可以选择停止服务或者进行其他错误处理
            return
        }
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        if (bluetoothLeScanner == null) {
            Log.e(TAG, "无法获取蓝牙 LE 扫描器")
            stopSelf()  // 停止服务或者提示用户
            return
        }
        // 启动前台服务
        startForegroundService()
    }

    @SuppressLint("ForegroundServiceType", "NewApi")
    private fun startForegroundService() {
        // 创建通知渠道（仅适用于 Android 8.0 及以上版本）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Bluetooth Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // 创建前台服务通知
        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0 及以上版本使用通知渠道
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Bluetooth Service")
                .setContentText("Maintaining Bluetooth connection...")
                .setSmallIcon(android.R.drawable.ic_menu_info_details) // 用你自己的图标
                .build()
        } else {
            // Android 8.0 以下版本直接使用不带渠道的构造方法
            Notification.Builder(this)
                .setContentTitle("Bluetooth Service")
                .setContentText("Maintaining Bluetooth connection...")
                .setSmallIcon(android.R.drawable.ic_menu_info_details) // 用你自己的图标
                .build()
        }
        // 启动前台服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 添加 RECEIVER_NOT_EXPORTED 标志
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        } else {
            // Android 12 以下版本无需设置标志
            startForeground(1, notification)
        }

    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (scanCallback != null) {
            Log.w(TAG, "扫描已在进行中")
            return
        }
        Log.d(TAG, "开始扫描")
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                Log.d(TAG, "发现设备: ${device.name} (${device.address})")
                if (device.name == "JD7_BLE") { // 根据设备名称筛选
                    stopScan() // 停止扫描
                    connectToDevice(device)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, "扫描失败: $errorCode")
            }
        }
        if (!::bluetoothLeScanner.isInitialized) {
            Log.e(TAG, "蓝牙扫描器未初始化")
            return
        }
        bluetoothLeScanner?.startScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        scanCallback?.let {
            bluetoothLeScanner.stopScan(it)
            scanCallback = null
            Log.d(TAG, "已停止扫描")
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(this, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    connectedDeviceName = device.name
                    Log.d(TAG, "设备已连接: $connectedDeviceName")
                    sendConnectionStateBroadcast(true, connectedDeviceName)
                    Log.d(TAG, "设备已连接2: $connectedDeviceName")
                    _bluetoothConnectionState.postValue(true) // 更新连接状态
                    gatt.discoverServices()
                    Log.d(TAG, "设备已连接3: $connectedDeviceName")
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(TAG, "设备已断开连接: $connectedDeviceName")
                    _bluetoothConnectionState.postValue(false) // 更新连接状态
                    sendConnectionStateBroadcast(false, connectedDeviceName)

                    Log.d(TAG, "设备已断开连接2: $connectedDeviceName")
                    connectedDeviceName = null
                    bluetoothGatt?.close()
                    bluetoothGatt = null
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                super.onServicesDiscovered(gatt, status)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    gatt.services.forEach { service ->
                        service.characteristics.forEach { characteristic ->
                            if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                                enableNotifications(gatt, characteristic)
                            }
                        }
                    }
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {
                val data = String(characteristic.value, Charsets.UTF_8)
                Log.d("收到通知数据3:", "$data")
                _dataConnectionState.postValue("$data")
                sendNotificationBroadcast(data)
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun enableNotifications(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor =
            characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
        descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        gatt.writeDescriptor(descriptor)
    }

    private fun sendConnectionStateBroadcast(isConnected: Boolean, deviceName: String?) {
        val intent = Intent(ACTION_CONNECTION_STATE_CHANGED).apply {
            putExtra(EXTRA_IS_CONNECTED, isConnected)
            putExtra(EXTRA_DEVICE_NAME, deviceName)
        }
        Log.d("sendConnectionStateBroadcast", "sendBroadcast")
        sendBroadcast(intent)
    }

    private fun sendNotificationBroadcast(data: String) {
        val intent = Intent(ACTION_NOTIFICATION_RECEIVED).apply {
            putExtra(EXTRA_NOTIFICATION_DATA, data)
        }
        sendBroadcast(intent)
    }

    fun getConnectedDeviceName(): String? = connectedDeviceName

    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
    }

    @SuppressLint("MissingPermission")
    fun close() {
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    override fun onDestroy() {
        super.onDestroy()
//        StopService()
    }

    fun StopService() {
        stopScan()  // 停止扫描
        disconnect() // 断开连接
        close() // 关闭连接
    }
}