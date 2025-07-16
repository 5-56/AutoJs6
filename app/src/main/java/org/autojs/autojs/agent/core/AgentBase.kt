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

    /**
     * 初始化Agent
     */
    open suspend fun initialize() {
        if (!isInitialized.compareAndSet(false, true)) {
            return
        }
        
        // 启动消息处理协程
        scope.launch {
            messageChannel.consumeAsFlow().collect { message ->
                if (isRunning.get()) {
                    processMessage(message)
                }
            }
        }
        
        onInitialize()
    }

    /**
     * 启动Agent
     */
    open fun start() {
        if (isRunning.compareAndSet(false, true)) {
            scope.launch {
                onStart()
            }
        }
    }

    /**
     * 停止Agent
     */
    open fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            scope.launch {
                onStop()
            }
        }
    }

    /**
     * 重启Agent
     */
    open fun restart() {
        scope.launch {
            stop()
            kotlinx.coroutines.delay(1000) // 等待1秒
            start()
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
     * 销毁Agent
     */
    open fun destroy() {
        isRunning.set(false)
        isInitialized.set(false)
        messageChannel.close()
    }
}