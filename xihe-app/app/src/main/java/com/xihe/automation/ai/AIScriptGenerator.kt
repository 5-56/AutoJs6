package com.xihe.automation.ai

import com.google.gson.Gson
import com.xihe.automation.BuildConfig
import com.xihe.automation.data.model.ScreenAnalysis
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
 * AI脚本生成器（使用真实AI API + AutoJs6 API）
 * 根据用户需求和屏幕分析结果生成AutoJs6脚本
 */
class AIScriptGenerator {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    
    private val conversationHistory = mutableListOf<Pair<String, String>>()

    /**
     * 根据用户需求生成脚本（智能版）
     */
    suspend fun generateScript(userIntent: String, screenAnalysis: ScreenAnalysis? = null): String = 
        withContext(Dispatchers.IO) {
        return@withContext try {
            val context = if (screenAnalysis != null) {
                buildContextFromScreen(screenAnalysis)
            } else {
                ""
            }
            
            // 尝试使用AI生成
            if (BuildConfig.AI_API_KEY.isNotEmpty() && BuildConfig.AI_API_URL.isNotEmpty()) {
                try {
                    generateWithAI(userIntent, context)
                } catch (e: Exception) {
                    Timber.w(e, "AI生成失败，使用后备方案")
                    buildScript(userIntent, screenAnalysis)
                }
            } else {
                // 使用智能后备方案
                buildScript(userIntent, screenAnalysis)
            }
        } catch (e: Exception) {
            Timber.e(e, "脚本生成失败")
            generateDefaultScript()
        }
    }
    
