package com.xihe.automation.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xihe.automation.ai.XiheAIEngine
import com.xihe.automation.data.model.ChatMessage
import com.xihe.automation.data.model.MessageType
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date

/**
 * 聊天界面的ViewModel（集成AutoJs6真实功能）
 */
class ChatViewModel : ViewModel() {

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val aiEngine = XiheAIEngine.getInstance()

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
            Timber.i("处理用户消息: $content")
            
            // 使用XiheAIEngine处理（完整流程：分析->生成->执行->优化）
            val processResult = aiEngine.processUserMessage(content)
            
            // 添加所有生成的消息（包括分析、脚本、执行结果）
            processResult.messages.forEach { message ->
                addMessage(message)
            }

        } catch (e: Exception) {
            Timber.e(e, "处理消息失败")
            _errorMessage.value = "处理失败: ${e.message}"
            
            val errorMessage = ChatMessage(
                content = "抱歉，处理您的请求时出现了错误。\n\n错误信息: ${e.message}\n\n请检查：\n1. 无障碍服务是否启用\n2. 网络连接是否正常\n3. API密钥是否配置",
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
            Timber.i("开始屏幕分析")
            
            // 使用XiheAIEngine分析屏幕（真实AutoJs6功能）
            val messages = aiEngine.analyzeScreenOnly()
            
            // 添加所有消息
            messages.forEach { message ->
                addMessage(message)
            }

        } catch (e: Exception) {
            Timber.e(e, "屏幕分析失败")
            _errorMessage.value = "屏幕分析失败: ${e.message}"
            
            val errorMessage = ChatMessage(
                content = "屏幕分析失败: ${e.message}\n\n请确保已启用无障碍服务。",
                type = MessageType.SYSTEM,
                timestamp = Date()
            )
            addMessage(errorMessage)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun executeScript(script: String) {
        // 注意：新版本中脚本会在sendMessage时自动执行
        // 这个方法保留用于手动执行已有脚本
        _isLoading.value = true
        
        try {
            val systemMessage = ChatMessage(
                content = "正在执行脚本...",
                type = MessageType.SYSTEM,
                timestamp = Date()
            )
            addMessage(systemMessage)

            // 直接发送执行请求
            sendMessage("执行以下脚本：\n```javascript\n$script\n```")

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
