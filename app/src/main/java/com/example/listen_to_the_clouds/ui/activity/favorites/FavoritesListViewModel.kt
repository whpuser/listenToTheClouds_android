package com.example.listen_to_the_clouds.ui.activity.favorites

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listen_to_the_clouds.data.model.FavoritesPagination
import com.example.listen_to_the_clouds.data.model.HomePlaylist
import com.example.listen_to_the_clouds.data.model.HomeSong
import com.example.listen_to_the_clouds.data.network.RetrofitClient.apiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoritesListViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isEmpty = MutableStateFlow(false)
    val isEmpty: StateFlow<Boolean> = _isEmpty.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _songs = MutableStateFlow<List<HomeSong>>(emptyList())
    val songs: StateFlow<List<HomeSong>> = _songs.asStateFlow()

    private val _playlists = MutableStateFlow<List<HomePlaylist>>(emptyList())
    val playlists: StateFlow<List<HomePlaylist>> = _playlists.asStateFlow()

    /**
     * 加载收藏的歌曲
     */
    fun loadFavoriteSongs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val pagination = FavoritesPagination(offset = 0, pageSize = 20)
                val response = apiService.setFavoritesSong(pagination)

                if (response.isSuccessful && response.body()?.code == 200) {
                    val data = response.body()?.data
                    val songList = data?.list ?: emptyList()
                    _songs.value = songList
                    _isEmpty.value = songList.isEmpty()
                    Log.d("FavoritesListViewModel", "Load favorite songs success: ${songList.size} songs")
                } else {
                    _errorMessage.value = response.body()?.message ?: "加载失败"
                    _isEmpty.value = true
                }
            } catch (e: Exception) {
                _errorMessage.value = "网络错误: ${e.message}"
                _isEmpty.value = true
                Log.e("FavoritesListViewModel", "Load favorite songs error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 加载收藏的歌单
     */
    fun loadFavoritePlaylists() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val pagination = FavoritesPagination(offset = 0, pageSize = 20)
                val response = apiService.getFavoritesPlaylist(pagination)

                if (response.isSuccessful && response.body()?.code == 200) {
                    val data = response.body()?.data
                    val playlistList = data?.list ?: emptyList()
                    _playlists.value = playlistList
                    _isEmpty.value = playlistList.isEmpty()
                    Log.d("FavoritesListViewModel", "Load favorite playlists success: ${playlistList.size} playlists")
                } else {
                    _errorMessage.value = response.body()?.message ?: "加载失败"
                    _isEmpty.value = true
                }
            } catch (e: Exception) {
                _errorMessage.value = "网络错误: ${e.message}"
                _isEmpty.value = true
                Log.e("FavoritesListViewModel", "Load favorite playlists error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 加载创建的歌单
     */
    fun loadCreatedPlaylists() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val pagination = FavoritesPagination(offset = 0, pageSize = 20)
                val response = apiService.getUserCreatedPlaylists(pagination)

                if (response.isSuccessful && response.body()?.code == 200) {
                    val data = response.body()?.data
                    val playlistList = data?.list ?: emptyList()
                    _playlists.value = playlistList
                    _isEmpty.value = playlistList.isEmpty()
                    Log.d("FavoritesListViewModel", "Load created playlists success: ${playlistList.size} playlists")
                } else {
                    _errorMessage.value = response.body()?.message ?: "加载失败"
                    _isEmpty.value = true
                }
            } catch (e: Exception) {
                _errorMessage.value = "网络错误: ${e.message}"
                _isEmpty.value = true
                Log.e("FavoritesListViewModel", "Load created playlists error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
