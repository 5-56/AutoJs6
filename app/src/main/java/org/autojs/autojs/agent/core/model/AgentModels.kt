package org.autojs.autojs.agent.core.model

/**
 * Agent事件类型
 */
enum class AgentEvent {
    INITIALIZED,
    STARTED,
    STOPPED,
    RESTARTED,
    DESTROYED,
    ERROR,
    MESSAGE_PROCESSED,
    TASK_COMPLETED
}

/**
 * Agent事件数据类
 */
data class AgentEventData(
    val event: AgentEvent,
    var data: Any? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Agent消息类型
 */
enum class MessageType {
    COMMAND,
    QUERY,
    NOTIFICATION,
    RESPONSE,
    ERROR
}

/**
 * Agent消息数据类
 */
data class AgentMessage(
    val type: MessageType,
    val content: String,
    val data: Map<String, Any>? = null,
    val sender: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * LLM配置
 */
data class LLMConfig(
    val modelType: String,
    val modelPath: String,
    val maxTokens: Int,
    val temperature: Float
)

/**
 * AI模型配置
 */
data class AIModelConfig(
    val modelType: String,
    val modelPath: String,
    val confidenceThreshold: Float
)

/**
 * 脚本生成请求
 */
data class ScriptGenerationRequest(
    val description: String,
    val screenshotPath: String? = null,
    val templateType: String? = null,
    val preferences: Map<String, Any>? = null
)

/**
 * 脚本生成响应
 */
data class ScriptGenerationResponse(
    val success: Boolean,
    val script: String,
    val suggestions: List<String>? = null,
    val errorMessage: String? = null
)

/**
 * 执行监控数据
 */
data class ExecutionMonitorData(
    val scriptPath: String,
    val status: ExecutionStatus,
    val startTime: Long,
    val endTime: Long? = null,
    val errorMessage: String? = null,
    val logs: List<String>? = null
)

/**
 * 执行状态
 */
enum class ExecutionStatus {
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED,
    TIMEOUT
}

/**
 * 用户行为数据
 */
data class UserBehaviorData(
    val action: String,
    val context: String,
    val timestamp: Long,
    val metadata: Map<String, Any>? = null
)

/**
 * UI元素信息
 */
data class UIElementInfo(
    val id: String,
    val className: String,
    val text: String,
    val bounds: Rect,
    val isClickable: Boolean,
    val isScrollable: Boolean,
    val packageName: String
)

/**
 * 矩形区域
 */
data class Rect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)

/**
 * 对话上下文
 */
data class DialogContext(
    val sessionId: String,
    val userId: String,
    val history: List<DialogMessage>,
    val currentIntent: String? = null,
    val entities: Map<String, Any>? = null
)

/**
 * 对话消息
 */
data class DialogMessage(
    val role: String, // user, assistant, system
    val content: String,
    val timestamp: Long
)