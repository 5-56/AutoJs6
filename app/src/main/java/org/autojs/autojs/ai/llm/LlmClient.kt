package org.autojs.autojs.ai.llm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface LlmClient {
    suspend fun chat(messages: List<Message>): List<Message>

    data class Message(val role: Role, val content: String) {
        enum class Role { System, User, Assistant, Tool }
    }
}

class NoopLlmClient : LlmClient {
    private val scope = CoroutineScope(Dispatchers.IO)
    override suspend fun chat(messages: List<LlmClient.Message>): List<LlmClient.Message> = withContext(scope.coroutineContext) {
        // Echo for MVP
        val last = messages.lastOrNull { it.role == LlmClient.Message.Role.User }
        listOf(LlmClient.Message(LlmClient.Message.Role.Assistant, last?.content ?: ""))
    }
}
