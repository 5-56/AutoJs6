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
                    LlmClient.Message(LlmClient.Message.Role.System, "你是脚本助手。根据观察数据，提供最小必要修改的脚本全文。"),
                    LlmClient.Message(LlmClient.Message.Role.User, "目标: $goal\n观察: $obs\n当前脚本:\n$script"),
                )
                val ai = llm.chat(messages).joinToString("\n") { it.content }
                onUpdate(ai)
                script = ai.ifBlank { script }
            }
        }
    }
}
