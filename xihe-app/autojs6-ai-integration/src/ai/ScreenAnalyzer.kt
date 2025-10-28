package org.autojs.autojs.ai

import android.graphics.Rect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.autojs.autojs.core.accessibility.UiObject
import org.autojs.autojs.core.image.ImageWrapper
import org.autojs.autojs.runtime.ScriptRuntime
import timber.log.Timber

/**
 * 屏幕分析器（使用AutoJs6的真实功能）
 */
class ScreenAnalyzer(private val runtime: ScriptRuntime) {

    /**
     * 分析当前屏幕
     * 使用AutoJs6的accessibility服务和OCR功能
     */
    suspend fun analyzeCurrentScreen(): ScreenInfo = withContext(Dispatchers.IO) {
        try {
            // 1. 获取UI元素树（使用AutoJs6的accessibility）
            val elements = analyzeUIElements()
            
            // 2. 捕获屏幕截图（使用AutoJs6的截图功能）
            val screenshot = captureScreen()
            
            // 3. OCR文字识别（使用AutoJs6的OCR）
            val texts = recognizeText(screenshot)
            
            ScreenInfo(
                elements = elements,
                texts = texts,
                screenshot = screenshot,
                screenWidth = screenshot?.width ?: 0,
                screenHeight = screenshot?.height ?: 0
            )
            
        } catch (e: Exception) {
            Timber.e(e, "屏幕分析失败")
            ScreenInfo.empty()
        }
    }

    /**
     * 分析UI元素（使用AutoJs6的accessibility）
     */
    private fun analyzeUIElements(): List<UIElementInfo> {
        val elements = mutableListOf<UIElementInfo>()
        
        try {
            // 获取当前窗口的根节点
            val bridge = runtime.accessibilityBridge
            val roots = bridge.windowRoots()
            
            // 遍历所有窗口
            roots.forEach { windowRoot ->
                windowRoot.root?.let { rootNode ->
                    traverseNode(rootNode, elements, 0)
                }
            }
            
            Timber.i("分析到 ${elements.size} 个UI元素")
            
        } catch (e: Exception) {
            Timber.e(e, "UI元素分析失败")
        }
        
        return elements
    }

    /**
     * 递归遍历UI节点树
     */
    private fun traverseNode(node: UiObject, elements: MutableList<UIElementInfo>, depth: Int) {
        try {
            // 提取节点信息
            val info = UIElementInfo(
                className = node.className() ?: "",
                text = node.text() ?: "",
                contentDesc = node.contentDescription() ?: "",
                viewId = node.id() ?: "",
                bounds = node.bounds(),
                isClickable = node.isClickable(),
                isScrollable = node.isScrollable(),
                isEnabled = node.isEnabled(),
                isFocused = node.isFocused(),
                isCheckable = node.isCheckable(),
                isChecked = node.isChecked(),
                depth = depth
            )
            
            // 只添加有意义的元素（有文本或可交互）
            if (info.text.isNotEmpty() || 
                info.contentDesc.isNotEmpty() || 
                info.isClickable || 
                info.isScrollable) {
                elements.add(info)
            }
            
            // 递归遍历子节点
            val childCount = node.childCount()
            for (i in 0 until childCount) {
                node.child(i)?.let { child ->
                    traverseNode(child, elements, depth + 1)
                }
            }
            
        } catch (e: Exception) {
            Timber.w(e, "遍历节点失败")
        }
    }

    /**
     * 捕获屏幕（使用AutoJs6的截图功能）
     */
    private fun captureScreen(): ImageWrapper? {
        return try {
            // 使用AutoJs6的images模块捕获屏幕
            runtime.images.captureScreen()
        } catch (e: Exception) {
            Timber.e(e, "屏幕捕获失败")
            null
        }
    }

    /**
     * OCR文字识别（使用AutoJs6的OCR功能）
     */
    private fun recognizeText(screenshot: ImageWrapper?): List<String> {
        if (screenshot == null) return emptyList()
        
        return try {
            // 使用AutoJs6的OCR模块（MLKit或其他）
            val ocrResult = runtime.ocr.detect(screenshot)
            
            ocrResult?.results?.map { it.text.toString() } ?: emptyList()
            
        } catch (e: Exception) {
            Timber.e(e, "OCR识别失败")
            emptyList()
        }
    }

    /**
     * 查找包含指定文本的元素
     */
    fun findElementByText(text: String, screenInfo: ScreenInfo): UIElementInfo? {
        return screenInfo.elements.find { 
            it.text.contains(text, ignoreCase = true) ||
            it.contentDesc.contains(text, ignoreCase = true)
        }
    }

    /**
     * 查找可点击的元素
     */
    fun findClickableElements(screenInfo: ScreenInfo): List<UIElementInfo> {
        return screenInfo.elements.filter { it.isClickable }
    }
}

/**
 * 屏幕信息
 */
data class ScreenInfo(
    val elements: List<UIElementInfo>,
    val texts: List<String>,
    val screenshot: ImageWrapper?,
    val screenWidth: Int,
    val screenHeight: Int
) {
    companion object {
        fun empty() = ScreenInfo(
            elements = emptyList(),
            texts = emptyList(),
            screenshot = null,
            screenWidth = 0,
            screenHeight = 0
        )
    }
}

/**
 * UI元素信息
 */
data class UIElementInfo(
    val className: String,
    val text: String,
    val contentDesc: String,
    val viewId: String,
    val bounds: Rect,
    val isClickable: Boolean,
    val isScrollable: Boolean,
    val isEnabled: Boolean,
    val isFocused: Boolean,
    val isCheckable: Boolean,
    val isChecked: Boolean,
    val depth: Int
) {
    val centerX: Int get() = (bounds.left + bounds.right) / 2
    val centerY: Int get() = (bounds.top + bounds.bottom) / 2
    
    override fun toString(): String {
        return buildString {
            append("$className")
            if (text.isNotEmpty()) append(" text='$text'")
            if (contentDesc.isNotEmpty()) append(" desc='$contentDesc'")
            if (viewId.isNotEmpty()) append(" id='$viewId'")
            append(" at (${centerX},${centerY})")
            val attrs = mutableListOf<String>()
            if (isClickable) attrs.add("clickable")
            if (isScrollable) attrs.add("scrollable")
            if (!isEnabled) attrs.add("disabled")
            if (attrs.isNotEmpty()) append(" [${attrs.joinToString()}]")
        }
    }
}
