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
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import org.autojs.autojs.App
import org.autojs.autojs.agent.AgentManager
import org.autojs.autojs.agent.core.AgentStatus
import org.autojs.autojs6.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Agent控制界面
 * Created by SuperMonster003 on 2024/01/15
 */
class AgentControlActivity : AppCompatActivity() {

    private lateinit var agentManager: AgentManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AgentStatusAdapter
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AgentControlActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agent_control)

        initViews()
        initAgentManager()
        loadAgentStatus()
    }

    private fun initViews() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Agent控制中心"

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        adapter = AgentStatusAdapter { agentId, isEnabled ->
            toggleAgent(agentId, isEnabled)
        }
        recyclerView.adapter = adapter
    }

    private fun initAgentManager() {
        agentManager = (application as App).agentManager
    }

    private fun loadAgentStatus() {
        coroutineScope.launch {
            try {
                val statusMap = withContext(Dispatchers.IO) {
                    agentManager.getAllAgentStatus()
                }
                adapter.updateData(statusMap.values.toList())
            } catch (e: Exception) {
                Toast.makeText(this@AgentControlActivity, "加载Agent状态失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleAgent(agentId: String, isEnabled: Boolean) {
        coroutineScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    if (isEnabled) {
                        agentManager.startAgent(agentId)
                    } else {
                        agentManager.stopAgent(agentId)
                    }
                }
                
                // 延迟一下再刷新状态
                kotlinx.coroutines.delay(500)
                loadAgentStatus()
                
                Toast.makeText(
                    this@AgentControlActivity,
                    if (isEnabled) "Agent已启动" else "Agent已停止",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(this@AgentControlActivity, "操作失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
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

    override fun onResume() {
        super.onResume()
        loadAgentStatus()
    }
}

/**
 * Agent状态适配器
 */
class AgentStatusAdapter(
    private val onToggleListener: (String, Boolean) -> Unit
) : RecyclerView.Adapter<AgentStatusAdapter.ViewHolder>() {

    private var agentStatusList = mutableListOf<AgentStatus>()

    fun updateData(newList: List<AgentStatus>) {
        agentStatusList.clear()
        agentStatusList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_agent_status, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val status = agentStatusList[position]
        holder.bind(status, onToggleListener)
    }

    override fun getItemCount(): Int = agentStatusList.size

    class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.card_view)
        private val nameText: android.widget.TextView = itemView.findViewById(R.id.tv_agent_name)
        private val descriptionText: android.widget.TextView = itemView.findViewById(R.id.tv_agent_description)
        private val statusText: android.widget.TextView = itemView.findViewById(R.id.tv_agent_status)
        private val enableSwitch: SwitchMaterial = itemView.findViewById(R.id.switch_enable)

        fun bind(status: AgentStatus, onToggleListener: (String, Boolean) -> Unit) {
            nameText.text = status.name
            descriptionText.text = status.description
            statusText.text = if (status.isRunning) "运行中" else "已停止"
            statusText.setTextColor(
                if (status.isRunning) 
                    itemView.context.getColor(android.R.color.holo_green_dark)
                else 
                    itemView.context.getColor(android.R.color.darker_gray)
            )
            
            enableSwitch.isChecked = status.isRunning
            enableSwitch.setOnCheckedChangeListener { _, isChecked ->
                onToggleListener(status.agentId, isChecked)
            }
        }
    }
}