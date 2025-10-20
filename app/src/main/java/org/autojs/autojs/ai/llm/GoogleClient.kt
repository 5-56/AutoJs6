package org.autojs.autojs.ai.llm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * Minimal Google Generative Language (Gemini) client for chat-like usage.
 * Endpoint: POST {base}/v1beta/models/{model}:generateContent?key=API_KEY
 */
class GoogleClient(
    private val apiKey: String,
    private val model: String,
    private val baseUrl: String = "https://generativelanguage.googleapis.com"
) : LlmClient {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val client = OkHttpClient()

    override suspend fun chat(messages: List<LlmClient.Message>): List<LlmClient.Message> = withContext(scope.coroutineContext) {
        val url = "$baseUrl/v1beta/models/${'$'}model:generateContent?key=${'$'}apiKey"
        val contents = JSONArray()
        messages.forEach { m ->
            val role = when (m.role) {
                LlmClient.Message.Role.User -> "user"
                LlmClient.Message.Role.Assistant -> "model"
                LlmClient.Message.Role.Tool -> "tool"
                LlmClient.Message.Role.System -> "user" // fold into user prompt
            }
            contents.put(JSONObject().apply {
                put("role", role)
                put("parts", JSONArray().put(JSONObject().put("text", m.content)))
            })
        }
        val bodyJson = JSONObject().apply {
            put("contents", contents)
        }.toString()
        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create(MediaType.parse("application/json"), bodyJson))
            .build()
        val resp = client.newCall(request).execute()
        if (!resp.isSuccessful) return@withContext listOf(LlmClient.Message(LlmClient.Message.Role.Assistant, ""))
        val text = resp.body()?.string().orEmpty()
        val obj = JSONObject(text)
        val content = obj.optJSONArray("candidates")
            ?.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?.optJSONObject(0)
            ?.optString("text")
            .orEmpty()
        listOf(LlmClient.Message(LlmClient.Message.Role.Assistant, content))
    }
}
