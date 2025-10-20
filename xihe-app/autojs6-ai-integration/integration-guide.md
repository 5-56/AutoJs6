# AutoJs6 AI模块集成指南

## 📋 集成步骤

### 前提条件

1. ✅ 已有AutoJs6项目（`/workspace/`）
2. ✅ Android Studio已安装
3. ✅ 已获取AI API密钥

---

## 🚀 方式一：自动集成（推荐）

### 步骤1：运行集成脚本

```bash
cd /workspace/xihe-app/autojs6-ai-integration
chmod +x integrate.sh
./integrate.sh
```

脚本会自动：
- 复制AI模块代码到AutoJs6项目
- 添加必要的依赖
- 创建配置文件模板
- 添加UI布局文件

### 步骤2：配置API密钥

编辑 `/workspace/local.properties`，添加：

```properties
ai.api.key=YOUR_API_KEY_HERE
ai.api.url=https://api.openai.com/v1/chat/completions
```

### 步骤3：在主界面添加AI助手入口

编辑 `app/src/main/java/org/autojs/autojs/ui/main/MainActivity.kt`，添加：

```kotlin
// 在onCreate方法中添加
private fun setupAIAssistant() {
    // 添加浮动按钮或菜单项
    binding.fabAI.setOnClickListener {
        startActivity(Intent(this, AIChatActivity::class.java))
    }
}
```

### 步骤4：编译运行

```bash
cd /workspace
./gradlew assembleDebug
```

---

## 🔧 方式二：手动集成

### 步骤1：复制源代码

```bash
# AI模块
cp -r autojs6-ai-integration/src/ai \
   /workspace/app/src/main/java/org/autojs/autojs/

# UI模块  
cp -r autojs6-ai-integration/src/ui/chat \
   /workspace/app/src/main/java/org/autojs/autojs/ui/

# 数据模型
cp -r autojs6-ai-integration/src/data \
   /workspace/app/src/main/java/org/autojs/autojs/
```

### 步骤2：复制资源文件

```bash
# 布局文件
cp autojs6-ai-integration/res/layout/* \
   /workspace/app/src/main/res/layout/

# 图标
cp autojs6-ai-integration/res/drawable/* \
   /workspace/app/src/main/res/drawable/

# 字符串
cp autojs6-ai-integration/res/values/strings_ai.xml \
   /workspace/app/src/main/res/values/
```

### 步骤3：添加依赖

编辑 `/workspace/app/build.gradle.kts`，在dependencies块中添加：

```kotlin
dependencies {
    // AI相关依赖
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // 协程（AutoJs6可能已有）
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // 日志（AutoJs6可能已有）
    implementation("com.jakewharton.timber:timber:5.0.1")
    
    // 已有的AutoJs6依赖保持不变
    // ...
}
```

### 步骤4：配置BuildConfig

编辑 `/workspace/app/build.gradle.kts`，在defaultConfig块中添加：

```kotlin
android {
    defaultConfig {
        // 读取AI API配置
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }
        
        buildConfigField("String", "AI_API_KEY", 
            "\"${properties.getProperty("ai.api.key", "")}\"")
        buildConfigField("String", "AI_API_URL", 
            "\"${properties.getProperty("ai.api.url", "")}\"")
    }
}
```

### 步骤5：添加权限（如果还没有）

编辑 `/workspace/app/src/main/AndroidManifest.xml`，确保有以下权限：

```xml
<!-- AI功能需要的权限 -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 步骤6：注册Activity

在AndroidManifest.xml中添加AI聊天Activity：

```xml
<activity
    android:name="org.autojs.autojs.ui.chat.AIChatActivity"
    android:theme="@style/AppTheme.FullScreen"
    android:exported="false" />
```

### 步骤7：创建local.properties

```bash
cd /workspace
cat > local.properties << 'EOF'
# 已有的SDK路径保持不变
sdk.dir=/path/to/android/sdk

# AI API配置
ai.api.key=YOUR_API_KEY
ai.api.url=https://api.openai.com/v1/chat/completions
EOF
```

### 步骤8：在主界面添加入口

有几种方式添加AI助手入口：

#### 方式A：添加浮动按钮

编辑 `app/src/main/res/layout/activity_main.xml`：

```xml
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:id="@+id/fab_ai_assistant"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_gravity="bottom|end"
    android:layout_margin="16dp"
    android:src="@drawable/ic_ai"
    android:contentDescription="AI助手" />
```

编辑 `MainActivity.kt`：

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 添加AI助手按钮点击事件
        findViewById<FloatingActionButton>(R.id.fab_ai_assistant)
            .setOnClickListener {
                startActivity(Intent(this, AIChatActivity::class.java))
            }
    }
}
```

