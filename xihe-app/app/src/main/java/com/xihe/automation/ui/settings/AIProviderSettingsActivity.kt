package com.xihe.automation.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.xihe.automation.R
import com.xihe.automation.ai.provider.AIProvider
import com.xihe.automation.ai.provider.AIProviderConfig
import com.xihe.automation.ai.provider.ModelFetcher
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * AI提供商设置界面
 */
class AIProviderSettingsActivity : AppCompatActivity() {
    
    private lateinit var providerSpinner: Spinner
    private lateinit var apiKeyInput: TextInputEditText
    private lateinit var baseUrlInput: TextInputEditText
    private lateinit var modelSpinner: Spinner
    private lateinit var fetchModelsButton: Button
    private lateinit var saveButton: Button
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    
    private lateinit var providerConfig: AIProviderConfig
    private lateinit var modelFetcher: ModelFetcher
    
    private var currentProvider: AIProvider = AIProvider.GOOGLE
    private var availableModels = listOf<String>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_provider_settings)
        
        providerConfig = AIProviderConfig.getInstance(this)
        modelFetcher = ModelFetcher()
        
        initViews()
        setupProviderSpinner()
        setupListeners()
        loadCurrentConfig()
    }
    
    private fun initViews() {
        providerSpinner = findViewById(R.id.provider_spinner)
        apiKeyInput = findViewById(R.id.api_key_input)
        baseUrlInput = findViewById(R.id.base_url_input)
        modelSpinner = findViewById(R.id.model_spinner)
        fetchModelsButton = findViewById(R.id.fetch_models_button)
        saveButton = findViewById(R.id.save_button)
        statusText = findViewById(R.id.status_text)
        progressBar = findViewById(R.id.progress_bar)
        
        supportActionBar?.apply {
            title = "AI提供商设置"
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun setupProviderSpinner() {
        val providers = AIProvider.values().map { provider ->
            val freeTag = if (provider.isFree()) " [免费]" else ""
            "${provider.displayName}$freeTag"
        }
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, providers)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        providerSpinner.adapter = adapter
        
        providerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentProvider = AIProvider.values()[position]
                onProviderChanged()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun setupListeners() {
        fetchModelsButton.setOnClickListener {
            fetchModels()
        }
        
        saveButton.setOnClickListener {
            saveConfig()
        }
    }
    
    private fun loadCurrentConfig() {
        currentProvider = providerConfig.getCurrentProvider()
        providerSpinner.setSelection(AIProvider.values().indexOf(currentProvider))
        
        apiKeyInput.setText(providerConfig.getApiKey(currentProvider))
        baseUrlInput.setText(providerConfig.getBaseUrl(currentProvider))
        
        // 加载默认模型
        updateModelList(currentProvider.getDefaultModels())
        
        val savedModel = providerConfig.getSelectedModel(currentProvider)
        if (savedModel.isNotEmpty()) {
            val index = availableModels.indexOf(savedModel)
            if (index >= 0) {
                modelSpinner.setSelection(index)
            }
        }
    }
    
    private fun onProviderChanged() {
        // 更新基础URL
        baseUrlInput.setText(currentProvider.defaultBaseUrl)
        
        // 加载保存的配置
        val savedApiKey = providerConfig.getApiKey(currentProvider)
        val savedBaseUrl = providerConfig.getBaseUrl(currentProvider)
        val savedModel = providerConfig.getSelectedModel(currentProvider)
        
        apiKeyInput.setText(savedApiKey)
        if (savedBaseUrl.isNotEmpty()) {
            baseUrlInput.setText(savedBaseUrl)
        }
        
        // 加载模型列表
        val models = if (savedModel.isNotEmpty()) {
            listOf(savedModel) + currentProvider.getDefaultModels()
        } else {
            currentProvider.getDefaultModels()
        }.distinct()
        
        updateModelList(models)
        
        // 显示提示
        updateStatus("已选择: ${currentProvider.displayName}", false)
    }
    
    private fun fetchModels() {
        val apiKey = apiKeyInput.text.toString().trim()
        val baseUrl = baseUrlInput.text.toString().trim()
        
        if (apiKey.isEmpty()) {
            Toast.makeText(this, "请先输入API密钥", Toast.LENGTH_SHORT).show()
            return
        }
        
        updateStatus("正在获取模型列表...", true)
        fetchModelsButton.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val result = modelFetcher.fetchModels(currentProvider, apiKey, baseUrl)
                
                result.fold(
                    onSuccess = { models ->
                        val modelIds = models.map { it.id }
                        
                        if (modelIds.isEmpty()) {
                            updateStatus("未获取到模型，使用默认列表", false)
                            updateModelList(currentProvider.getDefaultModels())
                        } else {
                            updateStatus("成功获取 ${modelIds.size} 个模型", false)
                            updateModelList(modelIds)
                            
                            // 显示免费模型提示
                            val freeModels = models.filter { it.isFree }
                            if (freeModels.isNotEmpty()) {
                                val message = "发现 ${freeModels.size} 个免费模型:\n" +
                                        freeModels.take(3).joinToString("\n") { "• ${it.name}" }
                                showInfoDialog("免费模型", message)
                            }
                        }
                    },
                    onFailure = { error ->
                        Timber.e(error, "获取模型失败")
                        updateStatus("获取失败: ${error.message}", false)
                        Toast.makeText(
                            this@AIProviderSettingsActivity,
                            "获取失败: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        
                        // 使用默认模型
                        updateModelList(currentProvider.getDefaultModels())
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "获取模型异常")
                updateStatus("获取异常: ${e.message}", false)
                updateModelList(currentProvider.getDefaultModels())
            } finally {
                fetchModelsButton.isEnabled = true
            }
        }
    }
    
    private fun updateModelList(models: List<String>) {
        availableModels = models
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, models)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modelSpinner.adapter = adapter
        
        // 选择之前保存的模型
        val savedModel = providerConfig.getSelectedModel(currentProvider)
        val index = models.indexOf(savedModel)
        if (index >= 0) {
            modelSpinner.setSelection(index)
        }
    }
    
    private fun saveConfig() {
        val apiKey = apiKeyInput.text.toString().trim()
        val baseUrl = baseUrlInput.text.toString().trim()
        val selectedModel = if (modelSpinner.selectedItem != null) {
            modelSpinner.selectedItem.toString()
        } else {
            ""
        }
        
        if (apiKey.isEmpty()) {
            Toast.makeText(this, "请输入API密钥", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedModel.isEmpty()) {
            Toast.makeText(this, "请选择模型", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 保存配置
        providerConfig.setCurrentProvider(currentProvider)
        providerConfig.setApiKey(currentProvider, apiKey)
        providerConfig.setBaseUrl(currentProvider, baseUrl)
        providerConfig.setSelectedModel(currentProvider, selectedModel)
        
        Toast.makeText(
            this,
            "已保存: ${currentProvider.displayName} - $selectedModel",
            Toast.LENGTH_SHORT
        ).show()
        
        updateStatus("✅ 配置已保存", false)
        
        finish()
    }
    
    private fun updateStatus(message: String, showProgress: Boolean) {
        statusText.text = message
        progressBar.visibility = if (showProgress) View.VISIBLE else View.GONE
    }
    
    private fun showInfoDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("确定", null)
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
