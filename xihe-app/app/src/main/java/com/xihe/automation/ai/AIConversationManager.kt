package com.xihe.automation.ai

import com.google.gson.Gson
import com.xihe.automation.BuildConfig
import com.xihe.automation.data.model.AIResponse
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
 * AI对话管理器
 * 负责与AI API通信，管理对话上下文
 */
class AIConversationManager {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val conversationHistory = mutableListOf<Pair<String, String>>()

    companion object {
        private const val SYSTEM_PROMPT = """
你是羲和AI助手，一个专业的Android自动化脚本编写助手。你的主要职责是：

1. 帮助用户编写AutoJs风格的JavaScript自动化脚本
2. 分析用户需求并生成可执行的脚本代码
3. 提供脚本优化建议
4. 解释脚本的功能和使用方法

当用户请求编写脚本时，请：
- 使用AutoJs的API（如auto()、click()、setText()等）
- 生成完整可运行的JavaScript代码
- 添加必要的注释说明
- 考虑异常处理和错误提示

请记住：
- 保持简洁专业
- 提供可直接运行的代码
- 解释关键步骤
        """
    }

    /**
     * 发送消息给AI
     */
    suspend fun sendMessage(userMessage: String): AIResponse = withContext(Dispatchers.IO) {
        try {
            // 检查API配置
            if (BuildConfig.AI_API_KEY.isEmpty() || BuildConfig.AI_API_URL.isEmpty()) {
                return@withContext AIResponse(
                    message = "AI API未配置。请在local.properties中设置ai.api.key和ai.api.url",
                    hasScript = false
                )
            }

            // 添加到对话历史
            conversationHistory.add(Pair("user", userMessage))

            // 构建请求
            val requestBody = buildRequestBody(userMessage)
            
            val request = Request.Builder()
                .url(BuildConfig.AI_API_URL)
                .addHeader("Authorization", "Bearer ${BuildConfig.AI_API_KEY}")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            // 发送请求
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                throw Exception("API请求失败: ${response.code}")
            }

            // 解析响应
            parseResponse(responseBody)

        } catch (e: Exception) {
            Timber.e(e, "AI请求失败")
            AIResponse(
                message = "AI服务暂时不可用，但我可以根据常见模式为您生成脚本。\n\n" +
                        generateFallbackScript(userMessage),
                hasScript = true,
                script = generateFallbackScript(userMessage)
            )
        }
    }

    /**
     * 构建API请求体
     */
    private fun buildRequestBody(userMessage: String): String {
        val messages = JSONArray()
        
        // 添加系统提示
        messages.put(JSONObject().apply {
            put("role", "system")
            put("content", SYSTEM_PROMPT)
        })

        // 添加对话历史
        conversationHistory.takeLast(10).forEach { (role, content) ->
            messages.put(JSONObject().apply {
                put("role", role)
                put("content", content)
            })
        }

        return JSONObject().apply {
            put("model", "gpt-3.5-turbo") // 或其他模型
            put("messages", messages)
            put("temperature", 0.7)
            put("max_tokens", 2000)
        }.toString()
    }

    /**
     * 解析AI响应
     */
    private fun parseResponse(responseBody: String): AIResponse {
        val json = JSONObject(responseBody)
        val aiMessage = json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")

        // 添加到对话历史
        conversationHistory.add(Pair("assistant", aiMessage))

        // 检查响应中是否包含脚本
        val scriptPattern = Regex("```(?:javascript|js)\\s*\\n([\\s\\S]*?)```")
        val scriptMatch = scriptPattern.find(aiMessage)

        return if (scriptMatch != null) {
            val script = scriptMatch.groupValues[1].trim()
            AIResponse(
                message = aiMessage.replace(scriptPattern, "").trim(),
                hasScript = true,
                script = script
            )
        } else {
            AIResponse(
                message = aiMessage,
                hasScript = false
            )
        }
    }

    /**
     * 生成后备脚本（当API不可用时）
     */
    private fun generateFallbackScript(userMessage: String): String {
        return when {
            userMessage.contains("点击", ignoreCase = true) -> {
                """
                // 自动点击脚本
                auto();
                
                // 等待应用启动
                sleep(2000);
                
                // 查找并点击目标元素
                var target = text("确定").findOne(5000);
                if (target) {
                    target.click();
                    toast("点击成功");
                } else {
                    toast("未找到目标元素");
                }
                """.trimIndent()
            }
            userMessage.contains("签到", ignoreCase = true) -> {
                """
                // 自动签到脚本
                auto();
                
                // 等待应用启动
                sleep(2000);
                
                // 查找签到按钮
                var signButton = textContains("签到").findOne(5000);
                if (signButton) {
                    signButton.click();
                    sleep(1000);
                    toast("签到成功");
                } else {
                    toast("未找到签到按钮");
                }
                """.trimIndent()
            }
            userMessage.contains("输入", ignoreCase = true) -> {
                """
                // 自动输入脚本
                auto();
                
                // 等待应用启动
                sleep(2000);
                
                // 查找输入框
                var input = className("EditText").findOne(5000);
                if (input) {
                    input.setText("这是自动输入的文本");
                    toast("输入成功");
                } else {
                    toast("未找到输入框");
                }
                """.trimIndent()
            }
            else -> {
                """
                // 基础自动化脚本模板
                auto();
                
                // 等待界面加载
                sleep(2000);
                
                // 在这里添加你的自动化逻辑
                toast("脚本开始执行");
                
                // 示例：查找并点击元素
                var element = text("目标文本").findOne(5000);
                if (element) {
                    element.click();
                    toast("操作完成");
                } else {
                    toast("未找到目标元素");
                }
                """.trimIndent()
            }
        }
    }

    /**
     * 清空对话历史
     */
    fun clearHistory() {
        conversationHistory.clear()
    }
}