#### 方式B：添加菜单项

创建或编辑 `app/src/main/res/menu/menu_main.xml`：

```xml
<item
    android:id="@+id/action_ai_assistant"
    android:title="AI助手"
    android:icon="@drawable/ic_ai"
    app:showAsAction="ifRoom" />
```

在MainActivity中处理：

```kotlin
override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
        R.id.action_ai_assistant -> {
            startActivity(Intent(this, AIChatActivity::class.java))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
```

---

## ✅ 验证集成

### 1. 编译项目

```bash
cd /workspace
./gradlew clean
./gradlew assembleDebug
```

### 2. 运行测试

在代码中测试AI功能：

```kotlin
// 测试AI助手
val runtime = AutoJs.getInstance().runtime
val aiAssistant = AIAssistant(runtime)

lifecycleScope.launch {
    // 测试屏幕分析
    val screenInfo = aiAssistant.analyzeScreen()
    Log.d("AI", "分析到 ${screenInfo.elements.size} 个元素")
    
    // 测试脚本生成
    val script = aiAssistant.generateScript("点击确定按钮")
    Log.d("AI", "生成的脚本:\n$script")
}
```

### 3. 检查日志

运行应用后，在Logcat中查看：
- `AI Assistant` - AI助手日志
- `ScreenAnalyzer` - 屏幕分析日志
- `ScriptGenerator` - 脚本生成日志

---

## 🎯 使用示例

### 示例1：通过对话生成并执行脚本

```kotlin
val aiAssistant = AIAssistant(AutoJs.getInstance().runtime)

lifecycleScope.launch {
    val result = aiAssistant.executeUserRequest("帮我点击屏幕上的确定按钮")
    
    if (result.isSuccess) {
        Toast.makeText(this@MainActivity, "执行成功", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(this@MainActivity, "执行失败: ${result.error}", 
            Toast.LENGTH_LONG).show()
    }
}
```

### 示例2：只生成脚本不执行

```kotlin
lifecycleScope.launch {
    val script = aiAssistant.generateScript("写一个自动签到的脚本")
    
    // 显示生成的脚本
    AlertDialog.Builder(this@MainActivity)
        .setTitle("生成的脚本")
        .setMessage(script)
        .setPositiveButton("执行") { _, _ ->
            // 手动执行脚本
            AutoJs.getInstance().scriptEngineService.execute(script, "AI Generated")
        }
        .setNegativeButton("取消", null)
        .show()
}
```

### 示例3：分析当前屏幕

```kotlin
lifecycleScope.launch {
    val screenInfo = aiAssistant.analyzeScreen()
    
    // 显示分析结果
    val message = buildString {
        appendLine("发现 ${screenInfo.elements.size} 个元素")
        appendLine("\n可点击元素:")
        screenInfo.elements.filter { it.isClickable }.forEach {
            appendLine("• ${it.text} at (${it.centerX}, ${it.centerY})")
        }
        appendLine("\n识别的文字:")
        screenInfo.texts.forEach {
            appendLine("• $it")
        }
    }
    
    AlertDialog.Builder(this@MainActivity)
        .setTitle("屏幕分析")
        .setMessage(message)
        .setPositiveButton("确定", null)
        .show()
}
```

---

## 🐛 常见问题

### Q1: 编译失败 - 找不到BuildConfig
**解决**: 确保在build.gradle.kts中启用了buildConfig:
```kotlin
android {
    buildFeatures {
        buildConfig = true
    }
}
```

### Q2: AI不回复
**检查**:
- [ ] local.properties中的API密钥是否正确
- [ ] 设备是否联网
- [ ] Logcat中是否有网络错误

### Q3: 屏幕分析返回空数据
**检查**:
- [ ] 无障碍服务是否已启用
- [ ] 是否授予了截图权限
- [ ] 查看Logcat中的错误信息

### Q4: 脚本执行失败
**检查**:
- [ ] 生成的脚本语法是否正确
- [ ] 是否有必要的权限
- [ ] AutoJs6的脚本引擎是否正常工作

---

## 📞 获取帮助

1. 查看AutoJs6文档: https://docs.autojs6.com
2. 查看本项目的示例代码
3. 检查Logcat日志

---

## 🎉 完成！

集成完成后，AutoJs6将拥有强大的AI增强功能！

现在你可以：
- ✅ 通过自然语言生成脚本
- ✅ 自动分析屏幕内容
- ✅ 智能优化和执行脚本
- ✅ 使用所有AutoJs6的原生功能
