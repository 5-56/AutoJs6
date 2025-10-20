package com.xihe.automation.ui.components

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import com.xihe.automation.R
import com.xihe.automation.data.ScriptTemplate
import com.xihe.automation.data.ScriptTemplates

/**
 * 脚本模板对话框
 */
class ScriptTemplatesDialog(
    context: Context,
    private val onTemplateSelected: (ScriptTemplate) -> Unit
) : Dialog(context) {
    
    private lateinit var categorySpinner: Spinner
    private lateinit var templatesListView: ListView
    private lateinit var descriptionText: TextView
    
    private var currentCategory = "全部"
    private var allTemplates = ScriptTemplates.getAllTemplates()
    private var filteredTemplates = allTemplates
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_script_templates)
        
        initViews()
        setupCategorySpinner()
        updateTemplatesList()
    }
    
    private fun initViews() {
        categorySpinner = findViewById(R.id.category_spinner)
        templatesListView = findViewById(R.id.templates_list_view)
        descriptionText = findViewById(R.id.description_text)
    }
    
    private fun setupCategorySpinner() {
        val categories = listOf("全部") + ScriptTemplates.getAllCategories()
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
        
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentCategory = categories[position]
                updateTemplatesList()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun updateTemplatesList() {
        filteredTemplates = if (currentCategory == "全部") {
            allTemplates
        } else {
            ScriptTemplates.getTemplatesByCategory(currentCategory)
        }
        
        val templateNames = filteredTemplates.map { it.name }
        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, templateNames)
        templatesListView.adapter = adapter
        
        templatesListView.setOnItemClickListener { _, _, position, _ ->
            val template = filteredTemplates[position]
            onTemplateSelected(template)
            dismiss()
        }
        
        // 显示第一个模板的描述
        if (filteredTemplates.isNotEmpty()) {
            descriptionText.text = filteredTemplates[0].description
        }
        
        // 点击列表项时更新描述
        templatesListView.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                descriptionText.text = filteredTemplates[position].description
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }
}
