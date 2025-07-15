package org.autojs.autojs.agent.execution

import android.content.Context
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.autojs.autojs.agent.core.AgentBase
import org.autojs.autojs.agent.core.AgentConfig
import org.autojs.autojs.agent.core.model.AgentEvent
import org.autojs.autojs.agent.core.model.AgentMessage
import org.autojs.autojs.agent.core.model.MessageType
import org.autojs.autojs.agent.core.model.ExecutionMonitorData
import org.autojs.autojs.agent.core.model.ExecutionStatus
import org.autojs.autojs.agent.execution.monitor.ScriptExecutionMonitor
import org.autojs.autojs.agent.execution.recovery.AutoRecoveryManager
import org.autojs.autojs.agent.execution.retry.RetryStrategyManager
import org.autojs.autojs.agent.execution.analysis.ExecutionAnalyzer
import org.autojs.autojs.agent.execution.model.ExecutionTask
import org.autojs.autojs.agent.execution.model.RecoveryAction
import org.autojs.autojs.agent.execution.model.RetryStrategy
import org.autojs.autojs.agent.execution.model.ExecutionMetrics
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * 智能执行监控Agent
 * 核心功能：
 * 1. 实时执行监控
 * 2. 自动异常恢复
 * 3. 智能重试策略
 * 4. 执行效果评估
 * 
 * Created by SuperMonster003 on 2024/01/15
 */
