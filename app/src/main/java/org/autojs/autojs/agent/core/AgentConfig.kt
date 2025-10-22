package org.autojs.autojs.agent.core

import android.content.Context
import android.content.SharedPreferences
import org.autojs.autojs.agent.core.model.LLMConfig
import org.autojs.autojs.agent.core.model.AIModelConfig

/**
 * Agent配置类 - 管理Agent的配置信息
 * Created by SuperMonster003 on 2024/01/15
 */
class AgentConfig(private val context: Context) {

    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("agent_config", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SCRIPT_GENERATION_ENABLED = "script_generation_enabled"
        private const val KEY_EXECUTION_MONITOR_ENABLED = "execution_monitor_enabled"
        private const val KEY_BEHAVIOR_LEARNING_ENABLED = "behavior_learning_enabled"
        private const val KEY_UI_ADAPTATION_ENABLED = "ui_adaptation_enabled"
        private const val KEY_DIALOG_AGENT_ENABLED = "dialog_agent_enabled"
        
        private const val KEY_LLM_MODEL_TYPE = "llm_model_type"
        private const val KEY_LLM_MODEL_PATH = "llm_model_path"
        private const val KEY_LLM_MAX_TOKENS = "llm_max_tokens"
        private const val KEY_LLM_TEMPERATURE = "llm_temperature"
        
        private const val KEY_AI_MODEL_TYPE = "ai_model_type"
        private const val KEY_AI_MODEL_PATH = "ai_model_path"
        private const val KEY_AI_CONFIDENCE_THRESHOLD = "ai_confidence_threshold"
        
        private const val KEY_LEARNING_RATE = "learning_rate"
        private const val KEY_BATCH_SIZE = "batch_size"
        private const val KEY_RETRY_COUNT = "retry_count"
        private const val KEY_TIMEOUT_SECONDS = "timeout_seconds"
    }

    // Agent 启用状态
    var isScriptGenerationEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_SCRIPT_GENERATION_ENABLED, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_SCRIPT_GENERATION_ENABLED, value).apply()

    var isExecutionMonitorEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_EXECUTION_MONITOR_ENABLED, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_EXECUTION_MONITOR_ENABLED, value).apply()

    var isBehaviorLearningEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_BEHAVIOR_LEARNING_ENABLED, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_BEHAVIOR_LEARNING_ENABLED, value).apply()

    var isUIAdaptationEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_UI_ADAPTATION_ENABLED, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_UI_ADAPTATION_ENABLED, value).apply()

    var isDialogAgentEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_DIALOG_AGENT_ENABLED, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_DIALOG_AGENT_ENABLED, value).apply()

    // LLM 配置
    var llmConfig: LLMConfig
        get() = LLMConfig(
            modelType = sharedPreferences.getString(KEY_LLM_MODEL_TYPE, "gemma-2b") ?: "gemma-2b",
            modelPath = sharedPreferences.getString(KEY_LLM_MODEL_PATH, "") ?: "",
            maxTokens = sharedPreferences.getInt(KEY_LLM_MAX_TOKENS, 512),
            temperature = sharedPreferences.getFloat(KEY_LLM_TEMPERATURE, 0.7f)
        )
        set(value) = sharedPreferences.edit().apply {
            putString(KEY_LLM_MODEL_TYPE, value.modelType)
            putString(KEY_LLM_MODEL_PATH, value.modelPath)
            putInt(KEY_LLM_MAX_TOKENS, value.maxTokens)
            putFloat(KEY_LLM_TEMPERATURE, value.temperature)
        }.apply()

    // AI 模型配置
    var aiModelConfig: AIModelConfig
        get() = AIModelConfig(
            modelType = sharedPreferences.getString(KEY_AI_MODEL_TYPE, "yolo-v8") ?: "yolo-v8",
            modelPath = sharedPreferences.getString(KEY_AI_MODEL_PATH, "") ?: "",
            confidenceThreshold = sharedPreferences.getFloat(KEY_AI_CONFIDENCE_THRESHOLD, 0.5f)
        )
        set(value) = sharedPreferences.edit().apply {
            putString(KEY_AI_MODEL_TYPE, value.modelType)
            putString(KEY_AI_MODEL_PATH, value.modelPath)
            putFloat(KEY_AI_CONFIDENCE_THRESHOLD, value.confidenceThreshold)
        }.apply()

    // 通用配置
    var learningRate: Float
        get() = sharedPreferences.getFloat(KEY_LEARNING_RATE, 0.001f)
        set(value) = sharedPreferences.edit().putFloat(KEY_LEARNING_RATE, value).apply()

    var batchSize: Int
        get() = sharedPreferences.getInt(KEY_BATCH_SIZE, 32)
        set(value) = sharedPreferences.edit().putInt(KEY_BATCH_SIZE, value).apply()

    var retryCount: Int
        get() = sharedPreferences.getInt(KEY_RETRY_COUNT, 3)
        set(value) = sharedPreferences.edit().putInt(KEY_RETRY_COUNT, value).apply()

    var timeoutSeconds: Int
        get() = sharedPreferences.getInt(KEY_TIMEOUT_SECONDS, 30)
        set(value) = sharedPreferences.edit().putInt(KEY_TIMEOUT_SECONDS, value).apply()

    /**
     * 重置为默认配置
     */
    fun resetToDefault() {
        sharedPreferences.edit().clear().apply()
    }

    /**
     * 导出配置
     */
    fun exportConfig(): Map<String, Any> {
        return mapOf(
            KEY_SCRIPT_GENERATION_ENABLED to isScriptGenerationEnabled,
            KEY_EXECUTION_MONITOR_ENABLED to isExecutionMonitorEnabled,
            KEY_BEHAVIOR_LEARNING_ENABLED to isBehaviorLearningEnabled,
            KEY_UI_ADAPTATION_ENABLED to isUIAdaptationEnabled,
            KEY_DIALOG_AGENT_ENABLED to isDialogAgentEnabled,
            "llm_config" to llmConfig,
            "ai_model_config" to aiModelConfig,
            KEY_LEARNING_RATE to learningRate,
            KEY_BATCH_SIZE to batchSize,
            KEY_RETRY_COUNT to retryCount,
            KEY_TIMEOUT_SECONDS to timeoutSeconds
        )
    }
}