package com.xihe.automation.data

/**
 * 脚本模板库
 * 提供常用的AutoJs6脚本模板
 */
object ScriptTemplates {
    
    /**
     * 获取所有模板
     */
    fun getAllTemplates(): List<ScriptTemplate> {
        return listOf(
            // 基础操作
            ScriptTemplate(
                name = "点击坐标",
                category = "基础操作",
                description = "点击指定坐标位置",
                script = """
                    auto();
                    sleep(1000);
                    
                    // 点击坐标 (x, y)
                    click(500, 1000);
                    toast("已点击");
                """.trimIndent()
            ),
            
            ScriptTemplate(
                name = "查找并点击文本",
                category = "基础操作",
                description = "查找包含指定文本的控件并点击",
                script = """
                    auto();
                    sleep(1000);
                    
                    // 查找文本"确定"的控件
                    var btn = text("确定").findOne(5000);
                    if (btn) {
                        btn.click();
                        toast("点击成功");
                    } else {
                        toast("未找到控件");
                    }
                """.trimIndent()
            ),
            
            ScriptTemplate(
                name = "滑动屏幕",
                category = "基础操作",
                description = "向上/向下滑动屏幕",
                script = """
                    auto();
                    sleep(1000);
                    
                    var width = device.width;
                    var height = device.height;
                    
                    // 向上滑动
                    swipe(width / 2, height * 0.8, width / 2, height * 0.2, 500);
                    toast("滑动完成");
                """.trimIndent()
            ),
            
            // 输入操作
            ScriptTemplate(
                name = "自动填写表单",
                category = "输入操作",
                description = "自动填写输入框",
                script = """
                    auto();
                    sleep(2000);
                    
                    // 填写第一个输入框
                    var input1 = className("EditText").findOne(5000);
                    if (input1) {
                        input1.setText("用户名");
                        sleep(500);
                    }
                    
                    // 填写第二个输入框（密码）
                    var inputs = className("EditText").find();
                    if (inputs.length >= 2) {
                        inputs[1].setText("密码");
                        sleep(500);
                    }
                    
                    // 点击登录按钮
                    var loginBtn = text("登录").findOne(3000);
                    if (loginBtn) {
                        loginBtn.click();
                        toast("表单已提交");
                    }
                """.trimIndent()
            ),
            
            // 循环操作
            ScriptTemplate(
                name = "循环点击",
                category = "循环操作",
                description = "循环点击某个元素多次",
                script = """
                    auto();
                    sleep(1000);
                    
                    var count = 5; // 点击次数
                    
                    for (var i = 0; i < count; i++) {
                        toast("第 " + (i + 1) + " 次点击");
                        
                        var target = text("目标").findOne(3000);
                        if (target) {
                            target.click();
                            sleep(2000); // 每次点击后等待2秒
                        } else {
                            toast("未找到目标");
                            break;
                        }
                    }
                    
                    toast("完成");
                """.trimIndent()
            ),
            
            ScriptTemplate(
                name = "滚动列表",
                category = "循环操作",
                description = "滚动查找目标元素",
                script = """
                    auto();
                    sleep(1000);
                    
                    var maxScroll = 10; // 最多滚动次数
                    var found = false;
                    
                    for (var i = 0; i < maxScroll; i++) {
                        // 查找目标
                        var target = text("目标文本").findOne(1000);
                        if (target) {
                            target.click();
                            toast("找到并点击");
                            found = true;
                            break;
                        }
                        
                        // 向上滑动
                        var width = device.width;
                        var height = device.height;
                        swipe(width / 2, height * 0.7, width / 2, height * 0.3, 500);
                        sleep(1000);
                    }
                    
                    if (!found) {
                        toast("未找到目标");
                    }
                """.trimIndent()
            ),
            
            // 高级操作
            ScriptTemplate(
                name = "等待元素出现",
                category = "高级操作",
                description = "等待某个元素出现后执行操作",
                script = """
                    auto();
                    toast("开始等待...");
                    
                    // 等待元素出现（最多等待10秒）
                    var element = text("目标").findOne(10000);
                    
                    if (element) {
                        toast("元素已出现");
                        element.click();
                        sleep(1000);
                    } else {
                        toast("等待超时");
                    }
                """.trimIndent()
            ),
            
            ScriptTemplate(
                name = "条件判断",
                category = "高级操作",
                description = "根据屏幕内容执行不同操作",
                script = """
                    auto();
                    sleep(1000);
                    
                    // 检查是否有"确定"按钮
                    var confirmBtn = text("确定").findOne(2000);
                    var cancelBtn = text("取消").findOne(2000);
                    
                    if (confirmBtn) {
                        confirmBtn.click();
                        toast("已点击确定");
                    } else if (cancelBtn) {
                        cancelBtn.click();
                        toast("已点击取消");
                    } else {
                        toast("没有找到按钮");
                    }
                """.trimIndent()
            ),
            
            // 实用脚本
            ScriptTemplate(
                name = "自动签到",
                category = "实用脚本",
                description = "自动点击签到按钮",
                script = """
                    auto();
                    sleep(2000);
                    
                    // 查找签到按钮（多种方式）
                    var signBtn = text("签到").findOne(3000);
                    if (!signBtn) {
                        signBtn = textContains("签到").findOne(2000);
                    }
                    if (!signBtn) {
                        signBtn = desc("签到").findOne(2000);
                    }
                    
                    if (signBtn) {
                        signBtn.click();
                        toast("签到成功");
                        sleep(1000);
                    } else {
                        toast("未找到签到按钮");
                    }
                """.trimIndent()
            ),
            
            ScriptTemplate(
                name = "连续操作",
                category = "实用脚本",
                description = "执行一系列连续操作",
                script = """
                    auto();
                    toast("开始执行连续操作");
                    sleep(2000);
                    
                    // 步骤1: 点击第一个按钮
                    var btn1 = text("按钮1").findOne(3000);
                    if (btn1) {
                        btn1.click();
                        toast("步骤1完成");
                        sleep(2000);
                    }
                    
                    // 步骤2: 输入文本
                    var input = className("EditText").findOne(3000);
                    if (input) {
                        input.setText("测试内容");
                        toast("步骤2完成");
                        sleep(1000);
                    }
                    
                    // 步骤3: 点击提交
                    var submitBtn = text("提交").findOne(3000);
                    if (submitBtn) {
                        submitBtn.click();
                        toast("步骤3完成");
                    }
                    
                    toast("全部完成");
                """.trimIndent()
            )
        )
    }
    
    /**
     * 按分类获取模板
     */
    fun getTemplatesByCategory(category: String): List<ScriptTemplate> {
        return getAllTemplates().filter { it.category == category }
    }
    
    /**
     * 获取所有分类
     */
    fun getAllCategories(): List<String> {
        return getAllTemplates().map { it.category }.distinct()
    }
}

/**
 * 脚本模板
 */
data class ScriptTemplate(
    val name: String,
    val category: String,
    val description: String,
    val script: String
)
