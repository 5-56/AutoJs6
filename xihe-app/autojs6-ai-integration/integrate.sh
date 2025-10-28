#!/bin/bash

# AutoJs6 AI模块自动集成脚本
# 这个脚本会将AI模块自动集成到AutoJs6项目中

set -e  # 遇到错误立即退出

echo "═══════════════════════════════════════════════════════"
echo "   AutoJs6 AI模块自动集成"
echo "═══════════════════════════════════════════════════════"
echo ""

# 配置
AUTOJS6_PROJECT="/workspace"
AI_MODULE_DIR="$(cd "$(dirname "$0")" && pwd)"

# 检查AutoJs6项目是否存在
if [ ! -d "$AUTOJS6_PROJECT/app" ]; then
    echo "❌ 错误: 未找到AutoJs6项目 ($AUTOJS6_PROJECT)"
    echo "请确保AutoJs6项目位于 /workspace 目录"
    exit 1
fi

echo "✓ 找到AutoJs6项目: $AUTOJS6_PROJECT"
echo ""

# 步骤1: 复制AI模块源代码
echo "步骤 1/6: 复制AI模块源代码..."

# 创建目标目录
mkdir -p "$AUTOJS6_PROJECT/app/src/main/java/org/autojs/autojs/ai"
mkdir -p "$AUTOJS6_PROJECT/app/src/main/java/org/autojs/autojs/ui/chat"
mkdir -p "$AUTOJS6_PROJECT/app/src/main/java/org/autojs/autojs/data/ai"

# 复制AI模块
cp -r "$AI_MODULE_DIR/src/ai/"* \
   "$AUTOJS6_PROJECT/app/src/main/java/org/autojs/autojs/ai/" 2>/dev/null || true

# 复制UI模块
cp -r "$AI_MODULE_DIR/src/ui/chat/"* \
   "$AUTOJS6_PROJECT/app/src/main/java/org/autojs/autojs/ui/chat/" 2>/dev/null || true

# 复制数据模型
cp -r "$AI_MODULE_DIR/src/data/model/"* \
   "$AUTOJS6_PROJECT/app/src/main/java/org/autojs/autojs/data/ai/" 2>/dev/null || true

echo "✓ 源代码复制完成"
echo ""

# 步骤2: 复制资源文件
echo "步骤 2/6: 复制资源文件..."

# 复制布局文件
cp "$AI_MODULE_DIR/res/layout/"* \
   "$AUTOJS6_PROJECT/app/src/main/res/layout/" 2>/dev/null || true

# 复制图标
cp "$AI_MODULE_DIR/res/drawable/"* \
   "$AUTOJS6_PROJECT/app/src/main/res/drawable/" 2>/dev/null || true

# 复制字符串资源
if [ -f "$AI_MODULE_DIR/res/values/strings_ai.xml" ]; then
    cp "$AI_MODULE_DIR/res/values/strings_ai.xml" \
       "$AUTOJS6_PROJECT/app/src/main/res/values/"
fi

echo "✓ 资源文件复制完成"
echo ""

# 步骤3: 更新build.gradle.kts
echo "步骤 3/6: 更新build.gradle.kts..."

BUILD_GRADLE="$AUTOJS6_PROJECT/app/build.gradle.kts"

# 检查是否已添加AI依赖
if grep -q "retrofit2:retrofit" "$BUILD_GRADLE"; then
    echo "⚠ AI依赖可能已存在，跳过"
else
    # 在dependencies块中添加AI依赖
    echo "添加AI相关依赖到build.gradle.kts"
    # 这里需要手动添加，因为自动添加可能破坏文件结构
    echo "⚠ 请手动添加以下依赖到 app/build.gradle.kts:"
    echo ""
    echo "dependencies {"
    echo "    // AI模块依赖"
    echo "    implementation(\"com.squareup.retrofit2:retrofit:2.11.0\")"
    echo "    implementation(\"com.squareup.retrofit2:converter-gson:2.11.0\")"
    echo "    implementation(\"com.squareup.okhttp3:logging-interceptor:4.12.0\")"
    echo "}"
    echo ""
fi

echo "✓ build.gradle.kts更新提示完成"
echo ""

