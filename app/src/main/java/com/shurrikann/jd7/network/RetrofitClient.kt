package com.shurrikann.jd7.network

import android.util.Log
import com.shurrikann.jd7.bean.DataManager
import com.shurrikann.jd7.myinterface.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://xxxx.xx.xxx.xx:xxxx/gas-web/"
//    private const val BASE_URL = "\"
    // 假设 token 存储在某个地方，比如 SharedPreferences 或 MMKV
    private fun getToken(): String {
        // 从 SharedPreferences/MMKV 等获取 token
        return DataManager.getDataString("token")
    }

    // 配置 OkHttpClient，确保支持清晰的 HTTP 流量
    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor { chain ->
            val originalRequest: Request = chain.request()
            // 获取 token
            val token = getToken()

            // 构建一个新的 URL，添加 token
            val newUrl: HttpUrl = originalRequest.url.newBuilder()
                .addQueryParameter("token", token) // 在 URL 后添加 token 参数
                .build()

            // 创建一个新的请求，附加上新的 URL
            val newRequest = originalRequest.newBuilder()
                .url(newUrl)
                .build()

            chain.proceed(newRequest)
        }
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    // 统一的请求处理 - 使用 suspend 函数，避免手动处理 Call
    suspend fun <T> makeRequest(request: suspend () -> T): Result<T> {
        return try {
            val response = withContext(Dispatchers.IO) { request() }
            Log.d("response","$response")
            Result.Success(response)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // 直接获取 Response
    suspend fun <T> getResponse(request: suspend () -> retrofit2.Response<T>): retrofit2.Response<T> {
        return try {
            // 获取原始的 Response 对象
            val response = withContext(Dispatchers.IO) { request() }
            response
        } catch (e: Exception) {
            throw e  // 直接抛出异常，Retrofit 会自动处理
        }
    }
}