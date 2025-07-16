package org.autojs.autojs.agent.core

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.autojs.autojs.agent.core.model.UserBehaviorData
import org.autojs.autojs.agent.behavior.model.UserProfile
import org.autojs.autojs.agent.execution.model.ExecutionTask
import org.autojs.autojs.agent.util.AgentUtils
import java.io.File
import java.io.FileWriter
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Agent数据管理器 - 管理Agent数据存储和缓存
 * Created by SuperMonster003 on 2024/01/15
 */
class AgentDataManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "AgentDataManager"
        private const val PREFS_NAME = "agent_data"
        private const val USER_PROFILE_KEY = "user_profile"
        private const val EXECUTION_HISTORY_KEY = "execution_history"
        private const val BEHAVIOR_DATA_KEY = "behavior_data"
        
        @Volatile
        private var INSTANCE: AgentDataManager? = null
        
        fun getInstance(context: Context): AgentDataManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AgentDataManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val memoryCache = ConcurrentHashMap<String, Any>()
    private val dataDir = File(context.filesDir, "agent_data")
    
    init {
        if (!dataDir.exists()) {
            dataDir.mkdirs()
        }
    }
    
    /**
     * 保存用户档案
     */
    suspend fun saveUserProfile(profile: UserProfile) {
        withContext(Dispatchers.IO) {
            try {
                val json = serializeUserProfile(profile)
                prefs.edit().putString(USER_PROFILE_KEY, json).apply()
                memoryCache[USER_PROFILE_KEY] = profile
                Log.d(TAG, "User profile saved: ${profile.userId}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save user profile", e)
            }
        }
    }
    
    /**
     * 加载用户档案
     */
    suspend fun loadUserProfile(): UserProfile? {
        return withContext(Dispatchers.IO) {
            try {
                // 先检查内存缓存
                memoryCache[USER_PROFILE_KEY]?.let { 
                    return@withContext it as UserProfile
                }
                
                // 从SharedPreferences加载
                val json = prefs.getString(USER_PROFILE_KEY, null)
                if (json != null) {
                    val profile = deserializeUserProfile(json)
                    memoryCache[USER_PROFILE_KEY] = profile
                    return@withContext profile
                }
                
                null
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load user profile", e)
                null
            }
        }
    }
    
    /**
     * 保存用户行为数据
     */
    suspend fun saveBehaviorData(data: UserBehaviorData) {
        withContext(Dispatchers.IO) {
            try {
                val behaviorList = loadBehaviorDataList().toMutableList()
                behaviorList.add(data)
                
                // 保持最近1000条记录
                if (behaviorList.size > 1000) {
                    behaviorList.removeAt(0)
                }
                
                val json = serializeBehaviorDataList(behaviorList)
                prefs.edit().putString(BEHAVIOR_DATA_KEY, json).apply()
                
                Log.d(TAG, "Behavior data saved: ${data.action}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save behavior data", e)
            }
        }
    }
    
    /**
     * 加载用户行为数据列表
     */
    suspend fun loadBehaviorDataList(): List<UserBehaviorData> {
        return withContext(Dispatchers.IO) {
            try {
                val json = prefs.getString(BEHAVIOR_DATA_KEY, null)
                if (json != null) {
                    deserializeBehaviorDataList(json)
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load behavior data", e)
                emptyList()
            }
        }
    }
    
    /**
     * 保存执行任务
     */
    suspend fun saveExecutionTask(task: ExecutionTask) {
        withContext(Dispatchers.IO) {
            try {
                val taskList = loadExecutionHistory().toMutableList()
                taskList.add(task)
                
                // 保持最近500条记录
                if (taskList.size > 500) {
                    taskList.removeAt(0)
                }
                
                val json = serializeExecutionTaskList(taskList)
                prefs.edit().putString(EXECUTION_HISTORY_KEY, json).apply()
                
                Log.d(TAG, "Execution task saved: ${task.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save execution task", e)
            }
        }
    }
    
    /**
     * 加载执行历史
     */
    suspend fun loadExecutionHistory(): List<ExecutionTask> {
        return withContext(Dispatchers.IO) {
            try {
                val json = prefs.getString(EXECUTION_HISTORY_KEY, null)
                if (json != null) {
                    deserializeExecutionTaskList(json)
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load execution history", e)
                emptyList()
            }
        }
    }
    
    /**
     * 保存文件到数据目录
     */
    suspend fun saveFile(fileName: String, content: String) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(dataDir, fileName)
                FileWriter(file).use { writer ->
                    writer.write(content)
                }
                Log.d(TAG, "File saved: $fileName")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save file: $fileName", e)
            }
        }
    }
    
    /**
     * 从数据目录读取文件
     */
    suspend fun loadFile(fileName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(dataDir, fileName)
                if (file.exists()) {
                    file.readText()
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load file: $fileName", e)
                null
            }
        }
    }
    
    /**
     * 删除文件
     */
    suspend fun deleteFile(fileName: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(dataDir, fileName)
                val deleted = file.delete()
                if (deleted) {
                    Log.d(TAG, "File deleted: $fileName")
                }
                deleted
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete file: $fileName", e)
                false
            }
        }
    }
    
    /**
     * 清空缓存
     */
    fun clearCache() {
        memoryCache.clear()
        Log.d(TAG, "Memory cache cleared")
    }
    
    /**
     * 获取缓存大小
     */
    fun getCacheSize(): Int {
        return memoryCache.size
    }
    
    /**
     * 获取数据目录大小
     */
    suspend fun getDataDirectorySize(): Long {
        return withContext(Dispatchers.IO) {
            calculateDirectorySize(dataDir)
        }
    }
    
    /**
     * 清理过期数据
     */
    suspend fun cleanupExpiredData(maxAge: Long = 30 * 24 * 60 * 60 * 1000L) {
        withContext(Dispatchers.IO) {
            try {
                val currentTime = System.currentTimeMillis()
                
                // 清理过期的行为数据
                val behaviorList = loadBehaviorDataList()
                val filteredBehaviorList = behaviorList.filter { 
                    currentTime - it.timestamp < maxAge 
                }
                
                if (filteredBehaviorList.size < behaviorList.size) {
                    val json = serializeBehaviorDataList(filteredBehaviorList)
                    prefs.edit().putString(BEHAVIOR_DATA_KEY, json).apply()
                    Log.d(TAG, "Cleaned up ${behaviorList.size - filteredBehaviorList.size} expired behavior records")
                }
                
                // 清理过期的执行任务
                val executionList = loadExecutionHistory()
                val filteredExecutionList = executionList.filter { 
                    currentTime - it.startTime < maxAge 
                }
                
                if (filteredExecutionList.size < executionList.size) {
                    val json = serializeExecutionTaskList(filteredExecutionList)
                    prefs.edit().putString(EXECUTION_HISTORY_KEY, json).apply()
                    Log.d(TAG, "Cleaned up ${executionList.size - filteredExecutionList.size} expired execution records")
                }
                
                // 清理过期的文件
                dataDir.listFiles()?.forEach { file ->
                    if (file.isFile && currentTime - file.lastModified() > maxAge) {
                        file.delete()
                        Log.d(TAG, "Deleted expired file: ${file.name}")
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to cleanup expired data", e)
            }
        }
    }
    
    /**
     * 导出数据
     */
    suspend fun exportData(): String {
        return withContext(Dispatchers.IO) {
            try {
                val data = mutableMapOf<String, Any>()
                
                // 导出用户档案
                loadUserProfile()?.let { profile ->
                    data["userProfile"] = profile
                }
                
                // 导出行为数据
                data["behaviorData"] = loadBehaviorDataList()
                
                // 导出执行历史
                data["executionHistory"] = loadExecutionHistory()
                
                // 导出文件列表
                val fileList = dataDir.listFiles()?.map { it.name } ?: emptyList()
                data["files"] = fileList
                
                // 序列化为JSON
                serializeExportData(data)
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to export data", e)
                "{}"
            }
        }
    }
    
    /**
     * 导入数据
     */
    suspend fun importData(jsonData: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val data = deserializeExportData(jsonData)
                
                // 导入用户档案
                data["userProfile"]?.let { profile ->
                    saveUserProfile(profile as UserProfile)
                }
                
                // 导入行为数据
                data["behaviorData"]?.let { behaviorList ->
                    @Suppress("UNCHECKED_CAST")
                    val list = behaviorList as List<UserBehaviorData>
                    val json = serializeBehaviorDataList(list)
                    prefs.edit().putString(BEHAVIOR_DATA_KEY, json).apply()
                }
                
                // 导入执行历史
                data["executionHistory"]?.let { executionList ->
                    @Suppress("UNCHECKED_CAST")
                    val list = executionList as List<ExecutionTask>
                    val json = serializeExecutionTaskList(list)
                    prefs.edit().putString(EXECUTION_HISTORY_KEY, json).apply()
                }
                
                Log.d(TAG, "Data imported successfully")
                true
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to import data", e)
                false
            }
        }
    }
    
    // 私有辅助方法
    private fun calculateDirectorySize(directory: File): Long {
        var size = 0L
        directory.listFiles()?.forEach { file ->
            size += if (file.isDirectory) {
                calculateDirectorySize(file)
            } else {
                file.length()
            }
        }
        return size
    }
    
    private fun serializeUserProfile(profile: UserProfile): String {
        // 这里应该使用实际的JSON序列化库
        return "{\"userId\":\"${profile.userId}\",\"createdAt\":${profile.createdAt}}"
    }
    
    private fun deserializeUserProfile(json: String): UserProfile {
        // 这里应该使用实际的JSON反序列化库
        return UserProfile(
            userId = "user_1",
            createdAt = System.currentTimeMillis()
        )
    }
    
    private fun serializeBehaviorDataList(list: List<UserBehaviorData>): String {
        // 这里应该使用实际的JSON序列化库
        return "[]"
    }
    
    private fun deserializeBehaviorDataList(json: String): List<UserBehaviorData> {
        // 这里应该使用实际的JSON反序列化库
        return emptyList()
    }
    
    private fun serializeExecutionTaskList(list: List<ExecutionTask>): String {
        // 这里应该使用实际的JSON序列化库
        return "[]"
    }
    
    private fun deserializeExecutionTaskList(json: String): List<ExecutionTask> {
        // 这里应该使用实际的JSON反序列化库
        return emptyList()
    }
    
    private fun serializeExportData(data: Map<String, Any>): String {
        // 这里应该使用实际的JSON序列化库
        return "{}"
    }
    
    private fun deserializeExportData(json: String): Map<String, Any> {
        // 这里应该使用实际的JSON反序列化库
        return emptyMap()
    }
}