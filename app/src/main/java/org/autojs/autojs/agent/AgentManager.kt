package org.autojs.autojs.agent

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.autojs.autojs.agent.script.ScriptGenerationAgent
import org.autojs.autojs.agent.execution.ExecutionMonitorAgent
import org.autojs.autojs.agent.behavior.BehaviorLearningAgent
import org.autojs.autojs.agent.ui.UIAdaptationAgent
import org.autojs.autojs.agent.dialog.DialogAgent
import org.autojs.autojs.agent.core.AgentBase
import org.autojs.autojs.agent.core.AgentConfig
import org.autojs.autojs.agent.core.AgentStatus
import org.autojs.autojs.agent.core.AgentDataManager
import org.autojs.autojs.agent.core.AgentLogger
import java.util.concurrent.ConcurrentHashMap

/**
 * Agent管理器 - 统一管理所有Agent实例
 * Created by SuperMonster003 on 2024/01/15
 */
class AgentManager private constructor(private val context: Context) {

    private val agentScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val agents = ConcurrentHashMap<String, AgentBase>()
    private val agentConfig = AgentConfig(context)
    private val logger = AgentLogger.getInstance(context)
    private val coordinator = AgentCoordinator.getInstance()
    private val dataManager = AgentDataManager.getInstance(context)

