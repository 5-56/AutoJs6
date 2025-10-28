package org.autojs.autojs.ai.llm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject

class ZhipuClient(
    private val apiKey: String,
    private val model: String,
    private val baseUrl: String = "https://open.bigmodel.cn/api"
) : LlmClient {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val client = OkHttpClient()

    override suspend fun chat(messages: List[LlmClient.Message]): List[LlmClient.Message] = withContext(scope.coroutineContext) {
        val url = "$baseUrl/paas/v4/chat/completions"
        val reqBody = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray(messages.map { m ->
                JSONObject().put("role", m.role.name.lowercase()).put("content", m.content)
            }))
        }.toString()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", apiKey)
            .post(RequestBody.create(MediaType.parse("application/json"), reqBody))
            .build()
        val resp = client.newCall(request).execute()
        if (!resp.isSuccessful) return@withContext listOf(LlmClient.Message(LlmClient.Message.Role.Assistant, ""))
        val body = resp.body()?.string().orEmpty()
        val json = JSONObject(body)
        val content = json.optJSONArray("choices")?.optJSONObject(0)?.optJSONObject("message")?.optString("content").orEmpty()
        listOf(LlmClient.Message(LlmClient.Message.Role.Assistant, content))
    }
}
