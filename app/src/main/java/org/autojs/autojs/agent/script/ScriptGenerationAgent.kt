package org.autojs.autojs.agent.script

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.launch
import org.autojs.autojs.agent.core.AgentBase
import org.autojs.autojs.agent.core.AgentConfig
import org.autojs.autojs.agent.core.model.AgentEvent
import org.autojs.autojs.agent.core.model.AgentMessage
import org.autojs.autojs.agent.core.model.MessageType
import org.autojs.autojs.agent.core.model.ScriptGenerationRequest
import org.autojs.autojs.agent.core.model.ScriptGenerationResponse
import org.autojs.autojs.agent.script.llm.LLMProcessor
import org.autojs.autojs.agent.script.vision.ScreenshotAnalyzer
import org.autojs.autojs.agent.script.template.ScriptTemplateManager
import org.autojs.autojs.agent.script.optimizer.ScriptOptimizer
import org.autojs.autojs.agent.script.nlp.NLPProcessor
import org.autojs.autojs.agent.script.model.ScriptTemplate
import org.autojs.autojs.agent.script.model.GenerationContext
import org.autojs.autojs.agent.script.model.OptimizationSuggestion
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * 智能脚本生成Agent
 * 核心功能：
 * 1. 自然语言转脚本
 * 2. 屏幕截图辅助生成
 * 3. 脚本智能优化
 * 4. 模板推荐系统
 * 
 * Created by SuperMonster003 on 2024/01/15
 */
