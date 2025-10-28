package org.autojs.autojs.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.autojs.autojs.runtime.ScriptRuntime
import org.autojs.autojs.runtime.exception.ScriptException
import timber.log.Timber

/**
 * AI助手核心类
 * 集成AutoJs6的所有功能，提供AI增强的自动化能力
 */
class AIAssistant(private val runtime: ScriptRuntime) {

    private val scriptGenerator = AIScriptGenerator()
    private val screenAnalyzer = ScreenAnalyzer(runtime)
    private val optimizer = ScriptOptimizer()
    private val conversationManager = ConversationManager()

    companion object {
        private const val MAX_RETRY_TIMES = 3
    }

    /**
     * 执行用户请求（完整流程）
     * 1. 分析屏幕
     * 2. 生成脚本
     * 3. 执行脚本
     * 4. 失败则优化并重试
     */
    suspend fun executeUserRequest(request: String): ExecutionResult = withContext(Dispatchers.IO) {
        try {
            Timber.i("AI助手收到请求: $request")
            
            // 1. 分析当前屏幕（使用AutoJs6的accessibility和OCR）
            val screenInfo = screenAnalyzer.analyzeCurrentScreen()
            Timber.d("屏幕分析完成，发现 ${screenInfo.elements.size} 个元素")
            
            // 2. 生成脚本（AI基于屏幕信息生成真实的AutoJs6代码）
            val script = scriptGenerator.generate(request, screenInfo)
            Timber.d("脚本生成完成:\n$script")
            
            // 3. 执行脚本（使用AutoJs6的脚本引擎）
            var result = executeScript(script)
            var retryCount = 0
            
            // 4. 如果失败，使用AI优化并重试
            while (!result.isSuccess && retryCount < MAX_RETRY_TIMES) {
                Timber.w("执行失败，第 ${retryCount + 1} 次尝试优化")
                
                // AI分析错误并优化脚本
                val optimizedScript = optimizer.optimize(
                    script = result.script,
                    error = result.error ?: "",
                    screenInfo = screenInfo
                )
                
                // 重新执行优化后的脚本
                result = executeScript(optimizedScript)
                retryCount++
            }
            
            // 5. 保存对话历史
            conversationManager.addConversation(request, result)
            
            result
            
        } catch (e: Exception) {
            Timber.e(e, "AI助手执行失败")
            ExecutionResult.failure(
                script = "",
                error = "AI助手执行失败: ${e.message}",
                output = ""
            )
        }
    }

    /**
     * 仅生成脚本，不执行
     */
    suspend fun generateScript(request: String): String = withContext(Dispatchers.IO) {
        val screenInfo = screenAnalyzer.analyzeCurrentScreen()
        scriptGenerator.generate(request, screenInfo)
    }

    /**
     * 分析当前屏幕
     */
    suspend fun analyzeScreen(): ScreenInfo = withContext(Dispatchers.IO) {
        screenAnalyzer.analyzeCurrentScreen()
    }

    /**
     * 执行脚本（使用AutoJs6的ScriptEngine）
     */
    private suspend fun executeScript(script: String): ExecutionResult = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            
            // 使用AutoJs6的脚本执行引擎
            val engine = runtime.engines.execution()
            val scriptExecution = engine.execute(script, "AI-Generated")
            
            // 等待执行完成
            val exception = scriptExecution.exception
            val executionTime = System.currentTimeMillis() - startTime
            
            if (exception != null) {
                ExecutionResult.failure(
                    script = script,
                    error = exception.message ?: "未知错误",
                    output = scriptExecution.engine.uncaughtException?.message ?: "",
                    executionTime = executionTime
                )
            } else {
                ExecutionResult.success(
                    script = script,
                    output = "执行成功",
                    executionTime = executionTime
                )
            }
            
        } catch (e: ScriptException) {
            Timber.e(e, "脚本执行异常")
            ExecutionResult.failure(
                script = script,
                error = e.message ?: "脚本执行异常",
                output = e.stackTraceToString()
            )
        } catch (e: Exception) {
            Timber.e(e, "未知异常")
            ExecutionResult.failure(
                script = script,
                error = "执行失败: ${e.message}",
                output = e.stackTraceToString()
            )
        }
    }

    /**
     * 清除对话历史
     */
    fun clearHistory() {
        conversationManager.clear()
    }

    /**
     * 获取对话历史
     */
    fun getHistory(): List<Conversation> {
        return conversationManager.getHistory()
    }
}

/**
 * 执行结果
 */
data class ExecutionResult(
    val isSuccess: Boolean,
    val script: String,
    val output: String,
    val error: String?,
    val executionTime: Long = 0
) {
    companion object {
        fun success(script: String, output: String, executionTime: Long = 0) = ExecutionResult(
            isSuccess = true,
            script = script,
            output = output,
            error = null,
            executionTime = executionTime
        )

        fun failure(script: String, error: String, output: String = "", executionTime: Long = 0) = ExecutionResult(
            isSuccess = false,
            script = script,
            output = output,
            error = error,
            executionTime = executionTime
        )
    }
}

/**
 * 对话记录
 */
data class Conversation(
    val userRequest: String,
    val generatedScript: String,
    val executionResult: ExecutionResult,
    val timestamp: Long = System.currentTimeMillis()
)
