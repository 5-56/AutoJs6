package com.xihe.automation.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xihe.automation.R
import com.xihe.automation.databinding.ActivityXiheMainBinding
import com.xihe.automation.ui.adapter.ChatMessageAdapter
import com.xihe.automation.ui.settings.SettingsActivity
import com.xihe.automation.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 羲和主界面 - AI聊天交互界面
 */
class XiheMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityXiheMainBinding
    private lateinit var viewModel: ChatViewModel
    private lateinit var chatAdapter: ChatMessageAdapter

    companion object {
        private const val REQUEST_PERMISSIONS = 100
        private const val REQUEST_ACCESSIBILITY = 101
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityXiheMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewModel()
        setupRecyclerView()
        setupInputArea()
        checkPermissions()
        
        // 显示欢迎消息
        viewModel.addWelcomeMessage()
    }
    
    override fun onResume() {
        super.onResume()
        
        // 刷新AI提供商状态显示
        binding.aiProviderStatus.refresh()
        
        // 首次启动显示欢迎引导
        com.xihe.automation.ui.components.WelcomeGuideDialog.showIfNeeded(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "羲和 AI助手"
            subtitle = "由AI驱动的自动化脚本引擎"
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        
        // 观察聊天消息
        viewModel.messages.observe(this) { messages ->
            chatAdapter.updateMessages(messages)
            binding.recyclerViewChat.scrollToPosition(messages.size - 1)
        }
        
        // 观察加载状态
        viewModel.isLoading.observe(this) { isLoading ->
            binding.sendButton.isEnabled = !isLoading
            binding.inputMessage.isEnabled = !isLoading
        }
        
        // 观察错误消息
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatMessageAdapter()
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(this@XiheMainActivity)
            adapter = chatAdapter
        }
    }

    private fun setupInputArea() {
        // 发送按钮点击
        binding.sendButton.setOnClickListener {
            sendMessage()
        }
        
        // 输入框回车发送
        binding.inputMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
        
        // 屏幕截图按钮
        binding.screenshotButton.setOnClickListener {
            captureScreenAndAnalyze()
        }
    }

    private fun sendMessage() {
        val message = binding.inputMessage.text.toString().trim()
        if (message.isEmpty()) {
            Toast.makeText(this, "请输入消息", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.inputMessage.text?.clear()
        
        lifecycleScope.launch {
            viewModel.sendMessage(message)
        }
    }

    private fun captureScreenAndAnalyze() {
        if (!isAccessibilityEnabled()) {
            showAccessibilityDialog()
            return
        }
        
        lifecycleScope.launch {
            viewModel.captureAndAnalyzeScreen()
        }
    }

    private fun checkPermissions() {
        val permissionsToRequest = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS
            )
        }
    }

    private fun isAccessibilityEnabled(): Boolean {
        // 检查无障碍服务是否启用
        val accessibilityEnabled = Settings.Secure.getInt(
            contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED,
            0
        )
        return accessibilityEnabled == 1
    }

    private fun showAccessibilityDialog() {
        AlertDialog.Builder(this)
            .setTitle("需要无障碍服务")
            .setMessage("羲和需要无障碍服务来识别屏幕内容和执行自动化操作。是否前往设置？")
            .setPositiveButton("前往设置") { _, _ ->
                startActivityForResult(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
                    REQUEST_ACCESSIBILITY
                )
            }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_quick_actions -> {
                showQuickActionsDialog()
                true
            }
            R.id.action_script_templates -> {
                showScriptTemplatesDialog()
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_clear_chat -> {
                AlertDialog.Builder(this)
                    .setTitle("清空聊天记录")
                    .setMessage("确定要清空所有聊天记录吗？")
                    .setPositiveButton("确定") { _, _ ->
                        viewModel.clearMessages()
                    }
                    .setNegativeButton("取消", null)
                    .show()
                true
            }
            R.id.action_about -> {
                showAboutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showQuickActionsDialog() {
        com.xihe.automation.ui.components.QuickActionsDialog(this) { promptText ->
            binding.inputMessage.setText(promptText)
            sendMessage()
        }.show()
    }
    
    private fun showScriptTemplatesDialog() {
        com.xihe.automation.ui.components.ScriptTemplatesDialog(this) { template ->
            // 插入模板到输入框
            binding.inputMessage.setText("使用这个脚本模板：\n${template.name}\n\n```javascript\n${template.script}\n```")
            Toast.makeText(this, "模板已插入，你可以修改后发送", Toast.LENGTH_SHORT).show()
        }.show()
    }
    
    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("关于羲和")
            .setMessage("""
                羲和 - AI驱动的自动化脚本引擎
                
                版本: 1.0.0
                
                基于 AutoJs6 项目
                通过AI智能生成和优化自动化脚本
                
                主要功能:
                • 8个AI提供商（多个免费）
                • 实时获取模型列表
                • AI聊天交互
                • 自动脚本生成
                • 屏幕内容识别
                • 智能脚本优化
                • 快速操作和模板
                • 全流程AI接管
            """.trimIndent())
            .setPositiveButton("确定", null)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == REQUEST_PERMISSIONS) {
            val deniedPermissions = permissions.filterIndexed { index, _ ->
                grantResults[index] != PackageManager.PERMISSION_GRANTED
            }
            
            if (deniedPermissions.isNotEmpty()) {
                Toast.makeText(
                    this,
                    "某些权限被拒绝，应用功能可能受限",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
