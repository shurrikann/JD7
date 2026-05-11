package com.shurrikann.jd7.base

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.shurrikann.jd7.R

abstract class BaseFragment : Fragment() {
    private var loadingDialog: AlertDialog? = null
    private var _binding: ViewBinding? = null
    open val binding get() = _binding!!

    // 绑定视图
    open fun bindView(binding: ViewBinding) {
        _binding = binding
    }

    /**
     * 显示加载中的对话框
     * @param context 上下文
     * @param message 可选的加载文本
     */
    fun showLoadingDialog(context: Context, message: String, message2: String) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.loading_dialog, null)
        val loadingMessage: TextView = view.findViewById(R.id.message_text)
        val loadingMessage2: TextView = view.findViewById(R.id.message2_text)
        if (message2.isNullOrEmpty()) {
            loadingMessage2.visibility = View.GONE
        }
        loadingMessage.text = message // 设置加载文本
        loadingMessage2.text = message2
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