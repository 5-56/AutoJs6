# AutoJs6 AI集成模块

## 📦 这是什么？

这是一个可以**直接添加到AutoJs6项目**的AI助手模块，提供：

✅ **真实的自动化功能**（使用AutoJs6的完整API）
✅ **AI智能脚本生成**（基于屏幕分析）  
✅ **自动优化循环**（执行失败自动优化）
✅ **完整的UI界面**（Material Design 3）

## 🎯 功能特性

### 1. 智能对话生成脚本
```
用户: 帮我点击"确定"按钮
AI: [分析屏幕] → [生成脚本] → [执行] → 显示结果
```

### 2. 自动屏幕分析
- 使用AutoJs6的accessibility服务获取UI树
- 使用AutoJs6的OCR识别文字
- 使用AutoJs6的图像识别功能

### 3. 智能优化循环
```
生成脚本 → 执行 → 失败？ → AI分析错误 → 优化脚本 → 重新执行
```

## 📁 文件结构

```
autojs6-ai-integration/
├── src/                        # 源代码（复制到AutoJs6项目）
│   ├── ai/                     # AI模块
│   │   ├── AIAssistant.kt
│   │   ├── AIScriptGenerator.kt
│   │   ├── ScreenAnalyzer.kt
│   │   ├── ScriptOptimizer.kt
│   │   └── ConversationManager.kt
│   ├── ui/chat/                # 聊天界面
│   │   ├── AIChatActivity.kt
│   │   ├── ChatViewModel.kt
│   │   └── ChatMessageAdapter.kt
│   └── data/model/             # 数据模型
│       └── ChatMessage.kt
├── res/                        # 资源文件
│   ├── layout/
│   ├── drawable/
│   └── values/
├── integration-guide.md        # 集成指南
└── README.md                   # 本文件
```

## 🚀 快速开始

### 方式一：自动集成（推荐）

```bash
# 1. 进入AutoJs6项目目录
cd /workspace

# 2. 运行集成脚本
./autojs6-ai-integration/integrate.sh
```

### 方式二：手动集成

参见 `integration-guide.md`

## 💡 使用示例

```kotlin
// 在AutoJs6中使用AI助手

val runtime = AutoJs.getInstance().runtime
val aiAssistant = AIAssistant(runtime)

// 方式1：通过对话生成并执行
lifecycleScope.launch {
    val result = aiAssistant.executeUserRequest("点击确定按钮")
    println(result.output)
}

// 方式2：只生成脚本
val script = aiAssistant.generateScript("自动签到")
println(script)

// 方式3：分析屏幕
val screenInfo = aiAssistant.analyzeScreen()
screenInfo.elements.forEach { println(it) }
```

## 📋 集成检查清单

- [ ] 复制代码文件到AutoJs6项目
- [ ] 添加AI依赖到build.gradle.kts
- [ ] 配置AI API密钥
- [ ] 添加UI布局文件
- [ ] 在主界面添加AI助手入口
- [ ] 测试运行

## 🔧 配置

在 `local.properties` 中添加：

```properties
ai.api.key=YOUR_API_KEY
ai.api.url=https://api.openai.com/v1/chat/completions
```

## 📚 文档

- [集成指南](integration-guide.md) - 详细的集成步骤
- [API文档](api-docs.md) - AI模块API说明
- [示例代码](examples/) - 使用示例

## ⚠️ 注意事项

1. **需要AutoJs6项目** - 这个模块必须添加到AutoJs6项目中使用
2. **需要AI API** - 需要OpenAI或兼容的API密钥
3. **需要权限** - 需要无障碍服务、网络权限等

## 🎉 完成后的效果

集成后，AutoJs6将拥有：
- ✅ 所有原有的自动化功能
- ✅ AI聊天界面
- ✅ 智能脚本生成
- ✅ 自动优化和执行
- ✅ 屏幕智能分析

## 📞 支持

如有问题，请查看：
- AutoJs6文档: https://docs.autojs6.com
- 集成指南: integration-guide.md
