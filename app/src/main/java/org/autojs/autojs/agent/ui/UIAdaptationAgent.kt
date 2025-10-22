package org.autojs.autojs.agent.ui

import android.content.Context
import android.util.Log
import kotlinx.coroutines.launch
import org.autojs.autojs.agent.core.AgentBase
import org.autojs.autojs.agent.core.AgentConfig
import org.autojs.autojs.agent.core.model.AgentEvent
import org.autojs.autojs.agent.core.model.AgentMessage
import org.autojs.autojs.agent.core.model.MessageType
import org.autojs.autojs.agent.core.model.UIElementInfo
import org.autojs.autojs.agent.ui.detection.UIChangeDetector
import org.autojs.autojs.agent.ui.locator.SmartElementLocator
import org.autojs.autojs.agent.ui.repair.ScriptRepairEngine
import org.autojs.autojs.agent.ui.understanding.LayoutAnalyzer
import org.autojs.autojs.agent.ui.model.UISnapshot
import org.autojs.autojs.agent.ui.model.UIChangeEvent
import org.autojs.autojs.agent.ui.model.ElementMapping
import org.autojs.autojs.agent.ui.model.RepairSuggestion
import java.util.concurrent.ConcurrentHashMap

/**
 * 智能UI适配Agent
 * 核心功能：
 * 1. UI变化检测
 * 2. 智能元素定位
 * 3. 布局理解
 * 4. 自动脚本修复
 * 
 * Created by SuperMonster003 on 2024/01/15
 */
