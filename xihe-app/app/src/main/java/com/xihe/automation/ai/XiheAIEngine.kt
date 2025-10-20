package com.xihe.automation.ai

import com.xihe.automation.XiheApplication
import com.xihe.automation.autojs.runtime.ScriptRuntime
import com.xihe.automation.data.model.ChatMessage
import com.xihe.automation.data.model.MessageType
import com.xihe.automation.data.model.ScriptExecutionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date

/**
 * 羲和AI引擎
 * 整合AI对话、脚本生成、屏幕分析、执行和优化
 */
class XiheAIEngine {

    private val autoJs by lazy { XiheApplication.getAutoJs() }
    private val runtime: ScriptRuntime by lazy { autoJs.getRuntime() }
    
    private val conversationManager = AIConversationManager()
    private val scriptGenerator = AIScriptGenerator()
    private val screenAnalyzer = ScreenAnalyzer()
    private val scriptExecutor = ScriptExecutor()
    private val scriptOptimizer = ScriptOptimizer()

    companion object {
        private const val MAX_RETRY_TIMES = 3
        
        @Volatile
        private var instance: XiheAIEngine? = null
        
        fun getInstance(): XiheAIEngine {
            return instance ?: synchronized(this) {
                instance ?: XiheAIEngine().also { instance = it }
            }
        }
    }

    /**
     * 处理用户消息（完整流程）
     * 1. 分析屏幕
     * 2. AI生成脚本
     * 3. 执行脚本
     * 4. 失败则优化重试
     * 5. 返回结果
     */
    suspend fun processUserMessage(message: String): ProcessResult = withContext(Dispatchers.IO) {
        try {
            Timber.i("处理用户消息: $message")
            
            val chatMessages = mutableListOf<ChatMessage>()
            
            // 1. 分析当前屏幕（使用AutoJs6的真实功能）
            chatMessages.add(ChatMessage(
                content = "正在分析屏幕...",
                type = MessageType.SYSTEM,
                timestamp = Date()
            ))
            
            val screenAnalysis = analyzeCurrentScreen()
            
            chatMessages.add(ChatMessage(
                content = buildScreenAnalysisMessage(screenAnalysis),
                type = MessageType.SYSTEM,
                timestamp = Date()
            ))
            
            // 2. AI生成脚本
            chatMessages.add(ChatMessage(
                content = "正在生成脚本...",
                type = MessageType.SYSTEM,
                timestamp = Date()
            ))
            
            var script = generateScript(message, screenAnalysis)
            
            chatMessages.add(ChatMessage(
                content = "已生成脚本",
                type = MessageType.AI,
                timestamp = Date()
            ))
            
            chatMessages.add(ChatMessage(
                content = "```javascript\n$script\n```",
                type = MessageType.SCRIPT,
                timestamp = Date(),
                scriptContent = script
            ))
            
            // 3. 执行脚本
            var executionResult = executeScript(script)
            var retryCount = 0
            
            // 4. 如果失败，自动优化并重试
            while (!executionResult.success && retryCount < MAX_RETRY_TIMES) {
                Timber.w("执行失败，第 ${retryCount + 1} 次优化尝试")
                
                chatMessages.add(ChatMessage(
                    content = "执行失败，正在优化脚本... (尝试 ${retryCount + 1}/$MAX_RETRY_TIMES)",
                    type = MessageType.SYSTEM,
                    timestamp = Date()
                ))
                
                // AI优化脚本
                script = optimizeScript(script, executionResult, screenAnalysis)
                
                chatMessages.add(ChatMessage(
                    content = "已优化脚本",
                    type = MessageType.AI,
                    timestamp = Date()
                ))
                
                chatMessages.add(ChatMessage(
                    content = "```javascript\n$script\n```",
                    type = MessageType.SCRIPT,
                    timestamp = Date(),
                    scriptContent = script
                ))
                
                // 重新执行
                executionResult = executeScript(script)
                retryCount++
            }
            
            // 5. 显示最终结果
            chatMessages.add(ChatMessage(
                content = if (executionResult.success) {
                    "✅ 脚本执行成功\n\n${executionResult.output}"
                } else {
                    "❌ 脚本执行失败\n\n错误: ${executionResult.error}\n\n请检查无障碍服务是否启用，或尝试修改需求描述。"
                },
                type = MessageType.SYSTEM,
                timestamp = Date()
            ))
            
            ProcessResult(
                success = executionResult.success,
                messages = chatMessages,
                finalScript = script,
                executionResult = executionResult
            )
            
        } catch (e: Exception) {
            Timber.e(e, "处理用户消息失败")
            ProcessResult(
                success = false,
                messages = listOf(
                    ChatMessage(
                        content = "处理失败: ${e.message}",
                        type = MessageType.SYSTEM,
                        timestamp = Date()
                    )
                ),
                finalScript = "",
                executionResult = ScriptExecutionResult(
                    success = false,
                    error = e.message
                )
            )
        }
    }

