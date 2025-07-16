package org.autojs.autojs.agent.core

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import org.autojs.autojs.agent.core.model.AgentMessage
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Agent基类 - 所有Agent的基础类
 * Created by SuperMonster003 on 2024/01/15
 */
abstract class AgentBase(
    protected val context: Context,
    protected val config: AgentConfig
) {
    protected val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    protected val messageChannel = Channel<AgentMessage>(Channel.UNLIMITED)
    protected val isRunning = AtomicBoolean(false)
    protected val isInitialized = AtomicBoolean(false)
    protected val startTime = AtomicLong(0)
    protected val processedMessages = AtomicLong(0)
    protected val errorCount = AtomicLong(0)
    protected val lastActivityTime = AtomicLong(System.currentTimeMillis())
    
    // 日志系统
    protected val logger = AgentLogger.getInstance(context)
    
    // Agent标识符
    protected abstract val agentId: String

    /**
     * 初始化Agent
     */
    open suspend fun initialize() {
        if (!isInitialized.compareAndSet(false, true)) {
            logger.warn(agentId, "Agent already initialized")
            return
        }
        
        logger.info(agentId, "Initializing Agent")
        
        try {
            // 启动消息处理协程
            scope.launch {
                messageChannel.consumeAsFlow().collect { message ->
                    if (isRunning.get()) {
                        try {
                            processMessage(message)
                            processedMessages.incrementAndGet()
                            lastActivityTime.set(System.currentTimeMillis())
                        } catch (e: Exception) {
                            errorCount.incrementAndGet()
                            logger.error(agentId, "Error processing message", e)
                        }
                    }
                }
            }
            
            onInitialize()
            logger.info(agentId, "Agent initialized successfully")
        } catch (e: Exception) {
            logger.error(agentId, "Failed to initialize Agent", e)
            isInitialized.set(false)
            throw e
        }
    }

    /**
     * 启动Agent
     */
    open fun start() {
        if (isRunning.compareAndSet(false, true)) {
            logger.info(agentId, "Starting Agent")
            startTime.set(System.currentTimeMillis())
            scope.launch {
                try {
                    onStart()
                    logger.info(agentId, "Agent started successfully")
                } catch (e: Exception) {
                    logger.error(agentId, "Failed to start Agent", e)
                    isRunning.set(false)
                    throw e
                }
            }
        } else {
            logger.warn(agentId, "Agent is already running")
        }
    }

    /**
     * 停止Agent
     */
    open fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            logger.info(agentId, "Stopping Agent")
            scope.launch {
                try {
                    onStop()
                    logger.info(agentId, "Agent stopped successfully")
                } catch (e: Exception) {
                    logger.error(agentId, "Error during Agent stop", e)
                }
            }
        } else {
            logger.warn(agentId, "Agent is not running")
        }
    }

    /**
     * 重启Agent
     */
    open fun restart() {
        logger.info(agentId, "Restarting Agent")
        scope.launch {
            try {
                stop()
                kotlinx.coroutines.delay(1000) // 等待1秒
                start()
                logger.info(agentId, "Agent restarted successfully")
            } catch (e: Exception) {
                logger.error(agentId, "Failed to restart Agent", e)
            }
        }
    }

    /**
     * 发送消息
     */
    open fun sendMessage(message: AgentMessage) {
        scope.launch {
            messageChannel.send(message)
        }
    }

    /**
     * 获取Agent状态
     */
    abstract fun getStatus(): AgentStatus

    /**
     * 处理消息
     */
    abstract suspend fun processMessage(message: AgentMessage)

    /**
     * 初始化时调用
     */
    protected abstract suspend fun onInitialize()

    /**
     * 启动时调用
     */
    protected abstract suspend fun onStart()

    /**
     * 停止时调用
     */
    protected abstract suspend fun onStop()

    /**
     * 获取Agent运行时间
     */
    fun getUptime(): Long {
        return if (startTime.get() > 0) {
            System.currentTimeMillis() - startTime.get()
        } else {
            0
        }
    }

    /**
     * 检查Agent健康状态
     */
    open fun checkHealth(): Boolean {
        val timeSinceLastActivity = System.currentTimeMillis() - lastActivityTime.get()
        return isRunning.get() && timeSinceLastActivity < 60000 // 1分钟内有活动
    }

    /**
     * 重置错误计数
     */
    fun resetErrorCount() {
        errorCount.set(0)
        logger.info(agentId, "Error count reset")
    }

    /**
     * 获取性能指标
     */
    fun getPerformanceMetrics(): Map<String, Any> {
        return mapOf(
            "uptime" to getUptime(),
            "processedMessages" to processedMessages.get(),
            "errorCount" to errorCount.get(),
            "lastActivityTime" to lastActivityTime.get(),
            "isHealthy" to checkHealth()
        )
    }

    /**
     * 销毁Agent
     */
    open fun destroy() {
        logger.info(agentId, "Destroying Agent")
        try {
            isRunning.set(false)
            isInitialized.set(false)
            messageChannel.close()
            logger.info(agentId, "Agent destroyed successfully")
        } catch (e: Exception) {
            logger.error(agentId, "Error during Agent destruction", e)
        }
    }
}