    companion object {
        @Volatile
        private var INSTANCE: AgentManager? = null

        fun getInstance(context: Context): AgentManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AgentManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * 初始化所有Agent
     */
    fun initialize() {
        agentScope.launch {
            try {
                logger.info("AgentManager", "Initializing Agent system...")
                
                // 初始化智能脚本生成Agent
                val scriptAgent = ScriptGenerationAgent(context, agentConfig)
                agents[ScriptGenerationAgent.AGENT_ID] = scriptAgent
                coordinator.registerAgent(ScriptGenerationAgent.AGENT_ID, scriptAgent)
                scriptAgent.initialize()

                // 初始化智能执行监控Agent
                val executionAgent = ExecutionMonitorAgent(context, agentConfig)
                agents[ExecutionMonitorAgent.AGENT_ID] = executionAgent
                coordinator.registerAgent(ExecutionMonitorAgent.AGENT_ID, executionAgent)
                executionAgent.initialize()

                // 初始化用户行为学习Agent
                val behaviorAgent = BehaviorLearningAgent(context, agentConfig)
                agents[BehaviorLearningAgent.AGENT_ID] = behaviorAgent
                coordinator.registerAgent(BehaviorLearningAgent.AGENT_ID, behaviorAgent)
                behaviorAgent.initialize()

                // 初始化智能UI适配Agent
                val uiAgent = UIAdaptationAgent(context, agentConfig)
                agents[UIAdaptationAgent.AGENT_ID] = uiAgent
                coordinator.registerAgent(UIAdaptationAgent.AGENT_ID, uiAgent)
                uiAgent.initialize()

                // 初始化对话式交互Agent
                val dialogAgent = DialogAgent(context, agentConfig)
                agents[DialogAgent.AGENT_ID] = dialogAgent
                coordinator.registerAgent(DialogAgent.AGENT_ID, dialogAgent)
                dialogAgent.initialize()
                
                // 设置Agent间的协作关系
                setupAgentCollaboration()
                
                logger.info("AgentManager", "Agent system initialized successfully")
            } catch (e: Exception) {
                logger.error("AgentManager", "Failed to initialize Agent system", e)
                throw e
            }
        }
    }

    /**
     * 获取指定Agent
     */
    fun <T : AgentBase> getAgent(agentId: String): T? {
        @Suppress("UNCHECKED_CAST")
        return agents[agentId] as? T
    }

    /**
     * 获取所有Agent状态
     */
    fun getAllAgentStatus(): Map<String, AgentStatus> {
        return agents.mapValues { it.value.getStatus() }
    }

    /**
     * 启动指定Agent
     */
    fun startAgent(agentId: String) {
        agents[agentId]?.start()
    }

    /**
     * 停止指定Agent
     */
    fun stopAgent(agentId: String) {
        agents[agentId]?.stop()
    }

    /**
     * 重启指定Agent
     */
    fun restartAgent(agentId: String) {
        agents[agentId]?.restart()
    }

    /**
     * 设置Agent间的协作关系
     */
    private fun setupAgentCollaboration() {
        // 脚本生成Agent订阅UI适配Agent的消息
        coordinator.subscribeToAgent(
            ScriptGenerationAgent.AGENT_ID,
            UIAdaptationAgent.AGENT_ID
        )
        
        // 执行监控Agent订阅脚本生成Agent的消息
        coordinator.subscribeToAgent(
            ExecutionMonitorAgent.AGENT_ID,
            ScriptGenerationAgent.AGENT_ID
        )
        
        // 行为学习Agent订阅所有Agent的消息
        coordinator.subscribeToAgent(
            BehaviorLearningAgent.AGENT_ID,
            ScriptGenerationAgent.AGENT_ID
        )
        coordinator.subscribeToAgent(
            BehaviorLearningAgent.AGENT_ID,
            ExecutionMonitorAgent.AGENT_ID
        )
        coordinator.subscribeToAgent(
            BehaviorLearningAgent.AGENT_ID,
            UIAdaptationAgent.AGENT_ID
        )
        
        // 创建常用工作流
        createCommonWorkflows()
    }
    
    /**
     * 创建常用工作流
     */
    private fun createCommonWorkflows() {
        // 脚本生成和执行工作流
        val scriptExecutionWorkflow = listOf(
            AgentCoordinator.WorkflowStep(
                agentId = ScriptGenerationAgent.AGENT_ID,
                command = "generate_script",
                delayMs = 2000
            ),
            AgentCoordinator.WorkflowStep(
                agentId = ExecutionMonitorAgent.AGENT_ID,
                command = "execute_script",
                delayMs = 1000
            )
        )
        coordinator.createWorkflow("script_generation_execution", scriptExecutionWorkflow)
        
        // UI适配工作流
        val uiAdaptationWorkflow = listOf(
            AgentCoordinator.WorkflowStep(
                agentId = UIAdaptationAgent.AGENT_ID,
                command = "detect_ui_changes",
                delayMs = 1000
            ),
            AgentCoordinator.WorkflowStep(
                agentId = ScriptGenerationAgent.AGENT_ID,
                command = "update_script",
                delayMs = 2000
            )
        )
        coordinator.createWorkflow("ui_adaptation", uiAdaptationWorkflow)
    }
    
    /**
     * 启动指定Agent
     */
    fun startAgent(agentId: String) {
        logger.info("AgentManager", "Starting agent: $agentId")
        agents[agentId]?.start()
    }

    /**
     * 停止指定Agent
     */
    fun stopAgent(agentId: String) {
        logger.info("AgentManager", "Stopping agent: $agentId")
        agents[agentId]?.stop()
    }

    /**
     * 重启指定Agent
     */
    fun restartAgent(agentId: String) {
        logger.info("AgentManager", "Restarting agent: $agentId")
        agents[agentId]?.restart()
    }
    
    /**
     * 执行工作流
     */
    fun executeWorkflow(workflowId: String, initialData: Map<String, Any> = emptyMap()) {
        logger.info("AgentManager", "Executing workflow: $workflowId")
        coordinator.executeWorkflow(workflowId, initialData)
    }
    
    /**
     * 获取Agent性能指标
     */
    fun getAgentMetrics(agentId: String): Map<String, Any>? {
        return agents[agentId]?.getPerformanceMetrics()
    }
    
    /**
     * 获取系统统计信息
     */
    fun getSystemStats(): Map<String, Any> {
        return agentScope.async {
            val coordinatorStats = coordinator.getCoordinatorStats()
            val dataStats = dataManager.getDataStats()
            val logStats = logger.getLogStats()
            
            mapOf(
                "totalAgents" to agents.size,
                "runningAgents" to agents.values.count { it.checkHealth() },
                "coordinator" to coordinatorStats,
                "dataManager" to dataStats,
                "logger" to logStats
            )
        }.let { 
            try {
                kotlinx.coroutines.runBlocking { it.await() }
            } catch (e: Exception) {
                logger.error("AgentManager", "Failed to get system stats", e)
                emptyMap()
            }
        }
    }
    
    /**
     * 重置Agent错误计数
     */
    fun resetAgentErrors(agentId: String) {
        agents[agentId]?.resetErrorCount()
    }
    
    /**
     * 获取Agent数据
     */
    suspend fun getAgentData(agentId: String, key: String): Any? {
        return dataManager.getAgentData<Any>(agentId, key)
    }
    
    /**
     * 保存Agent数据
     */
    suspend fun saveAgentData(agentId: String, key: String, data: Any) {
        dataManager.saveAgentData(agentId, key, data)
    }

    /**
     * 销毁所有Agent
     */
    fun destroy() {
        logger.info("AgentManager", "Destroying Agent system")
        agentScope.launch {
            try {
                agents.values.forEach { 
                    coordinator.unregisterAgent(it.agentId)
                    it.destroy()
                }
                agents.clear()
                logger.info("AgentManager", "Agent system destroyed successfully")
            } catch (e: Exception) {
                logger.error("AgentManager", "Error during Agent system destruction", e)
            }
        }
    }
}