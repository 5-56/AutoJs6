package org.autojs.autojs.ai

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.autojs.autojs.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * AI脚本生成器（使用真实的AI API）
 * 根据用户需求和屏幕信息生成AutoJs6脚本
 */
class AIScriptGenerator {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val conversationHistory = mutableListOf<Pair<String, String>>()

    companion object {
        private const val SYSTEM_PROMPT = """
你是羲和AI助手，一个专业的Android自动化脚本编写专家。

你的职责是根据用户需求和当前屏幕信息，生成可直接运行的AutoJs6脚本。

AutoJs6 API（你必须使用这些API）：
- auto() - 启用无障碍服务
- click(x, y) - 点击坐标
- press(x, y, duration) - 长按
- swipe(x1, y1, x2, y2, duration) - 滑动
- text(str).findOne(timeout) - 查找包含文本的控件
- id(resId).findOne(timeout) - 查找指定ID的控件
- className(name).findOne(timeout) - 查找指定类名的控件
- desc(str).findOne(timeout) - 查找包含描述的控件
- clickable(true).findOne(timeout) - 查找可点击控件
- UiObject.click() - 点击控件
- UiObject.setText(text) - 设置文本
- UiObject.bounds() - 获取边界
- sleep(ms) - 等待
- toast(msg) - 显示提示
- log(msg) - 打印日志
- images.captureScreen() - 截图
- requestScreenCapture() - 请求截图权限

脚本编写规则：
1. 必须以 auto(); 开始启用无障碍服务
2. 使用真实的AutoJs6 API，不要使用伪代码
3. 添加适当的等待时间（sleep）
4. 添加错误处理
5. 使用toast显示执行状态
6. 添加必要的注释

根据屏幕分析结果，优先使用：
- 如果找到了文本，使用 text().findOne()
- 如果有ID，使用 id().findOne()
- 如果有坐标，使用 click(x, y)

示例脚本：
```javascript
// 点击按钮示例
auto();
sleep(1000);
var btn = text("确定").findOne(5000);
if (btn) {
    btn.click();
    toast("点击成功");
} else {
    toast("未找到按钮");
}
```

请直接生成可执行的JavaScript代码，不要添加markdown标记。
"""
    }

    /**
     * 生成脚本
     */
    suspend fun generate(userRequest: String, screenInfo: ScreenInfo): String = withContext(Dispatchers.IO) {
        try {
            Timber.i("AI开始生成脚本，用户需求: $userRequest")
            
            // 检查API配置
            if (BuildConfig.AI_API_KEY.isEmpty() || BuildConfig.AI_API_URL.isEmpty()) {
                Timber.w("AI API未配置，使用后备方案")
                return@withContext generateFallbackScript(userRequest, screenInfo)
            }

            // 构建上下文
            val context = buildContext(screenInfo)
            
            // 调用AI API
            val response = callAIAPI(userRequest, context)
            
            // 提取脚本
            val script = extractScript(response)
            
            Timber.d("AI生成的脚本:\n$script")
            script
            
        } catch (e: Exception) {
            Timber.e(e, "AI脚本生成失败")
            // 使用后备方案
            generateFallbackScript(userRequest, screenInfo)
        }
    }

    /**
     * 构建上下文信息
     */
    private fun buildContext(screenInfo: ScreenInfo): String {
        return buildString {
            appendLine("当前屏幕分析结果：")
            appendLine()
            
            // 可点击元素
            val clickableElements = screenInfo.elements.filter { it.isClickable }
            if (clickableElements.isNotEmpty()) {
                appendLine("可点击元素：")
                clickableElements.take(10).forEach { element ->
                    appendLine("- ${element.className}")
                    if (element.text.isNotEmpty()) {
                        appendLine("  文本: \"${element.text}\"")
                    }
                    if (element.contentDesc.isNotEmpty()) {
                        appendLine("  描述: \"${element.contentDesc}\"")
                    }
                    if (element.viewId.isNotEmpty()) {
                        appendLine("  ID: ${element.viewId}")
                    }
                    appendLine("  坐标: (${element.centerX}, ${element.centerY})")
                }
            }
            
            appendLine()
            
            // 可滚动元素
            val scrollableElements = screenInfo.elements.filter { it.isScrollable }
            if (scrollableElements.isNotEmpty()) {
                appendLine("可滚动元素：")
                scrollableElements.take(5).forEach { element ->
                    appendLine("- ${element.className} at (${element.centerX}, ${element.centerY})")
                }
                appendLine()
            }
            
            // OCR识别的文字
            if (screenInfo.texts.isNotEmpty()) {
                appendLine("识别到的文字：")
                screenInfo.texts.take(20).forEach { text ->
                    appendLine("- \"$text\"")
                }
                appendLine()
            }
            
            appendLine("屏幕尺寸: ${screenInfo.screenWidth} x ${screenInfo.screenHeight}")
        }
    }

    /**
     * 调用AI API
     */
    private fun callAIAPI(userRequest: String, context: String): String {
        val messages = JSONArray().apply {
            // 系统提示
            put(JSONObject().apply {
                put("role", "system")
                put("content", SYSTEM_PROMPT)
            })
            
            // 对话历史
            conversationHistory.takeLast(5).forEach { (role, content) ->
                put(JSONObject().apply {
                    put("role", role)
                    put("content", content)
                })
            }
            
            // 当前请求
            put(JSONObject().apply {
                put("role", "user")
                put("content", "$context\n\n用户需求: $userRequest")
            })
        }

        val requestBody = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", messages)
            put("temperature", 0.7)
            put("max_tokens", 2000)
        }.toString()

