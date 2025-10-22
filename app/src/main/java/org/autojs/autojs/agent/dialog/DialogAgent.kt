package org.autojs.autojs.agent.dialog

import android.content.Context
import android.util.Log
import kotlinx.coroutines.launch
import org.autojs.autojs.agent.core.AgentBase
import org.autojs.autojs.agent.core.AgentConfig
import org.autojs.autojs.agent.core.model.AgentEvent
import org.autojs.autojs.agent.core.model.AgentMessage
import org.autojs.autojs.agent.core.model.MessageType
import org.autojs.autojs.agent.core.model.DialogContext
import org.autojs.autojs.agent.core.model.DialogMessage
import org.autojs.autojs.agent.dialog.nlp.IntentRecognizer
import org.autojs.autojs.agent.dialog.nlp.EntityExtractor
import org.autojs.autojs.agent.dialog.generation.ResponseGenerator
import org.autojs.autojs.agent.dialog.knowledge.KnowledgeBase
import org.autojs.autojs.agent.dialog.debug.DebugAssistant
import org.autojs.autojs.agent.dialog.guide.LearningGuide
import org.autojs.autojs.agent.dialog.model.DialogSession
import org.autojs.autojs.agent.dialog.model.Intent
import org.autojs.autojs.agent.dialog.model.Entity
import org.autojs.autojs.agent.dialog.model.Response
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 对话式交互Agent
 * 核心功能：
 * 1. 自然语言交互
 * 2. 智能调试助手
 * 3. 学习指导
 * 4. 知识问答
 * 
 * Created by SuperMonster003 on 2024/01/15
 */
