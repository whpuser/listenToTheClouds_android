package com.example.listen_to_the_clouds.data.network
//配置拦截器、全局获取数据、错误拦截等处理请求相关的问题

import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    //超时配置
    private const val CONNECT_TIMEOUT_SECONDS = 15L   // 连接服务器超时 15 秒
    private const val READ_TIMEOUT_SECONDS = 20L      // 读取服务器响应超时 20 秒
    private const val WRITE_TIMEOUT_SECONDS = 20L     // 上传请求体超时 20 秒

    //Gson 配置
    private val gson by lazy {
        GsonBuilder()
            .setLenient()      // 宽松模式：允许解析不严格符合 JSON 的格式
            .create()
    }

    //请求头拦截器
    private val headerInterceptor = Interceptor { chain ->
        val original: Request = chain.request()                                     // 获取原始请求
        val request = original.newBuilder()
            .header("Accept", "application/json")                       // 告诉服务端返回 JSON
            .header("Content-Type", "application/json; charset=UTF-8")  // 发送 JSON
            .build()
        chain.proceed(request)                                                       // 继续执行请求
    }

    //OkHttpClient
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS) // 连接超时
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)       // 读取超时
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)     // 写入超时
            .addInterceptor(headerInterceptor)                         // 添加拦截器
            .build()
    }

    //Base URL 处理
    private fun normalizedBaseUrl(): String {
        val base = REQUEST_ADDRESS
        return if (base.endsWith("/")) base else "$base/" // 保证 Retrofit baseUrl 以 / 结尾
    }

    //Retrofit 实例
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(normalizedBaseUrl())                           // 设置 Base URL
            .client(okHttpClient)                                   // 使用自定义 OkHttpClient
            .addConverterFactory(GsonConverterFactory.create(gson)) // Gson 转换器
            .build()
    }

    //API 接口实例
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}