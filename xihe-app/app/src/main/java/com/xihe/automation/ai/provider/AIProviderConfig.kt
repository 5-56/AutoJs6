package com.xihe.automation.ai.provider

import android.content.Context
import android.content.SharedPreferences

/**
 * AI提供商配置管理
 */
class AIProviderConfig(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "ai_provider_config",
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_PROVIDER = "current_provider"
        private const val KEY_API_KEY_PREFIX = "api_key_"
        private const val KEY_BASE_URL_PREFIX = "base_url_"
        private const val KEY_MODEL_PREFIX = "model_"
        
        @Volatile
        private var instance: AIProviderConfig? = null
        
        fun getInstance(context: Context): AIProviderConfig {
            return instance ?: synchronized(this) {
                instance ?: AIProviderConfig(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    /**
     * 获取当前提供商
     */
    fun getCurrentProvider(): AIProvider {
        val providerName = prefs.getString(KEY_PROVIDER, AIProvider.GOOGLE.name)
        return try {
            AIProvider.valueOf(providerName ?: AIProvider.GOOGLE.name)
        } catch (e: Exception) {
            AIProvider.GOOGLE
        }
    }
    
    /**
     * 设置当前提供商
     */
    fun setCurrentProvider(provider: AIProvider) {
        prefs.edit().putString(KEY_PROVIDER, provider.name).apply()
    }
    
    /**
     * 获取API密钥
     */
    fun getApiKey(provider: AIProvider): String {
        return prefs.getString("$KEY_API_KEY_PREFIX${provider.name}", "") ?: ""
    }
    
    /**
     * 设置API密钥
     */
    fun setApiKey(provider: AIProvider, apiKey: String) {
        prefs.edit().putString("$KEY_API_KEY_PREFIX${provider.name}", apiKey).apply()
    }
    
    /**
     * 获取基础URL
     */
    fun getBaseUrl(provider: AIProvider): String {
        val saved = prefs.getString("$KEY_BASE_URL_PREFIX${provider.name}", "")
        return if (saved.isNullOrEmpty()) {
            provider.defaultBaseUrl
        } else {
            saved
        }
    }
    
    /**
     * 设置基础URL
     */
    fun setBaseUrl(provider: AIProvider, baseUrl: String) {
        prefs.edit().putString("$KEY_BASE_URL_PREFIX${provider.name}", baseUrl).apply()
    }
    
    /**
     * 获取选中的模型
     */
    fun getSelectedModel(provider: AIProvider): String {
        val saved = prefs.getString("$KEY_MODEL_PREFIX${provider.name}", "")
        return if (saved.isNullOrEmpty()) {
            provider.getDefaultModels().firstOrNull() ?: ""
        } else {
            saved
        }
    }
    
    /**
     * 设置选中的模型
     */
    fun setSelectedModel(provider: AIProvider, model: String) {
        prefs.edit().putString("$KEY_MODEL_PREFIX${provider.name}", model).apply()
    }
    
    /**
     * 检查配置是否完整
     */
    fun isConfigured(provider: AIProvider): Boolean {
        val apiKey = getApiKey(provider)
        val model = getSelectedModel(provider)
        return apiKey.isNotEmpty() && model.isNotEmpty()
    }
    
    /**
     * 清除提供商配置
     */
    fun clearConfig(provider: AIProvider) {
        prefs.edit().apply {
            remove("$KEY_API_KEY_PREFIX${provider.name}")
            remove("$KEY_BASE_URL_PREFIX${provider.name}")
            remove("$KEY_MODEL_PREFIX${provider.name}")
        }.apply()
    }
}
