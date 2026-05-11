package com.shurrikann.jd7.bean

import com.google.gson.annotations.SerializedName

data class HistoryData(
    val code: Int,
    val count: Int,
    @SerializedName("data") val data: List<HData>,
    val msg: String,
    val info:String
)

data class HData(
    val isCj: Int,              // 是否采集
    val ch4Value: Double,   // CH4 值
    val o2Value: Double,     // O2 值
    val speedValue: Double,// 速度值
    val cjView: String,       // 采集视图
    val infoId: Int,          // 信息 ID
    val dataStateStr: String, // 数据状态描述
    val humValue: Double,   // 湿度值
    val userView: String,   // 用户视图
    val presValue: Double, // 压力值
    val laneView: String,   // 巷道视图
    val id: Int,                  // 数据 ID
    val heatValue: Double, // 热值
    val laneInnerId: String, // 巷道内部 ID
    val cjTimeStr: String, // 采集时间字符串
    val co2Value: Double,   // CO2 值
    val dataState: Int,    // 数据状态
    val laneNumber: Int,  // 巷道编号
    val curDateStr: String, // 当前日期字符串
    val upTime: Long,         // 上传时间戳
    val laneName: String,   // 巷道名称
    val upTimeStr: String, // 上传时间字符串
    val baseAddr: String,   // 基站地址
    val coValue: Double,     // CO 值
    val cjTime: Long,         // 采集时间戳
    val position: String,    // 位置
    val baseFlg: String,      // 基站标识
    val quantityValue:String, //风量
    val areaValue:String //横截面积
)