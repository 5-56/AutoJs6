package org.autojs.autojs.agent.core

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.autojs.autojs.agent.core.model.AgentEvent
import org.autojs.autojs.agent.core.model.AgentMessage
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Agent基类 - 提供通用的Agent接口和生命周期管理
 * Created by SuperMonster003 on 2024/01/15
 */
abstract class AgentBase(
    protected val context: Context,
    protected val config: AgentConfig
) {

    abstract val agentId: String
    abstract val agentName: String
    abstract val agentDescription: String

    protected val agentScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    protected val isRunning = AtomicBoolean(false)
    protected val isInitialized = AtomicBoolean(false)

    private val messageQueue = ConcurrentLinkedQueue<AgentMessage>()
    private val eventListeners = mutableListOf<(AgentEvent) -> Unit>()

    /**
     * 初始化Agent
     */
    open fun initialize() {
        if (isInitialized.compareAndSet(false, true)) {
            agentScope.launch {
                onInitialize()
                emitEvent(AgentEvent.INITIALIZED, agentId)
            }
        }
    }

    /**
     * 启动Agent
     */
    fun start() {
        if (isRunning.compareAndSet(false, true)) {
            agentScope.launch {
                onStart()
                emitEvent(AgentEvent.STARTED, agentId)
            }
        }
    }

    /**
     * 停止Agent
     */
    fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            agentScope.launch {
                onStop()
                emitEvent(AgentEvent.STOPPED, agentId)
            }
        }
    }

    /**
     * 重启Agent
     */
    fun restart() {
        agentScope.launch {
            stop()
            start()
            emitEvent(AgentEvent.RESTARTED, agentId)
        }
    }

    /**
     * 销毁Agent
     */
    fun destroy() {
        agentScope.launch {
            if (isRunning.get()) {
                stop()
            }
            onDestroy()
            emitEvent(AgentEvent.DESTROYED, agentId)
            agentScope.cancel()
        }
    }

    /**
     * 获取Agent状态
     */
    fun getStatus(): AgentStatus {
        return AgentStatus(
            agentId = agentId,
            name = agentName,
            description = agentDescription,
            isRunning = isRunning.get(),
            isInitialized = isInitialized.get(),
            messageQueueSize = messageQueue.size
        )
    }

    /**
     * 发送消息给Agent
     */
    fun sendMessage(message: AgentMessage) {
        messageQueue.offer(message)
        if (isRunning.get()) {
            agentScope.launch {
                processMessage(message)
            }
        }
    }

    /**
     * 添加事件监听器
     */
    fun addEventListener(listener: (AgentEvent) -> Unit) {
        eventListeners.add(listener)
    }

    /**
     * 移除事件监听器
     */
    fun removeEventListener(listener: (AgentEvent) -> Unit) {
        eventListeners.remove(listener)
    }

    /**
     * 发射事件
     */
    protected fun emitEvent(event: AgentEvent, data: Any? = null) {
        eventListeners.forEach { listener ->
            try {
                listener(event.apply { this.data = data })
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    // 抽象方法，由子类实现
    protected abstract suspend fun onInitialize()
    protected abstract suspend fun onStart()
    protected abstract suspend fun onStop()
    protected abstract suspend fun onDestroy()
    protected abstract suspend fun processMessage(message: AgentMessage)
    protected abstract fun onError(error: Exception)
}