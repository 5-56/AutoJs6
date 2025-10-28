package org.autojs.autojs.agent.script.vision.model

import org.autojs.autojs.agent.core.model.UIElementInfo
import org.autojs.autojs.agent.core.model.Rect

/**
 * 截图分析结果
 */
data class ScreenshotAnalysisResult(
    val uiElements: List<UIElementInfo>,
    val textElements: List<UIElementInfo>,
    val uiComponents: List<UIComponent>,
    val layoutStructure: Map<String, Any>,
    val confidence: Float
) {
    companion object {
        fun empty() = ScreenshotAnalysisResult(
            uiElements = emptyList(),
            textElements = emptyList(),
            uiComponents = emptyList(),
            layoutStructure = emptyMap(),
            confidence = 0.0f
        )
    }
}

/**
 * UI组件
 */
data class UIComponent(
    val type: String,
    val bounds: Rect,
    val properties: Map<String, Any>
)

/**
 * 图像处理配置
 */
data class ImageProcessingConfig(
    val targetWidth: Int = 640,
    val targetHeight: Int = 480,
    val normalizePixels: Boolean = true,
    val enhanceContrast: Boolean = true,
    val removeNoise: Boolean = true
)

/**
 * 对象检测结果
 */
data class DetectionResult(
    val className: String,
    val confidence: Float,
    val bounds: Rect,
    val properties: Map<String, Any> = emptyMap()
)

/**
 * 文本识别结果
 */
data class TextRecognitionResult(
    val text: String,
    val confidence: Float,
    val bounds: Rect,
    val language: String = "zh"
)

/**
 * 视觉特征
 */
data class VisualFeature(
    val type: String,
    val value: Any,
    val confidence: Float
)

/**
 * 界面模式
 */
enum class UIPattern {
    LIST_VIEW,
    GRID_VIEW,
    TAB_VIEW,
    DIALOG,
    FORM,
    NAVIGATION,
    MENU,
    UNKNOWN
}

/**
 * 界面状态
 */
data class UIState(
    val pattern: UIPattern,
    val activeElements: List<UIElementInfo>,
    val focusedElement: UIElementInfo?,
    val isLoading: Boolean,
    val isAnimating: Boolean
)