    /**
     * 使用AI API生成脚本
     */
    private suspend fun generateWithAI(userIntent: String, context: String): String {
        val systemPrompt = """
你是AutoJs6脚本专家。请根据用户需求和屏幕信息生成可执行的JavaScript脚本。

AutoJs6可用API:
- auto() - 启用无障碍服务（必须）
- click(x, y) - 点击坐标
- press(x, y, duration) - 长按
- swipe(x1, y1, x2, y2, duration) - 滑动
- text("文本").findOne(timeout) - 查找文本控件
- textContains("文本").findOne(timeout) - 模糊匹配文本
- id("id").findOne(timeout) - 查找ID
- className("类名").findOne(timeout) - 查找类名
- desc("描述").findOne(timeout) - 查找描述
- clickable(true).find() - 查找可点击控件
- element.click() - 点击控件
- element.setText("文本") - 设置文本
- element.bounds() - 获取边界
- sleep(ms) - 等待
- toast("消息") - 显示提示
- requestScreenCapture() - 请求截图权限
- exit() - 退出脚本

注意事项:
1. 必须以auto()开始
2. 使用真实的AutoJs6 API
3. 添加适当的sleep等待
4. 添加错误处理
5. 使用toast显示状态
6. 只生成JavaScript代码，不要markdown标记

根据屏幕信息优先选择最准确的选择器。
        """.trimIndent()
        
        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })
            
            conversationHistory.takeLast(5).forEach { (role, content) ->
                put(JSONObject().apply {
                    put("role", role)
                    put("content", content)
                })
            }
            
            put(JSONObject().apply {
                put("role", "user")
                put("content", "$context\n\n用户需求: $userIntent\n\n请生成AutoJs6脚本:")
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
            throw Exception("AI API失败: ${response.code}")
        }
        
        val aiMessage = JSONObject(responseBody)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
        
        conversationHistory.add(Pair("user", userIntent))
        conversationHistory.add(Pair("assistant", aiMessage))
        
        // 提取脚本
        extractScript(aiMessage)
    }
    
    /**
     * 提取脚本代码
     */
    private fun extractScript(aiResponse: String): String {
        val codePattern = Regex("```(?:javascript|js)?\\s*\\n([\\s\\S]*?)```")
        val match = codePattern.find(aiResponse)
        return match?.groupValues?.get(1)?.trim() ?: aiResponse.trim()
    }

    /**
     * 从屏幕分析结果构建上下文
     */
    private fun buildContextFromScreen(analysis: ScreenAnalysis): String {
        return buildString {
            appendLine("当前屏幕信息：")
            appendLine()
            
            // 可点击元素
            val clickable = analysis.elements.filter { it.isClickable }
            if (clickable.isNotEmpty()) {
                appendLine("可点击元素 (${clickable.size}个):")
                clickable.take(10).forEach { element ->
                    appendLine("- ${element.type}")
                    if (element.text?.isNotEmpty() == true) {
                        appendLine("  文本: \"${element.text}\"")
                    }
                    element.bounds?.let { bounds ->
                        appendLine("  坐标: (${bounds.centerX}, ${bounds.centerY})")
                    }
                }
                appendLine()
            }
            
            // 识别的文字
            if (analysis.texts.isNotEmpty()) {
                appendLine("OCR识别的文字:")
                analysis.texts.take(15).forEach { text ->
                    appendLine("- \"$text\"")
                }
                appendLine()
            }
        }
    }

    /**
     * 构建脚本（智能后备方案）
     */
    private fun buildScript(userIntent: String, screenAnalysis: ScreenAnalysis?): String {
        return when {
            userIntent.contains("点击") -> 
                generateClickScript(userIntent, screenAnalysis)
            userIntent.contains("滑动") || userIntent.contains("向上") || userIntent.contains("向下") -> 
                generateSwipeScript(userIntent, screenAnalysis)
            userIntent.contains("输入") || userIntent.contains("填写") -> 
                generateInputScript(userIntent, screenAnalysis)
            userIntent.contains("等待") -> 
                generateWaitScript(userIntent, screenAnalysis)
            userIntent.contains("循环") || userIntent.contains("重复") -> 
                generateLoopScript(userIntent, screenAnalysis)
            userIntent.contains("签到") -> 
                generateSignInScript(screenAnalysis)
            else -> 
                generateDefaultScript()
        }
    }

    /**
     * 生成点击脚本（基于真实屏幕分析）
     */
    private fun generateClickScript(intent: String, screenAnalysis: ScreenAnalysis?): String {
        // 尝试从需求中提取目标文本
        val keywords = listOf("确定", "登录", "注册", "提交", "下一步", "完成", "保存", "同意")
        val targetText = keywords.find { intent.contains(it) }
        
        // 从屏幕分析中查找匹配的元素
        val matchedElement = screenAnalysis?.elements?.find { element ->
            targetText?.let { 
                element.text?.contains(it) == true || 
                element.description.contains(it)
            } ?: false
        }
        
        return if (matchedElement != null) {
            // 找到了匹配的元素，生成精确的脚本
            """
            // 点击"${matchedElement.text ?: targetText}"
            auto();
            sleep(1500);
            
            // 优先使用文本查找
            var target = text("${matchedElement.text}").findOne(5000);
            
            if (target) {
                target.click();
                toast("点击成功");
                sleep(800);
            } else {
                // 备用方案：使用坐标点击
                click(${matchedElement.bounds?.centerX}, ${matchedElement.bounds?.centerY});
                toast("坐标点击完成");
                sleep(800);
            }
            """.trimIndent()
        } else if (targetText != null) {
            // 有目标文本但未在屏幕找到，生成通用查找脚本
            """
            // 查找并点击"$targetText"
            auto();
            sleep(1500);
            
            // 尝试多种方式查找
            var target = text("$targetText").findOne(5000);
            if (!target) {
                target = textContains("$targetText").findOne(3000);
            }
            if (!target) {
                target = desc("$targetText").findOne(3000);
            }
            
            if (target) {
                target.click();
                toast("点击成功");
                sleep(800);
            } else {
                toast("未找到目标: $targetText");
            }
            """.trimIndent()
        } else {
            // 没有明确目标，生成通用脚本
            """
            // 自动点击脚本
            auto();
            sleep(1500);
            
            // 请修改以下文本为实际目标
            var target = text("目标文本").findOne(5000);
            
            if (target) {
                target.click();
                toast("点击成功");
            } else {
                toast("未找到目标元素");
            }
            """.trimIndent()
        }
    }

    /**
     * 生成滑动脚本（基于屏幕尺寸）
     */
    private fun generateSwipeScript(intent: String, screenAnalysis: ScreenAnalysis?): String {
        val direction = when {
            intent.contains("向上") || intent.contains("上滑") -> "up"
            intent.contains("向下") || intent.contains("下滑") -> "down"
            intent.contains("向左") || intent.contains("左滑") -> "left"
            intent.contains("向右") || intent.contains("右滑") -> "right"
            else -> "up"
        }
        
        return """
            // 滑动屏幕
            auto();
            sleep(1000);
            
            var width = device.width;
            var height = device.height;
            
            // ${when(direction) {
                "up" -> "向上滑动"
                "down" -> "向下滑动"
                "left" -> "向左滑动"
                "right" -> "向右滑动"
                else -> "滑动"
            }}
            ${when(direction) {
                "up" -> "swipe(width / 2, height * 0.8, width / 2, height * 0.2, 500);"
                "down" -> "swipe(width / 2, height * 0.2, width / 2, height * 0.8, 500);"
                "left" -> "swipe(width * 0.8, height / 2, width * 0.2, height / 2, 500);"
                "right" -> "swipe(width * 0.2, height / 2, width * 0.8, height / 2, 500);"
                else -> "swipe(width / 2, height * 0.8, width / 2, height * 0.2, 500);"
            }}
            toast("滑动完成");
            sleep(800);
        """.trimIndent()
    }

    /**
     * 生成输入脚本（查找真实输入框）
     */
    private fun generateInputScript(intent: String, screenAnalysis: ScreenAnalysis?): String {
        // 查找输入框
        val inputElement = screenAnalysis?.elements?.find { 
            it.type.contains("EditText", ignoreCase = true)
        }
        
        // 尝试从需求中提取要输入的内容
        val inputPattern = Regex("""输入[""]([^""]+)[""]""")
        val inputMatch = inputPattern.find(intent)
        val inputText = inputMatch?.groupValues?.get(1) ?: "请替换为实际内容"
        
        return if (inputElement != null) {
            """
            // 自动输入
            auto();
            sleep(1500);
            
            // 查找输入框
            var input = className("EditText").findOne(5000);
            if (input) {
                input.setText("$inputText");
                toast("输入成功");
                sleep(500);
            } else {
                toast("未找到输入框");
            }
            """.trimIndent()
        } else {
            """
            // 自动输入
            auto();
            sleep(1500);
            
            // 查找第一个可编辑的输入框
            var input = className("EditText").findOne(5000);
            if (!input) {
                input = editable(true).findOne(3000);
            }
            
            if (input) {
                input.setText("$inputText");
                toast("输入成功");
                sleep(500);
            } else {
                toast("未找到输入框");
            }
            """.trimIndent()
        }
    }

    /**
     * 生成等待脚本
     */
    private fun generateWaitScript(intent: String, screenAnalysis: ScreenAnalysis?): String {
        val keywords = listOf("加载", "完成", "成功", "确定")
        val waitFor = keywords.find { intent.contains(it) } ?: "目标元素"
        
        return """
            // 等待元素出现
            auto();
            sleep(1000);
            
            toast("正在等待...");
            
            // 等待特定元素出现（最多10秒）
            var element = text("$waitFor").findOne(10000);
            
            if (element) {
                toast("元素已出现: $waitFor");
                sleep(500);
            } else {
                toast("等待超时");
            }
        """.trimIndent()
    }

    /**
     * 生成循环脚本
     */
    private fun generateLoopScript(intent: String, screenAnalysis: ScreenAnalysis?): String {
        // 尝试提取循环次数
        val countPattern = Regex("""(\d+)次""")
        val countMatch = countPattern.find(intent)
        val count = countMatch?.groupValues?.get(1)?.toIntOrNull() ?: 5
        
        return """
            // 循环执行
            auto();
            
            var count = $count;
            toast("开始循环 " + count + " 次");
            
            for (var i = 0; i < count; i++) {
                log("第 " + (i + 1) + " 次执行");
                toast("执行 " + (i + 1) + "/" + count);
                
                // 在这里添加循环执行的操作
                // 示例：点击下一页
                var nextBtn = text("下一页").findOne(3000);
                if (nextBtn) {
                    nextBtn.click();
                    sleep(2000);
                }
            }
            
            toast("循环完成");
        """.trimIndent()
    }

    /**
     * 生成签到脚本
     */
    private fun generateSignInScript(screenAnalysis: ScreenAnalysis?): String {
        // 在屏幕中查找签到相关元素
        val signInElement = screenAnalysis?.elements?.find { element ->
            element.text?.contains("签到") == true ||
            element.description.contains("签到")
        }
        
        return if (signInElement != null) {
            """
            // 自动签到
            auto();
            sleep(2000);
            
            // 查找签到按钮
            var signBtn = text("${signInElement.text}").findOne(5000);
            
            if (signBtn) {
                signBtn.click();
                toast("签到成功");
                sleep(1000);
            } else {
                toast("未找到签到按钮");
            }
            """.trimIndent()
        } else {
            """
            // 自动签到
            auto();
            sleep(2000);
            
            // 多种方式查找签到按钮
            var signBtn = text("签到").findOne(5000);
            if (!signBtn) {
                signBtn = textContains("签到").findOne(3000);
            }
            if (!signBtn) {
                signBtn = desc("签到").findOne(3000);
            }
            
            if (signBtn) {
                signBtn.click();
                toast("签到成功");
                sleep(1000);
            } else {
                toast("未找到签到按钮");
            }
            """.trimIndent()
        }
    }

    /**
     * 生成默认脚本
     */
    private fun generateDefaultScript(): String {
        return """
            // AutoJs6自动化脚本
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
            
            toast("脚本执行完成");
        """.trimIndent()
    }
    
    /**
     * 清空对话历史
     */
    fun clearHistory() {
        conversationHistory.clear()
    }

    /**
     * 优化脚本
     */
    fun optimizeScript(script: String, executionResult: String): String {
        // TODO: 基于执行结果优化脚本
        return script
    }
}
