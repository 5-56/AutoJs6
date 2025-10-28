package com.xihe.automation.ui.components

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.xihe.automation.R
import com.xihe.automation.ai.provider.AIProviderConfig
import com.xihe.automation.ui.settings.AIProviderSettingsActivity

/**
 * 首次使用欢迎引导对话框
 */
class WelcomeGuideDialog(context: Context) : Dialog(context) {
    
    private val prefs = context.getSharedPreferences("app_state", Context.MODE_PRIVATE)
    private val providerConfig = AIProviderConfig.getInstance(context)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_welcome_guide)
        
        setupViews()
    }
    
    private fun setupViews() {
        val titleText = findViewById<TextView>(R.id.welcome_title)
        val messageText = findViewById<TextView>(R.id.welcome_message)
        val configButton = findViewById<Button>(R.id.config_ai_button)
        val skipButton = findViewById<Button>(R.id.skip_button)
        
        // 检查是否已配置AI
        val currentProvider = providerConfig.getCurrentProvider()
        val isConfigured = providerConfig.isConfigured(currentProvider)
        
        if (isConfigured) {
            titleText.text = "✅ AI已配置"
            messageText.text = "当前使用: ${currentProvider.displayName}\n\n" +
                    "你可以直接开始使用，或者点击下方按钮更换AI提供商。"
            configButton.text = "更换AI提供商"
        } else {
            titleText.text = "👋 欢迎使用羲和"
            messageText.text = "羲和支持多个AI提供商：\n\n" +
                    "🌟 Google Gemini（免费）\n" +
                    "🌟 DeepSeek（免费）\n" +
                    "🌟 智谱GLM（免费）\n" +
                    "🌟 OpenRouter（部分免费）\n" +
                    "...以及更多\n\n" +
                    "配置后可使用AI自动生成和执行脚本。\n" +
                    "未配置时将使用智能后备方案。"
            configButton.text = "立即配置AI"
        }
        
        configButton.setOnClickListener {
            context.startActivity(Intent(context, AIProviderSettingsActivity::class.java))
            markAsShown()
            dismiss()
        }
        
        skipButton.setOnClickListener {
            markAsShown()
            dismiss()
        }
    }
    
    private fun markAsShown() {
        prefs.edit().putBoolean("welcome_guide_shown", true).apply()
    }
    
    companion object {
        /**
         * 检查是否需要显示欢迎引导
         */
        fun shouldShow(context: Context): Boolean {
            val prefs = context.getSharedPreferences("app_state", Context.MODE_PRIVATE)
            return !prefs.getBoolean("welcome_guide_shown", false)
        }
        
        /**
         * 显示欢迎引导（如果需要）
         */
        fun showIfNeeded(context: Context) {
            if (shouldShow(context)) {
                WelcomeGuideDialog(context).show()
            }
        }
    }
}
