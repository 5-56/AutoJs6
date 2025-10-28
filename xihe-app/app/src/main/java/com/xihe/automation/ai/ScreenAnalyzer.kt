package com.xihe.automation.ai

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.xihe.automation.autojs.core.accessibility.UiObject
import com.xihe.automation.autojs.runtime.ScriptRuntime
import com.xihe.automation.data.model.Bounds
import com.xihe.automation.data.model.ScreenAnalysis
import com.xihe.automation.data.model.UIElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream

/**
 * 屏幕分析器（集成AutoJs6真实功能）
 * 负责捕获和分析屏幕内容
 */
class ScreenAnalyzer {

    private val textRecognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

    /**
     * 捕获屏幕（使用AutoJs6的真实截图功能）
     */
    suspend fun captureScreen(runtime: ScriptRuntime): Bitmap? = withContext(Dispatchers.IO) {
        return@withContext try {
            Timber.i("使用AutoJs6捕获屏幕")
            
            // 使用AutoJs6的images模块捕获屏幕
            val imageWrapper = runtime.images.captureScreen()
            imageWrapper?.bitmap
            
        } catch (e: Exception) {
            Timber.e(e, "屏幕捕获失败")
            null
        }
    }

    /**
     * 分析屏幕内容（使用AutoJs6的真实功能）
     */
    suspend fun analyzeScreen(runtime: ScriptRuntime): ScreenAnalysis = withContext(Dispatchers.IO) {
        return@withContext try {
            Timber.i("开始分析屏幕")
            
            // 1. 分析UI元素（使用AutoJs6的accessibility）
            val elements = analyzeUIElementsWithAutoJs(runtime)
            Timber.d("分析到 ${elements.size} 个UI元素")
            
            // 2. 捕获屏幕并OCR识别（使用AutoJs6的功能）
            val texts = recognizeTextWithAutoJs(runtime)
            Timber.d("OCR识别到 ${texts.size} 段文字")
            
            ScreenAnalysis(
                elements = elements,
                texts = texts
            )
            
        } catch (e: Exception) {
            Timber.e(e, "屏幕分析失败")
            ScreenAnalysis(
                elements = emptyList(),
                texts = emptyList()
            )
        }
    }

    /**
     * 识别屏幕上的文字（使用AutoJs6的OCR）
     */
    private suspend fun recognizeTextWithAutoJs(runtime: ScriptRuntime): List<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            // 捕获屏幕
            val screenshot = runtime.images.captureScreen()
            
            if (screenshot == null) {
                Timber.w("无法捕获屏幕，可能需要截图权限")
                return@withContext emptyList()
            }
            
            // 使用AutoJs6的OCR功能
            val ocrResult = runtime.ocr.detect(screenshot)
            
            ocrResult?.results?.map { it.text.toString() } ?: emptyList()
            
        } catch (e: Exception) {
            Timber.e(e, "AutoJs6 OCR识别失败")
            
            // 降级到MLKit
            try {
                val bitmap = runtime.images.captureScreen()?.bitmap
                if (bitmap != null) {
                    val image = InputImage.fromBitmap(bitmap, 0)
                    val result = textRecognizer.process(image).await()
                    result.textBlocks.flatMap { block ->
                        block.lines.map { line -> line.text }
                    }
                } else {
                    emptyList()
                }
            } catch (e2: Exception) {
                Timber.e(e2, "MLKit OCR也失败")
                emptyList()
            }
        }
    }

    /**
     * 分析UI元素（使用AutoJs6的accessibility）
     */
    private fun analyzeUIElementsWithAutoJs(runtime: ScriptRuntime): List<UIElement> {
        val elements = mutableListOf<UIElement>()
        
        try {
            // 获取AutoJs6的accessibility bridge
            val accessibilityBridge = runtime.accessibilityBridge
            
            // 获取当前窗口的根节点
            val roots = accessibilityBridge.windowRoots()
            
            // 遍历所有窗口
            roots.forEach { windowRoot ->
                windowRoot.root?.let { rootNode ->
                    traverseUiObject(rootNode, elements)
                }
            }
            
            Timber.i("通过AutoJs6分析到 ${elements.size} 个UI元素")
            
        } catch (e: Exception) {
            Timber.e(e, "AutoJs6 UI元素分析失败")
        }
        
        return elements
    }

    /**
     * 遍历UiObject节点树
     */
    private fun traverseUiObject(node: UiObject, elements: MutableList<UIElement>) {
        try {
            val bounds = node.bounds()
            val text = node.text() ?: ""
            val desc = node.contentDescription() ?: ""
            
            // 只添加有意义的元素
            if (text.isNotEmpty() || desc.isNotEmpty() || 
                node.isClickable() || node.isScrollable()) {
                
                elements.add(UIElement(
                    type = node.className() ?: "Unknown",
                    text = text,
                    bounds = Bounds(
                        left = bounds.left,
                        top = bounds.top,
                        right = bounds.right,
                        bottom = bounds.bottom
                    ),
                    description = buildElementDescription(node, text, desc),
                    isClickable = node.isClickable(),
                    isScrollable = node.isScrollable()
                ))
            }
            
            // 递归遍历子节点
            for (i in 0 until node.childCount()) {
                node.child(i)?.let { child ->
                    traverseUiObject(child, elements)
                }
            }
            
        } catch (e: Exception) {
            Timber.w(e, "遍历节点失败")
        }
    }

    /**
     * 构建元素描述
     */
    private fun buildElementDescription(node: UiObject, text: String, desc: String): String {
        return buildString {
            append(node.className()?.split(".")?.lastOrNull() ?: "Unknown")
            
            if (text.isNotEmpty()) {
                append(" \"$text\"")
            } else if (desc.isNotEmpty()) {
                append(" \"$desc\"")
            }
            
            if (node.isClickable()) {
                append(" (可点击)")
            }
            if (node.isScrollable()) {
                append(" (可滚动)")
            }
        }
    }

    /**
     * 查找匹配的UI元素
     */
    fun findElement(text: String, elements: List<UIElement>): UIElement? {
        return elements.find { 
            it.text?.contains(text, ignoreCase = true) == true 
        }
    }

    /**
     * 将Bitmap转换为ByteArray
     */
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}
