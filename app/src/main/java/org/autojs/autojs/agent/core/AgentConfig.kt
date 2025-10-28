package org.autojs.autojs.agent.core

import android.content.Context
import android.content.SharedPreferences

/**
 * Agent配置类
 * Created by SuperMonster003 on 2024/01/15
 */
class AgentConfig(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("agent_config", Context.MODE_PRIVATE)

    var isScriptGenerationEnabled: Boolean
        get() = prefs.getBoolean("script_generation_enabled", true)
        set(value) = prefs.edit().putBoolean("script_generation_enabled", value).apply()

    var isExecutionMonitorEnabled: Boolean
        get() = prefs.getBoolean("execution_monitor_enabled", true)
        set(value) = prefs.edit().putBoolean("execution_monitor_enabled", value).apply()

    var isBehaviorLearningEnabled: Boolean
        get() = prefs.getBoolean("behavior_learning_enabled", true)
        set(value) = prefs.edit().putBoolean("behavior_learning_enabled", value).apply()

    var isUIAdaptationEnabled: Boolean
        get() = prefs.getBoolean("ui_adaptation_enabled", true)
        set(value) = prefs.edit().putBoolean("ui_adaptation_enabled", value).apply()

    var isDialogAgentEnabled: Boolean
        get() = prefs.getBoolean("dialog_agent_enabled", true)
        set(value) = prefs.edit().putBoolean("dialog_agent_enabled", value).apply()

    fun resetToDefault() {
        prefs.edit().clear().apply()
    }
}