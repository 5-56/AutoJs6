package org.autojs.autojs.agent.core.model

/**
 * Agent消息
 */
data class AgentMessage(
    val type: MessageType,
    val content: String,
    val data: Map<String, Any>? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 消息类型
 */
enum class MessageType {
    COMMAND,
    QUERY,
    RESPONSE,
    NOTIFICATION,
    ERROR
}

/**
 * 基础数据模型
 */
data class Rect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)

data class UIElementInfo(
    val id: String,
    val className: String,
    val text: String,
    val bounds: Rect,
    val isClickable: Boolean,
    val isScrollable: Boolean,
    val packageName: String
)

data class UserBehaviorData(
    val userId: String,
    val action: String,
    val context: String,
    val timestamp: Long,
    val metadata: Map<String, Any>? = null
)

data class DialogContext(
    val sessionId: String,
    val userId: String,
    val previousMessages: List<String>,
    val currentIntent: String?,
    val contextVariables: Map<String, Any>
)

data class LLMConfig(
    val modelType: String,
    val modelPath: String,
    val maxTokens: Int = 2048,
    val temperature: Float = 0.7f
)

data class AIModelConfig(
    val modelType: String,
    val modelPath: String,
    val inputSize: Pair<Int, Int>,
    val confidenceThreshold: Float = 0.5f
)

enum class ExecutionStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}