class ExecutionMonitorAgent(
    context: Context,
    config: AgentConfig
) : AgentBase(context, config) {

    companion object {
        const val AGENT_ID = "execution_monitor_agent"
        private const val TAG = "ExecutionMonitorAgent"
    }

    override val agentId: String = AGENT_ID
    override val agentName: String = "智能执行监控Agent"
    override val agentDescription: String = "监控脚本执行，智能处理异常和重试"

    private lateinit var scriptMonitor: ScriptExecutionMonitor
    private lateinit var recoveryManager: AutoRecoveryManager
    private lateinit var retryManager: RetryStrategyManager
    private lateinit var executionAnalyzer: ExecutionAnalyzer

    private val activeTasks = ConcurrentHashMap<String, ExecutionTask>()
    private val taskCounter = AtomicInteger(0)
    private val executionMetrics = ConcurrentHashMap<String, ExecutionMetrics>()

    override suspend fun onInitialize() {
        try {
            scriptMonitor = ScriptExecutionMonitor(context)
            recoveryManager = AutoRecoveryManager(context, config)
            retryManager = RetryStrategyManager(context, config)
            executionAnalyzer = ExecutionAnalyzer(context)

            // 启动监控服务
            scriptMonitor.initialize()
            recoveryManager.initialize()
            
            Log.i(TAG, "ExecutionMonitorAgent initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ExecutionMonitorAgent", e)
            onError(e)
        }
    }

    override suspend fun onStart() {
        agentScope.launch {
            startMonitoringLoop()
        }
        Log.i(TAG, "ExecutionMonitorAgent started")
    }

    override suspend fun onStop() {
        scriptMonitor.stop()
        Log.i(TAG, "ExecutionMonitorAgent stopped")
    }

    override suspend fun onDestroy() {
        activeTasks.clear()
        executionMetrics.clear()
        scriptMonitor.release()
        Log.i(TAG, "ExecutionMonitorAgent destroyed")
    }

    override suspend fun processMessage(message: AgentMessage) {
        when (message.type) {
            MessageType.COMMAND -> handleCommand(message)
            MessageType.QUERY -> handleQuery(message)
            MessageType.NOTIFICATION -> handleNotification(message)
            else -> Log.w(TAG, "Unsupported message type: ${message.type}")
        }
    }

    override fun onError(error: Exception) {
        Log.e(TAG, "ExecutionMonitorAgent error", error)
        emitEvent(AgentEvent.ERROR, error.message)
    }

    private suspend fun handleCommand(message: AgentMessage) {
        when (message.content) {
            "start_monitoring" -> {
                message.data?.let { data ->
                    val taskId = data["taskId"] as? String ?: return
                    val scriptPath = data["scriptPath"] as? String ?: return
                    startMonitoring(taskId, scriptPath)
                }
            }
            "stop_monitoring" -> {
                message.data?.let { data ->
                    val taskId = data["taskId"] as? String ?: return
                    stopMonitoring(taskId)
                }
            }
            "retry_execution" -> {
                message.data?.let { data ->
                    val taskId = data["taskId"] as? String ?: return
                    retryExecution(taskId)
                }
            }
            "recover_execution" -> {
                message.data?.let { data ->
                    val taskId = data["taskId"] as? String ?: return
                    val errorType = data["errorType"] as? String ?: return
                    recoverExecution(taskId, errorType)
                }
            }
        }
    }

    private suspend fun handleQuery(message: AgentMessage) {
        when (message.content) {
            "get_active_tasks" -> {
                val tasks = getActiveTasks()
                emitEvent(AgentEvent.TASK_COMPLETED, tasks)
            }
            "get_execution_metrics" -> {
                val metrics = getExecutionMetrics()
                emitEvent(AgentEvent.TASK_COMPLETED, metrics)
            }
            "get_task_status" -> {
                message.data?.let { data ->
                    val taskId = data["taskId"] as? String ?: return
                    val status = getTaskStatus(taskId)
                    emitEvent(AgentEvent.TASK_COMPLETED, status)
                }
            }
        }
    }

    private suspend fun handleNotification(message: AgentMessage) {
        when (message.content) {
            "script_started" -> {
                message.data?.let { data ->
                    val taskId = data["taskId"] as? String ?: return
                    onScriptStarted(taskId)
                }
            }
            "script_finished" -> {
                message.data?.let { data ->
                    val taskId = data["taskId"] as? String ?: return
                    val success = data["success"] as? Boolean ?: false
                    onScriptFinished(taskId, success)
                }
            }
            "script_error" -> {
                message.data?.let { data ->
                    val taskId = data["taskId"] as? String ?: return
                    val error = data["error"] as? String ?: return
                    onScriptError(taskId, error)
                }
            }
        }
    }

    /**
     * 启动监控循环
     */
    private suspend fun startMonitoringLoop() {
        while (isRunning.get()) {
            try {
                monitorActiveTasks()
                delay(1000) // 每秒检查一次
            } catch (e: Exception) {
                Log.e(TAG, "Error in monitoring loop", e)
            }
        }
    }

    /**
     * 监控活动任务
     */
    private suspend fun monitorActiveTasks() {
        activeTasks.values.forEach { task ->
            when (task.status) {
                ExecutionStatus.RUNNING -> {
                    checkTaskTimeout(task)
                    checkTaskHealth(task)
                }
                ExecutionStatus.FAILED -> {
                    handleTaskFailure(task)
                }
                else -> {
                    // 其他状态不需要特殊处理
                }
            }
        }
    }

    /**
     * 开始监控任务
     */
    private suspend fun startMonitoring(taskId: String, scriptPath: String) {
        val task = ExecutionTask(
            id = taskId,
            scriptPath = scriptPath,
            status = ExecutionStatus.RUNNING,
            startTime = System.currentTimeMillis(),
            retryCount = 0
        )
        
        activeTasks[taskId] = task
        
        val metrics = ExecutionMetrics(
            taskId = taskId,
            startTime = System.currentTimeMillis()
        )
        executionMetrics[taskId] = metrics
        
        scriptMonitor.startMonitoring(task)
        
        Log.i(TAG, "Started monitoring task: $taskId")
        emitEvent(AgentEvent.TASK_COMPLETED, "monitoring_started")
    }

    /**
     * 停止监控任务
     */
    private suspend fun stopMonitoring(taskId: String) {
        activeTasks.remove(taskId)?.let { task ->
            scriptMonitor.stopMonitoring(task)
            Log.i(TAG, "Stopped monitoring task: $taskId")
            emitEvent(AgentEvent.TASK_COMPLETED, "monitoring_stopped")
        }
    }

    /**
     * 检查任务超时
     */
    private suspend fun checkTaskTimeout(task: ExecutionTask) {
        val timeout = config.timeoutSeconds * 1000L
        val currentTime = System.currentTimeMillis()
        
        if (currentTime - task.startTime > timeout) {
            Log.w(TAG, "Task ${task.id} timeout detected")
            task.status = ExecutionStatus.TIMEOUT
            handleTaskFailure(task)
        }
    }

    /**
     * 检查任务健康状态
     */
    private suspend fun checkTaskHealth(task: ExecutionTask) {
        val healthCheck = scriptMonitor.checkHealth(task)
        if (!healthCheck.isHealthy) {
            Log.w(TAG, "Task ${task.id} health check failed: ${healthCheck.reason}")
            task.status = ExecutionStatus.FAILED
            task.lastError = healthCheck.reason
            handleTaskFailure(task)
        }
    }

    /**
     * 处理任务失败
     */
    private suspend fun handleTaskFailure(task: ExecutionTask) {
        val errorType = analyzeError(task.lastError ?: "Unknown error")
        
        // 尝试自动恢复
        val recoveryAction = recoveryManager.getRecoveryAction(errorType)
        if (recoveryAction != null) {
            Log.i(TAG, "Attempting auto-recovery for task ${task.id}")
            val recoveryResult = recoveryManager.executeRecovery(task, recoveryAction)
            
            if (recoveryResult.success) {
                task.status = ExecutionStatus.RUNNING
                task.retryCount++
                Log.i(TAG, "Auto-recovery successful for task ${task.id}")
                return
            }
        }
        
        // 如果恢复失败，尝试重试
        if (task.retryCount < config.retryCount) {
            val retryStrategy = retryManager.getRetryStrategy(errorType)
            scheduleRetry(task, retryStrategy)
        } else {
            task.status = ExecutionStatus.FAILED
            Log.e(TAG, "Task ${task.id} failed after ${task.retryCount} retries")
            
            // 更新执行指标
            updateExecutionMetrics(task)
        }
    }

    /**
     * 重试执行
     */
    private suspend fun retryExecution(taskId: String) {
        activeTasks[taskId]?.let { task ->
            val retryStrategy = retryManager.getRetryStrategy(task.lastError ?: "")
            scheduleRetry(task, retryStrategy)
        }
    }

    /**
     * 恢复执行
     */
    private suspend fun recoverExecution(taskId: String, errorType: String) {
        activeTasks[taskId]?.let { task ->
            val recoveryAction = recoveryManager.getRecoveryAction(errorType)
            if (recoveryAction != null) {
                recoveryManager.executeRecovery(task, recoveryAction)
            }
        }
    }

    /**
     * 安排重试
     */
    private suspend fun scheduleRetry(task: ExecutionTask, retryStrategy: RetryStrategy) {
        agentScope.launch {
            delay(retryStrategy.delayMs)
            
            task.retryCount++
            task.status = ExecutionStatus.RUNNING
            task.startTime = System.currentTimeMillis()
            
            Log.i(TAG, "Retrying task ${task.id} (attempt ${task.retryCount})")
            scriptMonitor.restartMonitoring(task)
        }
    }

    /**
     * 分析错误类型
     */
    private fun analyzeError(error: String): String {
        return when {
            error.contains("timeout") -> "timeout"
            error.contains("permission") -> "permission"
            error.contains("element not found") -> "element_not_found"
            error.contains("network") -> "network"
            else -> "unknown"
        }
    }

    /**
     * 脚本开始事件
     */
    private suspend fun onScriptStarted(taskId: String) {
        activeTasks[taskId]?.let { task ->
            task.status = ExecutionStatus.RUNNING
            task.startTime = System.currentTimeMillis()
            
            executionMetrics[taskId]?.let { metrics ->
                metrics.actualStartTime = System.currentTimeMillis()
            }
            
            Log.i(TAG, "Script started for task: $taskId")
        }
    }

    /**
     * 脚本完成事件
     */
    private suspend fun onScriptFinished(taskId: String, success: Boolean) {
        activeTasks[taskId]?.let { task ->
            task.status = if (success) ExecutionStatus.COMPLETED else ExecutionStatus.FAILED
            task.endTime = System.currentTimeMillis()
            
            updateExecutionMetrics(task)
            
            Log.i(TAG, "Script finished for task: $taskId, success: $success")
            
            // 移除已完成的任务
            activeTasks.remove(taskId)
        }
    }

    /**
     * 脚本错误事件
     */
    private suspend fun onScriptError(taskId: String, error: String) {
        activeTasks[taskId]?.let { task ->
            task.status = ExecutionStatus.FAILED
            task.lastError = error
            
            Log.w(TAG, "Script error for task: $taskId, error: $error")
            
            // 触发错误处理
            handleTaskFailure(task)
        }
    }

    /**
     * 更新执行指标
     */
    private fun updateExecutionMetrics(task: ExecutionTask) {
        executionMetrics[task.id]?.let { metrics ->
            metrics.endTime = System.currentTimeMillis()
            metrics.status = task.status
            metrics.retryCount = task.retryCount
            metrics.errorMessage = task.lastError
            
            executionAnalyzer.analyzeExecution(metrics)
        }
    }

    /**
     * 获取活动任务
     */
    private fun getActiveTasks(): List<ExecutionTask> {
        return activeTasks.values.toList()
    }

    /**
     * 获取执行指标
     */
    private fun getExecutionMetrics(): List<ExecutionMetrics> {
        return executionMetrics.values.toList()
    }

    /**
     * 获取任务状态
     */
    private fun getTaskStatus(taskId: String): ExecutionMonitorData? {
        return activeTasks[taskId]?.let { task ->
            ExecutionMonitorData(
                scriptPath = task.scriptPath,
                status = task.status,
                startTime = task.startTime,
                endTime = task.endTime,
                errorMessage = task.lastError
            )
        }
    }
}