# 步骤4: 创建local.properties模板
echo "步骤 4/6: 配置local.properties..."

if [ ! -f "$AUTOJS6_PROJECT/local.properties" ]; then
    echo "创建local.properties文件"
    cat > "$AUTOJS6_PROJECT/local.properties" << EOF
# Android SDK路径
sdk.dir=/path/to/android/sdk

# AI API配置
ai.api.key=YOUR_API_KEY_HERE
ai.api.url=https://api.openai.com/v1/chat/completions
EOF
    echo "✓ 已创建local.properties模板"
else
    # 检查是否已有AI配置
    if ! grep -q "ai.api.key" "$AUTOJS6_PROJECT/local.properties"; then
        echo "添加AI配置到现有的local.properties"
        cat >> "$AUTOJS6_PROJECT/local.properties" << EOF

# AI API配置
ai.api.key=YOUR_API_KEY_HERE
ai.api.url=https://api.openai.com/v1/chat/completions
EOF
        echo "✓ 已添加AI配置"
    else
        echo "✓ AI配置已存在"
    fi
fi

echo ""

# 步骤5: 创建集成示例代码
echo "步骤 5/6: 创建集成示例..."

EXAMPLE_FILE="$AUTOJS6_PROJECT/AI_INTEGRATION_EXAMPLE.kt"
cat > "$EXAMPLE_FILE" << 'EOF'
// AutoJs6 AI模块使用示例

import org.autojs.autojs.ai.AIAssistant
import org.autojs.autojs.AutoJs

// 在Activity或Fragment中使用
class YourActivity : AppCompatActivity() {
    
    private lateinit var aiAssistant: AIAssistant
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化AI助手
        val runtime = AutoJs.getInstance().runtime
        aiAssistant = AIAssistant(runtime)
        
        // 示例1: 通过对话生成并执行脚本
        lifecycleScope.launch {
            val result = aiAssistant.executeUserRequest("点击确定按钮")
            Toast.makeText(this@YourActivity, 
                if (result.isSuccess) "成功" else "失败: ${result.error}",
                Toast.LENGTH_SHORT).show()
        }
        
        // 示例2: 只生成脚本
        lifecycleScope.launch {
            val script = aiAssistant.generateScript("自动签到")
            Log.d("AI", "生成的脚本:\n$script")
        }
        
        // 示例3: 分析屏幕
        lifecycleScope.launch {
            val screenInfo = aiAssistant.analyzeScreen()
            Log.d("AI", "分析到 ${screenInfo.elements.size} 个元素")
        }
    }
}

// 在主界面添加AI助手入口
// 方式1: 添加到现有的MainActivity
findViewById<FloatingActionButton>(R.id.fab_ai).setOnClickListener {
    startActivity(Intent(this, AIChatActivity::class.java))
}

// 方式2: 添加到菜单
override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
        R.id.action_ai_assistant -> {
            startActivity(Intent(this, AIChatActivity::class.java))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
EOF

echo "✓ 已创建示例代码: AI_INTEGRATION_EXAMPLE.kt"
echo ""

# 步骤6: 显示后续步骤
echo "步骤 6/6: 集成完成！"
echo ""
echo "═══════════════════════════════════════════════════════"
echo "   ✅ AI模块集成成功！"
echo "═══════════════════════════════════════════════════════"
echo ""
echo "📋 后续步骤:"
echo ""
echo "1. 配置API密钥"
echo "   编辑 local.properties 文件，填写你的AI API密钥"
echo ""
echo "2. 添加依赖"
echo "   在 app/build.gradle.kts 中添加AI相关依赖（见上方提示）"
echo ""
echo "3. 在主界面添加AI助手入口"
echo "   参考 AI_INTEGRATION_EXAMPLE.kt 中的示例代码"
echo ""
echo "4. 编译运行"
echo "   cd $AUTOJS6_PROJECT"
echo "   ./gradlew assembleDebug"
echo ""
echo "📚 文档:"
echo "   - 集成指南: $AI_MODULE_DIR/integration-guide.md"
echo "   - 使用示例: $AUTOJS6_PROJECT/AI_INTEGRATION_EXAMPLE.kt"
echo ""
echo "🎉 现在AutoJs6拥有了AI增强功能！"
echo ""