class ScriptGenerationAgent(
    context: Context,
    config: AgentConfig
) : AgentBase(context, config) {

    companion object {
        const val AGENT_ID = "script_generation_agent"
        private const val TAG = "ScriptGenerationAgent"
    }

    override val agentId: String = AGENT_ID
    override val agentName: String = "智能脚本生成Agent"
    override val agentDescription: String = "基于自然语言和屏幕截图生成JavaScript自动化脚本"

    private lateinit var llmProcessor: LLMProcessor
    private lateinit var screenshotAnalyzer: ScreenshotAnalyzer
    private lateinit var templateManager: ScriptTemplateManager
    private lateinit var scriptOptimizer: ScriptOptimizer
    private lateinit var nlpProcessor: NLPProcessor

    private val generationCache = ConcurrentHashMap<String, ScriptGenerationResponse>()

    override suspend fun onInitialize() {
        try {
            llmProcessor = LLMProcessor(context, config.llmConfig)
            screenshotAnalyzer = ScreenshotAnalyzer(context, config.aiModelConfig)
            templateManager = ScriptTemplateManager(context)
            scriptOptimizer = ScriptOptimizer(context)
            nlpProcessor = NLPProcessor(context)

            // 加载预训练模型
            llmProcessor.loadModel()
            screenshotAnalyzer.loadModel()
            templateManager.loadTemplates()

            Log.i(TAG, "ScriptGenerationAgent initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ScriptGenerationAgent", e)
            onError(e)
        }
    }

    override suspend fun onStart() {
        Log.i(TAG, "ScriptGenerationAgent started")
    }

    override suspend fun onStop() {
        Log.i(TAG, "ScriptGenerationAgent stopped")
    }

    override suspend fun onDestroy() {
        generationCache.clear()
        llmProcessor.release()
        screenshotAnalyzer.release()
        Log.i(TAG, "ScriptGenerationAgent destroyed")
    }

    override suspend fun processMessage(message: AgentMessage) {
        when (message.type) {
            MessageType.COMMAND -> handleCommand(message)
            MessageType.QUERY -> handleQuery(message)
            else -> Log.w(TAG, "Unsupported message type: ${message.type}")
        }
    }

    override fun onError(error: Exception) {
        Log.e(TAG, "ScriptGenerationAgent error", error)
        emitEvent(AgentEvent.ERROR, error.message)
    }

    private suspend fun handleCommand(message: AgentMessage) {
        when (message.content) {
            "generate_script" -> {
                message.data?.let { data ->
                    val request = parseGenerationRequest(data)
                    val response = generateScript(request)
                    emitEvent(AgentEvent.TASK_COMPLETED, response)
                }
            }
            "optimize_script" -> {
                message.data?.let { data ->
                    val scriptContent = data["script"] as? String ?: return
                    val suggestions = optimizeScript(scriptContent)
                    emitEvent(AgentEvent.TASK_COMPLETED, suggestions)
                }
            }
            "recommend_templates" -> {
                message.data?.let { data ->
                    val description = data["description"] as? String ?: return
                    val templates = recommendTemplates(description)
                    emitEvent(AgentEvent.TASK_COMPLETED, templates)
                }
            }
        }
    }

    private suspend fun handleQuery(message: AgentMessage) {
        when (message.content) {
            "get_supported_templates" -> {
                val templates = templateManager.getSupportedTemplates()
                emitEvent(AgentEvent.TASK_COMPLETED, templates)
            }
            "get_generation_history" -> {
                val history = getGenerationHistory()
                emitEvent(AgentEvent.TASK_COMPLETED, history)
            }
        }
    }

    /**
     * 生成脚本
     */
    private suspend fun generateScript(request: ScriptGenerationRequest): ScriptGenerationResponse {
        return try {
            val cacheKey = generateCacheKey(request)
            generationCache[cacheKey]?.let { cached ->
                Log.i(TAG, "Returning cached script generation result")
                return cached
            }

            val context = buildGenerationContext(request)
            val script = generateScriptFromContext(context)
            val optimizedScript = scriptOptimizer.optimize(script)
            val suggestions = generateSuggestions(context, optimizedScript)

            val response = ScriptGenerationResponse(
                success = true,
                script = optimizedScript,
                suggestions = suggestions
            )

            generationCache[cacheKey] = response
            response
        } catch (e: Exception) {
            Log.e(TAG, "Script generation failed", e)
            ScriptGenerationResponse(
                success = false,
                script = "",
                errorMessage = e.message
            )
        }
    }

    /**
     * 构建生成上下文
     */
    private suspend fun buildGenerationContext(request: ScriptGenerationRequest): GenerationContext {
        val nlpResult = nlpProcessor.process(request.description)
        val uiInfo = request.screenshotPath?.let { path ->
            screenshotAnalyzer.analyzeScreenshot(File(path))
        }
        val recommendedTemplate = templateManager.findBestTemplate(request.description)

        return GenerationContext(
            description = request.description,
            intent = nlpResult.intent,
            entities = nlpResult.entities,
            uiElements = uiInfo?.uiElements ?: emptyList(),
            template = recommendedTemplate,
            preferences = request.preferences ?: emptyMap()
        )
    }

    /**
     * 基于上下文生成脚本
     */
    private suspend fun generateScriptFromContext(context: GenerationContext): String {
        val prompt = buildPrompt(context)
        return llmProcessor.generateScript(prompt)
    }

    /**
     * 构建提示词
     */
    private fun buildPrompt(context: GenerationContext): String {
        val promptBuilder = StringBuilder()
        
        promptBuilder.append("请根据以下信息生成AutoJs6脚本：\n\n")
        promptBuilder.append("用户需求：${context.description}\n")
        promptBuilder.append("意图：${context.intent}\n")
        
        if (context.entities.isNotEmpty()) {
            promptBuilder.append("实体信息：\n")
            context.entities.forEach { (key, value) ->
                promptBuilder.append("- $key: $value\n")
            }
        }
        
        if (context.uiElements.isNotEmpty()) {
            promptBuilder.append("UI元素信息：\n")
            context.uiElements.forEach { element ->
                promptBuilder.append("- ${element.className}: ${element.text} (${element.bounds})\n")
            }
        }
        
        context.template?.let { template ->
            promptBuilder.append("参考模板：\n")
            promptBuilder.append("${template.code}\n")
        }
        
        promptBuilder.append("\n请生成相应的JavaScript代码，确保代码可以在AutoJs6中正常运行。")
        
        return promptBuilder.toString()
    }

    /**
     * 优化脚本
     */
    private suspend fun optimizeScript(script: String): List<OptimizationSuggestion> {
        return scriptOptimizer.analyzeAndOptimize(script)
    }

    /**
     * 推荐模板
     */
    private suspend fun recommendTemplates(description: String): List<ScriptTemplate> {
        return templateManager.recommendTemplates(description)
    }

    /**
     * 生成建议
     */
    private suspend fun generateSuggestions(
        context: GenerationContext,
        script: String
    ): List<String> {
        val suggestions = mutableListOf<String>()

        // 基于UI元素的建议
        if (context.uiElements.isNotEmpty()) {
            suggestions.add("建议添加UI元素存在性检查")
            suggestions.add("考虑添加等待时间以确保UI加载完成")
        }

        // 基于脚本内容的建议
        if (script.contains("click") && !script.contains("sleep")) {
            suggestions.add("建议在点击操作后添加适当的等待时间")
        }

        if (!script.contains("try") && !script.contains("catch")) {
            suggestions.add("建议添加异常处理以提高脚本稳定性")
        }

        return suggestions
    }

    /**
     * 生成缓存键
     */
    private fun generateCacheKey(request: ScriptGenerationRequest): String {
        return "${request.description}_${request.screenshotPath}_${request.templateType}".hashCode().toString()
    }

    /**
     * 解析生成请求
     */
    private fun parseGenerationRequest(data: Map<String, Any>): ScriptGenerationRequest {
        return ScriptGenerationRequest(
            description = data["description"] as String,
            screenshotPath = data["screenshotPath"] as? String,
            templateType = data["templateType"] as? String,
            preferences = data["preferences"] as? Map<String, Any>
        )
    }

    /**
     * 获取生成历史
     */
    private fun getGenerationHistory(): List<ScriptGenerationResponse> {
        return generationCache.values.toList()
    }
}