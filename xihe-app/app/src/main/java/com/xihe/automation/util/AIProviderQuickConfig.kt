package com.xihe.automation.util

import com.xihe.automation.ai.provider.AIProvider

/**
 * AI提供商快速配置助手
 * 提供各个AI提供商的快速配置信息和帮助
 */
object AIProviderQuickConfig {
    
    /**
     * 获取提供商的帮助信息
     */
    fun getProviderHelp(provider: AIProvider): ProviderHelpInfo {
        return when (provider) {
            AIProvider.GOOGLE -> ProviderHelpInfo(
                name = "Google Gemini",
                isFree = true,
                apiKeyGuide = "访问 https://makersuite.google.com/app/apikey 获取",
                recommendedModels = listOf(
                    "gemini-2.0-flash-exp (最新最快)",
                    "gemini-1.5-flash (稳定快速)",
                    "gemini-1.5-pro (功能强大)"
                ),
                features = listOf(
                    "✅ 完全免费",
                    "✅ 强大的代码生成能力",
                    "✅ 支持长上下文",
                    "✅ 快速响应"
                ),
                notes = "推荐首选！免费且强大。"
            )
            
            AIProvider.DEEPSEEK -> ProviderHelpInfo(
                name = "DeepSeek",
                isFree = true,
                apiKeyGuide = "访问 https://platform.deepseek.com 注册获取",
                recommendedModels = listOf(
                    "deepseek-coder (代码优化)",
                    "deepseek-chat (通用对话)"
                ),
                features = listOf(
                    "✅ 免费额度充足",
                    "✅ 中文理解优秀",
                    "✅ 代码生成专业",
                    "✅ 响应快速"
                ),
                notes = "推荐中文用户使用！"
            )
            
            AIProvider.OPENROUTER -> ProviderHelpInfo(
                name = "OpenRouter",
                isFree = true,
                apiKeyGuide = "访问 https://openrouter.ai 注册获取",
                recommendedModels = listOf(
                    "google/gemini-2.0-flash-exp:free (免费)",
                    "meta-llama/llama-3.2-3b-instruct:free (免费)",
                    "其他付费模型"
                ),
                features = listOf(
                    "✅ 聚合多个AI模型",
                    "✅ 部分模型免费",
                    "✅ 可以尝试不同模型",
                    "✅ 统一API接口"
                ),
                notes = "适合想尝试多种模型的用户。"
            )
            
            AIProvider.ZHIPU -> ProviderHelpInfo(
                name = "智谱GLM",
                isFree = true,
                apiKeyGuide = "访问 https://open.bigmodel.cn 注册获取",
                recommendedModels = listOf(
                    "glm-4-flash (快速)",
                    "glm-4-plus (增强)",
                    "glm-4-air (轻量)"
                ),
                features = listOf(
                    "✅ 国产大模型",
                    "✅ 免费额度",
                    "✅ 中文优化",
                    "✅ 稳定可靠"
                ),
                notes = "国产模型，中文理解好。"
            )
            
            AIProvider.QWEN -> ProviderHelpInfo(
                name = "通义千问",
                isFree = true,
                apiKeyGuide = "访问 https://dashscope.aliyun.com 获取",
                recommendedModels = listOf(
                    "qwen-turbo (快速)",
                    "qwen-plus (增强)",
                    "qwen-max (旗舰)"
                ),
                features = listOf(
                    "✅ 阿里云大模型",
                    "✅ 免费额度",
                    "✅ 中文优化",
                    "✅ 企业级稳定"
                ),
                notes = "阿里云出品，稳定性好。"
            )
            
            AIProvider.KIMI -> ProviderHelpInfo(
                name = "Kimi (月之暗面)",
                isFree = false,
                apiKeyGuide = "访问 https://platform.moonshot.cn 获取",
                recommendedModels = listOf(
                    "moonshot-v1-8k",
                    "moonshot-v1-32k",
                    "moonshot-v1-128k (超长上下文)"
                ),
                features = listOf(
                    "✅ 超长上下文支持",
                    "✅ 中文优化",
                    "✅ 质量高",
                    "❌ 需要付费"
                ),
                notes = "需要付费，但支持超长上下文。"
            )
            
            AIProvider.OPENAI -> ProviderHelpInfo(
                name = "OpenAI",
                isFree = false,
                apiKeyGuide = "访问 https://platform.openai.com 获取",
                recommendedModels = listOf(
                    "gpt-4o (最新最强)",
                    "gpt-4o-mini (快速便宜)",
                    "gpt-3.5-turbo (经典)"
                ),
                features = listOf(
                    "✅ 业界领先",
                    "✅ 功能最强",
                    "✅ 生态完善",
                    "❌ 需要付费"
                ),
                notes = "最强大但需要付费。"
            )
            
            AIProvider.CUSTOM -> ProviderHelpInfo(
                name = "自定义",
                isFree = false,
                apiKeyGuide = "根据你的API提供商获取",
                recommendedModels = emptyList(),
                features = listOf(
                    "✅ 支持任意OpenAI兼容API",
                    "✅ 灵活配置",
                    "⚠️ 需要自行测试"
                ),
                notes = "适合有自己API的用户。"
            )
        }
    }
    
    /**
     * 获取免费提供商列表
     */
    fun getFreeProviders(): List<AIProvider> {
        return AIProvider.values().filter { it.isFree() }
    }
    
    /**
     * 获取推荐配置
     */
    fun getRecommendedConfig(): RecommendedConfig {
        return RecommendedConfig(
            beginners = AIProvider.GOOGLE,
            chinese = AIProvider.DEEPSEEK,
            advanced = AIProvider.OPENROUTER,
            enterprise = AIProvider.ZHIPU
        )
    }
}

/**
 * 提供商帮助信息
 */
data class ProviderHelpInfo(
    val name: String,
    val isFree: Boolean,
    val apiKeyGuide: String,
    val recommendedModels: List<String>,
    val features: List<String>,
    val notes: String
)

/**
 * 推荐配置
 */
data class RecommendedConfig(
    val beginners: AIProvider,      // 新手推荐
    val chinese: AIProvider,         // 中文用户推荐
    val advanced: AIProvider,        // 高级用户推荐
    val enterprise: AIProvider       // 企业用户推荐
)
