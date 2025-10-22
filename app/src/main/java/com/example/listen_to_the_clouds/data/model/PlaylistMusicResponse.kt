package com.example.listen_to_the_clouds.data.model

data class PlaylistMusicResponse(
    val list: List<HomeSong>,
    val total: Int,
    val pageNum: Int,
    val pageSize: Int
)
