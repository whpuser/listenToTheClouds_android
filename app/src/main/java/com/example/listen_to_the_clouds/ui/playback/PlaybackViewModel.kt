package com.example.listen_to_the_clouds.ui.playback

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlaybackViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "BF"
    }
    val text: LiveData<String> = _text
}