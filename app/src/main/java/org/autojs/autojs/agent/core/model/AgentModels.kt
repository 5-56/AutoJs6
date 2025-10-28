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
 * Agent间消息
 */
data class InterAgentMessage(
    val fromAgentId: String,
    val toAgentId: String,
    val message: AgentMessage,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 工作流请求
 */
data class WorkflowRequest(
    val workflowId: String,
    val name: String,
    val description: String,
    val steps: List<WorkflowStep>,
    val priority: Int = 0,
    val maxRetries: Int = 3,
    val timeout: Long = 30000L
)

/**
 * 工作流步骤
 */
data class WorkflowStep(
    val stepId: String,
    val agentId: String,
    val command: String,
    val parameters: Map<String, Any> = emptyMap(),
    val expectedDuration: Long = 5000L,
    val dependencies: List<String> = emptyList()
)

/**
 * 工作流响应
 */
data class WorkflowResponse(
    val workflowId: String,
    val success: Boolean,
    val message: String,
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
    ERROR,
    WORKFLOW_START,
    WORKFLOW_COMPLETE,
    WORKFLOW_FAILED,
    HEALTH_CHECK,
    STATUS_UPDATE
}

/**
 * 基础数据模型
 */
data class Rect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top
    val centerX: Int get() = left + width / 2
    val centerY: Int get() = top + height / 2
    
    fun contains(x: Int, y: Int): Boolean {
        return x >= left && x <= right && y >= top && y <= bottom
    }
    
    fun intersects(other: Rect): Boolean {
        return left < other.right && right > other.left && top < other.bottom && bottom > other.top
    }
}

data class UIElementInfo(
    val id: String,
    val className: String,
    val text: String,
    val bounds: Rect,
    val isClickable: Boolean,
    val isScrollable: Boolean,
    val packageName: String,
    val resourceId: String? = null,
    val contentDescription: String? = null,
    val isEnabled: Boolean = true,
    val isVisible: Boolean = true,
    val isCheckable: Boolean = false,
    val isChecked: Boolean = false,
    val isFocusable: Boolean = false,
    val isFocused: Boolean = false,
    val isSelected: Boolean = false,
    val depth: Int = 0,
    val parent: String? = null,
    val children: List<String> = emptyList()
)

data class UserBehaviorData(
    val userId: String,
    val action: String,
    val context: String,
    val timestamp: Long,
    val metadata: Map<String, Any>? = null,
    val duration: Long = 0L,
    val success: Boolean = true,
    val errorMessage: String? = null
)

data class DialogContext(
    val sessionId: String,
    val userId: String,
    val previousMessages: List<String>,
    val currentIntent: String?,
    val contextVariables: Map<String, Any>,
    val conversationState: ConversationState = ConversationState.ACTIVE,
    val lastActivityTime: Long = System.currentTimeMillis()
)

/**
 * 对话状态
 */
enum class ConversationState {
    ACTIVE,
    WAITING_FOR_INPUT,
    PROCESSING,
    COMPLETED,
    CANCELLED,
    TIMEOUT
}

data class LLMConfig(
    val modelType: String,
    val modelPath: String,
    val maxTokens: Int = 2048,
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val frequencyPenalty: Float = 0.0f,
    val presencePenalty: Float = 0.0f,
    val stopSequences: List<String> = emptyList()
)

data class AIModelConfig(
    val modelType: String,
    val modelPath: String,
    val inputSize: Pair<Int, Int>,
    val confidenceThreshold: Float = 0.5f,
    val nmsThreshold: Float = 0.4f,
    val maxDetections: Int = 100,
    val useGPU: Boolean = false,
    val numThreads: Int = 4
)

enum class ExecutionStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED,
    TIMEOUT,
    SKIPPED
}

/**
 * 性能指标
 */
data class PerformanceMetrics(
    val cpuUsage: Float = 0.0f,
    val memoryUsage: Long = 0L,
    val executionTime: Long = 0L,
    val throughput: Float = 0.0f,
    val errorRate: Float = 0.0f,
    val successRate: Float = 0.0f,
    val averageResponseTime: Long = 0L,
    val maxResponseTime: Long = 0L,
    val minResponseTime: Long = 0L
)

/**
 * 系统资源
 */
data class SystemResources(
    val totalMemory: Long,
    val freeMemory: Long,
    val usedMemory: Long,
    val cpuCores: Int,
    val diskSpaceTotal: Long,
    val diskSpaceFree: Long,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val networkType: String,
    val isNetworkAvailable: Boolean
)

/**
 * Agent健康状态
 */
data class AgentHealth(
    val agentId: String,
    val isHealthy: Boolean,
    val lastHealthCheck: Long,
    val issues: List<String> = emptyList(),
    val metrics: PerformanceMetrics,
    val uptime: Long = 0L,
    val restartCount: Int = 0,
    val errorCount: Int = 0
)

/**
 * 错误信息
 */
data class ErrorInfo(
    val code: String,
    val message: String,
    val details: String? = null,
    val stackTrace: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val agentId: String? = null,
    val context: Map<String, Any>? = null
)

/**
 * 配置项
 */
data class ConfigItem(
    val key: String,
    val value: Any,
    val type: ConfigType,
    val defaultValue: Any? = null,
    val description: String? = null,
    val category: String? = null,
    val isRequired: Boolean = false,
    val validationRules: List<String> = emptyList()
)

/**
 * 配置类型
 */
enum class ConfigType {
    BOOLEAN,
    INTEGER,
    FLOAT,
    STRING,
    LIST,
    MAP,
    OBJECT
}

/**
 * 事件
 */
data class AgentEvent(
    val eventId: String,
    val eventType: String,
    val agentId: String,
    val timestamp: Long,
    val data: Map<String, Any> = emptyMap(),
    val severity: EventSeverity = EventSeverity.INFO
)

/**
 * 事件严重程度
 */
enum class EventSeverity {
    DEBUG,
    INFO,
    WARN,
    ERROR,
    CRITICAL
}

/**
 * 缓存条目
 */
data class CacheEntry<T>(
    val key: String,
    val value: T,
    val timestamp: Long,
    val ttl: Long = -1L,
    val accessCount: Int = 0
) {
    fun isExpired(): Boolean {
        return if (ttl > 0) {
            System.currentTimeMillis() - timestamp > ttl
        } else {
            false
        }
    }
}

/**
 * 任务队列项
 */
data class TaskQueueItem(
    val taskId: String,
    val agentId: String,
    val priority: Int,
    val task: suspend () -> Unit,
    val createdAt: Long = System.currentTimeMillis(),
    val maxRetries: Int = 3,
    val retryCount: Int = 0
)

/**
 * 统计数据
 */
data class StatisticsData(
    val totalRequests: Long,
    val successfulRequests: Long,
    val failedRequests: Long,
    val averageResponseTime: Double,
    val maxResponseTime: Long,
    val minResponseTime: Long,
    val throughput: Double,
    val errorRate: Double,
    val uptime: Long,
    val startTime: Long
)