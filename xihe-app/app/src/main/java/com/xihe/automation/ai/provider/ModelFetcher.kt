package com.xihe.automation.ai.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * 模型获取器
 * 从AI提供商API获取可用模型列表
 */
class ModelFetcher {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    
    /**
     * 获取模型列表
     */
    suspend fun fetchModels(
        provider: AIProvider,
        apiKey: String,
        baseUrl: String = provider.defaultBaseUrl
    ): Result<List<AIModel>> = withContext(Dispatchers.IO) {
        return@withContext try {
            when (provider) {
                AIProvider.GOOGLE -> fetchGoogleModels(apiKey, baseUrl)
                AIProvider.OPENAI -> fetchOpenAIModels(apiKey, baseUrl)
                AIProvider.DEEPSEEK -> fetchOpenAICompatibleModels(provider, apiKey, baseUrl)
                AIProvider.KIMI -> fetchOpenAICompatibleModels(provider, apiKey, baseUrl)
                AIProvider.OPENROUTER -> fetchOpenRouterModels(apiKey, baseUrl)
                AIProvider.ZHIPU -> fetchZhipuModels(apiKey, baseUrl)
                AIProvider.QWEN -> fetchOpenAICompatibleModels(provider, apiKey, baseUrl)
                AIProvider.CUSTOM -> fetchOpenAICompatibleModels(provider, apiKey, baseUrl)
            }
        } catch (e: Exception) {
            Timber.e(e, "获取模型列表失败: ${provider.displayName}")
            // 返回默认模型列表
            Result.success(provider.getDefaultModels().map { 
                AIModel(id = it, name = it, provider = provider)
            })
        }
    }
    
    /**
     * 获取Google Gemini模型
     */
    private fun fetchGoogleModels(apiKey: String, baseUrl: String): Result<List<AIModel>> {
        val url = "$baseUrl/models?key=$apiKey"
        
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        
        val response = client.newCall(request).execute()
        val body = response.body?.string()
        
        if (!response.isSuccessful || body == null) {
            throw Exception("请求失败: ${response.code}")
        }
        
        val models = mutableListOf<AIModel>()
        val json = JSONObject(body)
        val modelsArray = json.optJSONArray("models")
        
        if (modelsArray != null) {
            for (i in 0 until modelsArray.length()) {
                val modelObj = modelsArray.getJSONObject(i)
                val name = modelObj.optString("name", "")
                
                // Google的模型名格式: models/gemini-pro
                val modelId = name.removePrefix("models/")
                
                // 只添加支持generateContent的模型
                val supportedActions = modelObj.optJSONArray("supportedGenerationMethods")
                if (supportedActions != null) {
                    var supportsGenerate = false
                    for (j in 0 until supportedActions.length()) {
                        if (supportedActions.getString(j) == "generateContent") {
                            supportsGenerate = true
                            break
                        }
                    }
                    
                    if (supportsGenerate) {
                        models.add(AIModel(
                            id = modelId,
                            name = modelObj.optString("displayName", modelId),
                            provider = AIProvider.GOOGLE,
                            description = modelObj.optString("description", "")
                        ))
                    }
                }
            }
        }
        
        return Result.success(models.ifEmpty { 
            AIProvider.GOOGLE.getDefaultModels().map { 
                AIModel(id = it, name = it, provider = AIProvider.GOOGLE)
            }
        })
    }
    
    /**
     * 获取OpenAI格式的模型
     */
    private fun fetchOpenAIModels(apiKey: String, baseUrl: String): Result<List<AIModel>> {
        return fetchOpenAICompatibleModels(AIProvider.OPENAI, apiKey, baseUrl)
    }
    
    /**
     * 获取OpenAI兼容格式的模型
     */
    private fun fetchOpenAICompatibleModels(
        provider: AIProvider,
        apiKey: String,
        baseUrl: String
    ): Result<List<AIModel>> {
        val url = provider.getModelsEndpoint(baseUrl)
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .get()
            .build()
        
        val response = client.newCall(request).execute()
        val body = response.body?.string()
        
        if (!response.isSuccessful || body == null) {
            throw Exception("请求失败: ${response.code}")
        }
        
        val models = mutableListOf<AIModel>()
        val json = JSONObject(body)
        val dataArray = json.optJSONArray("data")
        
        if (dataArray != null) {
            for (i in 0 until dataArray.length()) {
                val modelObj = dataArray.getJSONObject(i)
                val id = modelObj.optString("id", "")
                
                // 过滤掉非聊天模型
                if (id.isNotEmpty() && !id.contains("embedding") && 
                    !id.contains("whisper") && !id.contains("tts") &&
                    !id.contains("dall-e")) {
                    
                    models.add(AIModel(
                        id = id,
                        name = modelObj.optString("name", id),
                        provider = provider,
                        description = modelObj.optString("description", "")
                    ))
                }
            }
        }
        
        return Result.success(models.ifEmpty { 
            provider.getDefaultModels().map { 
                AIModel(id = it, name = it, provider = provider)
            }
        })
    }
    
    /**
     * 获取OpenRouter模型
     */
    private fun fetchOpenRouterModels(apiKey: String, baseUrl: String): Result<List<AIModel>> {
        val url = "$baseUrl/models"
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .get()
            .build()
        
        val response = client.newCall(request).execute()
        val body = response.body?.string()
        
        if (!response.isSuccessful || body == null) {
            throw Exception("请求失败: ${response.code}")
        }
        
        val models = mutableListOf<AIModel>()
        val json = JSONObject(body)
        val dataArray = json.optJSONArray("data")
        
        if (dataArray != null) {
            for (i in 0 until dataArray.length()) {
                val modelObj = dataArray.getJSONObject(i)
                val id = modelObj.optString("id", "")
                val pricing = modelObj.optJSONObject("pricing")
                
                // 优先显示免费模型
                val isFree = pricing?.optDouble("prompt", -1.0) == 0.0
                
                models.add(AIModel(
                    id = id,
                    name = modelObj.optString("name", id),
                    provider = AIProvider.OPENROUTER,
                    description = if (isFree) "免费" else modelObj.optString("description", ""),
                    isFree = isFree
                ))
            }
        }
        
        // 免费模型排在前面
        models.sortByDescending { it.isFree }
        
        return Result.success(models.ifEmpty { 
            AIProvider.OPENROUTER.getDefaultModels().map { 
                AIModel(id = it, name = it, provider = AIProvider.OPENROUTER, isFree = true)
            }
        })
    }
    
    /**
     * 获取智谱GLM模型
     */
    private fun fetchZhipuModels(apiKey: String, baseUrl: String): Result<List<AIModel>> {
        // 智谱的API格式不同，直接返回默认模型
        return Result.success(AIProvider.ZHIPU.getDefaultModels().map { 
            AIModel(id = it, name = it, provider = AIProvider.ZHIPU)
        })
    }
}

/**
 * AI模型数据类
 */
data class AIModel(
    val id: String,
    val name: String,
    val provider: AIProvider,
    val description: String = "",
    val isFree: Boolean = false
) {
    override fun toString(): String {
        return if (isFree) "$name (免费)" else name
    }
}
