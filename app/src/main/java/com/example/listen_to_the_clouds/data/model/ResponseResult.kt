package com.example.listen_to_the_clouds.data.model
//响应结果
data class ResponseResult<T> (
    val code: Int,
    val message: String,
    val data: T? = null
)