package org.autojs.autojs.ai.llm

import android.content.Context
import androidx.preference.PreferenceManager

object Providers {
    data class Config(
        val provider: String,
        val apiKey: String?,
        val baseUrl: String?,
        val model: String?
    )

    fun read(context: Context): Config {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val provider = sp.getString("xihe_llm_provider", "openrouter") ?: "openrouter"
        val apiKey = when (provider) {
            "google" -> sp.getString("xihe_llm_api_key_google", null)
            "deepseek" -> sp.getString("xihe_llm_api_key_deepseek", null)
            "kimi" -> sp.getString("xihe_llm_api_key_kimi", null)
            "openrouter" -> sp.getString("xihe_llm_api_key_openrouter", null)
            "zhipu" -> sp.getString("xihe_llm_api_key_zhipu", null)
            else -> null
        }
        val baseUrl = when (provider) {
            "google" -> "https://generativelanguage.googleapis.com"
            "deepseek" -> "https://api.deepseek.com"
            "kimi" -> "https://api.moonshot.cn"
            "openrouter" -> "https://openrouter.ai/api"
            "zhipu" -> "https://open.bigmodel.cn/api"
            else -> null
        }
        val model = sp.getString("xihe_llm_model", null)
        return Config(provider, apiKey, baseUrl, model)
    }
}
