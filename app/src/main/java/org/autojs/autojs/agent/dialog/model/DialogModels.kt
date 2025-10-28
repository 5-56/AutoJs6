package org.autojs.autojs.agent.dialog.model

import org.autojs.autojs.agent.core.model.DialogContext

/**
 * 对话会话
 */
data class DialogSession(
    val sessionId: String,
    val userId: String,
    val startTime: Long,
    var endTime: Long? = null,
    val context: DialogContext,
    var feedback: String? = null,
    var rating: Int? = null
)

/**
 * 意图
 */
data class Intent(
    val name: String,
    val confidence: Float,
    val parameters: Map<String, Any> = emptyMap()
)

/**
 * 实体
 */
data class Entity(
    val name: String,
    val value: Any,
    val confidence: Float,
    val start: Int,
    val end: Int
)

/**
 * 响应
 */
data class Response(
    val success: Boolean,
    val message: String,
    val data: Map<String, Any>? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 调试结果
 */
data class DebugResult(
    val explanation: String,
    val suggestions: List<String>,
    val fixes: List<String>,
    val confidence: Float
)

/**
 * 学习指导
 */
data class LearningGuidance(
    val topic: String,
    val content: String,
    val examples: List<CodeExample>,
    val nextSteps: List<String>
)

/**
 * 代码示例
 */
data class CodeExample(
    val title: String,
    val code: String,
    val explanation: String,
    val language: String = "javascript"
)

/**
 * 知识库项目
 */
data class KnowledgeItem(
    val id: String,
    val title: String,
    val content: String,
    val source: String,
    val category: String,
    val tags: List<String>,
    val lastUpdated: Long
)

/**
 * 意图类型
 */
enum class IntentType {
    SCRIPT_GENERATION,
    DEBUG_HELP,
    LEARNING_HELP,
    KNOWLEDGE_QUERY,
    FEATURE_INQUIRY,
    TROUBLESHOOTING,
    GREETING,
    GOODBYE
}

/**
 * 对话状态
 */
enum class DialogState {
    INITIAL,
    WAITING_FOR_INPUT,
    PROCESSING,
    PROVIDING_ANSWER,
    COLLECTING_FEEDBACK,
    ENDED
}

/**
 * 用户反馈
 */
data class UserFeedback(
    val sessionId: String,
    val messageId: String,
    val rating: Int,
    val comment: String?,
    val timestamp: Long
)

/**
 * 对话统计
 */
data class DialogStats(
    val totalSessions: Int,
    val averageSessionLength: Long,
    val successRate: Float,
    val commonIntents: List<String>,
    val userSatisfaction: Float
)

/**
 * 上下文变量
 */
data class ContextVariable(
    val name: String,
    val value: Any,
    val type: String,
    val ttl: Long? = null
)

/**
 * 话题
 */
data class Topic(
    val id: String,
    val name: String,
    val description: String,
    val keywords: List<String>,
    val difficulty: DifficultyLevel,
    val prerequisites: List<String> = emptyList()
)

/**
 * 难度级别
 */
enum class DifficultyLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

/**
 * 学习路径
 */
data class LearningPath(
    val id: String,
    val name: String,
    val description: String,
    val topics: List<Topic>,
    val estimatedTime: Long
)

/**
 * 问答对
 */
data class QAPair(
    val question: String,
    val answer: String,
    val category: String,
    val confidence: Float
)

/**
 * 对话流
 */
data class DialogFlow(
    val id: String,
    val name: String,
    val steps: List<DialogStep>,
    val conditions: Map<String, Any>
)

/**
 * 对话步骤
 */
data class DialogStep(
    val id: String,
    val type: StepType,
    val prompt: String,
    val expectedInputs: List<String>,
    val nextSteps: Map<String, String>
)

/**
 * 步骤类型
 */
enum class StepType {
    QUESTION,
    INFORMATION,
    CONFIRMATION,
    ACTION,
    BRANCH
}