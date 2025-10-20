package com.xihe.automation

import android.app.Application
import android.content.Context
import timber.log.Timber

/**
 * 羲和应用的Application类
 */
class XiheApplication : Application() {

    companion object {
        private lateinit var instance: XiheApplication
        
        fun getContext(): Context = instance.applicationContext
        
        val aiApiKey: String
            get() = BuildConfig.AI_API_KEY
            
        val aiApiUrl: String
            get() = BuildConfig.AI_API_URL
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // 初始化日志
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        Timber.i("羲和应用初始化完成")
    }
}
