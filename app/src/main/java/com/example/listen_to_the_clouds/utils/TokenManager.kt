package com.example.listen_to_the_clouds.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.listen_to_the_clouds.MyApplication

/**
 * Token 管理器，用于存储和获取用户 token
 */
object TokenManager {
    private const val PREF_NAME = "listen_to_the_clouds_prefs"
    private const val KEY_TOKEN = "user_token"
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * 保存 token
     */
    fun saveToken(context: Context, token: String) {
        getPreferences(context).edit().putString(KEY_TOKEN, token).apply()
    }
    
    /**
     * 获取 token
     */
    fun getToken(context: Context): String? {
        return getPreferences(context).getString(KEY_TOKEN, null)
    }
    
    /**
     * 清除 token
     */
    fun clearToken(context: Context) {
        getPreferences(context).edit().remove(KEY_TOKEN).apply()
    }
    
    /**
     * 检查是否已登录（需要传入 Context）
     */
    fun isLoggedIn(context: Context): Boolean {
        return !getToken(context).isNullOrEmpty()
    }
    
    /**
     * 全局静态方法：检查用户是否已登录
     * 使用全局 Application Context，无需传入参数
     * @return true 表示已登录，false 表示未登录
     */
    @JvmStatic
    fun isUserLoggedIn(): Boolean {
        return try {
            val context = MyApplication.getInstance()
            !getToken(context).isNullOrEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 全局静态方法：获取当前用户的 Token
     * 使用全局 Application Context，无需传入参数
     * @return Token 字符串，如果未登录则返回 null
     */
    @JvmStatic
    fun getCurrentToken(): String? {
        return try {
            val context = MyApplication.getInstance()
            getToken(context)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 全局静态方法：退出登录
     * 使用全局 Application Context，无需传入参数
     */
    @JvmStatic
    fun logout() {
        try {
            val context = MyApplication.getInstance()
            clearToken(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
