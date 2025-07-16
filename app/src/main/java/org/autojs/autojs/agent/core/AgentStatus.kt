package org.autojs.autojs.agent.core

/**
 * Agent状态数据类
 * Created by SuperMonster003 on 2024/01/15
 */
data class AgentStatus(
    val agentId: String,
    val name: String,
    val description: String,
    val isRunning: Boolean,
    val isInitialized: Boolean,
    val messageQueueSize: Int,
    val lastUpdateTime: Long = System.currentTimeMillis()
)