package com.xihe.automation.ui.main

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.xihe.automation.R
import com.xihe.automation.ai.provider.AIProviderConfig
import com.xihe.automation.ui.settings.AIProviderSettingsActivity

/**
 * AI提供商状态显示控件
 * 显示在主界面顶部，方便快速查看和配置
 */
class AIProviderStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {
    
    private val providerConfig = AIProviderConfig.getInstance(context)
    private val providerNameText: TextView
    private val modelNameText: TextView
    private val statusText: TextView
    
    init {
        LayoutInflater.from(context).inflate(R.layout.view_ai_provider_status, this, true)
        
        providerNameText = findViewById(R.id.provider_name_text)
        modelNameText = findViewById(R.id.model_name_text)
        statusText = findViewById(R.id.status_text)
        
        setOnClickListener {
            context.startActivity(Intent(context, AIProviderSettingsActivity::class.java))
        }
        
        updateStatus()
    }
    
    /**
     * 更新显示状态
     */
    fun updateStatus() {
        val currentProvider = providerConfig.getCurrentProvider()
        val isConfigured = providerConfig.isConfigured(currentProvider)
        
        if (isConfigured) {
            val model = providerConfig.getSelectedModel(currentProvider)
            providerNameText.text = currentProvider.displayName
            modelNameText.text = model
            statusText.text = "✅ 已配置"
            statusText.setTextColor(context.getColor(android.R.color.holo_green_dark))
        } else {
            providerNameText.text = "AI提供商"
            modelNameText.text = "点击配置"
            statusText.text = "⚠️ 未配置（使用智能后备）"
            statusText.setTextColor(context.getColor(android.R.color.holo_orange_dark))
        }
    }
    
    /**
     * 在Activity的onResume中调用，更新状态
     */
    fun refresh() {
        updateStatus()
    }
}
