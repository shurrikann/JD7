package com.shurrikann.jd7.utils

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shurrikann.jd7.bean.DataManager
import com.shurrikann.jd7.bean.LocationData
import com.shurrikann.jd7.bean.UpDataBean

object DataUtils {
    private var loclist = mutableListOf<LocationData>()
    private val longTime = 24 * 60 * 60
    fun saveLocationData(data: String, state: Int, name: String, time: String) {
        var uplist = upJsonToList(data)
        val str = DataManager.readDataString("data")
        if (!str.isNullOrEmpty()) {
            loclist.addAll(locationJsonToList(str))
            Log.d("saveLocationData", "1:$loclist.size")
        }
        for (item in uplist) {
            val locationData = LocationData(
                item.ch4Value,
                item.cjTime,
                item.coValue,
                item.heatValue,
                item.humValue,
                item.infoId,
                item.o2Value,
                item.presValue,
                item.speedValue,
                item.quantityValue,
                item.areaValue,
                state,
                name,
                time
            )
            loclist.add(locationData)
        }
        Log.d("saveLocationData", "2:$loclist.size")
        DataManager.saveDataString("data", Gson().toJson(loclist))
        loclist.clear()
    }


    fun upJsonToList(jsonString: String): List<UpDataBean> {
        val type = object : TypeToken<List<UpDataBean>>() {}.type
        return Gson().fromJson(jsonString, type)
    }

    fun locationJsonToList(jsonString: String): List<LocationData> {
        val type = object : TypeToken<List<LocationData>>() {}.type
        return Gson().fromJson(jsonString, type)
    }

    fun getUpData(): String {
        val lstr = DataManager.readDataString("data")
        val llist = mutableListOf<LocationData>()
        if (!lstr.isNullOrEmpty()) {
            llist.addAll(locationJsonToList(lstr!!))
            val ulist = mutableListOf<UpDataBean>()
            for (item in llist) {
                if (item.state == 0) {
                    val upDataBean = UpDataBean(
                        item.ch4Value,
                        item.cjTime,
                        item.coValue,
                        item.heatValue,
                        item.humValue,
                        item.infoId,
                        item.o2Value,
                        item.presValue,
                        item.speedValue,
                        item.quantityValue,
                        item.areaValue
                    )
                    ulist.add(upDataBean)
                }
            }
            if (ulist.size > 0) {
                val ustr = jsonToString(ulist)
                return ustr
            } else {
                return ""
            }
        }
        return ""
    }

    fun <T> jsonToString(list: List<T>): String {
        val json = Gson().toJson(list)
        return json
    }


    fun setDataState(state: Int, time: String) {
        val lstr = DataManager.readDataString("data")
        if (!lstr.isNullOrEmpty()) {
            val list = locationJsonToList(lstr)
            Log.d("saveLocationData", "1:${list.size}")
            for (item in list) {
                if(item.state==0){
                    item.state = state
                    item.upTime = time
                }
            }
            val str = jsonToString(list)
            DataManager.saveDataString("data", str)
        }
    }

    fun judgmentDate(time: Long): Boolean {
        val times = System.currentTimeMillis() / 1000//当前时间
        return (time + longTime) >= times
    }
}