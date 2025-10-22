package com.example.listen_to_the_clouds.ui.activity.playlist

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.listen_to_the_clouds.data.model.HomeSong
import com.example.listen_to_the_clouds.data.model.MusicDetails
import com.example.listen_to_the_clouds.data.model.PlaylistDetails
import com.example.listen_to_the_clouds.data.model.UpdatePlaylist
import com.example.listen_to_the_clouds.data.network.DEFAULT_PAGING
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import com.example.listen_to_the_clouds.data.network.RetrofitClient.apiService
import com.example.listen_to_the_clouds.data.paging.PlaylistMusicPagingSource
import com.example.listen_to_the_clouds.utils.GlobalMessageBus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistViewModel : ViewModel() {
    
    private val _playlistDetails = MutableStateFlow<PlaylistDetails?>(null)
    val playlistDetails: StateFlow<PlaylistDetails?> = _playlistDetails.asStateFlow()

    private val _isCollected = MutableStateFlow(false)
    val isCollected: StateFlow<Boolean> = _isCollected.asStateFlow()
    
    private val _collectionSuccess = MutableStateFlow<Boolean?>(null)
    val collectionSuccess: StateFlow<Boolean?> = _collectionSuccess.asStateFlow()
    
    private val _playlistId = MutableStateFlow<Long?>(null)
    
    // 分页获取歌单音乐列表
    val playlistMusicFlow: Flow<PagingData<HomeSong>> = _playlistId
        .asStateFlow()
        .let { playlistIdFlow ->
            Pager(
                config = PagingConfig(
                    pageSize = DEFAULT_PAGING,
                    enablePlaceholders = false,
                    initialLoadSize = DEFAULT_PAGING
                ),
                pagingSourceFactory = {
                    val id = playlistIdFlow.value ?: -1L
                    PlaylistMusicPagingSource(
                        apiService = apiService,
                        playlistId = id,
                        pageSize = DEFAULT_PAGING
                    )
                }
            ).flow.cachedIn(viewModelScope)
        }
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    fun getPlaylistDetails(playlistId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val response = apiService.getPlaylistDetails(playlistId)

                if (response.isSuccessful) {
                    val result = response.body()
                    if (result?.code == 200) {
                        _playlistDetails.value = result.data
                        // 更新收藏状态
                        _isCollected.value = (result.data?.playlistFavorites ?: 0) > 0
                    } else {
                        _errorMessage.value = result?.message ?: "获取歌单详情失败"
                    }
                } else {
                    _errorMessage.value = "网络请求失败: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "发生错误: ${e.message}"
                Log.e("PlaylistViewModel", "获取歌单详情失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun setPlaylistId(playlistId: Long) {
        _playlistId.value = playlistId
    }

    fun togglePlaylistCollection(playlistId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = apiService.setPlaylistCollection(playlistId)
                
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result?.code == 200) {
                        // 切换收藏状态
                        _isCollected.value = !_isCollected.value
                        _collectionSuccess.value = true
                        
                        // 刷新歌单详情以获取最新的收藏数
                        getPlaylistDetails(playlistId)
                        
                        GlobalMessageBus.post(if (_isCollected.value) "收藏成功" else "取消收藏成功")
                    } else {
                        _collectionSuccess.value = false
                        GlobalMessageBus.post(result?.message ?: "操作失败")
                    }
                } else {
                    _collectionSuccess.value = false
                    GlobalMessageBus.post("网络请求失败")
                }
            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "togglePlaylistCollection: ${e.message}", e)
                _collectionSuccess.value = false
                GlobalMessageBus.post("操作失败: ${e.message}")
            } finally {
                _isLoading.value = false
                // 重置成功状态
                _collectionSuccess.value = null
            }
        }
    }
    
    /**
     * 删除歌单
     */
    fun deletePlaylist(playlistId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val response = apiService.setPlaylistDelete(playlistId)
                
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result?.code == 200) {
                        GlobalMessageBus.post("删除歌单成功")
                        onSuccess()
                    } else {
                        _errorMessage.value = result?.message ?: "删除失败"
                        GlobalMessageBus.post(result?.message ?: "删除失败")
                    }
                } else {
                    _errorMessage.value = "网络请求失败: ${response.code()}"
                    GlobalMessageBus.post("网络请求失败")
                }
            } catch (e: Exception) {
                _errorMessage.value = "发生错误: ${e.message}"
                GlobalMessageBus.post("删除失败: ${e.message}")
                Log.e("PlaylistViewModel", "deletePlaylist: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 更新歌单信息（支持修改封面）
     */
    fun updatePlaylist(playlistId: Long, newTitle: String, coverImageFile: File?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                if (newTitle.isBlank()) {
                    _errorMessage.value = "歌单名称不能为空"
                    GlobalMessageBus.post("歌单名称不能为空")
                    return@launch
                }
                
                // 创建 RequestBody
                val playlistIdBody = playlistId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val titleBody = newTitle.toRequestBody("text/plain".toMediaTypeOrNull())
                
                // 创建封面图片的 MultipartBody.Part（如果有）
                val coverPart = coverImageFile?.let { file ->
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("playlistCover", file.name, requestFile)
                }
                
                val response = apiService.setUpdatePlaylist(playlistIdBody, titleBody, coverPart)
                
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result?.code == 200) {
                        GlobalMessageBus.post("更新歌单成功")
                        // 刷新歌单详情
                        getPlaylistDetails(playlistId)
                        onSuccess()
                    } else {
                        _errorMessage.value = result?.message ?: "更新失败"
                        GlobalMessageBus.post(result?.message ?: "更新失败")
                    }
                } else {
                    _errorMessage.value = "网络请求失败: ${response.code()}"
                    GlobalMessageBus.post("网络请求失败")
                }
            } catch (e: Exception) {
                _errorMessage.value = "发生错误: ${e.message}"
                GlobalMessageBus.post("更新失败: ${e.message}")
                Log.e("PlaylistViewModel", "updatePlaylist: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}