    /**
     * 分析当前屏幕（使用AutoJs6的真实功能）
     */
    private suspend fun analyzeCurrentScreen() = withContext(Dispatchers.IO) {
        screenAnalyzer.analyzeScreen(runtime)
    }

    /**
     * 生成脚本
     */
    private suspend fun generateScript(request: String, screenAnalysis: com.xihe.automation.data.model.ScreenAnalysis) = 
        withContext(Dispatchers.IO) {
            scriptGenerator.generateScript(request, screenAnalysis)
        }

    /**
     * 执行脚本（使用AutoJs6的脚本引擎）
     */
    private suspend fun executeScript(script: String) = withContext(Dispatchers.IO) {
        scriptExecutor.executeWithAutoJs(script, runtime)
    }

    /**
     * 优化脚本
     */
    private suspend fun optimizeScript(
        script: String,
        result: ScriptExecutionResult,
        screenAnalysis: com.xihe.automation.data.model.ScreenAnalysis
    ) = withContext(Dispatchers.IO) {
        scriptOptimizer.optimize(script, result, screenAnalysis)
    }

    /**
     * 构建屏幕分析消息
     */
    private fun buildScreenAnalysisMessage(analysis: com.xihe.automation.data.model.ScreenAnalysis): String {
        return buildString {
            appendLine("📊 屏幕分析完成")
            appendLine()
            
            if (analysis.elements.isNotEmpty()) {
                appendLine("发现 ${analysis.elements.size} 个UI元素")
                val clickable = analysis.elements.filter { it.isClickable }
                if (clickable.isNotEmpty()) {
                    appendLine("其中 ${clickable.size} 个可点击")
                }
            }
            
            if (analysis.texts.isNotEmpty()) {
                appendLine()
                appendLine("识别到 ${analysis.texts.size} 段文字")
                appendLine("文字内容:")
                analysis.texts.take(5).forEach {
                    appendLine("• $it")
                }
                if (analysis.texts.size > 5) {
                    appendLine("• ...还有 ${analysis.texts.size - 5} 段")
                }
            }
        }
    }

    /**
     * 只分析屏幕不执行
     */
    suspend fun analyzeScreenOnly(): List<ChatMessage> = withContext(Dispatchers.IO) {
        val messages = mutableListOf<ChatMessage>()
        
        messages.add(ChatMessage(
            content = "正在分析屏幕...",
            type = MessageType.SYSTEM,
            timestamp = Date()
        ))
        
        val analysis = analyzeCurrentScreen()
        
        messages.add(ChatMessage(
            content = buildDetailedScreenAnalysis(analysis),
            type = MessageType.AI,
            timestamp = Date()
        ))
        
        messages
    }

    /**
     * 构建详细的屏幕分析报告
     */
    private fun buildDetailedScreenAnalysis(analysis: com.xihe.automation.data.model.ScreenAnalysis): String {
        return buildString {
            appendLine("📱 屏幕详细分析报告")
            appendLine("═══════════════════════")
            appendLine()
            
            // 可点击元素
            val clickableElements = analysis.elements.filter { it.isClickable }
            if (clickableElements.isNotEmpty()) {
                appendLine("🖱️ 可点击元素 (${clickableElements.size}个):")
                clickableElements.take(10).forEach { element ->
                    appendLine("• ${element.description}")
                    if (element.text?.isNotEmpty() == true) {
                        appendLine("  文本: \"${element.text}\"")
                    }
                }
                if (clickableElements.size > 10) {
                    appendLine("  ...还有 ${clickableElements.size - 10} 个")
                }
                appendLine()
            }
            
            // 输入框
            val inputElements = analysis.elements.filter { 
                it.type.contains("EditText", ignoreCase = true) 
            }
            if (inputElements.isNotEmpty()) {
                appendLine("⌨️ 输入框 (${inputElements.size}个):")
                inputElements.forEach { element ->
                    appendLine("• ${element.description}")
                }
                appendLine()
            }
            
            // 可滚动元素
            val scrollableElements = analysis.elements.filter { it.isScrollable }
            if (scrollableElements.isNotEmpty()) {
                appendLine("📜 可滚动元素 (${scrollableElements.size}个)")
                appendLine()
            }
            
            // 识别的文字
            if (analysis.texts.isNotEmpty()) {
                appendLine("📝 识别的文字 (${analysis.texts.size}段):")
                analysis.texts.take(15).forEach { text ->
                    appendLine("• $text")
                }
                if (analysis.texts.size > 15) {
                    appendLine("  ...还有 ${analysis.texts.size - 15} 段")
                }
            }
            
            appendLine()
            appendLine("你想对这些内容进行什么操作？")
        }
    }
}

/**
 * 处理结果
 */
data class ProcessResult(
    val success: Boolean,
    val messages: List<ChatMessage>,
    val finalScript: String,
    val executionResult: ScriptExecutionResult
)
