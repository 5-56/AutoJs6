package org.autojs.autojs.ai.orchestrator

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.*
import org.autojs.autojs.ai.llm.LlmClient
import org.autojs.autojs.runtime.ScriptRuntime

class AgentOrchestrator(
    private val llm: LlmClient,
    private val runtime: ScriptRuntime? = null,
    private val tools: ToolBridge? = null,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    data class CardsPayload(
        val ocr: List<OcrBox> = emptyList(),
        val a11y: List<A11yNode> = emptyList(),
    )

    data class A11yNode(
        val clazz: String?,
        val text: String?,
        val content: String?,
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int,
    )

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
        onCard: ((CardsPayload) -> Unit)? = null,
    ) {
        scope.launch {
            var script = initialScript
            repeat(maxIters) { iter ->
                onUpdate("运行第 ${iter + 1} 轮…")
                val report = tools?.runScript(name = "xihe_${System.currentTimeMillis()}", source = script, timeoutMs = 60_000)
                val bmp = tools?.screenshot(null)
                val a11yText = tools?.accessibilityTree(4) ?: ""

                val ocrSummary = try {
                    val ocrBoxes = bmp?.let { tools?.ocr(it) } ?: emptyList()
                    if (ocrBoxes.isNotEmpty()) onCard?.invoke(CardsPayload(ocr = ocrBoxes))
                    ocrBoxes.take(20).joinToString("\n") { b ->
                        "${'$'}{b.text} @${'$'}{String.format(\"%.2f\", b.conf)} [${'$'}{b.left},${'$'}{b.top},${'$'}{b.right - b.left}x${'$'}{b.bottom - b.top}]"
                    }
                } catch (_: Throwable) { "" }

                runCatching {
                    val nodes = buildA11yList(maxDepth = 3).take(20)
                    if (nodes.isNotEmpty()) onCard?.invoke(CardsPayload(a11y = nodes))
                }

                val obs = buildString {
                    appendLine("报告: ${report}")
                    if (ocrSummary.isNotBlank()) appendLine("OCR:\n${ocrSummary}")
                    appendLine("无障碍树: ${a11yText.take(2000)}…")
                    appendLine("截图: ${if (bmp != null) \"captured\" else \"null\"}")
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

    private fun buildA11yList(maxDepth: Int): List<A11yNode> {
        val ctx = runtime?.appContext ?: return emptyList()
        val inspector = org.autojs.autojs.core.accessibility.LayoutInspector(ctx)
        val root = inspector.root ?: return emptyList()
        val list = mutableListOf<A11yNode>()
        fun walk(node: AccessibilityNodeInfo, depth: Int) {
            if (depth > maxDepth) return
            val r = android.graphics.Rect()
            node.getBoundsInScreen(r)
            list.add(
                A11yNode(
                    clazz = node.className?.toString(),
                    text = node.text?.toString(),
                    content = node.contentDescription?.toString(),
                    left = r.left, top = r.top, right = r.right, bottom = r.bottom,
                )
            )
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { walk(it, depth + 1) }
            }
        }
        walk(root, 0)
        return list
    }
}
