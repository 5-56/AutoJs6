package org.autojs.autojs.ai.orchestrator

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import org.autojs.autojs.execution.ExecutionConfig
import org.autojs.autojs.runtime.api.Engines
import org.autojs.autojs.runtime.ScriptRuntime
import org.autojs.autojs.runtime.api.augment.images.Images
import org.autojs.autojs.runtime.api.augment.ocr.OcrRapid
import org.autojs.autojs.core.accessibility.LayoutInspector
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicInteger

interface ToolBridge {
    suspend fun runScript(name: String, source: String, timeoutMs: Long = 30_000): RunReport
    suspend fun screenshot(region: Rect? = null): Bitmap?
    suspend fun ocr(bitmap: Bitmap): List<OcrBox>
    suspend fun accessibilityTree(maxDepth: Int = 6): String
    // Basic actions
    suspend fun tap(x: Int, y: Int): Boolean
    suspend fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, durationMs: Int = 300): Boolean
    suspend fun inputTextById(viewId: String, text: String): Boolean
}

data class OcrBox(val text: String, val conf: Float, val left: Int, val top: Int, val right: Int, val bottom: Int)

data class RunReport(val success: Boolean, val logs: String?, val error: String?)

class ScriptRunner(private val runtime: ScriptRuntime) {
    fun run(name: String, source: String, config: ExecutionConfig): org.autojs.autojs.execution.ScriptExecution {
        val engines = Engines(runtime)
        return engines.execScript(name, source, config)
    }
}

class DefaultToolBridge(private val runtime: ScriptRuntime) : ToolBridge {

    private val captureBadge = AtomicInteger(0)

    override suspend fun runScript(name: String, source: String, timeoutMs: Long): RunReport {
        return try {
            val exec = ScriptRunner(runtime).run(name, source, ExecutionConfig())
            val ok = exec.waitFor(timeoutMs)
            RunReport(success = ok, logs = null, error = if (ok) null else "timeout")
        } catch (e: Throwable) {
            RunReport(success = false, logs = null, error = e.message)
        }
    }

    override suspend fun screenshot(region: Rect?): Bitmap? {
        // Ensure permission and start capture once
        if (captureBadge.get() == 0) {
            Images.requestScreenCapture(runtime, emptyArray())
            captureBadge.incrementAndGet()
        }
        val image = Images.captureScreen(runtime, arrayOf()) as org.autojs.autojs.core.image.ImageWrapper
        val file = File(runtime.files.path("/cache/xihe/snap_${System.currentTimeMillis()}.png"))
        file.parentFile?.mkdirs()
        Images.save(runtime, arrayOf(image, file.absolutePath))
        val bmp = BitmapFactory.decodeFile(file.absolutePath)
        if (region == null) return bmp
        return try { Bitmap.createBitmap(bmp, region.left, region.top, region.width(), region.height()) } catch (_: Throwable) { bmp }
    }

    override suspend fun ocr(bitmap: Bitmap): List<OcrBox> {
        val result = OcrRapid.ocr(runtime, arrayOf(bitmap)) as org.autojs.autojs.runtime.api.OcrResult
        return result.texts.map { t ->
            OcrBox(t.text, t.confidence, t.bounds.left, t.bounds.top, t.bounds.right, t.bounds.bottom)
        }
    }

    override suspend fun accessibilityTree(maxDepth: Int): String {
        val inspector = LayoutInspector(runtime.appContext)
        val root = inspector.root ?: return ""
        return buildString { serializeNode(root, 0, maxDepth) }
    }

    private fun StringBuilder.serializeNode(node: android.view.accessibility.AccessibilityNodeInfo, depth: Int, maxDepth: Int) {
        if (depth > maxDepth) return
        append(" ".repeat(depth * 2))
        append("[${node.className}] text=${node.text} content=${node.contentDescription} bounds=${node.boundsInScreen}\n")
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { serializeNode(it, depth + 1, maxDepth) }
        }
    }

    override suspend fun tap(x: Int, y: Int): Boolean {
        return try {
            val bridge = org.autojs.autojs.core.accessibility.AccessibilityBridgeImpl(runtime.appContext)
            val automator = org.autojs.autojs.core.accessibility.SimpleActionAutomator(bridge, runtime)
            automator.click(x, y)
            true
        } catch (_: Throwable) { false }
    }

    override suspend fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, durationMs: Int): Boolean {
        return try {
            val bridge = org.autojs.autojs.core.accessibility.AccessibilityBridgeImpl(runtime.appContext)
            val automator = org.autojs.autojs.core.accessibility.SimpleActionAutomator(bridge, runtime)
            automator.swipe(x1, y1, x2, y2, durationMs)
            true
        } catch (_: Throwable) { false }
    }

    override suspend fun inputTextById(viewId: String, text: String): Boolean {
        return try {
            val bridge = org.autojs.autojs.core.accessibility.AccessibilityBridgeImpl(runtime.appContext)
            val automator = org.autojs.autojs.core.accessibility.SimpleActionAutomator(bridge, runtime)
            automator.setText(automator.id(viewId), text)
            true
        } catch (_: Throwable) { false }
    }
}
