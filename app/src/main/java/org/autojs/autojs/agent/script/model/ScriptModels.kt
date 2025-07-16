package org.autojs.autojs.agent.script.model

import org.autojs.autojs.agent.core.model.UIElementInfo

/**
 * 脚本模板
 */
data class ScriptTemplate(
    val id: String,
    val name: String,
    val description: String,
    val code: String,
    val category: String,
    val tags: List<String>,
    val parameters: List<TemplateParameter>,
    val difficulty: DifficultyLevel,
    val usage: Int = 0
)

/**
 * 模板参数
 */
data class TemplateParameter(
    val name: String,
    val type: String,
    val description: String,
    val required: Boolean = true,
    val defaultValue: Any? = null
)

/**
 * 生成上下文
 */
data class GenerationContext(
    val description: String,
    val intent: String,
    val entities: Map<String, Any>,
    val uiElements: List<UIElementInfo>,
    val template: ScriptTemplate?,
    val preferences: Map<String, Any>
)

/**
 * 优化建议
 */
data class OptimizationSuggestion(
    val type: OptimizationType,
    val description: String,
    val originalCode: String,
    val suggestedCode: String,
    val benefit: String,
    val priority: Priority
)

/**
 * 优化类型
 */
enum class OptimizationType {
    PERFORMANCE,
    STABILITY,
    READABILITY,
    ERROR_HANDLING,
    BEST_PRACTICES
}

/**
 * 优先级
 */
enum class Priority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * 难度级别
 */
enum class DifficultyLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

/**
 * 脚本分析结果
 */
data class ScriptAnalysisResult(
    val syntaxValid: Boolean,
    val issues: List<CodeIssue>,
    val suggestions: List<OptimizationSuggestion>,
    val complexity: Int,
    val estimatedRuntime: Long
)

/**
 * 代码问题
 */
data class CodeIssue(
    val type: IssueType,
    val severity: Severity,
    val message: String,
    val line: Int,
    val column: Int,
    val suggestion: String?
)

/**
 * 问题类型
 */
enum class IssueType {
    SYNTAX_ERROR,
    RUNTIME_ERROR,
    WARNING,
    STYLE_ISSUE,
    PERFORMANCE_ISSUE
}

/**
 * 严重程度
 */
enum class Severity {
    ERROR,
    WARNING,
    INFO
}

/**
 * 脚本元数据
 */
data class ScriptMetadata(
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val createdAt: Long,
    val lastModified: Long,
    val dependencies: List<String>,
    val permissions: List<String>
)

/**
 * 代码片段
 */
data class CodeSnippet(
    val id: String,
    val title: String,
    val code: String,
    val description: String,
    val category: String,
    val usage: Int = 0
)

/**
 * 脚本统计
 */
data class ScriptStats(
    val totalGenerated: Int,
    val successRate: Float,
    val averageGenerationTime: Long,
    val popularTemplates: List<String>,
    val commonPatterns: List<String>
)