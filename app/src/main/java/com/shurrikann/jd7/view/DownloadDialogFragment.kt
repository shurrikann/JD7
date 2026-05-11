package com.shurrikann.jd7.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.activity.result.contract.ActivityResultContracts
import com.blankj.utilcode.util.ToastUtils
import com.shurrikann.jd7.R
import java.io.File

class DownloadDialogFragment(private val url: String) : DialogFragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private val installPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 权限被授予，继续安装 APK
            startInstallation()
        } else {
            ToastUtils.showShort("安装权限未授予，请手动开启")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.dialog_download, container, false)

        progressBar = rootView.findViewById(R.id.progress_bar)
        progressText = rootView.findViewById(R.id.download_progress)

        return rootView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setCanceledOnTouchOutside(false)  // 禁止点击外部关闭
        return dialog
    }

    @SuppressLint("Range")
    override fun onStart() {
        super.onStart()

        val downloadDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val apkFile = File(downloadDir, "Luan.apk")
        if (apkFile.exists()) {
            apkFile.delete()
        }

        val request = android.app.DownloadManager.Request(Uri.parse(url))
            .setTitle("下载应用")
            .setDescription("正在下载...")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Luan.apk")
            .setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_WIFI or android.app.DownloadManager.Request.NETWORK_MOBILE)

        val downloadManager =
            context?.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
        val downloadId = downloadManager.enqueue(request)

        // 启动线程监控下载进度
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
                            activity?.runOnUiThread {
                                progressBar.progress = progress
                                progressText.text = "下载进度：$progress%"
                            }
                        }

                        if (bytesDownloaded == bytesTotal) {
                            downloading = false
                            close()
                            activity?.runOnUiThread {
                                dismiss() // 下载完成后关闭对话框
                                requestInstallPermission()
                            }
                        }
                    }
                }
            }
        }.start()
    }

    private fun requestInstallPermission() {
        // 提示用户安装权限等操作
        // 可以在此方法中处理安装请求
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 检查是否已经获得安装未知来源的权限
            val canInstall = requireActivity().packageManager.canRequestPackageInstalls()
            if (canInstall) {
                // 权限已经授予，直接启动安装
                installAPK()
            } else {
                // 未授予权限，请求用户授予权限
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                intent.data = Uri.parse("package:${requireActivity().packageName}")
                installPermissionLauncher.launch(intent) // 使用ActivityResultContracts处理权限请求
            }
        } else {
            // 在 Android 8.0 以下的版本不需要此权限，直接启动安装
            installAPK()
        }
    }

    private fun startInstallation() {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val apkFile = File(downloadDir, "Luan.apk")
        Log.d("DownloadDialog", "APK File Path: ${apkFile.absolutePath}")
        if (apkFile.exists()) {
            val uri = FileProvider.getUriForFile(requireContext(), "com.shurrikann.jd7.fileprovider", apkFile)
            // 启动 APK 安装
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                data = uri
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)  // Grant URI permission to the installer
            }

            // 启动安装界面
            startActivity(installIntent)
        } else {
            ToastUtils.showShort("安装文件不存在")
        }
    }
    fun installAPK() {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "Luan.apk"
        )
        // 打印文件路径，用于调试
        Log.d("InstallAPK", "APK file path: ${file.absolutePath}")
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(
                requireActivity(),
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
            ToastUtils.showShort("安装文件不存在")
        }
    }

}
