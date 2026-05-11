package com.shurrikann.jd7.bean

data class UpDataBean(
    var ch4Value: String,//甲烷
    var cjTime: String,//监测时间
    var coValue: String,//一氧化碳
    var heatValue: String,//温度
    var humValue: String,//湿度
    var infoId: String,//id
    var o2Value: String,//氧气
    var presValue: String,//气压
    var speedValue: String,//风速
    var quantityValue:String,//风量
    var areaValue:String//横截面积
)
