package com.xihe.automation.ui.components

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.ListView
import com.xihe.automation.R

/**
 * 快速操作对话框
 * 提供常用的自动化操作示例
 */
class QuickActionsDialog(
    context: Context,
    private val onActionSelected: (String) -> Unit
) : Dialog(context) {
    
    companion object {
        val QUICK_ACTIONS = listOf(
            QuickAction("点击确定按钮", "帮我点击确定按钮"),
            QuickAction("点击取消按钮", "帮我点击取消按钮"),
            QuickAction("向上滑动", "帮我向上滑动屏幕"),
            QuickAction("向下滑动", "帮我向下滑动屏幕"),
            QuickAction("返回上一页", "帮我点击返回按钮"),
            QuickAction("自动签到", "帮我自动签到"),
            QuickAction("填写表单", "帮我填写登录表单"),
            QuickAction("截图分析", "分析当前屏幕"),
            QuickAction("连续点击", "帮我连续点击5次"),
            QuickAction("等待元素出现", "等待登录按钮出现")
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_quick_actions)
        
        setupListView()
    }
    
    private fun setupListView() {
        val listView = findViewById<ListView>(R.id.actions_list_view)
        
        val items = QUICK_ACTIONS.map { it.displayName }
        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter
        
        listView.setOnItemClickListener { _, _, position, _ ->
            val action = QUICK_ACTIONS[position]
            onActionSelected(action.promptText)
            dismiss()
        }
    }
}

/**
 * 快速操作
 */
data class QuickAction(
    val displayName: String,
    val promptText: String
)
