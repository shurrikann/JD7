package com.shurrikann.jd7.bean

import com.tencent.mmkv.MMKV

object DataManager {
    private val mmkv: MMKV by lazy {
        MMKV.defaultMMKV()
    }

    fun setDataString(key: String, value: String) {
        mmkv.encode(key, value)
    }

    fun getDataString(key: String): String {
        return mmkv.decodeString(key, "") ?: ""
    }

    fun setDataInt(key: String, value: Int) {
        mmkv.encode(key, value)
    }

    fun getDataInt(key: String): Int {
        return mmkv.decodeInt(key, -1)
    }

    fun setDataBool(key: String, value: Boolean) {
        mmkv.encode(key, value)
    }

    fun getDataBool(key: String): Boolean {
        return mmkv.decodeBool(key, false)
    }

    fun setDataLong(key: String, value: Long) {
        mmkv.encode(key, value)
    }

    fun getDataLong(key: String): Long {
        return mmkv.decodeLong(key, 0L)
    }

    fun clearAllMMKV() {
        mmkv.clearAll()
    }

    fun cLearMMKV(key: String) {
        mmkv.remove(key)
    }

    //用于保存需要上传的云端数据
    fun saveDataString(key: String, value: String) {
        mmkv.encode(key, value)
    }

    //获取保存的需要上传的数据
    fun readDataString(key: String): String? {
        return mmkv.decodeString(key, "")
    }

}