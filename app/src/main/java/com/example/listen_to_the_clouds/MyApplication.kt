package com.example.listen_to_the_clouds

import android.app.Application
import android.content.Context

/**
 * 全局 Application 类
 */
class MyApplication : Application() {
    
    companion object {
        private lateinit var instance: MyApplication
        
        /**
         * 获取全局 Application 实例
         */
        fun getInstance(): MyApplication {
            return instance
        }

        // 全局获取 Application Context
        val context: Context
            get() = instance.applicationContext
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
