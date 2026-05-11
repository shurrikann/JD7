package com.shurrikann.jd7.utils

import com.google.gson.Gson

object JsonUtils {
    // 创建一个通用的函数，根据传入的对象动态生成 JSON 字符串
    fun <T> createJsonRequestBody(obj: T): String {
        val gson = Gson()
        return gson.toJson(obj)
    }
}