package com.example.listen_to_the_clouds.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * 歌单刷新事件总线
 * 用于在歌单被删除、更新时通知其他页面刷新
 */
object PlaylistRefreshBus {
    private val _refreshEvents = MutableSharedFlow<RefreshEvent>(extraBufferCapacity = 64)
    val refreshEvents: SharedFlow<RefreshEvent> = _refreshEvents

    fun notifyPlaylistDeleted() {
        _refreshEvents.tryEmit(RefreshEvent.PlaylistDeleted)
    }

    fun notifyPlaylistUpdated() {
        _refreshEvents.tryEmit(RefreshEvent.PlaylistUpdated)
    }
}

sealed class RefreshEvent {
    object PlaylistDeleted : RefreshEvent()
    object PlaylistUpdated : RefreshEvent()
}
