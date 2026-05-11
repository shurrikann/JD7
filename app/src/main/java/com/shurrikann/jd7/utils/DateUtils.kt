package com.shurrikann.jd7.utils

import android.annotation.SuppressLint
import android.os.Build
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object DateUtils {
    @SuppressLint("NewApi")
    fun convertMillisToDate(millis: Long): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0 及以上使用 java.time.Instant
            val instant = Instant.ofEpochMilli(millis)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
            formatter.format(instant)
        } else {
            // Android 8.0 以下使用 java.util.Date
            val date = Date(millis)
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            format.format(date)
        }
//        val instant = Instant.ofEpochMilli(millis)  // 将毫秒值转换为 Instant 对象
//        val dateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()  // 转换为本地时间（指定时区）
//        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")  // 设置日期时间格式
//        return dateTime.format(formatter)  // 格式化并返回
    }


    @SuppressLint("NewApi")
    fun getLocationDate(): String {
        val currentDateTime = LocalDateTime.now() // 获取当前日期和时间
        val uptime = currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        return uptime
    }
}