class UIAdaptationAgent(
    context: Context,
    config: AgentConfig
) : AgentBase(context, config) {

    companion object {
        const val AGENT_ID = "ui_adaptation_agent"
        private const val TAG = "UIAdaptationAgent"
    }

    override val agentId: String = AGENT_ID
    override val agentName: String = "智能UI适配Agent"
    override val agentDescription: String = "检测UI变化，智能定位元素，自动修复脚本"

    private lateinit var changeDetector: UIChangeDetector
    private lateinit var elementLocator: SmartElementLocator
    private lateinit var repairEngine: ScriptRepairEngine
    private lateinit var layoutAnalyzer: LayoutAnalyzer

    private val uiSnapshots = ConcurrentHashMap<String, UISnapshot>()
    private val elementMappings = ConcurrentHashMap<String, ElementMapping>()

    override suspend fun onInitialize() {
        try {
            changeDetector = UIChangeDetector(context, config)
            elementLocator = SmartElementLocator(context, config)
            repairEngine = ScriptRepairEngine(context, config)
            layoutAnalyzer = LayoutAnalyzer(context, config)

            // 初始化AI模型
            changeDetector.initialize()
            elementLocator.initialize()
            layoutAnalyzer.initialize()

            Log.i(TAG, "UIAdaptationAgent initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize UIAdaptationAgent", e)
            onError(e)
        }
    }

    override suspend fun onStart() {
        agentScope.launch {
            startUIMonitoring()
        }
        Log.i(TAG, "UIAdaptationAgent started")
    }

    override suspend fun onStop() {
        changeDetector.stop()
        Log.i(TAG, "UIAdaptationAgent stopped")
    }

    override suspend fun onDestroy() {
        uiSnapshots.clear()
        elementMappings.clear()
        changeDetector.release()
        elementLocator.release()
        repairEngine.release()
        layoutAnalyzer.release()
        Log.i(TAG, "UIAdaptationAgent destroyed")
    }

    override suspend fun processMessage(message: AgentMessage) {
        when (message.type) {
            MessageType.COMMAND -> handleCommand(message)
            MessageType.QUERY -> handleQuery(message)
            MessageType.NOTIFICATION -> handleNotification(message)
            else -> Log.w(TAG, "Unsupported message type: ${message.type}")
        }
    }

    override fun onError(error: Exception) {
        Log.e(TAG, "UIAdaptationAgent error", error)
        emitEvent(AgentEvent.ERROR, error.message)
    }

    private suspend fun handleCommand(message: AgentMessage) {
        when (message.content) {
            "detect_ui_changes" -> {
                message.data?.let { data ->
                    val packageName = data["packageName"] as? String ?: return
                    val changes = detectUIChanges(packageName)
                    emitEvent(AgentEvent.TASK_COMPLETED, changes)
                }
            }
            "locate_element" -> {
                message.data?.let { data ->
                    val elementInfo = parseElementInfo(data)
                    val location = locateElement(elementInfo)
                    emitEvent(AgentEvent.TASK_COMPLETED, location)
                }
            }
            "repair_script" -> {
                message.data?.let { data ->
                    val scriptPath = data["scriptPath"] as? String ?: return
                    val errorInfo = data["errorInfo"] as? String ?: return
                    val suggestions = repairScript(scriptPath, errorInfo)
                    emitEvent(AgentEvent.TASK_COMPLETED, suggestions)
                }
            }
            "analyze_layout" -> {
                message.data?.let { data ->
                    val packageName = data["packageName"] as? String ?: return
                    val analysis = analyzeLayout(packageName)
                    emitEvent(AgentEvent.TASK_COMPLETED, analysis)
                }
            }
            "update_element_mapping" -> {
                message.data?.let { data ->
                    val packageName = data["packageName"] as? String ?: return
                    val mapping = parseElementMapping(data)
                    updateElementMapping(packageName, mapping)
                }
            }
        }
    }

    private suspend fun handleQuery(message: AgentMessage) {
        when (message.content) {
            "get_ui_snapshot" -> {
                message.data?.let { data ->
                    val packageName = data["packageName"] as? String ?: return
                    val snapshot = getUISnapshot(packageName)
                    emitEvent(AgentEvent.TASK_COMPLETED, snapshot)
                }
            }
            "get_element_mappings" -> {
                message.data?.let { data ->
                    val packageName = data["packageName"] as? String ?: return
                    val mappings = getElementMappings(packageName)
                    emitEvent(AgentEvent.TASK_COMPLETED, mappings)
                }
            }
            "get_adaptation_stats" -> {
                val stats = getAdaptationStats()
                emitEvent(AgentEvent.TASK_COMPLETED, stats)
            }
        }
    }

    private suspend fun handleNotification(message: AgentMessage) {
        when (message.content) {
            "ui_changed" -> {
                message.data?.let { data ->
                    val changeEvent = parseUIChangeEvent(data)
                    handleUIChange(changeEvent)
                }
            }
            "script_failed" -> {
                message.data?.let { data ->
                    val scriptPath = data["scriptPath"] as? String ?: return
                    val errorInfo = data["errorInfo"] as? String ?: return
                    handleScriptFailure(scriptPath, errorInfo)
                }
            }
        }
    }

    /**
     * 启动UI监控
     */
    private suspend fun startUIMonitoring() {
        changeDetector.startMonitoring { changeEvent ->
            agentScope.launch {
                handleUIChange(changeEvent)
            }
        }
    }

    /**
     * 检测UI变化
     */
    private suspend fun detectUIChanges(packageName: String): List<UIChangeEvent> {
        val previousSnapshot = uiSnapshots[packageName]
        val currentSnapshot = captureUISnapshot(packageName)
        
        val changes = if (previousSnapshot != null) {
            changeDetector.detectChanges(previousSnapshot, currentSnapshot)
        } else {
            emptyList()
        }
        
        uiSnapshots[packageName] = currentSnapshot
        
        Log.i(TAG, "Detected ${changes.size} UI changes for $packageName")
        return changes
    }

    /**
     * 定位元素
     */
    private suspend fun locateElement(elementInfo: UIElementInfo): UIElementInfo? {
        return elementLocator.locateElement(elementInfo)
    }

    /**
     * 修复脚本
     */
    private suspend fun repairScript(scriptPath: String, errorInfo: String): List<RepairSuggestion> {
        return repairEngine.analyzeAndRepair(scriptPath, errorInfo)
    }

    /**
     * 分析布局
     */
    private suspend fun analyzeLayout(packageName: String): Map<String, Any> {
        val snapshot = captureUISnapshot(packageName)
        return layoutAnalyzer.analyzeLayout(snapshot)
    }

    /**
     * 更新元素映射
     */
    private suspend fun updateElementMapping(packageName: String, mapping: ElementMapping) {
        elementMappings[packageName] = mapping
        Log.i(TAG, "Updated element mapping for $packageName")
    }

    /**
     * 处理UI变化
     */
    private suspend fun handleUIChange(changeEvent: UIChangeEvent) {
        Log.i(TAG, "Handling UI change: ${changeEvent.type}")
        
        // 更新元素映射
        val packageName = changeEvent.packageName
        val currentMapping = elementMappings[packageName]
        
        if (currentMapping != null) {
            val updatedMapping = elementLocator.updateMapping(currentMapping, changeEvent)
            elementMappings[packageName] = updatedMapping
        }
        
        // 检查是否需要修复脚本
        val affectedScripts = findAffectedScripts(changeEvent)
        affectedScripts.forEach { scriptPath ->
            val suggestions = repairEngine.suggestRepairs(scriptPath, changeEvent)
            if (suggestions.isNotEmpty()) {
                emitEvent(AgentEvent.MESSAGE_PROCESSED, suggestions)
            }
        }
    }

    /**
     * 处理脚本失败
     */
    private suspend fun handleScriptFailure(scriptPath: String, errorInfo: String) {
        Log.w(TAG, "Script failed: $scriptPath, error: $errorInfo")
        
        // 分析失败原因
        val failureAnalysis = repairEngine.analyzeFailure(scriptPath, errorInfo)
        
        // 如果是UI相关的失败，尝试自动修复
        if (failureAnalysis.isUIRelated) {
            val suggestions = repairScript(scriptPath, errorInfo)
            if (suggestions.isNotEmpty()) {
                emitEvent(AgentEvent.MESSAGE_PROCESSED, suggestions)
            }
        }
    }

    /**
     * 捕获UI快照
     */
    private suspend fun captureUISnapshot(packageName: String): UISnapshot {
        return changeDetector.captureSnapshot(packageName)
    }

    /**
     * 查找受影响的脚本
     */
    private fun findAffectedScripts(changeEvent: UIChangeEvent): List<String> {
        // 这里需要实现查找使用了变化元素的脚本
        // 可以通过分析脚本文件或维护一个脚本-元素映射表
        return emptyList()
    }

    /**
     * 获取UI快照
     */
    private fun getUISnapshot(packageName: String): UISnapshot? {
        return uiSnapshots[packageName]
    }

    /**
     * 获取元素映射
     */
    private fun getElementMappings(packageName: String): ElementMapping? {
        return elementMappings[packageName]
    }

    /**
     * 获取适配统计信息
     */
    private fun getAdaptationStats(): Map<String, Any> {
        return mapOf(
            "monitoredApps" to uiSnapshots.size,
            "elementMappings" to elementMappings.size,
            "totalDetectedChanges" to changeDetector.getTotalChanges(),
            "successfulRepairs" to repairEngine.getSuccessfulRepairs()
        )
    }

    /**
     * 解析元素信息
     */
    private fun parseElementInfo(data: Map<String, Any>): UIElementInfo {
        return UIElementInfo(
            id = data["id"] as String,
            className = data["className"] as String,
            text = data["text"] as String,
            bounds = parseRect(data["bounds"] as Map<String, Any>),
            isClickable = data["isClickable"] as Boolean,
            isScrollable = data["isScrollable"] as Boolean,
            packageName = data["packageName"] as String
        )
    }

    /**
     * 解析矩形区域
     */
    private fun parseRect(data: Map<String, Any>): org.autojs.autojs.agent.core.model.Rect {
        return org.autojs.autojs.agent.core.model.Rect(
            left = data["left"] as Int,
            top = data["top"] as Int,
            right = data["right"] as Int,
            bottom = data["bottom"] as Int
        )
    }

    /**
     * 解析元素映射
     */
    private fun parseElementMapping(data: Map<String, Any>): ElementMapping {
        // 这里需要实现具体的解析逻辑
        return ElementMapping(
            packageName = data["packageName"] as String,
            mappings = data["mappings"] as Map<String, String>
        )
    }

    /**
     * 解析UI变化事件
     */
    private fun parseUIChangeEvent(data: Map<String, Any>): UIChangeEvent {
        return UIChangeEvent(
            type = data["type"] as String,
            packageName = data["packageName"] as String,
            elementId = data["elementId"] as? String,
            timestamp = data["timestamp"] as Long
        )
    }
}