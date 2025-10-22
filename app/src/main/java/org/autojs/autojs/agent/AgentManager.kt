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
import java.util.concurrent.ConcurrentHashMap

/**
 * Agent管理器 - 统一管理所有Agent实例
 * Created by SuperMonster003 on 2024/01/15
 */
class AgentManager private constructor(private val context: Context) {

    private val agentScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val agents = ConcurrentHashMap<String, AgentBase>()
    private val agentConfig = AgentConfig(context)

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
            // 初始化智能脚本生成Agent
            val scriptAgent = ScriptGenerationAgent(context, agentConfig)
            agents[ScriptGenerationAgent.AGENT_ID] = scriptAgent
            scriptAgent.initialize()

            // 初始化智能执行监控Agent
            val executionAgent = ExecutionMonitorAgent(context, agentConfig)
            agents[ExecutionMonitorAgent.AGENT_ID] = executionAgent
            executionAgent.initialize()

            // 初始化用户行为学习Agent
            val behaviorAgent = BehaviorLearningAgent(context, agentConfig)
            agents[BehaviorLearningAgent.AGENT_ID] = behaviorAgent
            behaviorAgent.initialize()

            // 初始化智能UI适配Agent
            val uiAgent = UIAdaptationAgent(context, agentConfig)
            agents[UIAdaptationAgent.AGENT_ID] = uiAgent
            uiAgent.initialize()

            // 初始化对话式交互Agent
            val dialogAgent = DialogAgent(context, agentConfig)
            agents[DialogAgent.AGENT_ID] = dialogAgent
            dialogAgent.initialize()
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
     * 销毁所有Agent
     */
    fun destroy() {
        agentScope.launch {
            agents.values.forEach { it.destroy() }
            agents.clear()
        }
    }
}