        val request = Request.Builder()
            .url(BuildConfig.AI_API_URL)
            .addHeader("Authorization", "Bearer ${BuildConfig.AI_API_KEY}")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()

        if (!response.isSuccessful || responseBody == null) {
            throw Exception("AI API请求失败: ${response.code}")
        }

        val aiMessage = JSONObject(responseBody)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")

        // 保存对话历史
        conversationHistory.add(Pair("user", userRequest))
        conversationHistory.add(Pair("assistant", aiMessage))

        return aiMessage
    }

    /**
     * 从AI响应中提取脚本
     */
    private fun extractScript(aiResponse: String): String {
        // 尝试提取代码块
        val codeBlockPattern = Regex("```(?:javascript|js)?\\s*\\n([\\s\\S]*?)```")
        val match = codeBlockPattern.find(aiResponse)
        
        return if (match != null) {
            match.groupValues[1].trim()
        } else {
            // 如果没有代码块标记，返回整个响应
            aiResponse.trim()
        }
    }

    /**
     * 后备脚本生成（当AI API不可用时）
     */
    private fun generateFallbackScript(userRequest: String, screenInfo: ScreenInfo): String {
        Timber.i("使用后备脚本生成方案")
        
        return when {
            // 点击操作
            userRequest.contains("点击", ignoreCase = true) -> {
                generateClickScript(userRequest, screenInfo)
            }
            
            // 输入操作
            userRequest.contains("输入", ignoreCase = true) || 
            userRequest.contains("填写", ignoreCase = true) -> {
                generateInputScript(userRequest, screenInfo)
            }
            
            // 滑动操作
            userRequest.contains("滑动", ignoreCase = true) || 
            userRequest.contains("向上", ignoreCase = true) ||
            userRequest.contains("向下", ignoreCase = true) -> {
                generateSwipeScript(userRequest, screenInfo)
            }
            
            // 签到操作
            userRequest.contains("签到", ignoreCase = true) -> {
                generateSignInScript(screenInfo)
            }
            
            // 默认脚本
            else -> {
                generateDefaultScript(userRequest, screenInfo)
            }
        }
    }

    /**
     * 生成点击脚本
     */
    private fun generateClickScript(request: String, screenInfo: ScreenInfo): String {
        // 尝试从请求中提取目标文本
        val targetTexts = listOf("确定", "登录", "注册", "提交", "下一步", "完成", "保存")
        val target = targetTexts.find { request.contains(it) } ?: "目标"
        
        // 查找匹配的元素
        val element = screenInfo.elements.find { 
            it.isClickable && (it.text.contains(target) || it.contentDesc.contains(target))
        }
        
        return if (element != null) {
            """
            // 点击"${element.text.ifEmpty { target }}"
            auto();
            sleep(1000);
            
            var target = text("${element.text}").findOne(5000);
            if (target) {
                target.click();
                toast("点击成功");
                sleep(500);
            } else {
                // 尝试使用坐标点击
                click(${element.centerX}, ${element.centerY});
                toast("坐标点击完成");
            }
            """.trimIndent()
        } else {
            """
            // 查找并点击包含"$target"的控件
            auto();
            sleep(1000);
            
            var target = text("$target").findOne(5000);
            if (target) {
                target.click();
                toast("点击成功");
            } else {
                toast("未找到目标控件");
            }
            """.trimIndent()
        }
    }

    /**
     * 生成输入脚本
     */
    private fun generateInputScript(request: String, screenInfo: ScreenInfo): String {
        val inputElement = screenInfo.elements.find { 
            it.className.contains("EditText", ignoreCase = true)
        }
        
        return """
        // 自动输入
        auto();
        sleep(1000);
        
        var input = className("EditText").findOne(5000);
        if (input) {
            input.setText("请替换为实际内容");
            toast("输入成功");
        } else {
            toast("未找到输入框");
        }
        """.trimIndent()
    }

    /**
     * 生成滑动脚本
     */
    private fun generateSwipeScript(request: String, screenInfo: ScreenInfo): String {
        val width = screenInfo.screenWidth
        val height = screenInfo.screenHeight
        
        return """
        // 滑动屏幕
        auto();
        sleep(1000);
        
        // 向上滑动
        swipe(${width / 2}, ${height * 0.8}, ${width / 2}, ${height * 0.2}, 500);
        toast("滑动完成");
        """.trimIndent()
    }

    /**
     * 生成签到脚本
     */
    private fun generateSignInScript(screenInfo: ScreenInfo): String {
        return """
        // 自动签到
        auto();
        sleep(2000);
        
        // 查找签到按钮
        var signButton = textContains("签到").findOne(5000);
        if (!signButton) {
            signButton = descContains("签到").findOne(3000);
        }
        
        if (signButton) {
            signButton.click();
            toast("签到成功");
            sleep(1000);
        } else {
            toast("未找到签到按钮");
        }
        """.trimIndent()
    }

    /**
     * 生成默认脚本
     */
    private fun generateDefaultScript(request: String, screenInfo: ScreenInfo): String {
        return """
        // AutoJs6自动化脚本
        // 需求: $request
        
        auto();
        toast("脚本开始执行");
        sleep(2000);
        
        // 在这里添加你的自动化逻辑
        // 示例：查找并点击元素
        var element = text("示例文本").findOne(5000);
        if (element) {
            element.click();
            toast("操作完成");
        } else {
            toast("未找到目标元素");
        }
        """.trimIndent()
    }

    /**
     * 清空对话历史
     */
    fun clearHistory() {
        conversationHistory.clear()
    }
}
