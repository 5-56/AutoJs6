package org.autojs.autojs.agent.behavior

import android.content.Context
import android.util.Log
import kotlinx.coroutines.launch
import org.autojs.autojs.agent.core.AgentBase
import org.autojs.autojs.agent.core.AgentConfig
import org.autojs.autojs.agent.core.model.AgentEvent
import org.autojs.autojs.agent.core.model.AgentMessage
import org.autojs.autojs.agent.core.model.MessageType
import org.autojs.autojs.agent.core.model.UserBehaviorData
import org.autojs.autojs.agent.behavior.analysis.BehaviorAnalyzer
import org.autojs.autojs.agent.behavior.recommendation.RecommendationEngine
import org.autojs.autojs.agent.behavior.learning.MLProcessor
import org.autojs.autojs.agent.behavior.pattern.PatternDetector
import org.autojs.autojs.agent.behavior.model.BehaviorPattern
import org.autojs.autojs.agent.behavior.model.UserProfile
import org.autojs.autojs.agent.behavior.model.Recommendation
import org.autojs.autojs.agent.behavior.model.AutomationSuggestion
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 用户行为学习Agent
 * 核心功能：
 * 1. 用户行为分析
 * 2. 个性化推荐
 * 3. 自动化建议
 * 4. 偏好学习
 * 
 * Created by SuperMonster003 on 2024/01/15
 */
