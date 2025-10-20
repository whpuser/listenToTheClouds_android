package com.example.listen_to_the_clouds.data.model

data class HomePlaylist(
    val playlistId: Long,
    val userId: Long,
    val playlistType: Int,
    val playlistCover: String,
    val playlistTitle: String,
    val playlistFavorites: Int,
    val playlistTimes: Int
)