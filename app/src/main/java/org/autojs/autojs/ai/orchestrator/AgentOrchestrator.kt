package org.autojs.autojs.ai.orchestrator

import android.graphics.Rect
import kotlinx.coroutines.*
import org.autojs.autojs.ai.llm.LlmClient
import org.autojs.autojs.runtime.ScriptRuntime

class AgentOrchestrator(
    private val llm: LlmClient,
    private val runtime: ScriptRuntime? = null,
    private val tools: ToolBridge? = null,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun sendUserQuery(text: String, onReply: (String) -> Unit) {
        scope.launch {
            val reply = llm.chat(listOf(LlmClient.Message(LlmClient.Message.Role.User, text)))
                .joinToString("\n") { it.content }
            onReply(reply)
        }
    }

    fun runClosedLoopScript(
        initialScript: String,
        goal: String,
        maxIters: Int = 5,
        onUpdate: (String) -> Unit,
    ) {
        scope.launch {
            var script = initialScript
            repeat(maxIters) { iter ->
                onUpdate("运行第 ${iter + 1} 轮…")
                val report = tools?.runScript(name = "xihe_${System.currentTimeMillis()}", source = script, timeoutMs = 60_000)
                val bmp = tools?.screenshot(Rect(0, 0, 0, 0))
                val a11y = tools?.accessibilityTree(4) ?: ""
                val obs = buildString {
                    appendLine("报告: ${report}")
                    appendLine("无障碍树: ${a11y.take(2000)}…")
                    appendLine("截图: ${if (bmp != null) "captured" else "null"}")
                }
                val messages = listOf(
                    LlmClient.Message(LlmClient.Message.Role.System, "你是Android自动化脚本助手。只能使用项目内已存在API与能力，产出最小必要修改的完整JS脚本。"),
                    LlmClient.Message(LlmClient.Message.Role.User, "目标: $goal\n观察(日志/OCR/无障碍/截图情况摘要):\n$obs\n\n当前脚本全文:\n$script\n\n请仅返回新脚本全文，不要解释。"),
                )
                val ai = llm.chat(messages).joinToString("\n") { it.content }
                onUpdate(ai)
                script = ai.ifBlank { script }
            }
        }
    }
}
