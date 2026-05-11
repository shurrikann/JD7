package com.shurrikann.jd7.activity

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.hjq.permissions.XXPermissions
import com.shurrikann.jd7.R
import com.shurrikann.jd7.adapter.MainViewPagerAdapter
import com.shurrikann.jd7.bean.CommonRequestBody
import com.shurrikann.jd7.bean.DataManager
import com.shurrikann.jd7.databinding.MainActivityBinding
import com.shurrikann.jd7.network.Result
import com.shurrikann.jd7.network.RetrofitClient
import com.shurrikann.jd7.server.BluetoothService
import com.shurrikann.jd7.utils.JsonUtils
import com.shurrikann.jd7.viewmodel.SharedViewModel
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.net.ConnectException

class MainActivity : FragmentActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var mainViewPagerAdapter: MainViewPagerAdapter
    private lateinit var gradient: LinearGradient
    private lateinit var sharedViewModel: SharedViewModel
    private var _binding: MainActivityBinding? = null
    private val binding get() = _binding!!
    private lateinit var mmkvCustom: MMKV
    private val INSTALL_REQUEST_CODE = 1001
    private var versionCode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.black)
        _binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mmkvCustom = MMKV.mmkvWithID("blueconnectstate", MMKV.SINGLE_PROCESS_MODE)
        // 获取 ViewModel
        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
        val serviceIntent = Intent(this, BluetoothService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        initView()
        initAdapter()
        // 观察 isSwipeDisabled LiveData
        sharedViewModel.isSwipeDisabled.observe(this, Observer { isDisabled ->
            // 根据 LiveData 的值来禁用或启用 ViewPager2 的滑动
            Log.d("mainactivity-ViewPager2", "$isDisabled")
            binding.viewPager.isUserInputEnabled = !isDisabled // true 表示可以滑动，false 禁用滑动
        })
        getVersion()
        // 请求蓝牙权限
        addPermission()
    }

    @SuppressLint("InlinedApi")
    private fun addPermission() {
        XXPermissions.with(this)
            .permission(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
            .request { granted, deniedList ->
                if (deniedList) {
                    initBlueTooth()
                    checkAppVersion(versionCode)
                } else {
                    ToastUtils.showShort("以下权限被拒绝: $granted,请前往设置页面手动开启")
                }
            }
    }

    private fun getVersion() {
        val packageManager = this.packageManager
        val packageInfo = packageManager.getPackageInfo(this.packageName, 0)
        versionCode = packageInfo.versionCode
        DataManager.setDataString("version", packageInfo.versionName)
        Log.d("versionCode", "$versionCode")
    }


    @SuppressLint("MissingInflatedId")
    private fun initAdapter() {
        mainViewPagerAdapter = MainViewPagerAdapter(this)
        binding.viewPager.adapter = mainViewPagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            val customView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null)
            val tabImg = customView.findViewById<ImageView>(R.id.tab_img)
            val tabText = customView?.findViewById<TextView>(R.id.tab_text)
            when (position) {
                0 -> {
                    tabText?.text = "实时监测"
                    tabText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
                    SetSelectTextColor(tabText!!)
                    tabImg.visibility = View.VISIBLE

                }

                1 -> {
                    tabText?.text = "历史数据"
                    tabText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                    setUnSelectTextColor(tabText!!)
                    tabImg.visibility = View.GONE
                }
            }
            tab.customView = customView
        }.attach()
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0))
        // 设置 Tab 之间的间隔
        for (i in 0 until binding.tabLayout.tabCount) {
            val tab = binding.tabLayout.getTabAt(i)
            val params = tab?.view?.layoutParams as? LinearLayout.LayoutParams
            params?.let {
                it.marginStart = -5 // 设置左间距
                it.marginEnd = -5   // 设置右间距
                tab.view.layoutParams = it
            }
        }
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    val tabImg = it.customView?.findViewById<ImageView>(R.id.tab_img)
                    val tabText = it.customView?.findViewById<TextView>(R.id.tab_text)
                    when (it.position) {
                        0 -> {
                            tabText?.text = "实时监测"
                            SetSelectTextColor(tabText!!)
                            tabText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
                            tabImg?.visibility = View.VISIBLE
                        }

                        1 -> {
                            tabText?.text = "历史数据"
                            SetSelectTextColor(tabText!!)
                            tabText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
                            tabImg?.visibility = View.VISIBLE
                        }
                    }
                }
            }

            @SuppressLint("ResourceAsColor")
            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.let {
                    val tabImg = it.customView?.findViewById<ImageView>(R.id.tab_img)
                    val tabText = it.customView?.findViewById<TextView>(R.id.tab_text)
                    when (it.position) {
                        0 -> {
                            tabText?.text = "实时监测"
                            tabText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                            setUnSelectTextColor(tabText!!)
                            tabImg?.visibility = View.GONE
                        }

                        1 -> {
                            tabText?.text = "历史数据"
                            tabText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                            setUnSelectTextColor(tabText!!)
                            tabImg?.visibility = View.GONE
                        }
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
    }

    private fun SetSelectTextColor(textView: TextView) {
        gradient = LinearGradient(
            0f, 0f, 0f, textView.textSize, // 渐变的起点和终点
            intArrayOf(0xFF78D1FF.toInt(), 0xFFFFFFFF.toInt()), // 渐变色的颜色
            null, // 不使用颜色位置
            Shader.TileMode.CLAMP // 渐变模式
        )
        textView.paint.shader = gradient
    }

    private fun setUnSelectTextColor(textView: TextView) {
        gradient = LinearGradient(
            0f, 0f, 0f, textView.textSize, // 渐变的起点和终点
            intArrayOf(0xFFABABAB.toInt(), 0xFFABABAB.toInt()), // 渐变色的颜色
            null, // 不使用颜色位置
            Shader.TileMode.CLAMP // 渐变模式
        )
        textView.paint.shader = gradient
    }

    private fun initView() {
        binding.loginLayoutImg.setOnClickListener {
            val intent = Intent()
            intent.setClass(this@MainActivity, MyActivity::class.java)
            startActivity(intent)
        }
        if (DataManager.getDataBool("login_state")) {
            binding.logImg.visibility = View.GONE
            binding.nameLayout.visibility = View.VISIBLE
            if(!DataManager.getDataString("name").isNullOrEmpty()){
                binding.nameText.text = DataManager.getDataString("name").substring(0, 1)
            }
        } else {
            binding.logImg.visibility = View.VISIBLE
            binding.nameLayout.visibility = View.GONE
        }

    }

    private fun initBlueTooth() {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "请确保蓝牙已开启", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("Bluetooth", "蓝牙已开启")
            // 蓝牙已开启，继续执行后续操作
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    private var lastBackPressedTime: Long = 0
    private val doubleClickTimeout: Long = 2000  // 设定双击的时间间隔（例如 2秒）

    override fun onBackPressed() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressedTime < doubleClickTimeout) {
            super.onBackPressed()  // 两次点击在设定的时间范围内，退出应用
            val serviceIntent = Intent(this, BluetoothService::class.java)
            stopService(serviceIntent)
            ActivityUtils.finishAllActivities()
        } else {
            Toast.makeText(this, "再按一次退出应用", Toast.LENGTH_SHORT).show()
            lastBackPressedTime = currentTime  // 记录当前时间
        }
    }

    private fun checkAppVersion(versionCode: Int) {
        val upAppJson = JsonUtils.createJsonRequestBody("")
        val requestBody = CommonRequestBody(upAppJson)
        CoroutineScope(Dispatchers.Main).launch {
            checkVersion(requestBody, versionCode)
        }
    }

    private suspend fun checkVersion(requestBody: CommonRequestBody, code: Int) {
        try {
            val result = RetrofitClient.makeRequest {
                RetrofitClient.apiService.upappversion(requestBody)// 将请求体传递给接口
            }
            when (result) {
                is Result.Success -> {
                    val cloudVersion = result.data.data.verCode
                    Log.d(
                        "up_app",
                        "${result.data.data.verCode},${result.data.data.verUrl},$code,${result.data.data.verContent}"
                    )
                    if (cloudVersion > code) {
                        showUpAppDialog(result.data.data.verUrl, result.data.data.verContent)
                    }
                }

                is Result.Error -> {
                    Log.e("checkAppVersion", "检查出错:${result.exception.message}")
                }
            }
        } catch (e: ConnectException) {
            // 网络异常处理
            ToastUtils.showShort("网络异常,请稍后再试")
            Log.e("LoginError", "网络异常: ${e.message}")
        } catch (e: Exception) {
            // 其他异常处理
            ToastUtils.showShort("发生异常: ${e.message}")
            Log.e("LoginError", "发生异常: ${e.message}")
        } finally {

        }
    }


    private fun showUpAppDialog(url: String, content: String) {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.upapp_dialog, null)
        val contentText: TextView = view.findViewById(R.id.content_text)
        contentText.text = content // 设置加载文本
        val cancelBtn: Button = view.findViewById(R.id.cancel_button)
        val upBtn: Button = view.findViewById(R.id.up_button)
        val loadingDialog = AlertDialog.Builder(this)
            .setView(view) // 设置自定义布局
            .setCancelable(false) // 点击外部区域不可取消
            .create()
        loadingDialog.show()
        val layoutParams = loadingDialog.window?.attributes
        layoutParams?.width =
            (resources.displayMetrics.widthPixels * 0.8).toInt() // 设置 Dialog 宽度为屏幕宽度的 60%
        loadingDialog.window?.attributes = layoutParams

        cancelBtn.setOnClickListener { loadingDialog.dismiss() }
        upBtn.setOnClickListener {
            loadingDialog.dismiss()
            startDownload(url)
        }
    }

    @SuppressLint("Range", "SetTextI18n")
    private fun startDownload(url: String) {
//        val downloadDialogFragment = DownloadDialogFragment(url)
//        // 显示下载对话框
//        downloadDialogFragment.show(supportFragmentManager, "DownloadDialog")
        // 创建自定义对话框
        // 创建 DialogFragment 的实例
        try {
            val dialogView = layoutInflater.inflate(R.layout.dialog_download, null)
            val progressBar = dialogView.findViewById<ProgressBar>(R.id.progress_bar)
            val progressText = dialogView.findViewById<TextView>(R.id.download_progress)
            // 创建一个 AlertDialog
            val progressDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false) // 设置为不可取消
                .create()
            // 显示自定义对话框
            if (!isFinishing && !isDestroyed) {
                // Activity 已经销毁，跳过对话框显示
                progressDialog.show()
            }
            val layoutParams = progressDialog.window?.attributes
            layoutParams?.width =
                (resources.displayMetrics.widthPixels * 0.8).toInt() // 设置 Dialog 宽度为屏幕宽度的 60%
            progressDialog.window?.attributes = layoutParams
            // 获取下载目录和目标文件
            val downloadDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val apkFile = File(downloadDir, "Luan.apk")
            Log.d("downloadDir", "$downloadDir")
            // 如果文件已经存在，先删除
            if (apkFile.exists()) {
                apkFile.delete()
            }
            val request = android.app.DownloadManager.Request(Uri.parse(url))
                .setTitle("下载应用")
                .setDescription("正在下载...")
//            .setDestinationUri(Uri.fromFile(apkFile))  // 使用 File URI，而不是 content URI
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Luan.apk")
                .setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_WIFI or android.app.DownloadManager.Request.NETWORK_MOBILE)

            val downloadManager =
                getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
            val downloadId = downloadManager.enqueue(request)

            // 启动一个线程来监控下载进度
            Thread {
                var downloading = true
                while (downloading) {
                    val cursor = downloadManager.query(
                        android.app.DownloadManager.Query().setFilterById(downloadId)
                    )
                    cursor?.apply {
                        if (moveToFirst()) {
                            val bytesDownloaded =
                                getLong(getColumnIndex(android.app.DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            val bytesTotal =
                                getLong(getColumnIndex(android.app.DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                            if (bytesTotal > 0) {
                                val progress = (bytesDownloaded * 100L / bytesTotal).toInt()
                                runOnUiThread {
                                    progressBar.progress = progress
                                    progressText.text = "下载进度：$progress%"
                                }
                            }

                            if (bytesDownloaded == bytesTotal) {
                                downloading = false
                                close()
                                runOnUiThread {
                                    progressDialog.dismiss() // 下载完成，关闭进度条
                                    requestInstallPermission()
                                }
                            }
                        }
                    }
                }
            }.start()
        } catch (e: Exception) {

        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == INSTALL_REQUEST_CODE) {
            // 检查权限是否已被授予
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (packageManager.canRequestPackageInstalls()) {
                    // 如果权限已被授予，调用安装 APK 方法
                    installAPK()
                } else {
                    // 权限仍未授予，提示用户
                    Toast.makeText(this, "请授予安装未知来源应用的权限", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun requestInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivityForResult(intent, INSTALL_REQUEST_CODE)
            } else {
                installAPK()
            }
        } else {
            installAPK() // 直接安装对于 Android 8 以下版本
        }
    }

    private fun installAPK() {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Luan.apk"
        )
        // 打印文件路径，用于调试
        Log.d("InstallAPK", "APK file path: ${file.absolutePath}")
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(
                this,
                "com.shurrikann.jd7.fileprovider", // provider 的 authority
                file
            )
            // 打印 URI，用于调试
            Log.d("InstallAPK", "Generated URI: $uri")
            // 创建 Intent 来启动 APK 安装
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            // 检查是否是 Android 7.0 及以上
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // 对于 Android 7.0 以上版本，必须通过 FileProvider 来传递文件 URI
                // 打印 URI，用于调试
                Log.d("InstallAPK", "Generated URI: $uri")
                startActivity(intent)
            } else {
                Log.d("InstallAPK", "Android version < 7.0, using file:// URI")
                // 对于 Android 7.0 以下版本，直接用 file:// URI
                intent.setData(Uri.fromFile(file))
                startActivity(intent)
            }
        } else {
            Toast.makeText(this, "APK 文件不存在", Toast.LENGTH_SHORT).show()
        }
    }
}

