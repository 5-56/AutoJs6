package com.xihe.automation.ai.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * 通用AI客户端
 * 支持多个AI提供商
 */
class UniversalAIClient(
    private val provider: AIProvider,
    private val apiKey: String,
    private val baseUrl: String,
    private val model: String
) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * 发送聊天请求
     */
    suspend fun chat(
        messages: List<ChatMessage>,
        systemPrompt: String? = null,
        temperature: Double = 0.7,
        maxTokens: Int = 2000
    ): String = withContext(Dispatchers.IO) {
        return@withContext when (provider) {
            AIProvider.GOOGLE -> chatWithGoogle(messages, systemPrompt, temperature, maxTokens)
            else -> chatWithOpenAICompatible(messages, systemPrompt, temperature, maxTokens)
        }
    }
    
    /**
     * OpenAI兼容格式的聊天
     */
    private fun chatWithOpenAICompatible(
        messages: List<ChatMessage>,
        systemPrompt: String?,
        temperature: Double,
        maxTokens: Int
    ): String {
        val messagesArray = JSONArray().apply {
            // 添加系统提示
            if (!systemPrompt.isNullOrEmpty()) {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
            }
            
            // 添加对话历史
            messages.forEach { msg ->
                put(JSONObject().apply {
                    put("role", msg.role)
                    put("content", msg.content)
                })
            }
        }
        
        val requestBody = JSONObject().apply {
            put("model", model)
            put("messages", messagesArray)
            put("temperature", temperature)
            put("max_tokens", maxTokens)
        }.toString()
        
        val url = provider.getChatEndpoint(baseUrl)
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .apply {
                // OpenRouter需要额外的header
                if (provider == AIProvider.OPENROUTER) {
                    addHeader("HTTP-Referer", "https://xihe-automation.app")
                    addHeader("X-Title", "Xihe Automation")
                }
            }
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()
        
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
        
        if (!response.isSuccessful || responseBody == null) {
            throw Exception("AI请求失败: ${response.code} - ${responseBody}")
        }
        
        val json = JSONObject(responseBody)
        
        return try {
            json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        } catch (e: Exception) {
            Timber.e(e, "解析响应失败: $responseBody")
            throw Exception("解析响应失败: ${e.message}")
        }
    }
    
    /**
     * Google Gemini格式的聊天
     */
    private fun chatWithGoogle(
        messages: List<ChatMessage>,
        systemPrompt: String?,
        temperature: Double,
        maxTokens: Int
    ): String {
        // Google的格式：将消息转换为contents数组
        val contents = JSONArray()
        
        // 添加系统提示作为第一条user消息
        if (!systemPrompt.isNullOrEmpty()) {
            contents.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", "System: $systemPrompt")
                    })
                })
            })
        }
        
        // 添加对话历史
        messages.forEach { msg ->
            val role = when (msg.role) {
                "user" -> "user"
                "assistant" -> "model"
                "system" -> "user"  // 系统消息转为user
                else -> "user"
            }
            
            contents.put(JSONObject().apply {
                put("role", role)
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", msg.content)
                    })
                })
            })
        }
        
        val requestBody = JSONObject().apply {
            put("contents", contents)
            put("generationConfig", JSONObject().apply {
                put("temperature", temperature)
                put("maxOutputTokens", maxTokens)
            })
        }.toString()
        
        // Google的URL格式：.../models/{model}:generateContent?key={apiKey}
        val url = "${baseUrl}/models/${model}:generateContent?key=$apiKey"
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()
        
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
        
        if (!response.isSuccessful || responseBody == null) {
            throw Exception("Google AI请求失败: ${response.code} - ${responseBody}")
        }
        
        val json = JSONObject(responseBody)
        
        return try {
            json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        } catch (e: Exception) {
            Timber.e(e, "解析Google响应失败: $responseBody")
            throw Exception("解析响应失败: ${e.message}")
        }
    }
}

/**
 * 聊天消息
 */
data class ChatMessage(
    val role: String,  // "user", "assistant", "system"
    val content: String
)
