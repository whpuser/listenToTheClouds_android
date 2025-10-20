package com.example.listen_to_the_clouds.ui.playback

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingData
import com.example.listen_to_the_clouds.data.model.HomePlaylist
import com.example.listen_to_the_clouds.data.model.HomeSong
import com.example.listen_to_the_clouds.data.model.MusicDetails
import com.example.listen_to_the_clouds.data.network.DEFAULT_PAGING
import com.example.listen_to_the_clouds.data.network.RetrofitClient.apiService
import com.example.listen_to_the_clouds.player.MusicPlayerManager
import com.example.listen_to_the_clouds.player.PlayMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlaybackViewModel : ViewModel() {

    //歌单详情
    private val musicDetails = MutableLiveData<MusicDetails>()
    val MusicDetails: LiveData<MusicDetails> = musicDetails

    // 全局播放器状态
    val currentSong: StateFlow<HomeSong?> = MusicPlayerManager.currentSong
    val isPlaying: StateFlow<Boolean> = MusicPlayerManager.isPlaying
    val playMode: StateFlow<PlayMode> = MusicPlayerManager.playMode
    val currentPosition: StateFlow<Int> = MusicPlayerManager.currentPosition
    val duration: StateFlow<Int> = MusicPlayerManager.duration

    fun getPaginationSong(id:Int) {
        viewModelScope.launch {
        val response = apiService.getSongDetails(id).body()!!
        if (response.code != 200){
            Log.e("TAG", "PlaybackViewModel: ${response.message}", )
            return@launch
        }
        musicDetails.value = response.data!!
        }
    }

    /**
     * 播放/暂停切换
     */
    fun togglePlayPause() {
        MusicPlayerManager.togglePlayPause()
    }

    /**
     * 播放下一曲
     */
    fun playNext() {
        MusicPlayerManager.playNext()
    }

    /**
     * 播放上一曲
     */
    fun playPrevious() {
        MusicPlayerManager.playPrevious()
    }

    /**
     * 切换播放模式
     */
    fun togglePlayMode() {
        MusicPlayerManager.togglePlayMode()
    }

    /**
     * 跳转到指定位置
     */
    fun seekTo(position: Int) {
        MusicPlayerManager.seekTo(position)
    }

    /**
     * 更新播放进度
     */
    fun updateProgress() {
        MusicPlayerManager.updateCurrentPosition()
    }

    /**
     * 加载默认歌曲列表（当没有播放歌曲时）
     */
    fun loadDefaultSongIfNeeded() {
        viewModelScope.launch {
            // 如果当前没有播放歌曲，则加载第一首
            if (MusicPlayerManager.currentSong.value == null) {
                try {
                    val response = apiService.getPaginationSong(
                        offset = 0,
                        pageSize = DEFAULT_PAGING,
                        sort = null,
                        keyword = null
                    )
                    
                    if (response.isSuccessful) {
                        val songs = response.body()?.data?.list
                        if (!songs.isNullOrEmpty()) {
                            // 设置播放列表并播放第一首歌曲
                            MusicPlayerManager.setPlaylistAndPlay(songs, 0)
                            Log.d("PlaybackViewModel", "Loaded default song: ${songs[0].name}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PlaybackViewModel", "Failed to load default song: ${e.message}")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // ViewModel 销毁时不释放播放器，因为是全局播放器
    }
}