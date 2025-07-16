package org.autojs.autojs.agent.behavior.model

import org.autojs.autojs.agent.core.model.UserBehaviorData

/**
 * 行为模式
 */
data class BehaviorPattern(
    val id: String,
    val name: String,
    val description: String,
    val frequency: Int,
    val confidence: Float,
    val actions: List<String>,
    val context: String,
    val timestamps: List<Long>
)

/**
 * 用户档案
 */
data class UserProfile(
    val userId: String,
    val createdAt: Long,
    var lastUpdateTime: Long = System.currentTimeMillis(),
    val preferences: MutableMap<String, Any> = mutableMapOf(),
    val appUsageStats: MutableMap<String, Int> = mutableMapOf(),
    val scriptUsageStats: MutableMap<String, Int> = mutableMapOf(),
    val scriptSuccessRate: MutableMap<String, Double> = mutableMapOf(),
    val behaviorPatterns: MutableList<BehaviorPattern> = mutableListOf()
) {
    fun updatePreferences(behaviors: List<UserBehaviorData>) {
        // 分析行为数据，更新用户偏好
        behaviors.forEach { behavior ->
            when (behavior.action) {
                "script_execution" -> {
                    val scriptType = behavior.metadata?.get("scriptType") as? String
                    if (scriptType != null) {
                        preferences[scriptType] = (preferences[scriptType] as? Int ?: 0) + 1
                    }
                }
                "app_usage" -> {
                    val appName = behavior.context
                    appUsageStats[appName] = (appUsageStats[appName] ?: 0) + 1
                }
            }
        }
    }

    fun updatePreferencesFromMap(newPreferences: Map<String, Any>) {
        preferences.putAll(newPreferences)
    }
}

/**
 * 推荐项
 */
data class Recommendation(
    val id: String,
    val type: RecommendationType,
    val title: String,
    val description: String,
    val confidence: Float,
    val data: Map<String, Any>,
    val priority: Int = 0
)

/**
 * 推荐类型
 */
enum class RecommendationType {
    SCRIPT_TEMPLATE,
    APP_AUTOMATION,
    WORKFLOW_OPTIMIZATION,
    FEATURE_DISCOVERY,
    PERFORMANCE_IMPROVEMENT
}

/**
 * 自动化建议
 */
data class AutomationSuggestion(
    val id: String,
    val title: String,
    val description: String,
    val estimatedTimeSaving: Long,
    val difficulty: DifficultyLevel,
    val steps: List<AutomationStep>,
    val benefits: List<String>
)

/**
 * 难度级别
 */
enum class DifficultyLevel {
    EASY,
    MEDIUM,
    HARD
}

/**
 * 自动化步骤
 */
data class AutomationStep(
    val order: Int,
    val description: String,
    val action: String,
    val parameters: Map<String, Any>
)

/**
 * 行为分析结果
 */
data class BehaviorAnalysisResult(
    val userId: String,
    val patterns: List<BehaviorPattern>,
    val insights: List<String>,
    val recommendations: List<Recommendation>,
    val confidence: Float
)

/**
 * 学习状态
 */
data class LearningState(
    val totalBehaviors: Int,
    val patternsDetected: Int,
    val lastUpdateTime: Long,
    val accuracy: Float,
    val isLearning: Boolean
)

/**
 * 行为分类
 */
enum class BehaviorCategory {
    SCRIPT_EXECUTION,
    APP_USAGE,
    UI_INTERACTION,
    SYSTEM_OPERATION,
    AUTOMATION_SETUP
}

/**
 * 行为频率
 */
data class BehaviorFrequency(
    val action: String,
    val count: Int,
    val timeWindow: TimeWindow,
    val trend: Trend
)

/**
 * 时间窗口
 */
enum class TimeWindow {
    HOUR,
    DAY,
    WEEK,
    MONTH
}

/**
 * 趋势
 */
enum class Trend {
    INCREASING,
    DECREASING,
    STABLE
}

/**
 * 用户意图
 */
data class UserIntent(
    val name: String,
    val confidence: Float,
    val context: String,
    val parameters: Map<String, Any>
)

/**
 * 行为序列
 */
data class BehaviorSequence(
    val id: String,
    val actions: List<String>,
    val timestamps: List<Long>,
    val duration: Long,
    val frequency: Int
)

/**
 * 偏好权重
 */
data class PreferenceWeight(
    val category: String,
    val weight: Float,
    val lastUpdated: Long
)