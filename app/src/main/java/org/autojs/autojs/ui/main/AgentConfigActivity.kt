package org.autojs.autojs.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.autojs.autojs.App
import org.autojs.autojs.agent.AgentManager
import org.autojs.autojs.agent.core.AgentConfig
import org.autojs.autojs.agent.core.AgentLogger
import org.autojs.autojs6.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Agent配置界面
 * Created by SuperMonster003 on 2024/01/15
 */
class AgentConfigActivity : AppCompatActivity() {

    private lateinit var agentManager: AgentManager
    private lateinit var agentConfig: AgentConfig
    private lateinit var agentLogger: AgentLogger
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ConfigItemAdapter
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AgentConfigActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agent_config)

        initViews()
        initAgentSystem()
        loadConfigItems()
    }

    private fun initViews() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Agent系统配置"

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        adapter = ConfigItemAdapter { item ->
            handleConfigItemClick(item)
        }
        recyclerView.adapter = adapter
    }

    private fun initAgentSystem() {
        val app = application as App
        agentManager = app.agentManager
        agentConfig = AgentConfig(this)
        agentLogger = AgentLogger.getInstance(this)
    }

    private fun loadConfigItems() {
        val configItems = mutableListOf<ConfigItem>()
        
        // Agent启用/禁用配置
        configItems.add(
            ConfigItem(
                id = "script_generation_enabled",
                title = "脚本生成Agent",
                description = "启用智能脚本生成功能",
                type = ConfigItem.Type.SWITCH,
                value = agentConfig.isScriptGenerationEnabled
            )
        )
        
        configItems.add(
            ConfigItem(
                id = "execution_monitor_enabled",
                title = "执行监控Agent",
                description = "启用智能执行监控功能",
                type = ConfigItem.Type.SWITCH,
                value = agentConfig.isExecutionMonitorEnabled
            )
        )
        
        configItems.add(
            ConfigItem(
                id = "behavior_learning_enabled",
                title = "行为学习Agent",
                description = "启用用户行为学习功能",
                type = ConfigItem.Type.SWITCH,
                value = agentConfig.isBehaviorLearningEnabled
            )
        )
        
        configItems.add(
            ConfigItem(
                id = "ui_adaptation_enabled",
                title = "UI适配Agent",
                description = "启用智能UI适配功能",
                type = ConfigItem.Type.SWITCH,
                value = agentConfig.isUIAdaptationEnabled
            )
        )
        
        configItems.add(
            ConfigItem(
                id = "dialog_agent_enabled",
                title = "对话交互Agent",
                description = "启用对话式交互功能",
                type = ConfigItem.Type.SWITCH,
                value = agentConfig.isDialogAgentEnabled
            )
        )
        
        // 日志配置
        configItems.add(
            ConfigItem(
                id = "log_level",
                title = "日志级别",
                description = "设置日志记录级别",
                type = ConfigItem.Type.SELECT,
                value = "DEBUG",
                options = listOf("VERBOSE", "DEBUG", "INFO", "WARN", "ERROR")
            )
        )
        
        configItems.add(
            ConfigItem(
                id = "file_logging_enabled",
                title = "文件日志",
                description = "启用文件日志记录",
                type = ConfigItem.Type.SWITCH,
                value = true
            )
        )
        
        // 性能配置
        configItems.add(
            ConfigItem(
                id = "max_concurrent_tasks",
                title = "最大并发任务数",
                description = "设置Agent系统的最大并发任务数",
                type = ConfigItem.Type.NUMBER,
                value = 5
            )
        )
        
        configItems.add(
            ConfigItem(
                id = "memory_cache_size",
                title = "内存缓存大小",
                description = "设置内存缓存的最大条目数",
                type = ConfigItem.Type.NUMBER,
                value = 1000
            )
        )
        
        // 高级配置
        configItems.add(
            ConfigItem(
                id = "debug_mode",
                title = "调试模式",
                description = "启用调试模式，输出更详细的日志",
                type = ConfigItem.Type.SWITCH,
                value = false
            )
        )
        
        configItems.add(
            ConfigItem(
                id = "auto_cleanup",
                title = "自动清理",
                description = "自动清理过期数据和日志",
                type = ConfigItem.Type.SWITCH,
                value = true
            )
        )
        
        // 分组配置项
        val groupedItems = groupConfigItems(configItems)
        adapter.updateData(groupedItems)
    }

    private fun groupConfigItems(items: List<ConfigItem>): List<ConfigSection> {
        val sections = mutableListOf<ConfigSection>()
        
        val agentItems = items.filter { it.id.contains("_enabled") }
        if (agentItems.isNotEmpty()) {
            sections.add(ConfigSection("Agent配置", agentItems))
        }
        
        val logItems = items.filter { it.id.contains("log") }
        if (logItems.isNotEmpty()) {
            sections.add(ConfigSection("日志配置", logItems))
        }
        
        val performanceItems = items.filter { 
            it.id.contains("concurrent") || it.id.contains("cache") 
        }
        if (performanceItems.isNotEmpty()) {
            sections.add(ConfigSection("性能配置", performanceItems))
        }
        
        val advancedItems = items.filter { 
            it.id.contains("debug") || it.id.contains("cleanup") 
        }
        if (advancedItems.isNotEmpty()) {
            sections.add(ConfigSection("高级配置", advancedItems))
        }
        
        return sections
    }

    private fun handleConfigItemClick(item: ConfigItem) {
        when (item.type) {
            ConfigItem.Type.SWITCH -> {
                val newValue = !(item.value as Boolean)
                updateConfigValue(item.id, newValue)
            }
            ConfigItem.Type.SELECT -> {
                showSelectDialog(item)
            }
            ConfigItem.Type.NUMBER -> {
                showNumberInputDialog(item)
            }
            ConfigItem.Type.TEXT -> {
                showTextInputDialog(item)
            }
        }
    }

    private fun updateConfigValue(id: String, value: Any) {
        coroutineScope.launch {
            try {
                when (id) {
                    "script_generation_enabled" -> {
                        agentConfig.isScriptGenerationEnabled = value as Boolean
                        if (value) {
                            agentManager.startAgent("script_generation_agent")
                        } else {
                            agentManager.stopAgent("script_generation_agent")
                        }
                    }
                    "execution_monitor_enabled" -> {
                        agentConfig.isExecutionMonitorEnabled = value as Boolean
                        if (value) {
                            agentManager.startAgent("execution_monitor_agent")
                        } else {
                            agentManager.stopAgent("execution_monitor_agent")
                        }
                    }
                    "behavior_learning_enabled" -> {
                        agentConfig.isBehaviorLearningEnabled = value as Boolean
                        if (value) {
                            agentManager.startAgent("behavior_learning_agent")
                        } else {
                            agentManager.stopAgent("behavior_learning_agent")
                        }
                    }
                    "ui_adaptation_enabled" -> {
                        agentConfig.isUIAdaptationEnabled = value as Boolean
                        if (value) {
                            agentManager.startAgent("ui_adaptation_agent")
                        } else {
                            agentManager.stopAgent("ui_adaptation_agent")
                        }
                    }
                    "dialog_agent_enabled" -> {
                        agentConfig.isDialogAgentEnabled = value as Boolean
                        if (value) {
                            agentManager.startAgent("dialog_agent")
                        } else {
                            agentManager.stopAgent("dialog_agent")
                        }
                    }
                    "log_level" -> {
                        val level = when (value as String) {
                            "VERBOSE" -> AgentLogger.LogLevel.VERBOSE
                            "DEBUG" -> AgentLogger.LogLevel.DEBUG
                            "INFO" -> AgentLogger.LogLevel.INFO
                            "WARN" -> AgentLogger.LogLevel.WARN
                            "ERROR" -> AgentLogger.LogLevel.ERROR
                            else -> AgentLogger.LogLevel.DEBUG
                        }
                        agentLogger.setLogLevel(level)
                    }
                    "file_logging_enabled" -> {
                        agentLogger.setFileLoggingEnabled(value as Boolean)
                    }
                    "debug_mode" -> {
                        // 处理调试模式
                        if (value as Boolean) {
                            agentLogger.setLogLevel(AgentLogger.LogLevel.DEBUG)
                        }
                    }
                }
                
                // 刷新配置项显示
                loadConfigItems()
                
                Toast.makeText(this@AgentConfigActivity, "配置已更新", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Toast.makeText(this@AgentConfigActivity, "配置更新失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSelectDialog(item: ConfigItem) {
        // 这里应该显示选择对话框
        // 简化实现，直接设置为第一个选项
        val options = item.options ?: return
        if (options.isNotEmpty()) {
            updateConfigValue(item.id, options[0])
        }
    }

    private fun showNumberInputDialog(item: ConfigItem) {
        // 这里应该显示数字输入对话框
        // 简化实现，使用默认值
        updateConfigValue(item.id, item.value)
    }

    private fun showTextInputDialog(item: ConfigItem) {
        // 这里应该显示文本输入对话框
        // 简化实现，使用默认值
        updateConfigValue(item.id, item.value)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 配置项数据类
     */
    data class ConfigItem(
        val id: String,
        val title: String,
        val description: String,
        val type: Type,
        val value: Any,
        val options: List<String>? = null
    ) {
        enum class Type {
            SWITCH, SELECT, NUMBER, TEXT
        }
    }

    /**
     * 配置分组
     */
    data class ConfigSection(
        val title: String,
        val items: List<ConfigItem>
    )
}

/**
 * 配置项适配器
 */
class ConfigItemAdapter(
    private val onItemClick: (ConfigItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val sections = mutableListOf<ConfigSection>()
    private val items = mutableListOf<Any>() // 包含section和item

    companion object {
        private const val TYPE_SECTION = 0
        private const val TYPE_ITEM = 1
    }

    fun updateData(newSections: List<ConfigSection>) {
        sections.clear()
        sections.addAll(newSections)
        
        items.clear()
        sections.forEach { section ->
            items.add(section)
            items.addAll(section.items)
        }
        
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ConfigSection -> TYPE_SECTION
            is ConfigItem -> TYPE_ITEM
            else -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = android.view.LayoutInflater.from(parent.context)
        
        return when (viewType) {
            TYPE_SECTION -> {
                val view = inflater.inflate(R.layout.item_config_section, parent, false)
                SectionViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_config_item, parent, false)
                ItemViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SectionViewHolder -> {
                val section = items[position] as ConfigSection
                holder.bind(section)
            }
            is ItemViewHolder -> {
                val item = items[position] as ConfigItem
                holder.bind(item, onItemClick)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class SectionViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: android.widget.TextView = itemView.findViewById(R.id.tv_section_title)

        fun bind(section: ConfigSection) {
            titleText.text = section.title
        }
    }

    class ItemViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: android.widget.TextView = itemView.findViewById(R.id.tv_title)
        private val descriptionText: android.widget.TextView = itemView.findViewById(R.id.tv_description)
        private val valueText: android.widget.TextView = itemView.findViewById(R.id.tv_value)
        private val switch: SwitchMaterial = itemView.findViewById(R.id.switch_value)

        fun bind(item: ConfigItem, onItemClick: (ConfigItem) -> Unit) {
            titleText.text = item.title
            descriptionText.text = item.description
            
            when (item.type) {
                ConfigItem.Type.SWITCH -> {
                    switch.visibility = android.view.View.VISIBLE
                    valueText.visibility = android.view.View.GONE
                    
                    switch.isChecked = item.value as Boolean
                    switch.setOnCheckedChangeListener { _, isChecked ->
                        onItemClick(item.copy(value = isChecked))
                    }
                }
                else -> {
                    switch.visibility = android.view.View.GONE
                    valueText.visibility = android.view.View.VISIBLE
                    valueText.text = item.value.toString()
                    
                    itemView.setOnClickListener {
                        onItemClick(item)
                    }
                }
            }
        }
    }
}

/**
 * 配置分组类型别名
 */
private typealias ConfigSection = AgentConfigActivity.ConfigSection