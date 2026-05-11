package com.shurrikann.jd7.myinterface

import com.shurrikann.jd7.bean.AllRequestBody
import com.shurrikann.jd7.bean.CommonRequestBody
import com.shurrikann.jd7.bean.HistoryData
import com.shurrikann.jd7.bean.LanWayResponse
import com.shurrikann.jd7.bean.LoginResponse
import com.shurrikann.jd7.bean.UpDataResultBean
import com.shurrikann.jd7.bean.upAppBean
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    // 使用 POST 请求进行登录
    @POST("sys/phone_post_login")
    suspend fun login(@Body commonRequestBody: CommonRequestBody): LoginResponse

    //根据token获取检测点
    @POST("business/search_base_config")
    suspend fun searchlayway(@Body allRequestBody: AllRequestBody): LanWayResponse

    //根据检测点id或者查询日期获取历史数据
    @POST("business/search_base_page_data")
    suspend fun historydata(@Body allRequestBody: AllRequestBody): HistoryData

    //检测app更新
    @POST("no_check/check_app")
    suspend fun upappversion(@Body commonRequestBody: CommonRequestBody): upAppBean

    //上传数据
    @POST("business/upload_user_base_data")
    suspend fun updata(@Body commonRequestBody: CommonRequestBody): UpDataResultBean
}