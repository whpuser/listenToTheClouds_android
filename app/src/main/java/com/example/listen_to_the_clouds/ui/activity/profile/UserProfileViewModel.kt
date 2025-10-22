package com.example.listen_to_the_clouds.ui.activity.profile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.listen_to_the_clouds.data.model.UserData
import com.example.listen_to_the_clouds.data.model.UserDate
import com.example.listen_to_the_clouds.data.network.RetrofitClient.apiService
import com.example.listen_to_the_clouds.utils.TokenManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val _userInfo = MutableLiveData<UserDate?>()
    val userInfo: LiveData<UserDate?> get() = _userInfo

    private val _updateResult = MutableLiveData<UpdateResult>()
    val updateResult: LiveData<UpdateResult> get() = _updateResult

    private val _uploadAvatarResult = MutableLiveData<UploadResult>()
    val uploadAvatarResult: LiveData<UploadResult> get() = _uploadAvatarResult

    data class UpdateResult(
        val success: Boolean,
        val message: String
    )

    data class UploadResult(
        val success: Boolean,
        val message: String
    )

    /**
     * 加载用户信息
     */
    fun loadUserInfo() {
        viewModelScope.launch {
            try {
                val response = apiService.getUserInfo()
                
                if (response.isSuccessful && response.body()?.code == 200) {
                    _userInfo.value = response.body()?.data
                    Log.d("UserProfileViewModel", "Load user info success")
                } else {
                    Log.e("UserProfileViewModel", "Load user info failed: ${response.body()?.message}")
                }
            } catch (e: Exception) {
                Log.e("UserProfileViewModel", "Load user info error: ${e.message}")
            }
        }
    }

    /**
     * 更新用户信息
     */
    fun updateUserInfo(name: String, gender: Int, age: Int) {
        viewModelScope.launch {
            try {
                val userData = UserData(name = name, gender = gender, age = age)
                val response = apiService.setUserModify(userData)
                
                if (response.isSuccessful && response.body()?.code == 200) {
                    _updateResult.value = UpdateResult(true, "保存成功")
                    Log.d("UserProfileViewModel", "Update user info success")
                } else {
                    _updateResult.value = UpdateResult(
                        false, 
                        response.body()?.message ?: "保存失败"
                    )
                    Log.e("UserProfileViewModel", "Update user info failed: ${response.body()?.message}")
                }
            } catch (e: Exception) {
                _updateResult.value = UpdateResult(false, "网络错误: ${e.message}")
                Log.e("UserProfileViewModel", "Update user info error: ${e.message}")
            }
        }
    }

    /**
     * 上传头像
     */
    fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                
                // 将 URI 转换为 File
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                // 创建 MultipartBody.Part
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                
                // 获取用户ID
                val userId = _userInfo.value?.id ?: 0L
                
                // 上传
                val response = apiService.uploadUserAvatar(body, userId)
                
                // 删除临时文件
                file.delete()
                
                if (response.isSuccessful && response.body()?.code == 200) {
                    _uploadAvatarResult.value = UploadResult(true, "头像上传成功")
                    Log.d("UserProfileViewModel", "Upload avatar success")
                } else {
                    _uploadAvatarResult.value = UploadResult(
                        false, 
                        response.body()?.message ?: "上传失败"
                    )
                    Log.e("UserProfileViewModel", "Upload avatar failed: ${response.body()?.message}")
                }
            } catch (e: Exception) {
                _uploadAvatarResult.value = UploadResult(false, "上传错误: ${e.message}")
                Log.e("UserProfileViewModel", "Upload avatar error: ${e.message}")
            }
        }
    }
}
