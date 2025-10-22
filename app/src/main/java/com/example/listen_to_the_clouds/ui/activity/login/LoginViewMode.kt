package com.example.listen_to_the_clouds.ui.activity.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.listen_to_the_clouds.data.model.LoginData
import com.example.listen_to_the_clouds.data.model.RegisterData
import com.example.listen_to_the_clouds.data.network.RetrofitClient.apiService
import com.example.listen_to_the_clouds.utils.GlobalMessageBus
import com.example.listen_to_the_clouds.utils.TokenManager
import kotlinx.coroutines.launch

class LoginViewMode(application: Application) : AndroidViewModel(application) {

    private val _logInData = MutableLiveData<Boolean>()
    val logInData: LiveData<Boolean> = _logInData

    private val _registered = MutableLiveData<Boolean>()
    val registered: LiveData<Boolean> = _registered

    private val _verificationCode = MutableLiveData<Boolean>()
    val verificationCode: LiveData<Boolean> = _verificationCode

    /**
     * 用户登录
     */
    fun login(mailbox: String, password: String) {
        if (mailbox.isBlank()) {
            GlobalMessageBus.post("邮箱不能为空")
            return
        }
        if (password.isBlank()) {
            GlobalMessageBus.post("密码不能为空")
            return
        }

        viewModelScope.launch {
            try {
                val response = apiService.setLogin(LoginData(mailbox, password))
                val result = response.body()

                if (!response.isSuccessful || result == null) {
                    GlobalMessageBus.post("登录失败：${response.message()}")
                    return@launch
                }

                if (result.code != 200) {
                    GlobalMessageBus.post("登录失败：" + response.message())
                    return@launch
                }

                result.data?.let {
                    TokenManager.saveToken(getApplication(), it)
                }

                _logInData.postValue(true)
            } catch (e: Exception) {
                GlobalMessageBus.post("登录失败，检查后，重新登录")
            }
        }
    }

    /**
     * 用户注册
     */
    fun register(mailbox: String, otpCode: String, password: String, confirmPassword: String) {
        if (mailbox.isBlank()) {
            GlobalMessageBus.post("邮箱不能为空")
            return
        }
        if (otpCode.isBlank()) {
            GlobalMessageBus.post("验证码不能为空")
            return
        }
        if (password.isBlank()) {
            GlobalMessageBus.post("密码不能为空")
            return
        }
        if (confirmPassword.isBlank()) {
            GlobalMessageBus.post("请确认密码")
            return
        }
        if (password != confirmPassword) {
            GlobalMessageBus.post("两次密码输入不一致")
            return
        }

        viewModelScope.launch {
            try {
                val response = apiService.setRegister(RegisterData(mailbox, otpCode, "用户:$mailbox", password))
                if (!response.isSuccessful) {
                    GlobalMessageBus.post("注册失败：" + response.message())
                    return@launch
                }
                _registered.postValue(true)
            } catch (e: Exception) {
                GlobalMessageBus.post("网络错误：" + e.message)
            }
        }
    }

    /**
     * 发送邮箱验证码
     */
    fun sendEmailCode(mailbox: String) {
        if (mailbox.isBlank()) {
            GlobalMessageBus.post("请输入邮箱")
            return
        }

        viewModelScope.launch {
            try {
                val result = apiService.setMailbox(mailbox)
                if (result.code() == 550){
                    GlobalMessageBus.post("邮箱找不到。")
                }
                if (!result.isSuccessful) {
                    GlobalMessageBus.post("验证码发送失败：" + result.message())
                    return@launch
                }
                _verificationCode.postValue(true)
            } catch (e: Exception) {
                GlobalMessageBus.post("邮件发生错误，请检查后，重新发送")
            }
        }
    }
}
