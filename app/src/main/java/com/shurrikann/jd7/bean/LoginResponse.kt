package com.shurrikann.jd7.bean

import com.google.gson.annotations.SerializedName


data class LoginResponse(
    val code: Int,        // 响应码
    @SerializedName("data") val data: LoginData,       // 包含数据的部分
    val info: String
)

data class LoginData(
    val curSysUser: CurSysUser   // 当前用户信息
)

data class CurSysUser(
    val password: String,            // 密码
    val passwordTime: Long,          // 密码时间
    val app_token: String?,          // 应用 Token
    val name: String?,               // 用户名
    val tokenTime: Long,             // Token 时间
    val uniqid: String,              // 用户唯一标识
    val id: Int,                     // 用户 ID
    val role_list_name: String,      // 角色列表名称
    val roleList: List<Role>,        // 角色列表
    val app_token_time: Long?,       // 应用 Token 时间
    val token: String,               // Token
    val passwordOutMsg: String       // 密码有效提示
)

data class Role(
    val organizationId: Int,         // 组织 ID
    val roleId: Int,                 // 角色 ID
    val roleName: String,            // 角色名称
    val uniqid: String,              // 用户唯一标识
    val id: Int,                     // 角色 ID
    val userId: Int,                 // 用户 ID
    val isRegister: Int              // 是否注册
)