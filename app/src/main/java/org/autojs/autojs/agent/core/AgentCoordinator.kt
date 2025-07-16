package org.autojs.autojs.agent.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import org.autojs.autojs.agent.core.model.AgentMessage
import org.autojs.autojs.agent.core.model.MessageType
import java.util.concurrent.ConcurrentHashMap

/**
 * Agent协作系统 - 管理Agent之间的通信和协作
 * Created by SuperMonster003 on 2024/01/15
 */
class AgentCoordinator private constructor() {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val messageChannel = Channel<CoordinatorMessage>(Channel.UNLIMITED)
    private val agents = ConcurrentHashMap<String, AgentBase>()
    private val subscriptions = ConcurrentHashMap<String, MutableSet<String>>()
    private val workflows = ConcurrentHashMap<String, AgentWorkflow>()
    
    companion object {
        @Volatile
        private var INSTANCE: AgentCoordinator? = null
        
        fun getInstance(): AgentCoordinator {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AgentCoordinator().also { INSTANCE = it }
            }
        }
    }
    
    init {
        // 启动消息处理协程
        scope.launch {
            messageChannel.consumeAsFlow().collect { message ->
                handleCoordinatorMessage(message)
            }
        }
    }
    
    /**
     * 注册Agent
     */
    fun registerAgent(agentId: String, agent: AgentBase) {
        agents[agentId] = agent
        subscriptions[agentId] = mutableSetOf()
    }
    
    /**
     * 注销Agent
     */
    fun unregisterAgent(agentId: String) {
        agents.remove(agentId)
        subscriptions.remove(agentId)
    }
    
    /**
     * 发送消息给特定Agent
     */
    fun sendMessage(targetAgentId: String, message: AgentMessage) {
        val coordinatorMessage = CoordinatorMessage(
            type = CoordinatorMessageType.DIRECT_MESSAGE,
            targetAgentId = targetAgentId,
            message = message
        )
        
        scope.launch {
            messageChannel.send(coordinatorMessage)
        }
    }
    
    /**
     * 广播消息给所有Agent
     */
    fun broadcastMessage(message: AgentMessage, excludeAgentId: String? = null) {
        val coordinatorMessage = CoordinatorMessage(
            type = CoordinatorMessageType.BROADCAST,
            message = message,
            excludeAgentId = excludeAgentId
        )
        
        scope.launch {
            messageChannel.send(coordinatorMessage)
        }
    }
    
    /**
     * 订阅Agent消息
     */
    fun subscribeToAgent(subscriberAgentId: String, publisherAgentId: String) {
        subscriptions[subscriberAgentId]?.add(publisherAgentId)
    }
    
    /**
     * 取消订阅Agent消息
     */
    fun unsubscribeFromAgent(subscriberAgentId: String, publisherAgentId: String) {
        subscriptions[subscriberAgentId]?.remove(publisherAgentId)
    }
    
    /**
     * 发布消息给订阅者
     */
    fun publishMessage(publisherAgentId: String, message: AgentMessage) {
        val coordinatorMessage = CoordinatorMessage(
            type = CoordinatorMessageType.PUBLISH,
            publisherAgentId = publisherAgentId,
            message = message
        )
        
        scope.launch {
            messageChannel.send(coordinatorMessage)
        }
    }
    
    /**
     * 创建工作流
     */
    fun createWorkflow(workflowId: String, steps: List<WorkflowStep>) {
        val workflow = AgentWorkflow(workflowId, steps)
        workflows[workflowId] = workflow
    }
    
    /**
     * 执行工作流
     */
    fun executeWorkflow(workflowId: String, initialData: Map<String, Any> = emptyMap()) {
        val workflow = workflows[workflowId]
        if (workflow != null) {
            val coordinatorMessage = CoordinatorMessage(
                type = CoordinatorMessageType.EXECUTE_WORKFLOW,
                workflowId = workflowId,
                data = initialData
            )
            
            scope.launch {
                messageChannel.send(coordinatorMessage)
            }
        }
    }
    
    /**
     * 处理协调器消息
     */
    private suspend fun handleCoordinatorMessage(message: CoordinatorMessage) {
        when (message.type) {
            CoordinatorMessageType.DIRECT_MESSAGE -> {
                handleDirectMessage(message)
            }
            CoordinatorMessageType.BROADCAST -> {
                handleBroadcastMessage(message)
            }
            CoordinatorMessageType.PUBLISH -> {
                handlePublishMessage(message)
            }
            CoordinatorMessageType.EXECUTE_WORKFLOW -> {
                handleWorkflowExecution(message)
            }
        }
    }
    
    /**
     * 处理直接消息
     */
    private fun handleDirectMessage(message: CoordinatorMessage) {
        val targetAgent = agents[message.targetAgentId]
        if (targetAgent != null && message.message != null) {
            targetAgent.sendMessage(message.message)
        }
    }
    
    /**
     * 处理广播消息
     */
    private fun handleBroadcastMessage(message: CoordinatorMessage) {
        if (message.message != null) {
            agents.values.forEach { agent ->
                if (agent.agentId != message.excludeAgentId) {
                    agent.sendMessage(message.message)
                }
            }
        }
    }
    
    /**
     * 处理发布消息
     */
    private fun handlePublishMessage(message: CoordinatorMessage) {
        if (message.publisherAgentId != null && message.message != null) {
            subscriptions.forEach { (subscriberAgentId, publisherIds) ->
                if (publisherIds.contains(message.publisherAgentId)) {
                    agents[subscriberAgentId]?.sendMessage(message.message)
                }
            }
        }
    }
    
    /**
     * 处理工作流执行
     */
    private suspend fun handleWorkflowExecution(message: CoordinatorMessage) {
        val workflowId = message.workflowId
        val workflow = workflows[workflowId]
        
        if (workflow != null) {
            executeWorkflowSteps(workflow, message.data ?: emptyMap())
        }
    }
    
    /**
     * 执行工作流步骤
     */
    private suspend fun executeWorkflowSteps(workflow: AgentWorkflow, data: Map<String, Any>) {
        var currentData = data.toMutableMap()
        
        for (step in workflow.steps) {
            val agent = agents[step.agentId]
            if (agent != null) {
                val message = AgentMessage(
                    type = MessageType.COMMAND,
                    content = step.command,
                    data = currentData
                )
                
                agent.sendMessage(message)
                
                // 等待步骤完成（简化实现）
                kotlinx.coroutines.delay(step.delayMs)
                
                // 更新数据（实际应从Agent获取结果）
                currentData.putAll(step.outputData)
            }
        }
    }
    
    /**
     * 获取Agent协作统计
     */
    fun getCoordinatorStats(): CoordinatorStats {
        return CoordinatorStats(
            registeredAgents = agents.size,
            totalSubscriptions = subscriptions.values.sumOf { it.size },
            activeWorkflows = workflows.size,
            messageQueueSize = messageChannel.tryReceive().isSuccess
        )
    }
    
    /**
     * 协调器消息类型
     */
    private enum class CoordinatorMessageType {
        DIRECT_MESSAGE,
        BROADCAST,
        PUBLISH,
        EXECUTE_WORKFLOW
    }
    
    /**
     * 协调器消息
     */
    private data class CoordinatorMessage(
        val type: CoordinatorMessageType,
        val targetAgentId: String? = null,
        val publisherAgentId: String? = null,
        val excludeAgentId: String? = null,
        val workflowId: String? = null,
        val message: AgentMessage? = null,
        val data: Map<String, Any>? = null
    )
    
    /**
     * Agent工作流
     */
    data class AgentWorkflow(
        val id: String,
        val steps: List<WorkflowStep>
    )
    
    /**
     * 工作流步骤
     */
    data class WorkflowStep(
        val agentId: String,
        val command: String,
        val delayMs: Long = 0,
        val outputData: Map<String, Any> = emptyMap()
    )
    
    /**
     * 协调器统计信息
     */
    data class CoordinatorStats(
        val registeredAgents: Int,
        val totalSubscriptions: Int,
        val activeWorkflows: Int,
        val messageQueueSize: Boolean
    )
}