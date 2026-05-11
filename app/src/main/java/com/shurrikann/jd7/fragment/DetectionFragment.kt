package com.shurrikann.jd7.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ServiceUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.chad.library.adapter4.BaseQuickAdapter
import com.google.gson.Gson
import com.shurrikann.jd7.R
import com.shurrikann.jd7.activity.WelcomeActivity
import com.shurrikann.jd7.adapter.LanWayAdapter
import com.shurrikann.jd7.base.BaseFragment
import com.shurrikann.jd7.bean.AllRequestBody
import com.shurrikann.jd7.bean.CommonRequestBody
import com.shurrikann.jd7.bean.DataManager
import com.shurrikann.jd7.bean.EventMessage
import com.shurrikann.jd7.bean.LWData
import com.shurrikann.jd7.bean.ScanData
import com.shurrikann.jd7.bean.UpDataBean
import com.shurrikann.jd7.bean.UpDataBody
import com.shurrikann.jd7.databinding.DetectionFragmentBinding
import com.shurrikann.jd7.databinding.ErrorDialogBinding
import com.shurrikann.jd7.network.NetworkMonitor
import com.shurrikann.jd7.network.NetworkStateListener
import com.shurrikann.jd7.network.Result
import com.shurrikann.jd7.network.RetrofitClient
import com.shurrikann.jd7.server.BluetoothService
import com.shurrikann.jd7.utils.DataUtils
import com.shurrikann.jd7.utils.DateUtils
import com.shurrikann.jd7.utils.JsonUtils
import com.shurrikann.jd7.utils.UploadDataHelper
import com.shurrikann.jd7.viewmodel.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.ConnectException

/**
 * 主界面，主要用来获取蓝牙设备传递的数据，上传数据等功能
 */
class DetectionFragment : BaseFragment(), NetworkStateListener {
    private lateinit var rotateAnim: ObjectAnimator
    private lateinit var bluetoothService: BluetoothService
    override lateinit var binding: DetectionFragmentBinding
    private var dataStr = ""//用于接收ble数据的值
    private lateinit var errordialog: AlertDialog
    private var lanwaylist = mutableListOf<LWData>()
    private lateinit var lanWayAdapter: LanWayAdapter
    private lateinit var sharedViewModel: SharedViewModel
    private var infoid = ""
    private lateinit var upDataBean: UpDataBean
    private lateinit var times: String
    private var dataState = false

    //    private val dataList: MutableList<Map<String, Double>> = mutableListOf() // 用来存储接收到的数据
    private val handler = Handler(Looper.getMainLooper())
    private var isCollectingData = false
    private val dataList = mutableListOf<Double>()
    private var timeInt = 0.5
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            bluetoothService = (service as BluetoothService.BluetoothBinder).getService()
            // 观察 LiveData
            bluetoothService.dataConnectionState.observe(requireActivity(), Observer { data ->
                // 在这里可以处理 LiveData 变化后的更新 UI 操作
                Log.d("livedata接收到到数据:", data)
                dataStr = data.toString()
            })

