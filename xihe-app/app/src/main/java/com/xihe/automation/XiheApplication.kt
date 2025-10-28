package com.xihe.automation

import android.app.Application
import android.content.Context
import timber.log.Timber

/**
 * 羲和应用的Application类
 * 集成了完整的AutoJs6功能
 */
class XiheApplication : Application() {

    companion object {
        private lateinit var instance: XiheApplication
        
        fun getContext(): Context = instance.applicationContext
        
        val aiApiKey: String
            get() = BuildConfig.AI_API_KEY
            
        val aiApiUrl: String
            get() = BuildConfig.AI_API_URL
            
        /**
         * 获取AutoJs实例
         */
        fun getAutoJs(): XiheAutoJs = XiheAutoJs.getInstance()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // 初始化日志
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        // 初始化AutoJs核心
        try {
            XiheAutoJs.initInstance(this)
            Timber.i("AutoJs核心初始化成功")
        } catch (e: Exception) {
            Timber.e(e, "AutoJs核心初始化失败")
        }
        
        Timber.i("羲和应用初始化完成")
    }
}
