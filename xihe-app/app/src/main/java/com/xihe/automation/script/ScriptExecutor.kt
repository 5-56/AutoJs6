package com.xihe.automation.script

import com.xihe.automation.autojs.execution.ExecutionConfig
import com.xihe.automation.autojs.execution.ScriptExecution
import com.xihe.automation.autojs.runtime.ScriptRuntime
import com.xihe.automation.data.model.ScriptExecutionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ScriptableObject
import timber.log.Timber
import java.io.StringWriter
import java.io.PrintWriter

/**
 * 脚本执行器（集成AutoJs6真实功能）
 * 负责执行JavaScript脚本
 */
class ScriptExecutor {

    /**
     * 使用AutoJs6执行脚本（真实执行）
     */
    suspend fun executeWithAutoJs(script: String, runtime: ScriptRuntime): ScriptExecutionResult = 
        withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        
        return@withContext try {
            Timber.i("使用AutoJs6引擎执行脚本")
            
            // 创建执行配置
            val config = ExecutionConfig().apply {
                workingDirectory = runtime.files.path()
                delay = 0
            }
            
            // 使用AutoJs6的脚本引擎执行
            val execution: ScriptExecution = runtime.engines.execution()
                .execute(script, "AI-Generated-Script", config)
            
            // 等待执行完成（最多60秒）
            var timeoutCount = 0
            while (execution.engine?.isAlive == true && timeoutCount < 600) {
                delay(100)
                timeoutCount++
            }
            
            val executionTime = System.currentTimeMillis() - startTime
            
            // 检查是否有异常
            val exception = execution.exception
            
            if (exception != null) {
                Timber.e(exception, "脚本执行异常")
                ScriptExecutionResult(
                    success = false,
                    error = exception.message ?: "未知错误",
                    executionTime = executionTime
                )
            } else {
                Timber.i("脚本执行成功，耗时: ${executionTime}ms")
                ScriptExecutionResult(
                    success = true,
                    output = "执行完成",
                    executionTime = executionTime
                )
            }
            
        } catch (e: Exception) {
            val executionTime = System.currentTimeMillis() - startTime
            Timber.e(e, "脚本执行失败")
            
            ScriptExecutionResult(
                success = false,
                error = e.message ?: "执行失败",
                output = e.stackTraceToString(),
                executionTime = executionTime
            )
        }
    }

    /**
     * 基础Rhino执行（备用方案）
     */
    fun execute(script: String): ScriptExecutionResult {
        val startTime = System.currentTimeMillis()
        val outputWriter = StringWriter()
        val errorWriter = StringWriter()

        return try {
            // 初始化Rhino JavaScript引擎
            val rhinoContext = Context.enter()
            rhinoContext.optimizationLevel = -1 // 使用解释模式
            
            // 创建作用域
            val scope: Scriptable = rhinoContext.initStandardObjects()
            
            // 注入全局对象和函数
            injectGlobalObjects(scope, outputWriter)
            
            // 执行脚本
            val result = rhinoContext.evaluateString(scope, script, "<script>", 1, null)
            
            val executionTime = System.currentTimeMillis() - startTime
            
            ScriptExecutionResult(
                success = true,
                output = outputWriter.toString() + "\n执行结果: ${Context.toString(result)}",
                executionTime = executionTime
            )
        } catch (e: Exception) {
            Timber.e(e, "脚本执行异常")
            e.printStackTrace(PrintWriter(errorWriter))
            
            val executionTime = System.currentTimeMillis() - startTime
            
            ScriptExecutionResult(
                success = false,
                output = outputWriter.toString(),
                error = "执行错误: ${e.message}\n${errorWriter}",
                executionTime = executionTime
            )
        } finally {
            Context.exit()
        }
    }

    /**
     * 注入全局对象和函数
     */
    private fun injectGlobalObjects(scope: Scriptable, outputWriter: StringWriter) {
        // 注入console对象
        val console = object : ScriptableObject() {
            override fun getClassName(): String = "console"
            
            @Suppress("unused")
            fun jsFunction_log(message: Any?) {
                val text = Context.toString(message)
                outputWriter.write("$text\n")
                Timber.d("Console: $text")
            }
        }
        console.defineFunctionProperties(
            arrayOf("log"),
            console.javaClass,
            ScriptableObject.DONTENUM
        )
        scope.put("console", scope, console)
        
        // 注入toast函数（模拟）
        val toastFunc = object : org.mozilla.javascript.BaseFunction() {
            override fun call(
                cx: Context?,
                scope: Scriptable?,
                thisObj: Scriptable?,
                args: Array<out Any>?
            ): Any {
                val message = if (args != null && args.isNotEmpty()) {
                    Context.toString(args[0])
                } else {
                    ""
                }
                outputWriter.write("[Toast] $message\n")
                Timber.d("Toast: $message")
                return message
            }
        }
        scope.put("toast", scope, toastFunc)
        
        // 注入sleep函数（模拟）
        val sleepFunc = object : org.mozilla.javascript.BaseFunction() {
            override fun call(
                cx: Context?,
                scope: Scriptable?,
                thisObj: Scriptable?,
                args: Array<out Any>?
            ): Any? {
                val duration = if (args != null && args.isNotEmpty()) {
                    Context.toNumber(args[0]).toLong()
                } else {
                    0L
                }
                Thread.sleep(duration)
                return null
            }
        }
        scope.put("sleep", scope, sleepFunc)
        
        // 注入auto函数（模拟）
        val autoFunc = object : org.mozilla.javascript.BaseFunction() {
            override fun call(
                cx: Context?,
                scope: Scriptable?,
                thisObj: Scriptable?,
                args: Array<out Any>?
            ): Any {
                outputWriter.write("[Auto] 无障碍服务已启用\n")
                return true
            }
        }
        scope.put("auto", scope, autoFunc)
    }

    /**
     * 验证脚本语法
     */
    fun validateScript(script: String): Pair<Boolean, String?> {
        return try {
            val rhinoContext = Context.enter()
            rhinoContext.optimizationLevel = -1
            
            val scope: Scriptable = rhinoContext.initStandardObjects()
            rhinoContext.compileString(script, "<script>", 1, null)
            
            Pair(true, null)
        } catch (e: Exception) {
            Pair(false, e.message)
        } finally {
            Context.exit()
        }
    }
}
