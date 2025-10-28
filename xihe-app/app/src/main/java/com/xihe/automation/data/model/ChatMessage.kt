package com.xihe.automation.data.model

import java.util.Date
import java.util.UUID

/**
 * 聊天消息数据类
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val type: MessageType,
    val timestamp: Date,
    val scriptContent: String? = null,
    val isExecuting: Boolean = false
)

/**
 * 消息类型
 */
enum class MessageType {
    USER,      // 用户消息
    AI,        // AI回复
    SCRIPT,    // 脚本消息
    SYSTEM     // 系统消息
}
