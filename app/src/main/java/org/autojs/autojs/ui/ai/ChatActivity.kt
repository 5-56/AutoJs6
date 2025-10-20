package org.autojs.autojs.ui.ai

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.autojs.autojs.R
import org.autojs.autojs.ai.llm.NoopLlmClient
import org.autojs.autojs.ai.llm.DeepSeekClient
import org.autojs.autojs.ai.llm.OpenAiClient
import org.autojs.autojs.ai.llm.Providers
import org.autojs.autojs.ai.orchestrator.AgentOrchestrator
import org.autojs.autojs.ai.orchestrator.DefaultToolBridge
import org.autojs.autojs.runtime.ScriptRuntime
import com.google.android.material.appbar.MaterialToolbar
import android.view.MenuItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    private val adapter: ChatAdapter by lazy { ChatAdapter(mutableListOf()) }
    private val orchestrator by lazy {
        // Attach runtime & tools when available
        val runtime = try { org.autojs.autojs.AutoJs.instance.scriptRuntime } catch (_: Throwable) { null }
        val tools = runtime?.let { DefaultToolBridge(it) }
        val cfg = Providers.read(this)
        val llm = when (cfg.provider) {
            "deepseek" -> if (!cfg.apiKey.isNullOrBlank() && !cfg.model.isNullOrBlank()) DeepSeekClient(cfg.apiKey, cfg.model, cfg.baseUrl ?: "https://api.deepseek.com") else NoopLlmClient()
            "openrouter" -> if (!cfg.apiKey.isNullOrBlank() && !cfg.model.isNullOrBlank()) org.autojs.autojs.ai.llm.OpenRouterClient(cfg.apiKey, cfg.model, cfg.baseUrl ?: "https://openrouter.ai/api") else NoopLlmClient()
            "kimi" -> if (!cfg.apiKey.isNullOrBlank() && !cfg.model.isNullOrBlank()) org.autojs.autojs.ai.llm.KimiClient(cfg.apiKey, cfg.model, cfg.baseUrl ?: "https://api.moonshot.cn") else NoopLlmClient()
            "zhipu" -> if (!cfg.apiKey.isNullOrBlank() && !cfg.model.isNullOrBlank()) org.autojs.autojs.ai.llm.ZhipuClient(cfg.apiKey, cfg.model, cfg.baseUrl ?: "https://open.bigmodel.cn/api") else NoopLlmClient()
            "google" -> if (!cfg.apiKey.isNullOrBlank() && !cfg.model.isNullOrBlank()) org.autojs.autojs.ai.llm.GoogleClient(cfg.apiKey, cfg.model, cfg.baseUrl ?: "https://generativelanguage.googleapis.com") else NoopLlmClient()
            else -> NoopLlmClient()
        }
        AgentOrchestrator(llm, runtime, tools)
    }

    private fun toolsTap(x: Int, y: Int) {
        val runtime = try { org.autojs.autojs.AutoJs.instance.scriptRuntime } catch (_: Throwable) { null } ?: return
        val tools = DefaultToolBridge(runtime)
        CoroutineScope(Dispatchers.IO).launch { runCatching { tools.tap(x, y) } }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        // Toolbar actions
        runCatching {
            val tbId = resources.getIdentifier("toolbar", "id", packageName)
            val toolbar = findViewById<MaterialToolbar>(tbId)
            toolbar.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    resources.getIdentifier("action_run_loop", "id", packageName) -> {
                        val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(rvId)
                        val goal = findViewById<EditText>(resources.getIdentifier("inputEdit", "id", packageName)).text?.toString().orEmpty().trim()
                        if (goal.isNotEmpty()) {
                            adapter.add(ChatAdapter.ChatMessage(true, "[AI 闭环] $goal"))
                            orchestrator.runClosedLoopScript(
                                initialScript = "",
                                goal = goal,
                                maxIters = 3
                            ) { update ->
                                runOnUiThread {
                                    adapter.add(ChatAdapter.ChatMessage(false, update))
                                    rv.scrollToPosition(adapter.itemCount - 1)
                                }
                            }
                        }
                        true
                    }
                    resources.getIdentifier("action_settings", "id", packageName) -> {
                        startActivity(android.content.Intent(this, SettingsActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
        }

        val rvId = resources.getIdentifier("recyclerView", "id", packageName)
        val inputId = resources.getIdentifier("inputEdit", "id", packageName)
        val sendId = resources.getIdentifier("sendButton", "id", packageName)

        val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(rvId)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        val input = findViewById<EditText>(inputId)
        val send = findViewById<TextView>(sendId)
        send.setOnClickListener {
            val text = input.text?.toString().orEmpty().trim()
            if (text.isNotEmpty()) {
                input.setText("")
                adapter.add(ChatAdapter.ChatMessage(true, text))
                orchestrator.sendUserQuery(text) { reply ->
                    runOnUiThread {
                        adapter.add(ChatAdapter.ChatMessage(false, reply))
                        rv.scrollToPosition(adapter.itemCount - 1)
                    }
                }
            }
        }

        // Long click to run closed-loop with given goal (3 iters)
        send.setOnLongClickListener {
            val goal = input.text?.toString().orEmpty().trim()
            if (goal.isNotEmpty()) {
                input.setText("")
                adapter.add(ChatAdapter.ChatMessage(true, "[AI 闭环] $goal"))
                orchestrator.runClosedLoopScript(
                    initialScript = "",
                    goal = goal,
                    maxIters = 3,
                    onUpdate = { update ->
                        runOnUiThread {
                            adapter.add(ChatAdapter.ChatMessage(false, update))
                            rv.scrollToPosition(adapter.itemCount - 1)
                        }
                    },
                    onCard = { payload ->
                        runOnUiThread {
                            if (payload.ocr.isNotEmpty()) {
                                val lines = payload.ocr.take(10).map { b -> "${b.text} (${String.format("%.2f", b.conf)}) @ [${b.left},${b.top}]" }
                                adapter.add(
                                    ChatAdapter.ChatMessage(
                                        isUser = false,
                                        text = "OCR 识别结果",
                                        card = ChatAdapter.ChatMessage.Card(
                                            title = "可点击执行动作（示例：点击第一个文本框）",
                                            lines = lines,
                                            onClickActions = listOf({
                                                payload.ocr.firstOrNull()?.let { box ->
                                                    val cx = (box.left + box.right) / 2
                                                    val cy = (box.top + box.bottom) / 2
                                                    runCatching { toolsTap(cx, cy) }
                                                }
                                            })
                                        )
                                    )
                                )
                            }
                            if (payload.a11y.isNotEmpty()) {
                                val lines = payload.a11y.take(10).map { n -> "${n.clazz ?: "?"} | ${n.text ?: ""} | ${n.content ?: ""}" }
                                adapter.add(
                                    ChatAdapter.ChatMessage(
                                        isUser = false,
                                        text = "无障碍节点摘要",
                                        card = ChatAdapter.ChatMessage.Card(
                                            title = "可点击执行动作（示例：点击第一个节点中点）",
                                            lines = lines,
                                            onClickActions = listOf({
                                                payload.a11y.firstOrNull()?.let { node ->
                                                    val cx = (node.left + node.right) / 2
                                                    val cy = (node.top + node.bottom) / 2
                                                    runCatching { toolsTap(cx, cy) }
                                                }
                                            })
                                        )
                                    )
                                )
                            }
                            rv.scrollToPosition(adapter.itemCount - 1)
                        }
                    }
                )
            }
            true
        }
    }
}
