package com.example.listen_to_the_clouds.data.model

data class PlaylistDetails(
    val playlistId: Long,
    val userId: Long,
    val user: String,
    val avatar: String,
    val musicNumber: String,
    val playlistTitle: String,
    val playlistType: Int,
    val playlistCover: String,
    val playlistFavorites: Int,
    val playlistTimes: Int,
    val playlistShare: String,
    val createTime: String,
    val updated: String
)
