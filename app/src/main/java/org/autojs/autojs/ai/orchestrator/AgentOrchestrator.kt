package org.autojs.autojs.ai.orchestrator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.autojs.autojs.ai.llm.LlmClient

class AgentOrchestrator(private val llm: LlmClient) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun sendUserQuery(text: String, onReply: (String) -> Unit) {
        scope.launch {
            val reply = llm.chat(listOf(LlmClient.Message(LlmClient.Message.Role.User, text)))
                .joinToString("\n") { it.content }
            onReply(reply)
        }
    }
}