            // 观察蓝牙连接状态变化
            bluetoothService.bluetoothConnectionState.observe(
                requireActivity(),
                Observer { isConnected ->
                    Log.d(
                        "serviceConnection",
                        "$isConnected,${bluetoothService.getConnectedDeviceName()}"
                    )
                    dataState = isConnected
                })
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(requireActivity(), BluetoothService::class.java)
        ServiceUtils.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DetectionFragmentBinding.inflate(inflater, container, false) // 初始化binding
        return binding.root // 返回绑
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        // 设置网络状态监听器
        NetworkMonitor.setNetworkStateListener(this)
        initView(view)
        initDialog("请选择巷道!", requireContext())
        initAdapter()
        getLanWay()
    }

    //获取检测点的信息
    private fun getLanWay() {
        val loginJson = JsonUtils.createJsonRequestBody("")
        // 创建 RequestBody 实例
        val requestBody = AllRequestBody(loginJson, DataManager.getDataString("token"))
        CoroutineScope(Dispatchers.Main).launch {
            getLanWays(requestBody)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun getLanWays(allRequestBody: AllRequestBody) {
        showLoadingDialog(requireActivity(), "获取巷道...", "")
        try {
            // 通过 RetrofitClient.makeRequest 统一处理请求
            val result = RetrofitClient.makeRequest {
                RetrofitClient.apiService.searchlayway(allRequestBody)// 将请求体传递给接口
            }
            // 根据 result 处理请求结果
            when (result) {
                is Result.Success -> {
                    Log.d("GetLanWays", "$result")
                    when(result.data.code){
                        0->{
                            DataManager.cLearMMKV("lanway")
                            ToastUtils.showShort("获取成功")
                            lanwaylist.clear()
                            lanwaylist.addAll(result.data.data)
                            lanWayAdapter.notifyDataSetChanged()
                            val jsonString = Gson().toJson(lanwaylist)
                            DataManager.setDataString("lanway", jsonString)
                        }
                        5->{
                            ToastUtils.showLong(result.data.info)
                            DataManager.clearAllMMKV()
                            ActivityUtils.finishAllActivitiesExceptNewest()
                            val intent = Intent()
                            intent.setClass(requireActivity(), WelcomeActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        }
                        else->{
                            ToastUtils.showLong(result.data.info)
                        }
                    }
                }

                is Result.Error -> {
                    ToastUtils.showLong("获取失败: ${result.exception.message}")
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
            dismissLoadingDialog()

        }
    }

    private fun initAdapter() {
        lanWayAdapter = LanWayAdapter()
        binding.lanwayRecy.layoutManager = LinearLayoutManager(context)
        binding.lanwayRecy.adapter = lanWayAdapter
        lanWayAdapter.submitList(lanwaylist)
        lanWayAdapter.setOnItemClickListener(BaseQuickAdapter.OnItemClickListener { _, _, position ->
            binding.xdNameText.text = lanwaylist[position].laneName
            binding.areaText.text = lanwaylist[position].areaValue
            infoid = lanwaylist[position].id.toString()
            hideViewWithObjectAnimator(binding.bottomLayout)
            if (!binding.areaText.text.isNullOrEmpty() && !binding.fsText.text.isNullOrEmpty()) {
                val airNum = BigDecimal(
                    binding.areaText.text.toString()
                        .toDoubleOrNull()!! * binding.fsText.text.toString().toDoubleOrNull()!!
                ).setScale(
                    3,
                    RoundingMode.HALF_UP
                ).toDouble()
                binding.airText.text = airNum.toString()
            }
        })
    }

    private fun initDialog(str: String, context: Context) {
        val inflater = LayoutInflater.from(context)
        val binding = ErrorDialogBinding.inflate(inflater)
        binding.errorText.text = str
        errordialog =
            AlertDialog.Builder(context).setView(binding.root).setCancelable(false).create()
    }

    private fun initView(view: View) {
        startAnim()
        Glide.with(view).load(R.drawable.loading2_img).into(binding.loadingImg)
        binding.submitBtnImg.isEnabled = false
        binding.submitBtnImg.setOnClickListener {
            if (binding.xdNameText.text.isNullOrEmpty()) {
                showDialog()
                return@setOnClickListener
            }
            if (binding.airText.text.isNullOrEmpty()) {
                ToastUtils.showLong("请进行风量测量")
                return@setOnClickListener
            }
            upDataBean = UpDataBean(
                binding.ch4Text.text.toString(),
                times,
                binding.coText.text.toString(),
                binding.wdText.text.toString(),
                binding.sdText.text.toString(),
                infoid,
                binding.o2Text.text.toString(),
                binding.qyText.text.toString(),
                binding.fsText.text.toString(),
                binding.airText.text.toString(),
                binding.areaText.text.toString()
            )
            setData(upDataBean)
        }
        binding.mmBtn.setOnClickListener {
            if (dataStr.isNullOrEmpty() || !dataState) {
                ToastUtils.showShort("没有获取数据...")
                return@setOnClickListener
            }
//            if (!isCollectingData) {
//                startDataCollection()
            setDataStr(dataStr)
//                binding.mmBtn.isEnabled = false
//            }
//            binding.xdNameText.text = ""
            binding.loadingRelat.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                // 更改控件内容
                binding.loadingOkImg.visibility = View.VISIBLE
                binding.loadingLayout.visibility = View.GONE
                // 显示1秒后消失
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.loadingRelat.visibility = View.GONE
                    binding.loadingOkImg.visibility = View.GONE
                    binding.loadingLayout.visibility = View.VISIBLE
                }, 1000)  // 延迟1秒
            }, (timeInt * 1000).toLong())  // 延迟1秒
        }
        binding.xdNameText.setOnClickListener {
            showViewWithObjectAnimator(binding.bottomLayout)
            // 禁用滑动
//            sharedViewModel.setSwipeDisabled(true)
        }
        binding.closeImg.setOnClickListener {
            hideViewWithObjectAnimator(binding.bottomLayout)
            // 恢复滑动
//            sharedViewModel.setSwipeDisabled(false)
        }
        binding.refreshLayout.setOnRefreshListener {
            getLanWay()
            binding.refreshLayout.finishRefresh(1000)
        }
        binding.addAirLayout.setOnClickListener {
            airDialog()
        }
    }

    //计算风量的dialog
    private fun airDialog() {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_air, null)
        val editText: EditText = view.findViewById(R.id.area_edit)
        val okBtn: TextView = view.findViewById(R.id.ok_btn)
        val cancelBtn: TextView = view.findViewById(R.id.cancel_btn)
        // 创建 AlertDialog
        val airDialog = AlertDialog.Builder(requireActivity())
            .setView(view) // 设置自定义布局
            .setCancelable(false) // 点击外部区域不可取消
            .create()
        // 显示 Dialog
        airDialog.show()
        val layoutParams = airDialog.window?.attributes
        layoutParams?.width =
            (resources.displayMetrics.widthPixels * 0.8).toInt() // 设置 Dialog 宽度为屏幕宽度的 60%
        airDialog.window?.attributes = layoutParams
        cancelBtn.setOnClickListener {
            airDialog.dismiss()
        }
        okBtn.setOnClickListener {
            if (editText.text.isNullOrEmpty()) {
                ToastUtils.showShort("请输入横截面积")
            }
            val areaStr = editText.text.toString()
            val doubleValue = binding.fsText.text.toString().toDoubleOrNull()
            if (doubleValue != null) {
                val airNum = BigDecimal(areaStr.toDoubleOrNull()!! * doubleValue).setScale(
                    3,
                    RoundingMode.HALF_UP
                ).toDouble()
                binding.airText.text = "$airNum"
                println("转换成功: $airNum")
            }
            binding.areaText.text = areaStr
            airDialog.dismiss()
        }
    }


    private fun showDialog() {
        errordialog.show()
        val layoutParams = errordialog.window?.attributes
        layoutParams?.width =
            (resources.displayMetrics.widthPixels * 0.6).toInt() // 设置 Dialog 宽度为屏幕宽度的 80%
        this.errordialog.window?.attributes = layoutParams
        GlobalScope.launch(Dispatchers.Main) {
            delay(1000)
            if (errordialog.isShowing) {
                errordialog.dismiss()
            }
        }
//        Handler().postDelayed({
//            if (errordialog.isShowing) {
//                errordialog.dismiss()
//            }
//        }, 1000)
    }

    private fun setData(upDataBean: UpDataBean) {
        val list = mutableListOf<UpDataBean>()
        list.add(upDataBean)
        val jsonString = Gson().toJson(list)
        val times = (System.currentTimeMillis() / 1000).toString()
        if (NetworkMonitor.isWiFiConnectedAndAvailable()) {//isNetworkConnected()
            // 如果有网络，立即上传数据
            uploadData(jsonString, times)
        } else {
            showLoadingDialog(requireActivity(), "未连接网络", "数据正在缓存...")
            // 如果没有网络，保存数据到本地
            DataUtils.saveLocationData(jsonString, 0, binding.xdNameText.text.toString(), "")
            Handler(Looper.getMainLooper()).postDelayed({
                // 更改控件内容
                dismissLoadingDialog()
                ToastUtils.showShort("缓存成功，请在传输列表查看")
                setNullText()
            }, 1500)  // 延迟1.5秒
        }
    }

    private fun setNullText() {
        binding.fsText.text = ""
        binding.qyText.text = ""
        binding.wdText.text = ""
        binding.ch4Text.text = ""
        binding.sdText.text = ""
        binding.o2Text.text = ""
        binding.coText.text = ""
        binding.airText.text = ""
        binding.xdNameText.text = ""
        binding.areaText.text = ""
        binding.submitBtnImg.isEnabled = false
        Glide.with(this).load(R.drawable.no_click).into(binding.submitBtnImg)
    }


    private fun startAnim() {
        rotateAnim = ObjectAnimator.ofFloat(binding.circleImg, "rotationAngle", 0f, 360f)
        rotateAnim.duration = 2000 // 设置动画持续时间为2秒
        rotateAnim.repeatCount = ObjectAnimator.INFINITE // 无限循环
        rotateAnim.repeatMode = ObjectAnimator.RESTART // 设置动画平滑循环
        // 添加插值器，确保动画更平滑
        rotateAnim.interpolator = null // 去掉插值器，可以让动画更加平滑
        rotateAnim.start()
    }


    private fun parseStringToObject(input: String): ScanData {
        val values = input.split(",") // 用 , 分割字符串
            .map { it.split(":")[1].trim() } // 提取 : 后的值
        return ScanData(
            fs = values[0].toDouble(),
            wd = values[1].toDouble(),
            sd = values[2].toDouble(),
            qy = values[3].toDouble(),
            ch4 = values[4].toDouble(),
            co = values[5].toDouble(),
            o2 = values[6].toDouble(),
            pjfs = values[7].toDouble()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 停止动画
        rotateAnim.cancel() // 取消动画并释放资源
    }

    // 显示视图（VISIBLE）
    fun showViewWithObjectAnimator(view: View) {
        val fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f) // 透明度动画
        val slideIn =
            ObjectAnimator.ofFloat(view, "translationY", view.height.toFloat(), 0f) // 平移动画

        // 并行执行两个动画
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(fadeIn, slideIn)
        animatorSet.duration = 300 // 设置动画持续时间
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                view.visibility = View.VISIBLE // 动画开始时，确保视图可见
            }
        })
        animatorSet.start()
    }

    // 隐藏视图（GONE）
    private fun hideViewWithObjectAnimator(view: View) {
        val fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f) // 透明度动画
        val slideOut =
            ObjectAnimator.ofFloat(view, "translationY", 0f, view.height.toFloat()) // 平移动画

        // 并行执行两个动画
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(fadeOut, slideOut)
        animatorSet.duration = 300 // 设置动画持续时间
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view.visibility = View.GONE // 动画结束后，设置视图为 GONE
            }
        })
        animatorSet.start()
    }

    // 上传数据
    private fun uploadData(data: String, time: String) {
        CoroutineScope(Dispatchers.Main).launch {
            upData(data, time)
        }
    }

    private suspend fun upData(data: String, time: String) {
        showLoadingDialog(requireActivity(), "上传数据...", "")
        try {
            val jsontext = Gson().toJson(UpDataBody(data))
            val body = CommonRequestBody(jsontext)
            // 通过 RetrofitClient.makeRequest 统一处理请求
            val result = RetrofitClient.makeRequest {
                RetrofitClient.apiService.updata(body)// 将请求体传递给接口
            }
            // 根据 result 处理请求结果
            when (result) {
                is Result.Success -> {
                    when (result.data.code) {
                        0 -> {
                            ToastUtils.showShort("上传成功")
                            DataUtils.saveLocationData(
                                data,
                                2,
                                binding.xdNameText.text.toString(),
                                time
                            )
                            setNullText()
                        }
                        5 -> {
                            ToastUtils.showLong(result.data.info)
                            DataManager.clearAllMMKV()
                            ActivityUtils.finishAllActivitiesExceptNewest()
                            val intent = Intent(requireActivity(), WelcomeActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        }
                        else -> {
                            ToastUtils.showLong(result.data.info)
                        }
                    }
                }

                is Result.Error -> {
                    ToastUtils.showLong("上传失败: ${result.exception.message}")
                    DataUtils.saveLocationData(data, 0, binding.xdNameText.text.toString(), time)
                }
            }
        } catch (e: ConnectException) {
            // 网络异常处理
            ToastUtils.showLong("网络异常,请稍后再试")
            Log.e("LoginError", "网络异常: ${e.message}")
            DataUtils.saveLocationData(data, 0, binding.xdNameText.text.toString(), time)
            Handler(Looper.getMainLooper()).postDelayed({//网络异常时，5秒后重新调用接口
                // 更改控件内容
                uploadData(data, time)
            }, 5000)  // 延迟5秒
        } catch (e: Exception) {
            // 其他异常处理
            ToastUtils.showLong("发生异常: ${e.message}")
            Log.e("LoginError", "发生异常: ${e.message}")

        } finally {
            // 不管成功与否，都关闭加载动画
            dismissLoadingDialog()
        }
    }


    override fun onNetworkAvailable() {
        println("全局回调：网络已连接")
        backgroundUpData()
    }

    //当网络连接时静默调用上传的方法
    private fun backgroundUpData() {
        if (DataUtils.getUpData().isNotEmpty()) {
            val str = DataUtils.getUpData()
            Log.d("onNetworkAvailable", str)
            if (str.isNotEmpty()) {
                uploadDataExample(str)
            }
        }
    }

    override fun onNetworkLost() {
        println("全局回调：网络已断开")
    }

    // 需要上传数据的地方
    private fun uploadDataExample(data: String) {
        val data = data
        // 调用公共方法进行上传，并传入回调处理结果
        UploadDataHelper.uploadData(data) { success, message, code ->
            if (success) {
                if (code == 0) {
                    Log.d("uploadDataExample", "上传成功:$message")
                    val times = (System.currentTimeMillis() / 1000).toString()
                    DataUtils.setDataState(2, times)
                    val event = EventMessage(true)
                    EventBus.getDefault().post(event)
                } else if (code == 5) {
                    ToastUtils.showLong(message)
                    DataManager.clearAllMMKV()
                    ActivityUtils.finishAllActivitiesExceptNewest()
                    val intent = Intent()
                    intent.setClass(requireActivity(), WelcomeActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        //重新调用接口
                        backgroundUpData()
                    }, 5000)  // 延迟5秒
                    Log.d("UploadDataHelper", message)
                }
            } else {
                Log.d("uploadDataExample", "上传失败:" + message)
            }
        }
    }

    //循环获取推送的数据，间隔时间500毫秒
    private fun startDataCollection() {
        val startTime = System.currentTimeMillis() // 记录开始时间
        val endTime = startTime + timeInt * 1000 // 结束时间，60秒后

        // 用于后台线程收集数据
        val runnable = object : Runnable {
            override fun run() {
                if (System.currentTimeMillis() < endTime) {
                    // 获取最新的 dataStr 值并进行处理
                    val simulatedData = dataStr
                    // 检查 dataStr 是否为空或无效，避免无效数据影响
                    if (simulatedData.isNotBlank()) {
                        Log.d("simulatedData", simulatedData)
                        val firstValue = parseFirstValue(dataStr)
                        if (firstValue != null) {
                            dataList.add(firstValue)
                        } else {
                            Log.w("DataCollection", "解析数据失败，跳过当前循环")
                        }
//                        val dataMap = parseData(simulatedData)
//                        dataList.add(dataMap)
                    } else {
                        Log.w("DataCollection", "接收到空数据，跳过当前循环")
                    }
                    // 每500毫秒继续执行，减少延迟更频繁地更新
                    handler.postDelayed(this, 500)
                } else {
                    // 测量时间结束后，计算并显示结果
//                    calculateAndDisplayAverage()
                    processCollectedData()
                    isCollectingData = false
                    binding.mmBtn.isEnabled = true
                }
            }
        }
        // 启动数据收集
        isCollectingData = true
        handler.post(runnable)
    }

    // 解析字符串数据并返回Map
    private fun parseData(data: String): Map<String, Double> {
        val map = mutableMapOf<String, Double>()
        // 分割字符串为键值对数组
        val keyValuePairs = data.split(",")
        for (pair in keyValuePairs) {
            val parts = pair.split(":")
            if (parts.size == 2) {
                val key = parts[0]
                val value = parts[1].toDoubleOrNull() ?: 0.0
                map[key] = value
            }
        }
        return map
    }


    // 计算并显示每个键的平均值保留小数点两位 四舍五入
