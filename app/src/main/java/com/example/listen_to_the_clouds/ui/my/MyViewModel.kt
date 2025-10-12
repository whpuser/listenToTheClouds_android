package com.example.listen_to_the_clouds.ui.my

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "MY"
    }
    val text: LiveData<String> = _text
}