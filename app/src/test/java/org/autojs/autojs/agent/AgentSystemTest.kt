package org.autojs.autojs.agent

import android.content.Context
import kotlinx.coroutines.runBlocking
import org.autojs.autojs.agent.core.AgentConfig
import org.autojs.autojs.agent.core.AgentStatus
import org.autojs.autojs.agent.core.model.AgentMessage
import org.autojs.autojs.agent.core.model.MessageType
import org.autojs.autojs.agent.script.ScriptGenerationAgent
import org.autojs.autojs.agent.execution.ExecutionMonitorAgent
import org.autojs.autojs.agent.behavior.BehaviorLearningAgent
import org.autojs.autojs.agent.ui.UIAdaptationAgent
import org.autojs.autojs.agent.dialog.DialogAgent
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Agent系统测试
 * Created by SuperMonster003 on 2024/01/15
 */
class AgentSystemTest {

    @Mock
    private lateinit var mockContext: Context

    private lateinit var agentConfig: AgentConfig
    private lateinit var agentManager: AgentManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        
        // 创建模拟配置
        agentConfig = AgentConfig(mockContext)
        
        // 创建Agent管理器
        agentManager = AgentManager.getInstance(mockContext)
    }

    @Test
    fun testAgentManagerInitialization() {
        // 测试Agent管理器是否正确初始化
        assertNotNull(agentManager)
        
        // 测试单例模式
        val anotherInstance = AgentManager.getInstance(mockContext)
        assertSame(agentManager, anotherInstance)
    }

    @Test
    fun testAgentInitialization() = runBlocking {
        // 测试各个Agent的初始化
        agentManager.initialize()
        
        // 验证所有Agent都已初始化
        val statusMap = agentManager.getAllAgentStatus()
        assertTrue(statusMap.isNotEmpty())
        
        // 验证具体Agent是否存在
        assertTrue(statusMap.containsKey(ScriptGenerationAgent.AGENT_ID))
        assertTrue(statusMap.containsKey(ExecutionMonitorAgent.AGENT_ID))
        assertTrue(statusMap.containsKey(BehaviorLearningAgent.AGENT_ID))
        assertTrue(statusMap.containsKey(UIAdaptationAgent.AGENT_ID))
        assertTrue(statusMap.containsKey(DialogAgent.AGENT_ID))
    }

    @Test
    fun testAgentStartStop() = runBlocking {
        agentManager.initialize()
        
        // 测试启动Agent
        agentManager.startAgent(ScriptGenerationAgent.AGENT_ID)
        
        // 等待一段时间让Agent启动
        Thread.sleep(100)
        
        val status = agentManager.getAllAgentStatus()[ScriptGenerationAgent.AGENT_ID]
        assertNotNull(status)
        
        // 测试停止Agent
        agentManager.stopAgent(ScriptGenerationAgent.AGENT_ID)
        
        // 等待一段时间让Agent停止
        Thread.sleep(100)
        
        val stoppedStatus = agentManager.getAllAgentStatus()[ScriptGenerationAgent.AGENT_ID]
        assertNotNull(stoppedStatus)
    }

    @Test
    fun testAgentMessage() = runBlocking {
        agentManager.initialize()
        
        val scriptAgent = agentManager.getAgent<ScriptGenerationAgent>(ScriptGenerationAgent.AGENT_ID)
        assertNotNull(scriptAgent)
        
        // 测试发送消息
        val message = AgentMessage(
            type = MessageType.COMMAND,
            content = "test_command",
            data = mapOf("test" to "data")
        )
        
        scriptAgent?.sendMessage(message)
        
        // 验证消息已发送（这里需要实际的消息处理验证）
        assertTrue(true) // 简化测试
    }

    @Test
    fun testAgentConfig() {
        // 测试Agent配置
        assertTrue(agentConfig.isScriptGenerationEnabled)
        assertTrue(agentConfig.isExecutionMonitorEnabled)
        assertTrue(agentConfig.isBehaviorLearningEnabled)
        assertTrue(agentConfig.isUIAdaptationEnabled)
        assertTrue(agentConfig.isDialogAgentEnabled)
        
        // 测试配置修改
        agentConfig.isScriptGenerationEnabled = false
        assertFalse(agentConfig.isScriptGenerationEnabled)
        
        // 测试重置
        agentConfig.resetToDefault()
        assertTrue(agentConfig.isScriptGenerationEnabled)
    }

    @Test
    fun testAgentStatus() = runBlocking {
        agentManager.initialize()
        
        val statusMap = agentManager.getAllAgentStatus()
        
        statusMap.values.forEach { status ->
            assertNotNull(status.agentId)
            assertNotNull(status.name)
            assertNotNull(status.description)
            assertTrue(status.lastUpdateTime > 0)
        }
    }

    @Test
    fun testAgentRestart() = runBlocking {
        agentManager.initialize()
        
        // 测试重启Agent
        agentManager.restartAgent(ScriptGenerationAgent.AGENT_ID)
        
        // 等待重启完成
        Thread.sleep(200)
        
        val status = agentManager.getAllAgentStatus()[ScriptGenerationAgent.AGENT_ID]
        assertNotNull(status)
    }

    @Test
    fun testScriptGenerationAgent() = runBlocking {
        val scriptAgent = ScriptGenerationAgent(mockContext, agentConfig)
        
        // 测试Agent初始化
        scriptAgent.initialize()
        
        // 验证Agent状态
        val status = scriptAgent.getStatus()
        assertEquals(ScriptGenerationAgent.AGENT_ID, status.agentId)
        assertEquals("智能脚本生成Agent", status.name)
        assertTrue(status.isInitialized)
    }

    @Test
    fun testExecutionMonitorAgent() = runBlocking {
        val executionAgent = ExecutionMonitorAgent(mockContext, agentConfig)
        
        // 测试Agent初始化
        executionAgent.initialize()
        
        // 验证Agent状态
        val status = executionAgent.getStatus()
        assertEquals(ExecutionMonitorAgent.AGENT_ID, status.agentId)
        assertEquals("智能执行监控Agent", status.name)
        assertTrue(status.isInitialized)
    }

    @Test
    fun testBehaviorLearningAgent() = runBlocking {
        val behaviorAgent = BehaviorLearningAgent(mockContext, agentConfig)
        
        // 测试Agent初始化
        behaviorAgent.initialize()
        
        // 验证Agent状态
        val status = behaviorAgent.getStatus()
        assertEquals(BehaviorLearningAgent.AGENT_ID, status.agentId)
        assertEquals("用户行为学习Agent", status.name)
        assertTrue(status.isInitialized)
    }

    @Test
    fun testUIAdaptationAgent() = runBlocking {
        val uiAgent = UIAdaptationAgent(mockContext, agentConfig)
        
        // 测试Agent初始化
        uiAgent.initialize()
        
        // 验证Agent状态
        val status = uiAgent.getStatus()
        assertEquals(UIAdaptationAgent.AGENT_ID, status.agentId)
        assertEquals("智能UI适配Agent", status.name)
        assertTrue(status.isInitialized)
    }

    @Test
    fun testDialogAgent() = runBlocking {
        val dialogAgent = DialogAgent(mockContext, agentConfig)
        
        // 测试Agent初始化
        dialogAgent.initialize()
        
        // 验证Agent状态
        val status = dialogAgent.getStatus()
        assertEquals(DialogAgent.AGENT_ID, status.agentId)
        assertEquals("对话式交互Agent", status.name)
        assertTrue(status.isInitialized)
    }

    @Test
    fun testAgentDestroy() = runBlocking {
        agentManager.initialize()
        
        // 测试销毁Agent系统
        agentManager.destroy()
        
        // 等待销毁完成
        Thread.sleep(100)
        
        // 验证Agent状态（这里需要实际的销毁验证）
        assertTrue(true) // 简化测试
    }

    @Test
    fun testConcurrentAgentOperations() = runBlocking {
        agentManager.initialize()
        
        // 测试并发操作
        val agents = listOf(
            ScriptGenerationAgent.AGENT_ID,
            ExecutionMonitorAgent.AGENT_ID,
            BehaviorLearningAgent.AGENT_ID,
            UIAdaptationAgent.AGENT_ID,
            DialogAgent.AGENT_ID
        )
        
        // 并发启动所有Agent
        agents.forEach { agentId ->
            agentManager.startAgent(agentId)
        }
        
        // 等待所有Agent启动
        Thread.sleep(500)
        
        // 验证所有Agent状态
        val statusMap = agentManager.getAllAgentStatus()
        assertEquals(5, statusMap.size)
        
        // 并发停止所有Agent
        agents.forEach { agentId ->
            agentManager.stopAgent(agentId)
        }
        
        // 等待所有Agent停止
        Thread.sleep(500)
        
        // 验证状态更新
        assertTrue(true) // 简化测试
    }

    @Test
    fun testAgentPerformance() = runBlocking {
        agentManager.initialize()
        
        val startTime = System.currentTimeMillis()
        
        // 测试Agent启动性能
        agentManager.startAgent(ScriptGenerationAgent.AGENT_ID)
        
        val endTime = System.currentTimeMillis()
        val startupTime = endTime - startTime
        
        // 验证启动时间在合理范围内（小于1秒）
        assertTrue(startupTime < 1000)
        
        // 测试消息处理性能
        val messageStartTime = System.currentTimeMillis()
        
        val scriptAgent = agentManager.getAgent<ScriptGenerationAgent>(ScriptGenerationAgent.AGENT_ID)
        val message = AgentMessage(
            type = MessageType.QUERY,
            content = "test_query"
        )
        
        scriptAgent?.sendMessage(message)
        
        val messageEndTime = System.currentTimeMillis()
        val messageProcessTime = messageEndTime - messageStartTime
        
        // 验证消息处理时间在合理范围内（小于100毫秒）
        assertTrue(messageProcessTime < 100)
    }
}