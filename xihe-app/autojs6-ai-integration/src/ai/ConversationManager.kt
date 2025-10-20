package org.autojs.autojs.ai

import timber.log.Timber
import java.util.Date

/**
 * 对话管理器
 * 管理AI对话历史和上下文
 */
class ConversationManager {

    private val conversations = mutableListOf<ConversationRecord>()
    private val maxHistorySize = 50

    /**
     * 添加对话记录
     */
    fun addConversation(userRequest: String, result: ExecutionResult) {
        val record = ConversationRecord(
            userRequest = userRequest,
            generatedScript = result.script,
            executionResult = result,
            timestamp = Date()
        )
        
        conversations.add(record)
        
        // 限制历史记录数量
        if (conversations.size > maxHistorySize) {
            conversations.removeAt(0)
        }
        
        Timber.d("对话记录已保存，当前共 ${conversations.size} 条")
    }

    /**
     * 获取对话历史
     */
    fun getHistory(): List<ConversationRecord> {
        return conversations.toList()
    }

    /**
     * 获取最近的对话
     */
    fun getRecentConversations(count: Int = 10): List<ConversationRecord> {
        return conversations.takeLast(count)
    }

    /**
     * 查找相似的历史对话
     */
    fun findSimilarConversations(request: String): List<ConversationRecord> {
        val keywords = extractKeywords(request)
        
        return conversations.filter { conversation ->
            keywords.any { keyword ->
                conversation.userRequest.contains(keyword, ignoreCase = true)
            }
        }.sortedByDescending { it.timestamp }
    }

    /**
     * 提取关键词
     */
    private fun extractKeywords(text: String): List<String> {
        // 简单的关键词提取（可以改进为更智能的分词）
        val stopWords = setOf("帮我", "请", "一下", "的", "了", "吗", "呢", "啊", "吧")
        
        return text
            .split(Regex("\\s+"))
            .filter { it.length >= 2 && it !in stopWords }
    }

    /**
     * 获取成功的脚本示例
     */
    fun getSuccessfulScripts(): List<ConversationRecord> {
        return conversations.filter { it.executionResult.isSuccess }
    }

    /**
     * 获取失败的脚本（用于学习和改进）
     */
    fun getFailedScripts(): List<ConversationRecord> {
        return conversations.filter { !it.executionResult.isSuccess }
    }

    /**
     * 清空历史
     */
    fun clear() {
        conversations.clear()
        Timber.i("对话历史已清空")
    }

    /**
     * 获取统计信息
     */
    fun getStatistics(): ConversationStatistics {
        val total = conversations.size
        val successful = conversations.count { it.executionResult.isSuccess }
        val failed = total - successful
        val avgExecutionTime = conversations
            .mapNotNull { it.executionResult.executionTime }
            .average()
            .takeIf { !it.isNaN() } ?: 0.0
        
        return ConversationStatistics(
            total = total,
            successful = successful,
            failed = failed,
            successRate = if (total > 0) successful.toDouble() / total else 0.0,
            avgExecutionTime = avgExecutionTime.toLong()
        )
    }
}

/**
 * 对话记录
 */
data class ConversationRecord(
    val userRequest: String,
    val generatedScript: String,
    val executionResult: ExecutionResult,
    val timestamp: Date
)

/**
 * 对话统计
 */
data class ConversationStatistics(
    val total: Int,
    val successful: Int,
    val failed: Int,
    val successRate: Double,
    val avgExecutionTime: Long
)
