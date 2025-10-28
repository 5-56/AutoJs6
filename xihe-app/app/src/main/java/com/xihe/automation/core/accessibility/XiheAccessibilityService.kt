package com.xihe.automation.core.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import timber.log.Timber

/**
 * 羲和无障碍服务
 * 用于获取屏幕UI元素信息和执行自动化操作
 */
class XiheAccessibilityService : AccessibilityService() {

    companion object {
        private var instance: XiheAccessibilityService? = null
        
        fun getInstance(): XiheAccessibilityService? = instance
        
        fun isServiceEnabled(): Boolean = instance != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Timber.i("羲和无障碍服务已连接")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 处理无障碍事件（如果需要）
    }

    override fun onInterrupt() {
        Timber.w("羲和无障碍服务被中断")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Timber.i("羲和无障碍服务已销毁")
    }

    /**
     * 获取当前窗口的根节点
     */
    fun getRootNode(): AccessibilityNodeInfo? {
        return rootInActiveWindow
    }

    /**
     * 查找包含指定文本的节点
     */
    fun findNodeByText(text: String): AccessibilityNodeInfo? {
        val rootNode = getRootNode() ?: return null
        val nodes = rootNode.findAccessibilityNodeInfosByText(text)
        return nodes.firstOrNull()
    }

    /**
     * 查找指定ID的节点
     */
    fun findNodeById(id: String): AccessibilityNodeInfo? {
        val rootNode = getRootNode() ?: return null
        val nodes = rootNode.findAccessibilityNodeInfosByViewId(id)
        return nodes.firstOrNull()
    }

    /**
     * 点击节点
     */
    fun clickNode(node: AccessibilityNodeInfo): Boolean {
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    /**
     * 点击屏幕坐标
     */
    fun clickAt(x: Int, y: Int): Boolean {
        val path = Path().apply {
            moveTo(x.toFloat(), y.toFloat())
        }
        
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()
        
        return dispatchGesture(gesture, null, null)
    }

    /**
     * 滑动
     */
    fun swipe(startX: Int, startY: Int, endX: Int, endY: Int, duration: Long = 500): Boolean {
        val path = Path().apply {
            moveTo(startX.toFloat(), startY.toFloat())
            lineTo(endX.toFloat(), endY.toFloat())
        }
        
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()
        
        return dispatchGesture(gesture, null, null)
    }

    /**
     * 输入文本
     */
    fun setText(node: AccessibilityNodeInfo, text: String): Boolean {
        val arguments = android.os.Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    /**
     * 递归遍历节点树
     */
    fun traverseNodes(node: AccessibilityNodeInfo?, action: (AccessibilityNodeInfo) -> Unit) {
        node?.let {
            action(it)
            for (i in 0 until it.childCount) {
                traverseNodes(it.getChild(i), action)
            }
        }
    }
}
