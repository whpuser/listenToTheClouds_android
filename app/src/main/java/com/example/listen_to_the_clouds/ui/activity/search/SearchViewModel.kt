package com.example.listen_to_the_clouds.ui.activity.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listen_to_the_clouds.data.model.HomePlaylist
import com.example.listen_to_the_clouds.data.model.HomeSong
import com.example.listen_to_the_clouds.data.network.RetrofitClient
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    
    // 搜索歌曲结果
    private val _songSearchResults = MutableLiveData<List<HomeSong>>()
    val songSearchResults: LiveData<List<HomeSong>> = _songSearchResults
    
    // 搜索歌单结果
    private val _playlistSearchResults = MutableLiveData<List<HomePlaylist>>()
    val playlistSearchResults: LiveData<List<HomePlaylist>> = _playlistSearchResults
    
    // 加载状态
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // 错误信息
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    /**
     * 搜索歌曲
     */
    fun searchSongs(keyword: String) {
        if (keyword.isBlank()) {
            _songSearchResults.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val response = RetrofitClient.apiService.getPaginationSong(
                    offset = 0,
                    pageSize = 100, // 获取足够多的结果
                    sort = null,
                    keyword = keyword
                )
                
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result?.code == 200) {
                        _songSearchResults.value = result.data?.list ?: emptyList()
                    } else {
                        _errorMessage.value = result?.message ?: "搜索失败"
                        _songSearchResults.value = emptyList()
                    }
                } else {
                    _errorMessage.value = "网络请求失败: ${response.code()}"
                    _songSearchResults.value = emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = "搜索出错: ${e.message}"
                _songSearchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 搜索歌单
     */
    fun searchPlaylists(keyword: String) {
        if (keyword.isBlank()) {
            _playlistSearchResults.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val response = RetrofitClient.apiService.getPaginationPlaylist(
                    offset = 0,
                    pageSize = 100, // 获取足够多的结果
                    sort = null,
                    keyword = keyword
                )
                
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result?.code == 200) {
                        _playlistSearchResults.value = result.data?.list ?: emptyList()
                    } else {
                        _errorMessage.value = result?.message ?: "搜索失败"
                        _playlistSearchResults.value = emptyList()
                    }
                } else {
                    _errorMessage.value = "网络请求失败: ${response.code()}"
                    _playlistSearchResults.value = emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = "搜索出错: ${e.message}"
                _playlistSearchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 执行搜索（同时搜索歌曲和歌单）
     */
    fun search(keyword: String) {
        searchSongs(keyword)
        searchPlaylists(keyword)
    }
    
    /**
     * 清空搜索结果
     */
    fun clearResults() {
        _songSearchResults.value = emptyList()
        _playlistSearchResults.value = emptyList()
        _errorMessage.value = null
    }
}