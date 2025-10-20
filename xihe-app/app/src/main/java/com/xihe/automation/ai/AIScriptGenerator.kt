package com.xihe.automation.ai

import com.xihe.automation.data.model.ScreenAnalysis
import timber.log.Timber

/**
 * AI脚本生成器
 * 根据用户需求和屏幕分析结果生成AutoJs脚本
 */
class AIScriptGenerator {

    /**
     * 根据用户需求生成脚本
     */
    fun generateScript(userIntent: String, screenAnalysis: ScreenAnalysis? = null): String {
        return try {
            val context = if (screenAnalysis != null) {
                buildContextFromScreen(screenAnalysis)
            } else {
                ""
            }

            buildScript(userIntent, context)
        } catch (e: Exception) {
            Timber.e(e, "脚本生成失败")
            generateDefaultScript()
        }
    }

    /**
     * 从屏幕分析结果构建上下文
     */
    private fun buildContextFromScreen(analysis: ScreenAnalysis): String {
        val elements = analysis.elements.joinToString("\n") { element ->
            "- ${element.type}: \"${element.text}\" (${if (element.isClickable) "可点击" else "不可点击"})"
        }

        val texts = analysis.texts.joinToString("\n") { "- $it" }

        return """
            当前屏幕包含以下元素：
            $elements
            
            识别到的文字：
            $texts
        """.trimIndent()
    }

    /**
     * 构建脚本
     */
    private fun buildScript(userIntent: String, context: String): String {
        return when {
            userIntent.contains("点击") -> generateClickScript(userIntent, context)
            userIntent.contains("滑动") -> generateSwipeScript(userIntent)
            userIntent.contains("输入") -> generateInputScript(userIntent)
            userIntent.contains("等待") -> generateWaitScript(userIntent)
            userIntent.contains("循环") -> generateLoopScript(userIntent)
            else -> generateDefaultScript()
        }
    }

    /**
     * 生成点击脚本
     */
    private fun generateClickScript(intent: String, context: String): String {
        return """
            // 自动点击脚本
            // 需求: $intent
            
            auto();
            
            // 等待界面加载
            sleep(2000);
            
            // 查找目标元素
            var target = text("目标文本").findOne(5000);
            
            if (target) {
                // 点击元素
                target.click();
                toast("点击成功");
                
                // 等待操作完成
                sleep(1000);
            } else {
                toast("未找到目标元素");
                exit();
            }
        """.trimIndent()
    }

    /**
     * 生成滑动脚本
     */
    private fun generateSwipeScript(intent: String): String {
        return """
            // 自动滑动脚本
            // 需求: $intent
            
            auto();
            
            // 获取屏幕尺寸
            var width = device.width;
            var height = device.height;
            
            // 向上滑动
            swipe(width / 2, height * 0.8, width / 2, height * 0.2, 500);
            toast("滑动完成");
            
            sleep(1000);
        """.trimIndent()
    }

    /**
     * 生成输入脚本
     */
    private fun generateInputScript(intent: String): String {
        return """
            // 自动输入脚本
            // 需求: $intent
            
            auto();
            
            // 等待界面加载
            sleep(2000);
            
            // 查找输入框
            var inputField = className("EditText").findOne(5000);
            
            if (inputField) {
                // 设置文本
                inputField.setText("自动输入的内容");
                toast("输入成功");
            } else {
                toast("未找到输入框");
                exit();
            }
        """.trimIndent()
    }

    /**
     * 生成等待脚本
     */
    private fun generateWaitScript(intent: String): String {
        return """
            // 等待元素出现脚本
            // 需求: $intent
            
            auto();
            
            // 等待特定元素出现
            var element = text("目标文本").findOne(10000);
            
            if (element) {
                toast("元素已出现");
                // 在这里添加后续操作
            } else {
                toast("等待超时");
                exit();
            }
        """.trimIndent()
    }

    /**
     * 生成循环脚本
     */
    private fun generateLoopScript(intent: String): String {
        return """
            // 循环执行脚本
            // 需求: $intent
            
            auto();
            
            // 循环次数
            var count = 5;
            
            for (var i = 0; i < count; i++) {
                toast("第 " + (i + 1) + " 次执行");
                
                // 在这里添加循环执行的操作
                sleep(2000);
            }
            
            toast("循环完成");
        """.trimIndent()
    }

    /**
     * 生成默认脚本
     */
    private fun generateDefaultScript(): String {
        return """
            // AutoJs 自动化脚本
            
            auto();
            
            // 等待界面加载
            sleep(2000);
            
            toast("脚本开始执行");
            
            // 在这里添加你的自动化逻辑
            
            toast("脚本执行完成");
        """.trimIndent()
    }

    /**
     * 优化脚本
     */
    fun optimizeScript(script: String, executionResult: String): String {
        // TODO: 基于执行结果优化脚本
        return script
    }
}
