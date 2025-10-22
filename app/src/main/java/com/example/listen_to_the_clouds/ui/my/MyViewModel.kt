package com.example.listen_to_the_clouds.ui.my

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.listen_to_the_clouds.data.model.MusicDetails
import com.example.listen_to_the_clouds.data.model.UserCollectionNumberVo
import com.example.listen_to_the_clouds.data.model.UserDate
import com.example.listen_to_the_clouds.data.network.RetrofitClient.apiService
import com.example.listen_to_the_clouds.utils.GlobalMessageBus
import com.example.listen_to_the_clouds.utils.TokenManager
import kotlinx.coroutines.launch

class MyViewModel(application: Application) : AndroidViewModel(application) {

    private val _myTotal = MutableLiveData<UserCollectionNumberVo?>()
    val myTotal: LiveData<UserCollectionNumberVo?> get() = _myTotal

    private val _userInfo = MutableLiveData<UserDate?>()
    val userInfo: LiveData<UserDate?> get() = _userInfo

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    private val _createPlaylistResult = MutableLiveData<CreatePlaylistResult>()
    val createPlaylistResult: LiveData<CreatePlaylistResult> get() = _createPlaylistResult

    private val _feedbackResult = MutableLiveData<FeedbackResult>()
    val feedbackResult: LiveData<FeedbackResult> get() = _feedbackResult

    data class CreatePlaylistResult(
        val success: Boolean,
        val message: String
    )

    data class FeedbackResult(
        val success: Boolean,
        val message: String
    )

    /**
     * 检查登录状态
     */
    private fun checkLoginStatus() {
        val loggedIn = TokenManager.isLoggedIn(getApplication())
        _isLoggedIn.value = loggedIn
    }

    /**
     * 获取用户信息
     */
    private fun getUserInfo() {
        viewModelScope.launch {
            val result = apiService.getUserInfo().body()!!
            if (result.code != 200) {
                GlobalMessageBus.post(result.message)
                return@launch
            }
            _userInfo.value = result.data
        }
    }

    /**
     * 收藏创造总数
     */
    private fun getMyTotal() {
        viewModelScope.launch {
            val result = apiService.getMyTotal().body()!!
            if (result.code != 200) {
                GlobalMessageBus.post(result.message)
                return@launch
            }
            _myTotal.value = result.data
        }
    }

    /**
     * 刷新所有用户数据
     */
    fun refreshUserData() {
        checkLoginStatus()
        if (TokenManager.isLoggedIn(getApplication())) {
            getUserInfo()
            getMyTotal()
        }
    }

    /**
     * 创建歌单（默认公开）
     */
    fun createPlaylist(title: String) {
        if (title.isBlank()) {
            _createPlaylistResult.value = CreatePlaylistResult(false, "歌单名称不能为空")
            return
        }

        viewModelScope.launch {
            try {
                val response = apiService.setCreatePlaylist(title)
                
                if (response.isSuccessful && response.body()?.code == 200) {
                    _createPlaylistResult.value = CreatePlaylistResult(true, "创建成功")
                    Log.d("MyViewModel", "Create playlist success: $title")
                    // 刷新用户数据以更新创建歌单数量
                    getMyTotal()
                } else {
                    _createPlaylistResult.value = CreatePlaylistResult(
                        false, 
                        response.body()?.message ?: "创建失败"
                    )
                    Log.e("MyViewModel", "Create playlist failed: ${response.body()?.message}")
                }
            } catch (e: Exception) {
                _createPlaylistResult.value = CreatePlaylistResult(false, "网络错误: ${e.message}")
                Log.e("MyViewModel", "Create playlist error: ${e.message}")
            }
        }
    }

    /**
     * 提交用户反馈
     */
    fun submitFeedback(content: String) {
        if (content.isBlank()) {
            _feedbackResult.value = FeedbackResult(false, "反馈内容不能为空")
            return
        }

        viewModelScope.launch {
            try {
                val userId = _userInfo.value?.id ?: 0L
                val response = apiService.setUserFeedback(userId, content)
                
                if (response.isSuccessful && response.body()?.code == 200) {
                    _feedbackResult.value = FeedbackResult(true, "感谢您的反馈！")
                    Log.d("MyViewModel", "Submit feedback success")
                } else {
                    _feedbackResult.value = FeedbackResult(
                        false, 
                        response.body()?.message ?: "提交失败"
                    )
                    Log.e("MyViewModel", "Submit feedback failed: ${response.body()?.message}")
                }
            } catch (e: Exception) {
                _feedbackResult.value = FeedbackResult(false, "网络错误: ${e.message}")
                Log.e("MyViewModel", "Submit feedback error: ${e.message}")
            }
        }
    }
}