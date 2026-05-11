package com.shurrikann.jd7.bean

import com.google.gson.annotations.SerializedName

data class upAppBean(
    var msg: String,
    var code: Int,
    @SerializedName("data") var data: UpAppData,
    var info: String
)


data class UpAppData(
    var verContent: String,
    var id: Int,
    var verUrl: String,
    var verCode: Int
)