class BehaviorLearningAgent(
    context: Context,
    config: AgentConfig
) : AgentBase(context, config) {

    companion object {
        const val AGENT_ID = "behavior_learning_agent"
        private const val TAG = "BehaviorLearningAgent"
    }

    override val agentId: String = AGENT_ID
    override val agentName: String = "用户行为学习Agent"
    override val agentDescription: String = "分析用户行为模式，提供个性化推荐和自动化建议"

    private lateinit var behaviorAnalyzer: BehaviorAnalyzer
    private lateinit var recommendationEngine: RecommendationEngine
    private lateinit var mlProcessor: MLProcessor
    private lateinit var patternDetector: PatternDetector

    private val behaviorHistory = ConcurrentLinkedQueue<UserBehaviorData>()
    private val userProfiles = ConcurrentHashMap<String, UserProfile>()
    private val detectedPatterns = ConcurrentHashMap<String, BehaviorPattern>()

    override suspend fun onInitialize() {
        try {
            behaviorAnalyzer = BehaviorAnalyzer(context)
            recommendationEngine = RecommendationEngine(context, config)
            mlProcessor = MLProcessor(context, config)
            patternDetector = PatternDetector(context)

            // 初始化机器学习模型
            mlProcessor.initializeModel()
            
            // 加载用户历史数据
            loadUserProfiles()
            
            Log.i(TAG, "BehaviorLearningAgent initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize BehaviorLearningAgent", e)
            onError(e)
        }
    }

    override suspend fun onStart() {
        agentScope.launch {
            startLearningLoop()
        }
        Log.i(TAG, "BehaviorLearningAgent started")
    }

    override suspend fun onStop() {
        saveUserProfiles()
        Log.i(TAG, "BehaviorLearningAgent stopped")
    }

    override suspend fun onDestroy() {
        behaviorHistory.clear()
        userProfiles.clear()
        detectedPatterns.clear()
        mlProcessor.release()
        Log.i(TAG, "BehaviorLearningAgent destroyed")
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
        Log.e(TAG, "BehaviorLearningAgent error", error)
        emitEvent(AgentEvent.ERROR, error.message)
    }

    private suspend fun handleCommand(message: AgentMessage) {
        when (message.content) {
            "record_behavior" -> {
                message.data?.let { data ->
                    val behavior = parseBehaviorData(data)
                    recordBehavior(behavior)
                }
            }
            "analyze_patterns" -> {
                message.data?.let { data ->
                    val userId = data["userId"] as? String ?: return
                    val patterns = analyzeUserPatterns(userId)
                    emitEvent(AgentEvent.TASK_COMPLETED, patterns)
                }
            }
            "generate_recommendations" -> {
                message.data?.let { data ->
                    val userId = data["userId"] as? String ?: return
                    val recommendations = generateRecommendations(userId)
                    emitEvent(AgentEvent.TASK_COMPLETED, recommendations)
                }
            }
            "suggest_automation" -> {
                message.data?.let { data ->
                    val userId = data["userId"] as? String ?: return
                    val suggestions = suggestAutomation(userId)
                    emitEvent(AgentEvent.TASK_COMPLETED, suggestions)
                }
            }
            "update_preferences" -> {
                message.data?.let { data ->
                    val userId = data["userId"] as? String ?: return
                    val preferences = data["preferences"] as? Map<String, Any> ?: return
                    updateUserPreferences(userId, preferences)
                }
            }
        }
    }

    private suspend fun handleQuery(message: AgentMessage) {
        when (message.content) {
            "get_user_profile" -> {
                message.data?.let { data ->
                    val userId = data["userId"] as? String ?: return
                    val profile = getUserProfile(userId)
                    emitEvent(AgentEvent.TASK_COMPLETED, profile)
                }
            }
            "get_behavior_history" -> {
                message.data?.let { data ->
                    val userId = data["userId"] as? String ?: return
                    val history = getBehaviorHistory(userId)
                    emitEvent(AgentEvent.TASK_COMPLETED, history)
                }
            }
            "get_detected_patterns" -> {
                val patterns = getDetectedPatterns()
                emitEvent(AgentEvent.TASK_COMPLETED, patterns)
            }
            "get_learning_stats" -> {
                val stats = getLearningStats()
                emitEvent(AgentEvent.TASK_COMPLETED, stats)
            }
        }
    }

    private suspend fun handleNotification(message: AgentMessage) {
        when (message.content) {
            "user_action" -> {
                message.data?.let { data ->
                    val behavior = parseBehaviorData(data)
                    processUserAction(behavior)
                }
            }
            "app_usage" -> {
                message.data?.let { data ->
                    val behavior = parseBehaviorData(data)
                    processAppUsage(behavior)
                }
            }
            "script_execution" -> {
                message.data?.let { data ->
                    val behavior = parseBehaviorData(data)
                    processScriptExecution(behavior)
                }
            }
        }
    }

    /**
     * 启动学习循环
     */
    private suspend fun startLearningLoop() {
        while (isRunning.get()) {
            try {
                processBehaviorData()
                updateUserProfiles()
                detectNewPatterns()
                kotlinx.coroutines.delay(5000) // 每5秒处理一次
            } catch (e: Exception) {
                Log.e(TAG, "Error in learning loop", e)
            }
        }
    }

    /**
     * 记录用户行为
     */
    private suspend fun recordBehavior(behavior: UserBehaviorData) {
        behaviorHistory.offer(behavior)
        
        // 如果队列太大，移除旧数据
        if (behaviorHistory.size > 10000) {
            behaviorHistory.poll()
        }
        
        // 实时分析
        behaviorAnalyzer.analyze(behavior)
        
        Log.d(TAG, "Recorded behavior: ${behavior.action}")
    }

    /**
     * 处理行为数据
     */
    private suspend fun processBehaviorData() {
        val recentBehaviors = behaviorHistory.takeLast(100)
        if (recentBehaviors.isNotEmpty()) {
            mlProcessor.processBatch(recentBehaviors)
        }
    }

    /**
     * 更新用户档案
     */
    private suspend fun updateUserProfiles() {
        val userBehaviors = behaviorHistory.groupBy { it.metadata?.get("userId") as? String ?: "default" }
        
        userBehaviors.forEach { (userId, behaviors) ->
            val profile = userProfiles.getOrPut(userId) { 
                UserProfile(userId = userId, createdAt = System.currentTimeMillis())
            }
            
            // 更新用户偏好
            profile.updatePreferences(behaviors)
            profile.lastUpdateTime = System.currentTimeMillis()
            
            userProfiles[userId] = profile
        }
    }

    /**
     * 检测新模式
     */
    private suspend fun detectNewPatterns() {
        val newPatterns = patternDetector.detectPatterns(behaviorHistory.toList())
        
        newPatterns.forEach { pattern ->
            detectedPatterns[pattern.id] = pattern
            Log.i(TAG, "New pattern detected: ${pattern.name}")
            
            // 通知其他Agent
            emitEvent(AgentEvent.MESSAGE_PROCESSED, pattern)
        }
    }

    /**
     * 分析用户模式
     */
    private suspend fun analyzeUserPatterns(userId: String): List<BehaviorPattern> {
        val userBehaviors = behaviorHistory.filter { 
            it.metadata?.get("userId") as? String == userId 
        }
        
        return patternDetector.analyzeUserPatterns(userBehaviors)
    }

    /**
     * 生成推荐
     */
    private suspend fun generateRecommendations(userId: String): List<Recommendation> {
        val userProfile = userProfiles[userId] ?: return emptyList()
        return recommendationEngine.generateRecommendations(userProfile)
    }

    /**
     * 建议自动化
     */
    private suspend fun suggestAutomation(userId: String): List<AutomationSuggestion> {
        val userProfile = userProfiles[userId] ?: return emptyList()
        val userPatterns = analyzeUserPatterns(userId)
        
        return recommendationEngine.suggestAutomation(userProfile, userPatterns)
    }

    /**
     * 更新用户偏好
     */
    private suspend fun updateUserPreferences(userId: String, preferences: Map<String, Any>) {
        val profile = userProfiles.getOrPut(userId) {
            UserProfile(userId = userId, createdAt = System.currentTimeMillis())
        }
        
        profile.updatePreferencesFromMap(preferences)
        profile.lastUpdateTime = System.currentTimeMillis()
        
        userProfiles[userId] = profile
        
        Log.i(TAG, "Updated preferences for user: $userId")
    }

    /**
     * 处理用户动作
     */
    private suspend fun processUserAction(behavior: UserBehaviorData) {
        recordBehavior(behavior)
        
        // 实时推荐
        val userId = behavior.metadata?.get("userId") as? String ?: "default"
        val recommendations = generateRecommendations(userId)
        
        if (recommendations.isNotEmpty()) {
            emitEvent(AgentEvent.MESSAGE_PROCESSED, recommendations)
        }
    }

    /**
     * 处理应用使用情况
     */
    private suspend fun processAppUsage(behavior: UserBehaviorData) {
        recordBehavior(behavior)
        
        // 检测频繁使用的应用
        val userId = behavior.metadata?.get("userId") as? String ?: "default"
        val profile = userProfiles[userId]
        
        profile?.let { p ->
            val appUsage = p.appUsageStats[behavior.context] ?: 0
            p.appUsageStats[behavior.context] = appUsage + 1
            
            // 如果某个应用使用频繁，建议自动化
            if (appUsage > 10) {
                val suggestions = suggestAutomation(userId)
                if (suggestions.isNotEmpty()) {
                    emitEvent(AgentEvent.MESSAGE_PROCESSED, suggestions)
                }
            }
        }
    }

    /**
     * 处理脚本执行
     */
    private suspend fun processScriptExecution(behavior: UserBehaviorData) {
        recordBehavior(behavior)
        
        // 学习脚本使用模式
        val userId = behavior.metadata?.get("userId") as? String ?: "default"
        val profile = userProfiles[userId]
        
        profile?.let { p ->
            val scriptUsage = p.scriptUsageStats[behavior.context] ?: 0
            p.scriptUsageStats[behavior.context] = scriptUsage + 1
            
            // 更新成功率
            val success = behavior.metadata?.get("success") as? Boolean ?: false
            if (success) {
                p.scriptSuccessRate[behavior.context] = 
                    (p.scriptSuccessRate[behavior.context] ?: 0.0) + 0.1
            }
        }
    }

    /**
     * 解析行为数据
     */
    private fun parseBehaviorData(data: Map<String, Any>): UserBehaviorData {
        return UserBehaviorData(
            action = data["action"] as String,
            context = data["context"] as String,
            timestamp = data["timestamp"] as? Long ?: System.currentTimeMillis(),
            metadata = data["metadata"] as? Map<String, Any>
        )
    }

    /**
     * 获取用户档案
     */
    private fun getUserProfile(userId: String): UserProfile? {
        return userProfiles[userId]
    }

    /**
     * 获取行为历史
     */
    private fun getBehaviorHistory(userId: String): List<UserBehaviorData> {
        return behaviorHistory.filter { 
            it.metadata?.get("userId") as? String == userId 
        }
    }

    /**
     * 获取检测到的模式
     */
    private fun getDetectedPatterns(): List<BehaviorPattern> {
        return detectedPatterns.values.toList()
    }

    /**
     * 获取学习统计信息
     */
    private fun getLearningStats(): Map<String, Any> {
        return mapOf(
            "totalBehaviors" to behaviorHistory.size,
            "totalUsers" to userProfiles.size,
            "detectedPatterns" to detectedPatterns.size,
            "activeProfiles" to userProfiles.values.count { 
                System.currentTimeMillis() - it.lastUpdateTime < 24 * 60 * 60 * 1000 
            }
        )
    }

    /**
     * 加载用户档案
     */
    private suspend fun loadUserProfiles() {
        // 从本地存储加载用户档案
        // 这里可以实现具体的持久化逻辑
        Log.d(TAG, "Loading user profiles")
    }

    /**
     * 保存用户档案
     */
    private suspend fun saveUserProfiles() {
        // 保存用户档案到本地存储
        // 这里可以实现具体的持久化逻辑
        Log.d(TAG, "Saving user profiles")
    }
}