package org.autojs.autojs.agent.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * Agent系统工具类
 * Created by SuperMonster003 on 2024/01/15
 */
object AgentUtils {
    private const val TAG = "AgentUtils"

    /**
     * 格式化时间戳
     */
    fun formatTimestamp(timestamp: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    /**
     * 获取相对时间
     */
    fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "刚刚"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前"
            diff < 30 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}天前"
            else -> formatTimestamp(timestamp)
        }
    }

    /**
     * 获取设备信息
     */
    fun getDeviceInfo(): Map<String, String> {
        return mapOf(
            "model" to Build.MODEL,
            "manufacturer" to Build.MANUFACTURER,
            "brand" to Build.BRAND,
            "device" to Build.DEVICE,
            "android_version" to Build.VERSION.RELEASE,
            "sdk_version" to Build.VERSION.SDK_INT.toString(),
            "build_id" to Build.ID
        )
    }

    /**
     * 检查应用是否已安装
     */
    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * 获取应用版本
     */
    fun getAppVersion(context: Context, packageName: String): String? {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    /**
     * 计算字符串相似度
     */
    fun calculateSimilarity(str1: String, str2: String): Float {
        val maxLen = maxOf(str1.length, str2.length)
        if (maxLen == 0) return 1.0f
        
        val distance = levenshteinDistance(str1, str2)
        return (maxLen - distance).toFloat() / maxLen
    }

    /**
     * 计算编辑距离
     */
    private fun levenshteinDistance(str1: String, str2: String): Int {
        val m = str1.length
        val n = str2.length
        val dp = Array(m + 1) { IntArray(n + 1) }
        
        for (i in 0..m) {
            dp[i][0] = i
        }
        
        for (j in 0..n) {
            dp[0][j] = j
        }
        
        for (i in 1..m) {
            for (j in 1..n) {
                if (str1[i - 1] == str2[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1]
                } else {
                    dp[i][j] = 1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }
        
        return dp[m][n]
    }

    /**
     * 生成唯一ID
     */
    fun generateUniqueId(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    /**
     * 验证电子邮件格式
     */
    fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(emailPattern.toRegex())
    }

    /**
     * 格式化文件大小
     */
    fun formatFileSize(size: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var fileSize = size.toDouble()
        var unitIndex = 0
        
        while (fileSize >= 1024 && unitIndex < units.size - 1) {
            fileSize /= 1024
            unitIndex++
        }
        
        return String.format("%.2f %s", fileSize, units[unitIndex])
    }

    /**
     * 检查权限
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 安全地执行代码块
     */
    inline fun <T> safeCall(block: () -> T): T? {
        return try {
            block()
        } catch (e: Exception) {
            Log.e(TAG, "Safe call failed", e)
            null
        }
    }

    /**
     * 延迟执行
     */
    fun delay(milliseconds: Long, action: () -> Unit) {
        Thread {
            Thread.sleep(milliseconds)
            action()
        }.start()
    }

    /**
     * 验证JSON格式
     */
    fun isValidJson(json: String): Boolean {
        return try {
            // 这里应该使用JSON库验证
            // 暂时使用简单的检查
            json.trim().startsWith("{") && json.trim().endsWith("}")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 截断字符串
     */
    fun truncate(text: String, maxLength: Int): String {
        return if (text.length <= maxLength) {
            text
        } else {
            text.substring(0, maxLength - 3) + "..."
        }
    }

    /**
     * 检查网络连接
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
            as android.net.ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            networkCapabilities != null
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }

    /**
     * 格式化持续时间
     */
    fun formatDuration(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days > 0 -> "${days}天${hours % 24}小时"
            hours > 0 -> "${hours}小时${minutes % 60}分钟"
            minutes > 0 -> "${minutes}分钟${seconds % 60}秒"
            else -> "${seconds}秒"
        }
    }

    /**
     * 计算百分比
     */
    fun calculatePercentage(value: Int, total: Int): Float {
        return if (total == 0) 0.0f else (value.toFloat() / total) * 100
    }

    /**
     * 深拷贝Map
     */
    fun <K, V> deepCopyMap(original: Map<K, V>): MutableMap<K, V> {
        return original.toMutableMap()
    }
}