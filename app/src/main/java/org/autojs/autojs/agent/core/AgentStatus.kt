package org.autojs.autojs.agent.core

/**
 * Agent状态类
 * Created by SuperMonster003 on 2024/01/15
 */
data class AgentStatus(
    val agentId: String,
    val name: String,
    val description: String,
    val isRunning: Boolean,
    val isInitialized: Boolean,
    val messageQueueSize: Int,
    val lastUpdateTime: Long,
    val uptime: Long = 0,
    val processedMessages: Int = 0,
    val errorCount: Int = 0,
    val metadata: Map<String, Any> = emptyMap()
)