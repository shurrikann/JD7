package com.shurrikann.jd7.bean

import com.google.gson.annotations.SerializedName

data class LanWayResponse(
    val code: Int,
    @SerializedName("data") val data: List<LWData>,
    val info: String
)

data class LWData(
    val addrImg: String,
    val addrImgView: String,
    val baseAddr: String,
    val baseFlg: String,
    val cjView: String,
    val id: Int,
    val isCj: Int,
    val laneInnerId: String,
    val laneName: String,
    val laneNumber: Int,
    val laneView: String,
    val position: String,
    val userView: String,
    val areaValue: String
)