//    private fun calculateAndDisplayAverage() {
//        // 计算数据的平均值并显示
//        if (dataList.isNotEmpty()) {
//            val averages = mutableMapOf<String, Double>()
//            // 计算每个字段的平均值
//            for (i in 1..8) {
//                var total = 0.0
//                var count = 0
//                for (dataMap in dataList) {
//                    val key = "$i"
//                    dataMap[key]?.let {
//                        total += it
//                        count++
//                    }
//                }
//                if (count > 0) {
//                    averages["$i"] = total / count
//                }
//            }
//            // 将结果格式化为原始数据的形式
//            val resultText = StringBuilder()
//            for (i in 1..8) {
//                val avg = averages["$i"] ?: 0.0
//                // 使用 String.format 保留两位小数并四舍五入
//                resultText.append("$i:${"%.2f".format(avg)}")
//                if (i < 8) resultText.append(",")  // 除了最后一个元素，其他元素后面加逗号
//            }
//            setDataStr(resultText.toString())
//            // 输出结果
//            Log.d("resultText", resultText.toString())
//            ToastUtils.showLong("数据收集完成，平均值：${resultText.toString()}")
//        } else {
//            Log.w("DataCollection", "没有有效数据进行平均计算")
//        }
//    }

    //计算平均值 精确到小数点后十位
