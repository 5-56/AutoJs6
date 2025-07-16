package org.autojs.autojs.agent.ui.model

import org.autojs.autojs.agent.core.model.UIElementInfo
import org.autojs.autojs.agent.core.model.Rect

/**
 * UI快照
 */
data class UISnapshot(
    val packageName: String,
    val activityName: String,
    val timestamp: Long,
    val elements: List<UIElementInfo>,
    val screenSize: Rect,
    val orientation: Int,
    val hash: String
)

/**
 * UI变化事件
 */
data class UIChangeEvent(
    val type: String,
    val packageName: String,
    val elementId: String?,
    val timestamp: Long,
    val changeDetails: Map<String, Any> = emptyMap()
)

/**
 * 元素映射
 */
data class ElementMapping(
    val packageName: String,
    val mappings: Map<String, String>,
    val version: String = "1.0",
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * 修复建议
 */
data class RepairSuggestion(
    val id: String,
    val type: RepairType,
    val description: String,
    val scriptPath: String,
    val oldElement: UIElementInfo?,
    val newElement: UIElementInfo?,
    val confidence: Float,
    val autoFix: Boolean = false
)

/**
 * 修复类型
 */
enum class RepairType {
    ELEMENT_NOT_FOUND,
    ELEMENT_MOVED,
    ELEMENT_CHANGED,
    LAYOUT_CHANGED,
    APP_UPDATED,
    PERMISSION_CHANGED
}

/**
 * 布局分析结果
 */
data class LayoutAnalysisResult(
    val layoutType: LayoutType,
    val hierarchyDepth: Int,
    val elementCount: Int,
    val interactableElements: List<UIElementInfo>,
    val textElements: List<UIElementInfo>,
    val imageElements: List<UIElementInfo>,
    val patterns: List<UIPattern>
)

/**
 * 布局类型
 */
enum class LayoutType {
    LINEAR,
    RELATIVE,
    CONSTRAINT,
    FRAME,
    GRID,
    RECYCLER_VIEW,
    CUSTOM
}

/**
 * UI模式
 */
data class UIPattern(
    val name: String,
    val description: String,
    val elements: List<UIElementInfo>,
    val confidence: Float
)

/**
 * 元素定位策略
 */
data class ElementLocatorStrategy(
    val type: LocatorType,
    val value: String,
    val fallbacks: List<ElementLocatorStrategy> = emptyList(),
    val priority: Int = 0
)

/**
 * 定位器类型
 */
enum class LocatorType {
    ID,
    TEXT,
    CLASS_NAME,
    XPATH,
    ACCESSIBILITY_ID,
    BOUNDS,
    IMAGE_MATCH,
    SEMANTIC_MATCH
}

/**
 * UI适配配置
 */
data class UIAdaptationConfig(
    val enableAutoRepair: Boolean = true,
    val enableElementCaching: Boolean = true,
    val maxRetryAttempts: Int = 3,
    val similarityThreshold: Float = 0.8f,
    val trackingEnabled: Boolean = true
)

/**
 * 元素相似度
 */
data class ElementSimilarity(
    val element1: UIElementInfo,
    val element2: UIElementInfo,
    val similarity: Float,
    val factors: Map<String, Float>
)

/**
 * UI状态快照
 */
data class UIStateSnapshot(
    val packageName: String,
    val timestamp: Long,
    val state: UIState,
    val elements: List<UIElementInfo>,
    val metadata: Map<String, Any>
)

/**
 * UI状态
 */
enum class UIState {
    LOADING,
    LOADED,
    ERROR,
    EMPTY,
    REFRESHING,
    NAVIGATING
}

/**
 * 失败分析
 */
data class FailureAnalysis(
    val isUIRelated: Boolean,
    val failureType: FailureType,
    val affectedElements: List<UIElementInfo>,
    val suggestions: List<String>,
    val confidence: Float
)

/**
 * 失败类型
 */
enum class FailureType {
    ELEMENT_NOT_FOUND,
    ELEMENT_NOT_CLICKABLE,
    ELEMENT_NOT_VISIBLE,
    TIMEOUT,
    PERMISSION_DENIED,
    APP_CRASH,
    NETWORK_ERROR,
    UNKNOWN
}

/**
 * UI变化类型
 */
enum class UIChangeType {
    ELEMENT_ADDED,
    ELEMENT_REMOVED,
    ELEMENT_MODIFIED,
    LAYOUT_CHANGED,
    THEME_CHANGED,
    ORIENTATION_CHANGED,
    SCREEN_SIZE_CHANGED
}

/**
 * 适配统计
 */
data class AdaptationStats(
    val totalAttempts: Int,
    val successfulAdaptations: Int,
    val failedAdaptations: Int,
    val averageAdaptationTime: Long,
    val mostCommonFailures: List<FailureType>
)