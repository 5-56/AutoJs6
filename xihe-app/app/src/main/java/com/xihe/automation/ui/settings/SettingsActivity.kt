package com.xihe.automation.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.xihe.automation.R
import com.xihe.automation.ai.provider.AIProviderConfig

/**
 * 设置界面
 */
class SettingsActivity : AppCompatActivity() {
    
    private lateinit var providerConfig: AIProviderConfig
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        providerConfig = AIProviderConfig.getInstance(this)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        setupAIProviderOption()
    }
    
    private fun setupAIProviderOption() {
        val aiProviderOption = findViewById<LinearLayout>(R.id.ai_provider_option)
        val providerStatusText = findViewById<TextView>(R.id.provider_status_text)
        
        // 显示当前配置状态
        updateProviderStatus(providerStatusText)
        
        aiProviderOption?.setOnClickListener {
            startActivity(Intent(this, AIProviderSettingsActivity::class.java))
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 更新提供商状态
        val providerStatusText = findViewById<TextView>(R.id.provider_status_text)
        updateProviderStatus(providerStatusText)
    }
    
    private fun updateProviderStatus(textView: TextView) {
        val currentProvider = providerConfig.getCurrentProvider()
        val isConfigured = providerConfig.isConfigured(currentProvider)
        
        textView.text = if (isConfigured) {
            val model = providerConfig.getSelectedModel(currentProvider)
            "${currentProvider.displayName} - $model"
        } else {
            "未配置（使用智能后备方案）"
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
