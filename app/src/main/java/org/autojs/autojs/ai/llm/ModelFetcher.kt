package org.autojs.autojs.ai.llm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class ModelFetcher {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val client = OkHttpClient()

    suspend fun fetch(provider: String, baseUrl: String, apiKey: String): List<String> = withContext(scope.coroutineContext) {
        try {
            when (provider) {
                "google" -> fetchGoogle(baseUrl, apiKey)
                "deepseek" -> fetchOpenAIStyle("$baseUrl/v1/models", apiKey, headerName = "Authorization", bearer = true)
                "kimi" -> fetchOpenAIStyle("$baseUrl/v1/models", apiKey, headerName = "Authorization", bearer = true)
                "openrouter" -> fetchOpenAIStyle("$baseUrl/v1/models", apiKey, headerName = "Authorization", bearer = true)
                "zhipu" -> fetchOpenAIStyle("$baseUrl/paas/v4/models", apiKey, headerName = "Authorization", bearer = false)
                else -> emptyList()
            }
        } catch (_: Throwable) { emptyList() }
    }

    private fun fetchGoogle(baseUrl: String, apiKey: String): List<String> {
        val url = "$baseUrl/v1beta/models?key=$apiKey"
        val req = Request.Builder().url(url).get().build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return emptyList()
            val text = resp.body()?.string().orEmpty()
            val json = JSONObject(text)
            val arr = json.optJSONArray("models") ?: return emptyList()
            return List(arr.length()) { i -> arr.optJSONObject(i)?.optString("name") ?: "" }.filter { it.isNotBlank() }
        }
    }

    private fun fetchOpenAIStyle(url: String, apiKey: String, headerName: String, bearer: Boolean): List<String> {
        val req = Request.Builder()
            .url(url)
            .addHeader(headerName, if (bearer) "Bearer $apiKey" else apiKey)
            .get()
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return emptyList()
            val text = resp.body()?.string().orEmpty()
            val json = JSONObject(text)
            val data = json.optJSONArray("data") ?: return emptyList()
            return List(data.length()) { i -> data.optJSONObject(i)?.optString("id") ?: "" }.filter { it.isNotBlank() }
        }
    }
}
