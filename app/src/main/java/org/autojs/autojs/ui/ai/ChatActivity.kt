package org.autojs.autojs.ui.ai

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.autojs.autojs.R
import org.autojs.autojs.ai.llm.NoopLlmClient
import org.autojs.autojs.ai.orchestrator.AgentOrchestrator

class ChatActivity : AppCompatActivity() {
    private val adapter: ChatAdapter by lazy { ChatAdapter(mutableListOf()) }
    private val orchestrator by lazy { AgentOrchestrator(NoopLlmClient()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

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
    }
}
