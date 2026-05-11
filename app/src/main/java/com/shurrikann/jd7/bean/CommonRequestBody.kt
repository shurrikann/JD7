package com.shurrikann.jd7.bean

data class CommonRequestBody(val json_text: String)

data class AllRequestBody(val json_text: String, val token: String)

data class UpDataBody(val dataList: String)