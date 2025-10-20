package org.autojs.autojs.ai.orchestrator

import android.graphics.Bitmap
import org.autojs.autojs.execution.ExecutionConfig
import org.autojs.autojs.runtime.api.Engines
import org.autojs.autojs.runtime.ScriptRuntime

interface ToolBridge {
    suspend fun runScript(name: String, source: String, timeoutMs: Long = 30_000): RunReport
    suspend fun screenshot(): Bitmap?
    suspend fun ocr(bitmap: Bitmap): List<OcrBox>
    suspend fun accessibilityTree(): String
}

data class OcrBox(val text: String, val conf: Float, val left: Int, val top: Int, val right: Int, val bottom: Int)

data class RunReport(val success: Boolean, val logs: String?, val error: String?)

class ScriptRunner(private val runtime: ScriptRuntime) {
    fun run(name: String, source: String, config: ExecutionConfig): org.autojs.autojs.execution.ScriptExecution {
        val engines = Engines(runtime)
        return engines.execScript(name, source, config)
    }
}
