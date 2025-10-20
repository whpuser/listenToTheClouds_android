package com.example.listen_to_the_clouds.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.listen_to_the_clouds.data.model.HomePlaylist
import com.example.listen_to_the_clouds.data.model.HomeSong
import com.example.listen_to_the_clouds.data.network.DEFAULT_PAGING
import com.example.listen_to_the_clouds.data.network.RetrofitClient.apiService
import com.example.listen_to_the_clouds.data.paging.PlaylistPagingSource
import com.example.listen_to_the_clouds.data.paging.SongPagingSource
import kotlinx.coroutines.flow.Flow

class HomeViewModel : ViewModel() {

    //分页获取歌单
    val playlistsFlow: Flow<PagingData<HomePlaylist>> = Pager(
        config = PagingConfig(
            pageSize = DEFAULT_PAGING,
            enablePlaceholders = false,
            initialLoadSize = DEFAULT_PAGING
        ),
        pagingSourceFactory = { PlaylistPagingSource(
            pageSize = DEFAULT_PAGING,
            apiService = apiService) }
    ).flow.cachedIn(viewModelScope)

    //分页获取歌曲
    val songsFlow: Flow<PagingData<HomeSong>> = Pager(
        config = PagingConfig(
            pageSize = DEFAULT_PAGING,
            enablePlaceholders = false,
            initialLoadSize = DEFAULT_PAGING
        ),
        pagingSourceFactory = { SongPagingSource(
            pageSize = DEFAULT_PAGING,
            apiService = apiService) }
    ).flow.cachedIn(viewModelScope)
}