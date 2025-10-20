package com.example.listen_to_the_clouds.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

//全局信息提示
object GlobalMessageBus {
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val messages: SharedFlow<String> = _messages

    fun post(message: String) {
        _messages.tryEmit(message)
    }
}
