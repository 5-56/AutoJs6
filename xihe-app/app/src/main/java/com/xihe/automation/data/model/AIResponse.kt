package com.xihe.automation.data.model

/**
 * AI响应数据类
 */
data class AIResponse(
    val message: String,
    val hasScript: Boolean = false,
    val script: String? = null,
    val needsScreenAnalysis: Boolean = false,
    val suggestions: List<String> = emptyList()
)

/**
 * 屏幕分析结果
 */
data class ScreenAnalysis(
    val elements: List<UIElement>,
    val texts: List<String>,
    val screenshot: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScreenAnalysis

        if (elements != other.elements) return false
        if (texts != other.texts) return false
        if (screenshot != null) {
            if (other.screenshot == null) return false
            if (!screenshot.contentEquals(other.screenshot)) return false
        } else if (other.screenshot != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = elements.hashCode()
        result = 31 * result + texts.hashCode()
        result = 31 * result + (screenshot?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * UI元素
 */
data class UIElement(
    val type: String,
    val text: String?,
    val bounds: Bounds?,
    val description: String,
    val isClickable: Boolean = false,
    val isScrollable: Boolean = false
)

/**
 * 元素边界
 */
data class Bounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top
    val centerX: Int get() = (left + right) / 2
    val centerY: Int get() = (top + bottom) / 2
}

/**
 * 脚本执行结果
 */
data class ScriptExecutionResult(
    val success: Boolean,
    val output: String = "",
    val error: String? = null,
    val executionTime: Long = 0
)
