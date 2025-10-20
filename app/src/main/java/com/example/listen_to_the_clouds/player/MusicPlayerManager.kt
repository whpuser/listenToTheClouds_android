package com.example.listen_to_the_clouds.player

import android.media.MediaPlayer
import android.util.Log
import com.example.listen_to_the_clouds.data.model.HomeSong
import com.example.listen_to_the_clouds.data.network.RESOURCE_ADDRESS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 全局音乐播放器管理器 - 单例模式
 */
object MusicPlayerManager {
    private const val TAG = "MusicPlayerManager"

    // MediaPlayer 实例
    private var mediaPlayer: MediaPlayer? = null

    // 当前播放列表
    private val _playlist = MutableStateFlow<List<HomeSong>>(emptyList())
    val playlist: StateFlow<List<HomeSong>> = _playlist.asStateFlow()

    // 当前播放歌曲
    private val _currentSong = MutableStateFlow<HomeSong?>(null)
    val currentSong: StateFlow<HomeSong?> = _currentSong.asStateFlow()

    // 当前播放索引
    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    // 播放状态
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // 播放模式
    private val _playMode = MutableStateFlow(PlayMode.LIST_LOOP)
    val playMode: StateFlow<PlayMode> = _playMode.asStateFlow()

    // 当前播放进度（毫秒）
    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    // 歌曲总时长（毫秒）
    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration.asStateFlow()

    /**
     * 设置播放列表并播放指定歌曲
     */
    fun setPlaylistAndPlay(songs: List<HomeSong>, index: Int) {
        if (songs.isEmpty() || index < 0 || index >= songs.size) {
            Log.e(TAG, "Invalid playlist or index")
            return
        }

        _playlist.value = songs
        _currentIndex.value = index
        _currentSong.value = songs[index]
        
        playSong(songs[index])
    }

    /**
     * 播放指定歌曲
     */
    fun playSong(song: HomeSong) {
        try {
            // 释放之前的 MediaPlayer
            mediaPlayer?.release()
            
            _currentSong.value = song
            
            // 立即重置进度和时长，避免旧值导致 UI 崩溃
            _currentPosition.value = 0
            _duration.value = 0
            
            // 创建新的 MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                // 拼接完整的音频 URL
                val audioUrl = RESOURCE_ADDRESS + song.link
                Log.d(TAG, "Playing audio from: $audioUrl")
                
                setDataSource(audioUrl)
                prepareAsync()
                
                setOnPreparedListener {
                    _duration.value = duration
                    start()
                    _isPlaying.value = true
                    Log.d(TAG, "Song prepared and started: ${song.name}")
                }
                
                setOnCompletionListener {
                    // 播放完成后自动播放下一曲
                    playNext()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra, url=$audioUrl")
                    _isPlaying.value = false
                    true
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error playing song: ${e.message}")
            _isPlaying.value = false
        }
    }

    /**
     * 播放/暂停切换
     */
    fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
            } else {
                it.start()
                _isPlaying.value = true
            }
        }
    }

    /**
     * 播放
     */
    fun play() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                _isPlaying.value = true
            }
        }
    }

    /**
     * 暂停
     */
    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
            }
        }
    }

    /**
     * 播放下一曲
     */
    fun playNext() {
        val songs = _playlist.value
        if (songs.isEmpty()) return

        val nextIndex = when (_playMode.value) {
            PlayMode.LIST_LOOP -> {
                // 列表循环：播放下一首，到末尾后回到开头
                (_currentIndex.value + 1) % songs.size
            }
            PlayMode.SINGLE_LOOP -> {
                // 单曲循环：继续播放当前歌曲
                _currentIndex.value
            }
            PlayMode.RANDOM -> {
                // 随机播放：随机选择一首（避免重复当前歌曲）
                if (songs.size == 1) {
                    0
                } else {
                    var random = (songs.indices).random()
                    while (random == _currentIndex.value) {
                        random = (songs.indices).random()
                    }
                    random
                }
            }
        }

        _currentIndex.value = nextIndex
        _currentSong.value = songs[nextIndex]
        playSong(songs[nextIndex])
    }

    /**
     * 播放上一曲
     */
    fun playPrevious() {
        val songs = _playlist.value
        if (songs.isEmpty()) return

        val previousIndex = when (_playMode.value) {
            PlayMode.LIST_LOOP -> {
                // 列表循环：播放上一首，到开头后回到末尾
                if (_currentIndex.value - 1 < 0) {
                    songs.size - 1
                } else {
                    _currentIndex.value - 1
                }
            }
            PlayMode.SINGLE_LOOP -> {
                // 单曲循环：重新播放当前歌曲
                _currentIndex.value
            }
            PlayMode.RANDOM -> {
                // 随机播放：随机选择一首
                if (songs.size == 1) {
                    0
                } else {
                    var random = (songs.indices).random()
                    while (random == _currentIndex.value) {
                        random = (songs.indices).random()
                    }
                    random
                }
            }
        }

        _currentIndex.value = previousIndex
        _currentSong.value = songs[previousIndex]
        playSong(songs[previousIndex])
    }

    /**
     * 切换播放模式
     */
    fun togglePlayMode() {
        _playMode.value = when (_playMode.value) {
            PlayMode.LIST_LOOP -> PlayMode.SINGLE_LOOP
            PlayMode.SINGLE_LOOP -> PlayMode.RANDOM
            PlayMode.RANDOM -> PlayMode.LIST_LOOP
        }
    }

    /**
     * 设置播放模式
     */
    fun setPlayMode(mode: PlayMode) {
        _playMode.value = mode
    }

    /**
     * 跳转到指定位置（毫秒）
     */
    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
        _currentPosition.value = position
    }

    /**
     * 获取当前播放进度
     */
    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    /**
     * 获取歌曲总时长
     */
    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    /**
     * 更新当前播放进度（用于UI更新）
     */
    fun updateCurrentPosition() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                _currentPosition.value = it.currentPosition
            }
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
    }
}
