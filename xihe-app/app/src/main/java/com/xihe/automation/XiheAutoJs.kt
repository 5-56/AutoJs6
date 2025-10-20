package com.xihe.automation

import android.app.Application
import android.content.Context
import com.xihe.automation.autojs.AbstractAutoJs
import com.xihe.automation.autojs.core.accessibility.AccessibilityTool
import com.xihe.automation.autojs.core.console.GlobalConsole
import com.xihe.automation.autojs.execution.ScriptExecutionGlobalListener
import com.xihe.automation.autojs.runtime.api.AppUtils
import timber.log.Timber

/**
 * 羲和AutoJs核心类
 * 继承自AutoJs6，提供完整的自动化功能
 */
class XiheAutoJs private constructor(appContext: Application) : AbstractAutoJs(appContext) {

    private val accessibilityTool = AccessibilityTool(appContext)

    companion object {
        @Volatile
        private var instance: XiheAutoJs? = null

        fun getInstance(): XiheAutoJs {
            return instance ?: throw IllegalStateException("XiheAutoJs未初始化")
        }

        fun initInstance(application: Application): XiheAutoJs {
            return instance ?: synchronized(this) {
                instance ?: XiheAutoJs(application).also {
                    instance = it
                    Timber.i("XiheAutoJs初始化完成")
                }
            }
        }
    }

    init {
        // 注册脚本执行监听器
        scriptEngineService.registerGlobalScriptExecutionListener(
            ScriptExecutionGlobalListener()
        )
        
        Timber.d("XiheAutoJs核心初始化完成")
    }

    override fun createAppUtils(context: Context): AppUtils {
        return AppUtils(context, "${BuildConfig.APPLICATION_ID}.fileprovider")
    }

    override fun createGlobalConsole(): GlobalConsole {
        return object : GlobalConsole(uiHandler) {
            override fun println(level: Int, charSequence: CharSequence): String {
                return super.println(level, charSequence).also {
                    Timber.d("Console: $charSequence")
                }
            }
        }
    }

    /**
     * 确保无障碍服务启用
     */
    fun ensureAccessibilityServiceEnabled() {
        accessibilityTool.ensureService()
    }

    /**
     * 检查无障碍服务状态
     */
    fun isAccessibilityServiceEnabled(): Boolean {
        return accessibilityTool.isServiceEnabled
    }

    /**
     * 获取运行时
     */
    fun getRuntime() = runtime
}
