package org.autojs.autojs.agent.core

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * Agent日志记录器 - 统一的日志管理和分析
 * Created by SuperMonster003 on 2024/01/15
 */
class AgentLogger private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "AgentLogger"
        private const val LOG_FILE_PREFIX = "agent_log_"
        private const val LOG_FILE_EXTENSION = ".log"
        private const val MAX_LOG_FILES = 5
        private const val MAX_LOG_FILE_SIZE = 10 * 1024 * 1024 // 10MB
        private const val MAX_MEMORY_LOGS = 1000
        
        @Volatile
        private var INSTANCE: AgentLogger? = null
        
        fun getInstance(context: Context): AgentLogger {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AgentLogger(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val logDir = File(context.filesDir, "agent_logs")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val fileDateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    
    // 内存日志队列
    private val memoryLogs = ConcurrentLinkedQueue<LogEntry>()
    private val logCounter = AtomicInteger(0)
    
    // 日志级别
    enum class LogLevel(val priority: Int) {
        VERBOSE(2),
        DEBUG(3),
        INFO(4),
        WARN(5),
        ERROR(6)
    }
    
    // 日志条目
    data class LogEntry(
        val timestamp: Long,
        val level: LogLevel,
        val agentId: String,
        val tag: String,
        val message: String,
        val threadName: String,
        val exception: Throwable? = null
    )
    
    // 日志统计
    data class LogStats(
        val totalLogs: Int,
        val errorCount: Int,
        val warnCount: Int,
        val infoCount: Int,
        val debugCount: Int,
        val verboseCount: Int,
        val agentStats: Map<String, Int>
    )
    
    private var currentLogLevel = LogLevel.DEBUG
    private var enableFileLogging = true
    private var enableMemoryLogging = true
    
    init {
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        
        // 清理旧日志文件
        scope.launch {
            cleanupOldLogFiles()
        }
    }
    
    /**
     * 设置日志级别
     */
    fun setLogLevel(level: LogLevel) {
        currentLogLevel = level
        i("AgentLogger", "Log level set to: $level")
    }
    
    /**
     * 启用/禁用文件日志
     */
    fun setFileLoggingEnabled(enabled: Boolean) {
        enableFileLogging = enabled
        i("AgentLogger", "File logging enabled: $enabled")
    }
    
    /**
     * 启用/禁用内存日志
     */
    fun setMemoryLoggingEnabled(enabled: Boolean) {
        enableMemoryLogging = enabled
        i("AgentLogger", "Memory logging enabled: $enabled")
    }
    
    /**
     * 记录VERBOSE级别日志
     */
    fun v(agentId: String, message: String, exception: Throwable? = null) {
        log(LogLevel.VERBOSE, agentId, message, exception)
    }
    
    /**
     * 记录DEBUG级别日志
     */
    fun d(agentId: String, message: String, exception: Throwable? = null) {
        log(LogLevel.DEBUG, agentId, message, exception)
    }
    
    /**
     * 记录INFO级别日志
     */
    fun i(agentId: String, message: String, exception: Throwable? = null) {
        log(LogLevel.INFO, agentId, message, exception)
    }
    
    /**
     * 记录WARN级别日志
     */
    fun w(agentId: String, message: String, exception: Throwable? = null) {
        log(LogLevel.WARN, agentId, message, exception)
    }
    
    /**
     * 记录ERROR级别日志
     */
    fun e(agentId: String, message: String, exception: Throwable? = null) {
        log(LogLevel.ERROR, agentId, message, exception)
    }
    
    /**
     * 记录日志
     */
    private fun log(level: LogLevel, agentId: String, message: String, exception: Throwable? = null) {
        if (level.priority < currentLogLevel.priority) {
            return
        }
        
        val timestamp = System.currentTimeMillis()
        val threadName = Thread.currentThread().name
        val logEntry = LogEntry(
            timestamp = timestamp,
            level = level,
            agentId = agentId,
            tag = "Agent-$agentId",
            message = message,
            threadName = threadName,
            exception = exception
        )
        
        // 写入Android日志
        writeToAndroidLog(logEntry)
        
        // 写入内存日志
        if (enableMemoryLogging) {
            writeToMemoryLog(logEntry)
        }
        
        // 写入文件日志
        if (enableFileLogging) {
            scope.launch {
                writeToFileLog(logEntry)
            }
        }
    }
    
    /**
     * 写入Android日志
     */
    private fun writeToAndroidLog(entry: LogEntry) {
        val message = if (entry.exception != null) {
            "${entry.message}\n${Log.getStackTraceString(entry.exception)}"
        } else {
            entry.message
        }
        
        when (entry.level) {
            LogLevel.VERBOSE -> Log.v(entry.tag, message)
            LogLevel.DEBUG -> Log.d(entry.tag, message)
            LogLevel.INFO -> Log.i(entry.tag, message)
            LogLevel.WARN -> Log.w(entry.tag, message)
            LogLevel.ERROR -> Log.e(entry.tag, message)
        }
    }
    
    /**
     * 写入内存日志
     */
    private fun writeToMemoryLog(entry: LogEntry) {
        memoryLogs.offer(entry)
        logCounter.incrementAndGet()
        
        // 限制内存日志数量
        while (memoryLogs.size > MAX_MEMORY_LOGS) {
            memoryLogs.poll()
        }
    }
    
    /**
     * 写入文件日志
     */
    private suspend fun writeToFileLog(entry: LogEntry) {
        try {
            val logFile = getCurrentLogFile()
            val logMessage = formatLogMessage(entry)
            
            FileWriter(logFile, true).use { writer ->
                writer.write(logMessage)
                writer.write("\n")
            }
            
            // 检查文件大小，如果超过限制则轮转
            if (logFile.length() > MAX_LOG_FILE_SIZE) {
                rotateLogFile()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write to log file", e)
        }
    }
    
    /**
     * 获取当前日志文件
     */
    private fun getCurrentLogFile(): File {
        val dateString = fileDateFormat.format(Date())
        return File(logDir, "$LOG_FILE_PREFIX$dateString$LOG_FILE_EXTENSION")
    }
    
    /**
     * 格式化日志消息
     */
    private fun formatLogMessage(entry: LogEntry): String {
        val timestamp = dateFormat.format(Date(entry.timestamp))
        val level = entry.level.name.padEnd(7)
        val agentId = entry.agentId.padEnd(20)
        val thread = entry.threadName.padEnd(15)
        
        val baseMessage = "$timestamp $level $agentId $thread: ${entry.message}"
        
        return if (entry.exception != null) {
            val stackTrace = Log.getStackTraceString(entry.exception)
            "$baseMessage\n$stackTrace"
        } else {
            baseMessage
        }
    }
    
    /**
     * 轮转日志文件
     */
    private fun rotateLogFile() {
        try {
            val logFiles = logDir.listFiles { _, name ->
                name.startsWith(LOG_FILE_PREFIX) && name.endsWith(LOG_FILE_EXTENSION)
            }?.sortedBy { it.lastModified() }
            
            if (logFiles != null && logFiles.size >= MAX_LOG_FILES) {
                // 删除最旧的日志文件
                val filesToDelete = logFiles.take(logFiles.size - MAX_LOG_FILES + 1)
                filesToDelete.forEach { file ->
                    file.delete()
                    Log.d(TAG, "Deleted old log file: ${file.name}")
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to rotate log file", e)
        }
    }
    
    /**
     * 清理旧日志文件
     */
    private fun cleanupOldLogFiles() {
        try {
            val currentTime = System.currentTimeMillis()
            val maxAge = 7 * 24 * 60 * 60 * 1000L // 7天
            
            logDir.listFiles()?.forEach { file ->
                if (file.isFile && currentTime - file.lastModified() > maxAge) {
                    file.delete()
                    Log.d(TAG, "Deleted expired log file: ${file.name}")
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup old log files", e)
        }
    }
    
    /**
     * 获取内存日志
     */
    fun getMemoryLogs(): List<LogEntry> {
        return memoryLogs.toList()
    }
    
    /**
     * 获取指定Agent的日志
     */
    fun getAgentLogs(agentId: String): List<LogEntry> {
        return memoryLogs.filter { it.agentId == agentId }
    }
    
    /**
     * 获取指定级别的日志
     */
    fun getLogsByLevel(level: LogLevel): List<LogEntry> {
        return memoryLogs.filter { it.level == level }
    }
    
    /**
     * 获取日志统计
     */
    fun getLogStats(): LogStats {
        val logs = memoryLogs.toList()
        val agentStats = logs.groupBy { it.agentId }
            .mapValues { it.value.size }
        
        return LogStats(
            totalLogs = logs.size,
            errorCount = logs.count { it.level == LogLevel.ERROR },
            warnCount = logs.count { it.level == LogLevel.WARN },
            infoCount = logs.count { it.level == LogLevel.INFO },
            debugCount = logs.count { it.level == LogLevel.DEBUG },
            verboseCount = logs.count { it.level == LogLevel.VERBOSE },
            agentStats = agentStats
        )
    }
    
    /**
     * 清空内存日志
     */
    fun clearMemoryLogs() {
        memoryLogs.clear()
        logCounter.set(0)
        i("AgentLogger", "Memory logs cleared")
    }
    
    /**
     * 导出日志到文件
     */
    suspend fun exportLogs(outputFile: File): Boolean {
        return try {
            FileWriter(outputFile).use { writer ->
                memoryLogs.forEach { entry ->
                    writer.write(formatLogMessage(entry))
                    writer.write("\n")
                }
            }
            i("AgentLogger", "Logs exported to: ${outputFile.absolutePath}")
            true
        } catch (e: Exception) {
            e("AgentLogger", "Failed to export logs", e)
            false
        }
    }
    
    /**
     * 获取日志文件列表
     */
    fun getLogFiles(): List<File> {
        return logDir.listFiles { _, name ->
            name.startsWith(LOG_FILE_PREFIX) && name.endsWith(LOG_FILE_EXTENSION)
        }?.sortedBy { it.lastModified() }?.toList() ?: emptyList()
    }
    
    /**
     * 读取日志文件内容
     */
    suspend fun readLogFile(file: File): String? {
        return try {
            if (file.exists() && file.canRead()) {
                file.readText()
            } else {
                null
            }
        } catch (e: Exception) {
            e("AgentLogger", "Failed to read log file: ${file.name}", e)
            null
        }
    }
    
    /**
     * 搜索日志
     */
    fun searchLogs(query: String, agentId: String? = null, level: LogLevel? = null): List<LogEntry> {
        return memoryLogs.filter { entry ->
            val matchesQuery = entry.message.contains(query, ignoreCase = true)
            val matchesAgent = agentId == null || entry.agentId == agentId
            val matchesLevel = level == null || entry.level == level
            
            matchesQuery && matchesAgent && matchesLevel
        }
    }
    
    /**
     * 获取最近的错误日志
     */
    fun getRecentErrors(count: Int = 10): List<LogEntry> {
        return memoryLogs.filter { it.level == LogLevel.ERROR }
            .sortedByDescending { it.timestamp }
            .take(count)
    }
    
    /**
     * 获取Agent活动统计
     */
    fun getAgentActivity(agentId: String): Map<String, Int> {
        val agentLogs = getAgentLogs(agentId)
        return mapOf(
            "total" to agentLogs.size,
            "errors" to agentLogs.count { it.level == LogLevel.ERROR },
            "warnings" to agentLogs.count { it.level == LogLevel.WARN },
            "info" to agentLogs.count { it.level == LogLevel.INFO },
            "debug" to agentLogs.count { it.level == LogLevel.DEBUG }
        )
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        clearMemoryLogs()
        i("AgentLogger", "AgentLogger cleaned up")
    }
}