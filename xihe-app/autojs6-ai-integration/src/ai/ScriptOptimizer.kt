package org.autojs.autojs.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 脚本优化器
 * 根据执行错误自动优化脚本
 */
class ScriptOptimizer {

    /**
     * 优化脚本
     */
    suspend fun optimize(
        script: String,
        error: String,
        screenInfo: ScreenInfo
    ): String = withContext(Dispatchers.IO) {
        try {
            Timber.i("开始优化脚本，错误: $error")
            
            val optimizedScript = when {
                // 超时错误 - 增加等待时间
                error.contains("timeout", ignoreCase = true) ||
                error.contains("超时", ignoreCase = true) -> {
                    optimizeTimeout(script)
                }
                
                // 找不到元素 - 尝试其他选择器
                error.contains("null", ignoreCase = true) ||
                error.contains("未找到", ignoreCase = true) -> {
                    optimizeSelector(script, screenInfo)
                }
                
                // 权限错误 - 添加权限请求
                error.contains("permission", ignoreCase = true) ||
                error.contains("权限", ignoreCase = true) -> {
                    optimizePermission(script, error)
                }
                
                // 点击失败 - 使用坐标点击
                error.contains("click", ignoreCase = true) ||
                error.contains("点击", ignoreCase = true) -> {
                    optimizeClick(script, screenInfo)
                }
                
                // 其他错误 - 添加错误处理
                else -> {
                    addErrorHandling(script)
                }
            }
            
            Timber.d("优化后的脚本:\n$optimizedScript")
            optimizedScript
            
        } catch (e: Exception) {
            Timber.e(e, "脚本优化失败")
            script  // 返回原脚本
        }
    }

    /**
     * 优化超时问题 - 增加等待时间
     */
    private fun optimizeTimeout(script: String): String {
        return script
            .replace(Regex("findOne\\((\\d+)\\)")) { matchResult ->
                val currentTimeout = matchResult.groupValues[1].toIntOrNull() ?: 5000
                val newTimeout = currentTimeout * 2
                "findOne($newTimeout)"
            }
            .replace(Regex("sleep\\((\\d+)\\)")) { matchResult ->
                val currentSleep = matchResult.groupValues[1].toIntOrNull() ?: 1000
                val newSleep = currentSleep + 1000
                "sleep($newSleep)"
            }
    }

    /**
     * 优化选择器 - 尝试多种方式查找元素
     */
    private fun optimizeSelector(script: String, screenInfo: ScreenInfo): String {
        // 提取原始的text查找
        val textPattern = Regex("text\\(\"([^\"]+)\"\\)\\.findOne")
        val textMatch = textPattern.find(script)
        
        if (textMatch != null) {
            val searchText = textMatch.groupValues[1]
            
            // 在屏幕信息中查找相关元素
            val relatedElement = screenInfo.elements.find {
                it.text.contains(searchText, ignoreCase = true) ||
                it.contentDesc.contains(searchText, ignoreCase = true)
            }
            
            return if (relatedElement != null) {
                // 添加多种查找方式
                script.replace(textPattern.pattern.toRegex()) {
                    """
                    // 尝试多种方式查找
                    (text("$searchText").findOne(3000) ||
                     desc("$searchText").findOne(3000) ||
                     textContains("$searchText").findOne(3000) ||
                     descContains("$searchText").findOne(3000))
                    """.trimIndent()
                }
            } else {
                script
            }
        }
        
        return script
    }

    /**
     * 优化权限问题 - 添加权限请求
     */
    private fun optimizePermission(script: String, error: String): String {
        val permissionCode = when {
            error.contains("CAPTURE", ignoreCase = true) ||
            error.contains("截图", ignoreCase = true) -> {
                "requestScreenCapture();\nsleep(2000);\n"
            }
            else -> ""
        }
        
        // 在auto()之后添加权限请求
        return script.replace("auto();", "auto();\n$permissionCode")
    }

    /**
     * 优化点击问题 - 使用坐标点击
     */
    private fun optimizeClick(script: String, screenInfo: ScreenInfo): String {
        // 如果click()调用失败，添加备用的坐标点击
        val clickPattern = Regex("(\\w+)\\.click\\(\\)")
        
        return script.replace(clickPattern) { matchResult ->
            val varName = matchResult.groupValues[1]
            """
            if ($varName) {
                try {
                    $varName.click();
                } catch (e) {
                    // 备用方案：使用坐标点击
                    var bounds = $varName.bounds();
                    click(bounds.centerX(), bounds.centerY());
                }
            }
            """.trimIndent()
        }
    }

    /**
     * 添加错误处理
     */
    private fun addErrorHandling(script: String): String {
        // 如果脚本中没有try-catch，添加全局错误处理
        return if (!script.contains("try") && !script.contains("catch")) {
            """
            try {
            $script
            } catch (e) {
                log("执行出错: " + e);
                toast("执行失败: " + e.message);
            }
            """.trimIndent()
        } else {
            script
        }
    }

    /**
     * 智能分析错误并提供优化建议
     */
    fun analyzeError(script: String, error: String): OptimizationSuggestion {
        val suggestions = mutableListOf<String>()
        
        when {
            error.contains("null") -> {
                suggestions.add("元素未找到，建议检查选择器或增加等待时间")
                suggestions.add("可以尝试使用textContains()或descContains()进行模糊匹配")
            }
            
            error.contains("timeout") -> {
                suggestions.add("操作超时，建议增加findOne()的等待时间")
                suggestions.add("或者添加更多的sleep()等待元素加载")
            }
            
            error.contains("permission") -> {
                suggestions.add("缺少权限，需要添加权限请求")
                suggestions.add("如截图权限: requestScreenCapture()")
            }
            
            error.contains("click") -> {
                suggestions.add("点击失败，可以尝试使用坐标点击")
                suggestions.add("或者检查元素是否可点击")
            }
        }
        
        return OptimizationSuggestion(
            canOptimize = suggestions.isNotEmpty(),
            suggestions = suggestions,
            errorType = categorizeError(error)
        )
    }

    /**
     * 错误分类
     */
    private fun categorizeError(error: String): ErrorType {
        return when {
            error.contains("null") -> ErrorType.ELEMENT_NOT_FOUND
            error.contains("timeout") -> ErrorType.TIMEOUT
            error.contains("permission") -> ErrorType.PERMISSION_DENIED
            error.contains("click") -> ErrorType.CLICK_FAILED
            else -> ErrorType.UNKNOWN
        }
    }
}

/**
 * 优化建议
 */
data class OptimizationSuggestion(
    val canOptimize: Boolean,
    val suggestions: List<String>,
    val errorType: ErrorType
)

/**
 * 错误类型
 */
enum class ErrorType {
    ELEMENT_NOT_FOUND,
    TIMEOUT,
    PERMISSION_DENIED,
    CLICK_FAILED,
    UNKNOWN
}
