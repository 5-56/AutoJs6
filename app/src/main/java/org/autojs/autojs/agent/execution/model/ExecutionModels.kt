package org.autojs.autojs.agent.execution.model

import org.autojs.autojs.agent.core.model.ExecutionStatus

/**
 * 执行任务
 */
data class ExecutionTask(
    val id: String,
    val scriptPath: String,
    var status: ExecutionStatus,
    val startTime: Long,
    var endTime: Long? = null,
    var retryCount: Int = 0,
    var lastError: String? = null,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * 执行指标
 */
data class ExecutionMetrics(
    val taskId: String,
    val startTime: Long,
    var endTime: Long? = null,
    var actualStartTime: Long? = null,
    var status: ExecutionStatus? = null,
    var retryCount: Int = 0,
    var errorMessage: String? = null,
    val performance: PerformanceMetrics = PerformanceMetrics()
)

/**
 * 性能指标
 */
data class PerformanceMetrics(
    var cpuUsage: Float = 0.0f,
    var memoryUsage: Long = 0L,
    var executionTime: Long = 0L,
    var throughput: Float = 0.0f,
    var errorRate: Float = 0.0f
)

/**
 * 恢复操作
 */
data class RecoveryAction(
    val type: RecoveryType,
    val action: String,
    val parameters: Map<String, Any> = emptyMap(),
    val maxRetries: Int = 3,
    val delayMs: Long = 1000L
)

/**
 * 恢复类型
 */
enum class RecoveryType {
    RESTART_SCRIPT,
    RESTART_APP,
    CLEAR_CACHE,
    RESET_PERMISSIONS,
    WAIT_AND_RETRY,
    MANUAL_INTERVENTION
}

/**
 * 重试策略
 */
data class RetryStrategy(
    val type: RetryType,
    val maxAttempts: Int,
    val delayMs: Long,
    val backoffMultiplier: Float = 1.0f,
    val maxDelayMs: Long = 30000L
)

/**
 * 重试类型
 */
enum class RetryType {
    FIXED_DELAY,
    EXPONENTIAL_BACKOFF,
    LINEAR_BACKOFF,
    ADAPTIVE
}

/**
 * 健康检查结果
 */
data class HealthCheckResult(
    val isHealthy: Boolean,
    val reason: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val details: Map<String, Any> = emptyMap()
)

/**
 * 执行上下文
 */
data class ExecutionContext(
    val taskId: String,
    val scriptPath: String,
    val environment: Map<String, Any>,
    val userPreferences: Map<String, Any>,
    val systemInfo: SystemInfo
)

/**
 * 系统信息
 */
data class SystemInfo(
    val osVersion: String,
    val deviceModel: String,
    val availableMemory: Long,
    val batteryLevel: Int,
    val isCharging: Boolean
)

/**
 * 执行事件
 */
data class ExecutionEvent(
    val type: ExecutionEventType,
    val taskId: String,
    val timestamp: Long,
    val message: String,
    val data: Map<String, Any> = emptyMap()
)

/**
 * 执行事件类型
 */
enum class ExecutionEventType {
    TASK_STARTED,
    TASK_COMPLETED,
    TASK_FAILED,
    TASK_CANCELLED,
    TASK_TIMEOUT,
    RECOVERY_ATTEMPTED,
    RETRY_SCHEDULED,
    HEALTH_CHECK_FAILED
}

/**
 * 恢复结果
 */
data class RecoveryResult(
    val success: Boolean,
    val action: RecoveryAction,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)