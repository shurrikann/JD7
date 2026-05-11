package com.shurrikann.jd7.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.viewbinding.ViewBinding
import com.shurrikann.jd7.R

abstract class BaseActivity : ComponentActivity() {
    private var _binding: ViewBinding? = null
    open val binding get() = _binding!!

    private var loadingDialog: AlertDialog? = null

    // 子类必须实现，指定布局资源 ID
    abstract fun getLayoutResId(): Int


    // 设置状态栏颜色，可被子类覆盖
    open fun getStatusBarColorResId(): Int {
        return android.R.color.transparent // 默认透明，可覆盖
    }

    // 可选覆盖：初始化子类逻辑
    open fun initialize() {}

    // 可选覆盖：配置窗口行为
    open fun configureWindow() {
        // 默认实现：侵入式状态栏
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
        window.statusBarColor = android.graphics.Color.TRANSPARENT // 设置状态栏为透明
    }

    protected abstract fun init(savedInstanceState: Bundle?)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 配置窗口
//        configureWindow()
        // 设置布局
        setContentView(getLayoutResId())
        // 子类逻辑
        initialize()
        init(savedInstanceState)
    }

    /**
     * 显示加载中的对话框
     * @param context 上下文
     * @param message 可选的加载文本
     */
    fun showLoadingDialog(context: Context, message: String) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.loading_dialog, null)
        val loadingMessage: TextView = view.findViewById(R.id.message_text)
        loadingMessage.text = message // 设置加载文本
        // 创建 AlertDialog
        loadingDialog = AlertDialog.Builder(context)
            .setView(view) // 设置自定义布局
            .setCancelable(false) // 点击外部区域不可取消
            .create()
        // 显示 Dialog
        loadingDialog?.show()
        val layoutParams = loadingDialog!!.window?.attributes
        layoutParams?.width =
            (resources.displayMetrics.widthPixels * 0.6).toInt() // 设置 Dialog 宽度为屏幕宽度的 60%
        loadingDialog!!.window?.attributes = layoutParams
    }


    /**
     * 关闭加载中的对话框
     */
    fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}