class DialogAgent(
    context: Context,
    config: AgentConfig
) : AgentBase(context, config) {

    companion object {
        const val AGENT_ID = "dialog_agent"
        private const val TAG = "DialogAgent"
    }

    override val agentId: String = AGENT_ID
    override val agentName: String = "对话式交互Agent"
    override val agentDescription: String = "提供自然语言交互、调试助手和学习指导"

    private lateinit var intentRecognizer: IntentRecognizer
    private lateinit var entityExtractor: EntityExtractor
    private lateinit var responseGenerator: ResponseGenerator
    private lateinit var knowledgeBase: KnowledgeBase
    private lateinit var debugAssistant: DebugAssistant
    private lateinit var learningGuide: LearningGuide

    private val activeSessions = ConcurrentHashMap<String, DialogSession>()

    override suspend fun onInitialize() {
        try {
            intentRecognizer = IntentRecognizer(context, config)
            entityExtractor = EntityExtractor(context, config)
            responseGenerator = ResponseGenerator(context, config)
            knowledgeBase = KnowledgeBase(context)
            debugAssistant = DebugAssistant(context, config)
            learningGuide = LearningGuide(context, config)

            // 初始化NLP模型
            intentRecognizer.initialize()
            entityExtractor.initialize()
            responseGenerator.initialize()
            
            // 加载知识库
            knowledgeBase.loadKnowledge()
            
            Log.i(TAG, "DialogAgent initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize DialogAgent", e)
            onError(e)
        }
    }

    override suspend fun onStart() {
        Log.i(TAG, "DialogAgent started")
    }

    override suspend fun onStop() {
        activeSessions.clear()
        Log.i(TAG, "DialogAgent stopped")
    }

    override suspend fun onDestroy() {
        activeSessions.clear()
        intentRecognizer.release()
        entityExtractor.release()
        responseGenerator.release()
        debugAssistant.release()
        learningGuide.release()
        Log.i(TAG, "DialogAgent destroyed")
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
        Log.e(TAG, "DialogAgent error", error)
        emitEvent(AgentEvent.ERROR, error.message)
    }

    private suspend fun handleCommand(message: AgentMessage) {
        when (message.content) {
            "start_conversation" -> {
                message.data?.let { data ->
                    val userId = data["userId"] as? String ?: return
                    val sessionId = startConversation(userId)
                    emitEvent(AgentEvent.TASK_COMPLETED, sessionId)
                }
            }
            "end_conversation" -> {
                message.data?.let { data ->
                    val sessionId = data["sessionId"] as? String ?: return
                    endConversation(sessionId)
                    emitEvent(AgentEvent.TASK_COMPLETED, "conversation_ended")
                }
            }
            "process_user_input" -> {
                message.data?.let { data ->
                    val sessionId = data["sessionId"] as? String ?: return
                    val userInput = data["userInput"] as? String ?: return
                    val response = processUserInput(sessionId, userInput)
                    emitEvent(AgentEvent.TASK_COMPLETED, response)
                }
            }
            "debug_script" -> {
                message.data?.let { data ->
                    val sessionId = data["sessionId"] as? String ?: return
                    val scriptPath = data["scriptPath"] as? String ?: return
                    val errorInfo = data["errorInfo"] as? String ?: return
                    val debugResponse = debugScript(sessionId, scriptPath, errorInfo)
                    emitEvent(AgentEvent.TASK_COMPLETED, debugResponse)
                }
            }
            "provide_guidance" -> {
                message.data?.let { data ->
                    val sessionId = data["sessionId"] as? String ?: return
                    val topic = data["topic"] as? String ?: return
                    val guidance = provideGuidance(sessionId, topic)
                    emitEvent(AgentEvent.TASK_COMPLETED, guidance)
                }
            }
        }
    }

    private suspend fun handleQuery(message: AgentMessage) {
        when (message.content) {
            "get_active_sessions" -> {
                val sessions = getActiveSessions()
                emitEvent(AgentEvent.TASK_COMPLETED, sessions)
            }
            "get_session_context" -> {
                message.data?.let { data ->
                    val sessionId = data["sessionId"] as? String ?: return
                    val context = getSessionContext(sessionId)
                    emitEvent(AgentEvent.TASK_COMPLETED, context)
                }
            }
            "search_knowledge" -> {
                message.data?.let { data ->
                    val query = data["query"] as? String ?: return
                    val results = searchKnowledge(query)
                    emitEvent(AgentEvent.TASK_COMPLETED, results)
                }
            }
            "get_conversation_history" -> {
                message.data?.let { data ->
                    val sessionId = data["sessionId"] as? String ?: return
                    val history = getConversationHistory(sessionId)
                    emitEvent(AgentEvent.TASK_COMPLETED, history)
                }
            }
        }
    }

    private suspend fun handleNotification(message: AgentMessage) {
        when (message.content) {
            "user_feedback" -> {
                message.data?.let { data ->
                    val sessionId = data["sessionId"] as? String ?: return
                    val feedback = data["feedback"] as? String ?: return
                    val rating = data["rating"] as? Int ?: return
                    processFeedback(sessionId, feedback, rating)
                }
            }
            "context_update" -> {
                message.data?.let { data ->
                    val sessionId = data["sessionId"] as? String ?: return
                    val context = data["context"] as? Map<String, Any> ?: return
                    updateSessionContext(sessionId, context)
                }
            }
        }
    }

    /**
     * 开始对话
     */
    private suspend fun startConversation(userId: String): String {
        val sessionId = UUID.randomUUID().toString()
        val session = DialogSession(
            sessionId = sessionId,
            userId = userId,
            startTime = System.currentTimeMillis(),
            context = DialogContext(
                sessionId = sessionId,
                userId = userId,
                history = mutableListOf()
            )
        )
        
        activeSessions[sessionId] = session
        
        // 发送欢迎消息
        val welcomeMessage = generateWelcomeMessage(userId)
        session.context.history.add(
            DialogMessage(
                role = "assistant",
                content = welcomeMessage,
                timestamp = System.currentTimeMillis()
            )
        )
        
        Log.i(TAG, "Started conversation for user: $userId, session: $sessionId")
        return sessionId
    }

    /**
     * 结束对话
     */
    private suspend fun endConversation(sessionId: String) {
        activeSessions.remove(sessionId)?.let { session ->
            session.endTime = System.currentTimeMillis()
            
            // 保存对话记录
            saveConversationHistory(session)
            
            Log.i(TAG, "Ended conversation for session: $sessionId")
        }
    }

    /**
     * 处理用户输入
     */
    private suspend fun processUserInput(sessionId: String, userInput: String): Response {
        val session = activeSessions[sessionId] ?: return Response(
            success = false,
            message = "Session not found"
        )
        
        // 记录用户消息
        session.context.history.add(
            DialogMessage(
                role = "user",
                content = userInput,
                timestamp = System.currentTimeMillis()
            )
        )
        
        // 意图识别
        val intent = intentRecognizer.recognize(userInput, session.context)
        
        // 实体提取
        val entities = entityExtractor.extract(userInput, session.context)
        
        // 更新会话上下文
        session.context.currentIntent = intent.name
        session.context.entities = entities.associate { it.name to it.value }
        
        // 生成响应
        val response = generateResponse(session, intent, entities)
        
        // 记录助手响应
        session.context.history.add(
            DialogMessage(
                role = "assistant",
                content = response.message,
                timestamp = System.currentTimeMillis()
            )
        )
        
        Log.d(TAG, "Processed user input: $userInput, intent: ${intent.name}")
        return response
    }

    /**
     * 调试脚本
     */
    private suspend fun debugScript(sessionId: String, scriptPath: String, errorInfo: String): Response {
        val session = activeSessions[sessionId] ?: return Response(
            success = false,
            message = "Session not found"
        )
        
        val debugResult = debugAssistant.analyzeError(scriptPath, errorInfo, session.context)
        
        val response = Response(
            success = true,
            message = debugResult.explanation,
            data = mapOf(
                "suggestions" to debugResult.suggestions,
                "fixes" to debugResult.fixes
            )
        )
        
        // 记录调试交互
        session.context.history.add(
            DialogMessage(
                role = "assistant",
                content = response.message,
                timestamp = System.currentTimeMillis()
            )
        )
        
        Log.i(TAG, "Provided debug assistance for script: $scriptPath")
        return response
    }

    /**
     * 提供指导
     */
    private suspend fun provideGuidance(sessionId: String, topic: String): Response {
        val session = activeSessions[sessionId] ?: return Response(
            success = false,
            message = "Session not found"
        )
        
        val guidance = learningGuide.provideGuidance(topic, session.context)
        
        val response = Response(
            success = true,
            message = guidance.content,
            data = mapOf(
                "examples" to guidance.examples,
                "nextSteps" to guidance.nextSteps
            )
        )
        
        // 记录指导交互
        session.context.history.add(
            DialogMessage(
                role = "assistant",
                content = response.message,
                timestamp = System.currentTimeMillis()
            )
        )
        
        Log.i(TAG, "Provided guidance on topic: $topic")
        return response
    }

    /**
     * 生成响应
     */
    private suspend fun generateResponse(
        session: DialogSession,
        intent: Intent,
        entities: List<Entity>
    ): Response {
        return when (intent.name) {
            "script_generation" -> {
                val description = entities.find { it.name == "description" }?.value as? String ?: ""
                handleScriptGenerationRequest(session, description)
            }
            "debug_help" -> {
                val error = entities.find { it.name == "error" }?.value as? String ?: ""
                handleDebugRequest(session, error)
            }
            "learning_help" -> {
                val topic = entities.find { it.name == "topic" }?.value as? String ?: ""
                handleLearningRequest(session, topic)
            }
            "knowledge_query" -> {
                val query = entities.find { it.name == "query" }?.value as? String ?: ""
                handleKnowledgeQuery(session, query)
            }
            else -> {
                responseGenerator.generateGenericResponse(session.context, intent, entities)
            }
        }
    }

    /**
     * 处理脚本生成请求
     */
    private suspend fun handleScriptGenerationRequest(
        session: DialogSession,
        description: String
    ): Response {
        // 与脚本生成Agent交互
        val scriptGenerationRequest = mapOf(
            "description" to description,
            "context" to session.context
        )
        
        // 这里可以通过AgentManager获取ScriptGenerationAgent并发送请求
        // val scriptAgent = agentManager.getAgent<ScriptGenerationAgent>(ScriptGenerationAgent.AGENT_ID)
        // val result = scriptAgent?.generateScript(scriptGenerationRequest)
        
        return Response(
            success = true,
            message = "我可以帮您生成脚本。请告诉我更多的详细需求，比如要操作的应用、具体的步骤等。",
            data = mapOf("nextAction" to "collect_requirements")
        )
    }

    /**
     * 处理调试请求
     */
    private suspend fun handleDebugRequest(session: DialogSession, error: String): Response {
        val debugResult = debugAssistant.analyzeError("", error, session.context)
        
        return Response(
            success = true,
            message = debugResult.explanation,
            data = mapOf(
                "suggestions" to debugResult.suggestions,
                "fixes" to debugResult.fixes
            )
        )
    }

    /**
     * 处理学习请求
     */
    private suspend fun handleLearningRequest(session: DialogSession, topic: String): Response {
        val guidance = learningGuide.provideGuidance(topic, session.context)
        
        return Response(
            success = true,
            message = guidance.content,
            data = mapOf(
                "examples" to guidance.examples,
                "nextSteps" to guidance.nextSteps
            )
        )
    }

    /**
     * 处理知识查询
     */
    private suspend fun handleKnowledgeQuery(session: DialogSession, query: String): Response {
        val results = knowledgeBase.search(query)
        
        if (results.isEmpty()) {
            return Response(
                success = true,
                message = "抱歉，我没有找到相关的信息。您可以尝试重新描述您的问题。"
            )
        }
        
        val answer = results.first()
        return Response(
            success = true,
            message = answer.content,
            data = mapOf(
                "source" to answer.source,
                "related" to results.take(3).map { it.title }
            )
        )
    }

    /**
     * 生成欢迎消息
     */
    private fun generateWelcomeMessage(userId: String): String {
        return """
            您好！我是AutoJs6智能助手，很高兴为您服务！
            
            我可以帮您：
            • 📝 生成自动化脚本
            • 🐛 调试脚本问题
            • 📚 学习AutoJs6使用方法
            • ❓ 回答相关问题
            
            请告诉我您需要什么帮助？
        """.trimIndent()
    }

    /**
     * 处理反馈
     */
    private suspend fun processFeedback(sessionId: String, feedback: String, rating: Int) {
        val session = activeSessions[sessionId]
        if (session != null) {
            // 记录反馈
            session.feedback = feedback
            session.rating = rating
            
            // 用于改进对话质量
            responseGenerator.updateFromFeedback(feedback, rating)
            
            Log.i(TAG, "Received feedback for session $sessionId: $feedback (rating: $rating)")
        }
    }

    /**
     * 更新会话上下文
     */
    private suspend fun updateSessionContext(sessionId: String, context: Map<String, Any>) {
        val session = activeSessions[sessionId]
        if (session != null) {
            context.forEach { (key, value) ->
                session.context.entities = session.context.entities.orEmpty().toMutableMap().apply {
                    put(key, value)
                }
            }
            
            Log.d(TAG, "Updated context for session $sessionId")
        }
    }

    /**
     * 获取活动会话
     */
    private fun getActiveSessions(): List<DialogSession> {
        return activeSessions.values.toList()
    }

    /**
     * 获取会话上下文
     */
    private fun getSessionContext(sessionId: String): DialogContext? {
        return activeSessions[sessionId]?.context
    }

    /**
     * 搜索知识库
     */
    private suspend fun searchKnowledge(query: String): List<Map<String, Any>> {
        val results = knowledgeBase.search(query)
        return results.map { result ->
            mapOf(
                "title" to result.title,
                "content" to result.content,
                "source" to result.source
            )
        }
    }

    /**
     * 获取对话历史
     */
    private fun getConversationHistory(sessionId: String): List<DialogMessage> {
        return activeSessions[sessionId]?.context?.history ?: emptyList()
    }

    /**
     * 保存对话历史
     */
    private suspend fun saveConversationHistory(session: DialogSession) {
        // 这里可以实现保存对话历史的逻辑
        Log.d(TAG, "Saving conversation history for session: ${session.sessionId}")
    }
}