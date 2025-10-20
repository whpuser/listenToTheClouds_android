package com.example.listen_to_the_clouds.data.model

//分页数据接口
data class PaginationData<T>(
    val list: List<T>,
    val total: Int,
    val pageNum: Int,
    val pageSize: Int
)