//    private fun calculateAndDisplayAverage() {
//        // 计算数据的平均值并显示
//        if (dataList.isNotEmpty()) {
//            val averages = mutableMapOf<String, BigDecimal>()
//
//            // 计算每个字段的平均值
//            for (i in 1..8) {
//                var total = BigDecimal.ZERO
//                var count = 0
//                for (dataMap in dataList) {
//                    val key = "$i"
//                    dataMap[key]?.let {
//                        total = total.add(BigDecimal(it))
//                        count++
//                    }
//                }
//
//                if (count > 0) {
//                    // 计算平均值并保留更多的小数位
//                    averages["$i"] = total.divide(BigDecimal(count), 10, RoundingMode.HALF_UP) // 保留10位小数
//                }
//            }
//            // 将结果格式化为原始数据的形式
//            val resultText = StringBuilder()
//            for (i in 1..8) {
//                val avg = averages["$i"] ?: BigDecimal.ZERO
//                resultText.append("$i:${avg.stripTrailingZeros().toPlainString()}") // 移除尾部零，保留有效数字
//                if (i < 8) resultText.append(",")  // 除了最后一个元素，其他元素后面加逗号
//            }
//
//            // 输出结果
//            Log.d("resultText", resultText.toString())
//            ToastUtils.showLong("数据收集完成，平均值：${resultText.toString()}")
//        } else {
//            Log.w("DataCollection", "没有有效数据进行平均计算")
//        }
//    }

    //将计算的平均值显示到界面
    @SuppressLint("SetTextI18n")
    private fun setDataStr(data: String) {
        binding.fsText.text = parseStringToObject(data).pjfs.toString()
        binding.qyText.text = parseStringToObject(data).qy.toString()
        binding.wdText.text = parseStringToObject(data).wd.toString()
        binding.ch4Text.text = parseStringToObject(data).ch4.toString()
        binding.sdText.text = parseStringToObject(data).sd.toString()
        binding.o2Text.text = parseStringToObject(data).o2.toString()
        binding.coText.text = parseStringToObject(data).co.toString()
        if (binding.areaText.text.toString().isNotEmpty()) {
            val airNum =
                binding.fsText.text.toString().toDouble() * binding.areaText.text.toString()
                    .toDouble()
            val num = BigDecimal(airNum).setScale(
                3,
                RoundingMode.HALF_UP
            ).toDouble()
            binding.airText.text = num.toString()
        }
        val time = System.currentTimeMillis()
        times = (time / 1000).toString()//监测时间
        binding.jcTimeText.text = "监测时间:${DateUtils.convertMillisToDate(time)}"
        binding.submitBtnImg.isEnabled = true
        Glide.with(requireActivity()).load(R.drawable.main_submit_btn_img)
            .into(binding.submitBtnImg)
    }

    // 解析字符串数据的第一个值
    private fun parseFirstValue(data: String): Double? {
        val firstPair = data.split(",").firstOrNull() ?: return null
        return firstPair.split(":").getOrNull(1)?.toDoubleOrNull()
    }

    // 计算第一个值的平均值，并替换最后一个字符串中的第一个值
    private fun processCollectedData() {
        if (dataList.isNotEmpty()) {
            val average = dataList.average() // 计算平均值

            // 获取时间段内接收到的最后一条数据
            val lastData = dataStr

            // 替换最后一条数据中的第一个值为平均值
            val modifiedData = replaceFirstValueWithAverage(lastData, average)
            setDataStr(modifiedData)
            // 输出结果并显示
            Log.d("ModifiedData", modifiedData)
            ToastUtils.showLong("数据收集完成，结果：$modifiedData")
        } else {
            Log.w("DataCollection", "没有有效数据进行处理")
        }
    }

    // 替换字符串中的第一个值为计算的平均值
    private fun replaceFirstValueWithAverage(data: String, average: Double): String {
        val keyValuePairs = data.split(",")
        val modifiedPairs = keyValuePairs.toMutableList()
        if (modifiedPairs.isNotEmpty()) {
            modifiedPairs[0] = "1:${"%.2f".format(average)}" // 替换第一个键值对的值
        }
        return modifiedPairs.joinToString(",")
    }
}