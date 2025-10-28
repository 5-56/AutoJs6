package com.xihe.automation.script

import com.xihe.automation.data.model.ScreenAnalysis
import com.xihe.automation.data.model.ScriptExecutionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 脚本优化器
 * 根据执行结果和屏幕信息优化脚本
 */
class ScriptOptimizer {

    /**
     * 优化脚本
     */
    suspend fun optimize(
        script: String,
        executionResult: ScriptExecutionResult,
        screenAnalysis: ScreenAnalysis
    ): String = withContext(Dispatchers.IO) {
        try {
            val error = executionResult.error ?: ""
            Timber.i("优化脚本，错误类型: ${analyzeErrorType(error)}")
            
            return@withContext when {
                // 找不到元素
                error.contains("null") ||
                error.contains("找不到") ||
                error.contains("not found", ignoreCase = true) -> {
                    optimizeElementNotFound(script, screenAnalysis)
                }
                
                // 超时
                error.contains("timeout", ignoreCase = true) ||
                error.contains("超时") -> {
                    optimizeTimeout(script)
                }
                
                // 权限问题
                error.contains("permission", ignoreCase = true) ||
                error.contains("权限") -> {
                    optimizePermission(script, error)
                }
                
                // 点击失败
                error.contains("click", ignoreCase = true) ||
                error.contains("点击失败") -> {
                    optimizeClickFailure(script, screenAnalysis)
                }
                
                // 其他错误
                else -> {
                    addErrorHandlingAndRetry(script)
                }
            }
            
        } catch (e: Exception) {
            Timber.e(e, "脚本优化失败")
            script
        }
    }

    /**
     * 优化元素未找到问题
     */
    private fun optimizeElementNotFound(script: String, screenAnalysis: ScreenAnalysis): String {
        // 提取脚本中查找的文本
        val textPattern = Regex("""text\("([^"]+)"\)""")
        val matches = textPattern.findAll(script)
        
        return if (matches.any()) {
            var optimizedScript = script
            
            matches.forEach { match ->
                val searchText = match.groupValues[1]
                
                // 在屏幕分析中查找相似文本
                val similarElement = screenAnalysis.elements.find { element ->
                    element.text?.contains(searchText, ignoreCase = true) == true ||
                    element.description.contains(searchText, ignoreCase = true)
                }
                
                if (similarElement != null && similarElement.text != searchText) {
                    // 替换为实际找到的文本
                    optimizedScript = optimizedScript.replace(
                        """text("$searchText")""",
                        """text("${similarElement.text}")"""
                    )
                    Timber.d("优化: \"$searchText\" -> \"${similarElement.text}\"")
                } else {
                    // 改用模糊匹配
                    optimizedScript = optimizedScript.replace(
                        """text("$searchText").findOne""",
                        """textContains("$searchText").findOne"""
                    )
                    Timber.d("优化: 使用textContains模糊匹配")
                }
            }
            
            optimizedScript
        } else {
            script
        }
    }

    /**
     * 优化超时问题
     */
    private fun optimizeTimeout(script: String): String {
        return script
            // 增加findOne的超时时间
            .replace(Regex("""findOne\((\d+)\)""")) { matchResult ->
                val timeout = matchResult.groupValues[1].toIntOrNull() ?: 5000
                val newTimeout = minOf(timeout * 2, 15000)
                "findOne($newTimeout)"
            }
            // 增加sleep时间
            .replace(Regex("""sleep\((\d+)\)""")) { matchResult ->
                val sleepTime = matchResult.groupValues[1].toIntOrNull() ?: 1000
                val newSleepTime = sleepTime + 1000
                "sleep($newSleepTime)"
            }
            .also {
                Timber.d("优化: 增加等待时间")
            }
    }

    /**
     * 优化权限问题
     */
    private fun optimizePermission(script: String, error: String): String {
        val permissionCode = when {
            error.contains("CAPTURE") || error.contains("截图") -> {
                """
                // 请求截图权限
                if (!requestScreenCapture()) {
                    toast("需要截图权限");
                    exit();
                }
                sleep(2000);
                
                """.trimIndent()
            }
            else -> ""
        }
        
        // 在auto()后添加权限请求
        return script.replace(
            "auto();",
            "auto();\n$permissionCode"
        ).also {
            Timber.d("优化: 添加权限请求")
        }
    }

    /**
     * 优化点击失败问题
     */
    private fun optimizeClickFailure(script: String, screenAnalysis: ScreenAnalysis): String {
        // 如果有元素信息，添加坐标点击作为备用
        val clickableElements = screenAnalysis.elements.filter { it.isClickable }
        
        return if (clickableElements.isNotEmpty()) {
            script.replace(
                Regex("""(\w+)\.click\(\);""")
            ) { matchResult ->
                val varName = matchResult.groupValues[1]
                """
                // 尝试点击，失败则使用坐标
                if ($varName) {
                    if (!$varName.click()) {
                        var b = $varName.bounds();
                        click(b.centerX(), b.centerY());
                    }
                }
                """.trimIndent()
            }.also {
                Timber.d("优化: 添加坐标点击备用方案")
            }
        } else {
            script
        }
    }

    /**
     * 添加错误处理和重试逻辑
     */
    private fun addErrorHandlingAndRetry(script: String): String {
        return if (!script.contains("try") && !script.contains("catch")) {
            """
            // 添加全局错误处理
            try {
            $script
            } catch (e) {
                log("脚本执行出错: " + e);
                toast("执行失败: " + e.message);
            }
            """.trimIndent().also {
                Timber.d("优化: 添加错误处理")
            }
        } else {
            script
        }
    }

    /**
     * 分析错误类型
     */
    private fun analyzeErrorType(error: String): String {
        return when {
            error.contains("null") -> "元素未找到"
            error.contains("timeout") -> "操作超时"
            error.contains("permission") -> "权限不足"
            error.contains("click") -> "点击失败"
            else -> "未知错误"
        }
    }
}
