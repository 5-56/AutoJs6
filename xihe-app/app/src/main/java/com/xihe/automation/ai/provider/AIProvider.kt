package com.xihe.automation.ai.provider

/**
 * AI提供商枚举
 */
enum class AIProvider(
    val displayName: String,
    val defaultBaseUrl: String,
    val supportChat: Boolean = true,
    val requiresApiKey: Boolean = true
) {
    OPENAI(
        displayName = "OpenAI",
        defaultBaseUrl = "https://api.openai.com/v1"
    ),
    
    GOOGLE(
        displayName = "Google Gemini",
        defaultBaseUrl = "https://generativelanguage.googleapis.com/v1beta"
    ),
    
    DEEPSEEK(
        displayName = "DeepSeek",
        defaultBaseUrl = "https://api.deepseek.com/v1"
    ),
    
    KIMI(
        displayName = "月之暗面 Kimi",
        defaultBaseUrl = "https://api.moonshot.cn/v1"
    ),
    
    OPENROUTER(
        displayName = "OpenRouter",
        defaultBaseUrl = "https://openrouter.ai/api/v1"
    ),
    
    ZHIPU(
        displayName = "智谱 GLM",
        defaultBaseUrl = "https://open.bigmodel.cn/api/paas/v4"
    ),
    
    QWEN(
        displayName = "通义千问",
        defaultBaseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1"
    ),
    
    CUSTOM(
        displayName = "自定义",
        defaultBaseUrl = ""
    );
    
    /**
     * 获取模型列表端点
     */
    fun getModelsEndpoint(baseUrl: String = defaultBaseUrl): String {
        return when (this) {
            GOOGLE -> "$baseUrl/models"
            OPENAI, DEEPSEEK, KIMI, OPENROUTER, QWEN -> "$baseUrl/models"
            ZHIPU -> "$baseUrl/fine_tuning/personal_models" // 智谱的端点不同
            CUSTOM -> "$baseUrl/models"
        }
    }
    
    /**
     * 获取聊天补全端点
     */
    fun getChatEndpoint(baseUrl: String = defaultBaseUrl): String {
        return when (this) {
            GOOGLE -> "$baseUrl/models/{model}:generateContent"
            OPENAI, DEEPSEEK, KIMI, OPENROUTER, QWEN -> "$baseUrl/chat/completions"
            ZHIPU -> "$baseUrl/chat/completions"
            CUSTOM -> "$baseUrl/chat/completions"
        }
    }
    
    /**
     * 获取默认模型
     */
    fun getDefaultModels(): List<String> {
        return when (this) {
            OPENAI -> listOf("gpt-4o", "gpt-4o-mini", "gpt-3.5-turbo")
            GOOGLE -> listOf("gemini-2.0-flash-exp", "gemini-1.5-flash", "gemini-1.5-pro")
            DEEPSEEK -> listOf("deepseek-chat", "deepseek-coder")
            KIMI -> listOf("moonshot-v1-8k", "moonshot-v1-32k", "moonshot-v1-128k")
            OPENROUTER -> listOf("google/gemini-2.0-flash-exp:free", "meta-llama/llama-3.2-3b-instruct:free")
            ZHIPU -> listOf("glm-4-flash", "glm-4-plus", "glm-4-air")
            QWEN -> listOf("qwen-max", "qwen-plus", "qwen-turbo")
            CUSTOM -> emptyList()
        }
    }
    
    /**
     * 是否支持免费使用
     */
    fun isFree(): Boolean {
        return when (this) {
            GOOGLE -> true  // Gemini API 有免费额度
            DEEPSEEK -> true  // DeepSeek 有免费额度
            OPENROUTER -> true  // 有免费模型
            ZHIPU -> true  // 智谱有免费额度
            QWEN -> true  // 通义千问有免费额度
            KIMI -> false  // 需要付费
            OPENAI -> false  // 需要付费
            CUSTOM -> false
        }
    }
}
