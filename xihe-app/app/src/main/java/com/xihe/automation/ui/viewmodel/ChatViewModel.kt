package com.xihe.automation.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xihe.automation.ai.AIConversationManager
import com.xihe.automation.ai.AIScriptGenerator
import com.xihe.automation.ai.ScreenAnalyzer
import com.xihe.automation.data.model.ChatMessage
import com.xihe.automation.data.model.MessageType
import com.xihe.automation.script.ScriptExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date

/**
 * 聊天界面的ViewModel
 */
class ChatViewModel : ViewModel() {

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val conversationManager = AIConversationManager()
    private val scriptGenerator = AIScriptGenerator()
    private val screenAnalyzer = ScreenAnalyzer()
    private val scriptExecutor = ScriptExecutor()

    fun addWelcomeMessage() {
        val welcomeMessage = ChatMessage(
            content = """
                你好！我是羲和AI助手 🤖
                
                我可以帮你：
                ✨ 编写自动化脚本
                📱 分析屏幕内容
                🔧 优化和调试脚本
                🚀 执行自动化任务
                
                请告诉我你想要实现什么功能，我会帮你生成相应的脚本。
                
                例如：
                • "帮我写一个自动签到的脚本"
                • "分析当前屏幕内容"
                • "自动点击屏幕上的确认按钮"
            """.trimIndent(),
            type = MessageType.AI,
            timestamp = Date()
        )
        
        addMessage(welcomeMessage)
    }

    suspend fun sendMessage(content: String) {
        // 添加用户消息
        val userMessage = ChatMessage(
            content = content,
            type = MessageType.USER,
            timestamp = Date()
        )
        addMessage(userMessage)

        _isLoading.value = true

        try {
            // 发送给AI处理
            val response = conversationManager.sendMessage(content)
            
            // 添加AI回复
            val aiMessage = ChatMessage(
                content = response.message,
                type = MessageType.AI,
                timestamp = Date()
            )
            addMessage(aiMessage)

            // 如果AI生成了脚本，显示脚本并询问是否执行
            if (response.hasScript) {
                val scriptMessage = ChatMessage(
                    content = "我已经生成了以下脚本：\n\n```javascript\n${response.script}\n```\n\n是否立即执行？",
                    type = MessageType.SCRIPT,
                    timestamp = Date(),
                    scriptContent = response.script
                )
                addMessage(scriptMessage)
            }

        } catch (e: Exception) {
            Timber.e(e, "发送消息失败")
            _errorMessage.value = "发送失败: ${e.message}"
            
            val errorMessage = ChatMessage(
                content = "抱歉，处理您的请求时出现了错误。请稍后重试。",
                type = MessageType.AI,
                timestamp = Date()
            )
            addMessage(errorMessage)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun captureAndAnalyzeScreen() {
        _isLoading.value = true
        
        try {
            val systemMessage = ChatMessage(
                content = "正在捕获并分析屏幕...",
                type = MessageType.SYSTEM,
                timestamp = Date()
            )
            addMessage(systemMessage)

            // 捕获屏幕
            val screenshot = withContext(Dispatchers.IO) {
                screenAnalyzer.captureScreen()
            }

            // 分析屏幕内容
            val analysis = withContext(Dispatchers.IO) {
                screenAnalyzer.analyzeScreen(screenshot)
            }

            // 显示分析结果
            val analysisMessage = ChatMessage(
                content = """
                    屏幕分析完成：
                    
                    📊 识别到的元素：
                    ${analysis.elements.joinToString("\n") { "• ${it.description}" }}
                    
                    📝 识别到的文字：
                    ${analysis.texts.joinToString("\n") { "• $it" }}
                    
                    你想对这些内容做什么操作呢？
                """.trimIndent(),
                type = MessageType.AI,
                timestamp = Date()
            )
            addMessage(analysisMessage)

        } catch (e: Exception) {
            Timber.e(e, "屏幕分析失败")
            _errorMessage.value = "屏幕分析失败: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun executeScript(script: String) {
        _isLoading.value = true
        
        try {
            val systemMessage = ChatMessage(
                content = "正在执行脚本...",
                type = MessageType.SYSTEM,
                timestamp = Date()
            )
            addMessage(systemMessage)

            // 执行脚本
            val result = withContext(Dispatchers.IO) {
                scriptExecutor.execute(script)
            }

            // 显示执行结果
            val resultMessage = ChatMessage(
                content = if (result.success) {
                    "✅ 脚本执行成功\n\n${result.output}"
                } else {
                    "❌ 脚本执行失败\n\n${result.error}"
                },
                type = MessageType.SYSTEM,
                timestamp = Date()
            )
            addMessage(resultMessage)

        } catch (e: Exception) {
            Timber.e(e, "脚本执行失败")
            _errorMessage.value = "脚本执行失败: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearMessages() {
        _messages.value = emptyList()
        addWelcomeMessage()
    }

    private fun addMessage(message: ChatMessage) {
        val currentMessages = _messages.value.orEmpty().toMutableList()
        currentMessages.add(message)
        _messages.value = currentMessages
    }
}
