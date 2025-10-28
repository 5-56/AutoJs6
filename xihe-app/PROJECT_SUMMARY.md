# 羲和 (Xihe) 项目总结

## 🎯 项目概述

羲和是一款**AI驱动的Android自动化脚本应用**，基于AutoJs6项目开发。它通过AI聊天界面让用户用自然语言描述需求，AI自动生成并执行AutoJs脚本，实现全流程智能自动化。

### 核心特性

✨ **AI聊天交互** - 通过对话式界面与AI交互
🤖 **智能脚本生成** - AI根据需求自动生成可执行的AutoJs脚本
📱 **屏幕内容识别** - 实时分析屏幕UI元素和文字内容
🔧 **脚本自动执行** - 即时执行生成的脚本并显示结果
♻️ **智能优化循环** - 根据执行结果自动优化脚本
🎮 **全流程AI接管** - 完全由AI接管自动化流程

## 📦 项目文件清单

### ✅ 已创建的所有文件

#### 配置文件 (5个)
1. `xihe-app/settings.gradle.kts` - Gradle设置
2. `xihe-app/build.gradle.kts` - 项目级构建脚本
3. `xihe-app/gradle.properties` - Gradle属性配置
4. `xihe-app/app/build.gradle.kts` - 应用级构建脚本（含所有依赖）
5. `xihe-app/app/proguard-rules.pro` - ProGuard混淆规则

#### Java/Kotlin源码文件 (11个)
6. `xihe-app/app/src/main/java/com/xihe/automation/XiheApplication.kt` - Application类
7. `xihe-app/app/src/main/java/com/xihe/automation/ui/main/XiheMainActivity.kt` - 主Activity
8. `xihe-app/app/src/main/java/com/xihe/automation/ui/viewmodel/ChatViewModel.kt` - ViewModel
9. `xihe-app/app/src/main/java/com/xihe/automation/ui/adapter/ChatMessageAdapter.kt` - 消息适配器
10. `xihe-app/app/src/main/java/com/xihe/automation/ui/settings/SettingsActivity.kt` - 设置Activity
11. `xihe-app/app/src/main/java/com/xihe/automation/data/model/ChatMessage.kt` - 消息模型
12. `xihe-app/app/src/main/java/com/xihe/automation/data/model/AIResponse.kt` - 响应模型
13. `xihe-app/app/src/main/java/com/xihe/automation/ai/AIConversationManager.kt` - AI对话管理
14. `xihe-app/app/src/main/java/com/xihe/automation/ai/AIScriptGenerator.kt` - 脚本生成器
15. `xihe-app/app/src/main/java/com/xihe/automation/ai/ScreenAnalyzer.kt` - 屏幕分析器
16. `xihe-app/app/src/main/java/com/xihe/automation/script/ScriptExecutor.kt` - 脚本执行器
17. `xihe-app/app/src/main/java/com/xihe/automation/core/accessibility/XiheAccessibilityService.kt` - 无障碍服务

#### 布局文件 (6个)
18. `xihe-app/app/src/main/res/layout/activity_xihe_main.xml` - 主界面布局
19. `xihe-app/app/src/main/res/layout/activity_settings.xml` - 设置界面布局
20. `xihe-app/app/src/main/res/layout/item_message_user.xml` - 用户消息布局
21. `xihe-app/app/src/main/res/layout/item_message_ai.xml` - AI消息布局
22. `xihe-app/app/src/main/res/layout/item_message_script.xml` - 脚本消息布局
23. `xihe-app/app/src/main/res/layout/item_message_system.xml` - 系统消息布局

#### 资源配置文件 (11个)
24. `xihe-app/app/src/main/res/values/strings.xml` - 字符串资源
25. `xihe-app/app/src/main/res/values/colors.xml` - 颜色资源
26. `xihe-app/app/src/main/res/values/themes.xml` - 主题资源
27. `xihe-app/app/src/main/res/values/styles.xml` - 样式资源
28. `xihe-app/app/src/main/res/values/arrays.xml` - 数组资源
29. `xihe-app/app/src/main/res/drawable/bg_input_message.xml` - 输入框背景
30. `xihe-app/app/src/main/res/drawable/splash_background.xml` - 启动屏背景
31. `xihe-app/app/src/main/res/menu/menu_main.xml` - 主菜单
32. `xihe-app/app/src/main/res/xml/accessibility_service_config.xml` - 无障碍服务配置
33. `xihe-app/app/src/main/res/xml/file_paths.xml` - 文件共享路径
34. `xihe-app/app/src/main/res/xml/preferences.xml` - 设置项配置

#### 清单文件 (1个)
35. `xihe-app/app/src/main/AndroidManifest.xml` - 应用清单文件

#### 文档文件 (4个)
36. `xihe-app/README.md` - 项目说明
37. `xihe-app/DEPLOYMENT_GUIDE.md` - 详细部署指南
38. `xihe-app/CODE_LOCATION_MAP.md` - 代码位置索引
39. `xihe-app/PROJECT_SUMMARY.md` - 本文件（项目总结）

**总计：39个文件** ✅

### ⚠️ 需要手动创建/配置的文件

#### 1. local.properties（必需）
位置：`xihe-app/local.properties`

内容模板：
```properties
# Android SDK路径（根据实际情况修改）
sdk.dir=/Users/YourName/Library/Android/sdk

# AI API配置（必需）
ai.api.key=YOUR_API_KEY_HERE
ai.api.url=https://api.openai.com/v1/chat/completions
```

#### 2. Rhino库文件（必需）
- 从AutoJs6项目复制：`libs/org.mozilla.rhino-1.8.1-SNAPSHOT.jar`
- 目标位置：`xihe-app/app/libs/rhino-1.8.1-SNAPSHOT.jar`

命令：
```bash
cp /workspace/libs/org.mozilla.rhino-1.8.1-SNAPSHOT.jar xihe-app/app/libs/
```

#### 3. 图标文件（推荐）
需要创建以下矢量图标（或使用Material Icons）：

在 `xihe-app/app/src/main/res/drawable/` 目录：
- `ic_screenshot.xml` - 截图图标
- `ic_send.xml` - 发送图标
- `ic_ai.xml` - AI图标
- `ic_script.xml` - 脚本图标
- `ic_copy.xml` - 复制图标
- `ic_play.xml` - 播放/执行图标
- `ic_clear.xml` - 清除图标
- `ic_settings.xml` - 设置图标
- `ic_info.xml` - 信息图标

在 `xihe-app/app/src/main/res/mipmap-xxhdpi/` 目录：
- `ic_launcher.png` - 应用图标
- `ic_launcher_round.png` - 圆形应用图标

## 🏗️ 架构设计

### 模块划分

```
羲和应用
├── UI层
│   ├── MainActivity (聊天界面)
│   ├── SettingsActivity (设置)
│   ├── ChatViewModel (状态管理)
│   └── ChatMessageAdapter (消息展示)
│
├── AI引擎层
│   ├── AIConversationManager (AI对话)
│   ├── AIScriptGenerator (脚本生成)
│   └── ScreenAnalyzer (屏幕分析)
│
├── 脚本引擎层
│   └── ScriptExecutor (Rhino引擎执行)
│
├── 核心服务层
│   └── XiheAccessibilityService (无障碍服务)
│
└── 数据层
    └── Models (数据模型)
```

### 工作流程

```
用户输入
   ↓
AI对话管理器 → 解析需求
   ↓
脚本生成器 → 生成AutoJs脚本
   ↓
显示给用户（可编辑）
   ↓
用户确认执行
   ↓
脚本执行器 → 运行脚本
   ↓
显示执行结果
   ↓
（可选）AI根据结果优化脚本
```

## 🚀 快速开始

### 第一步：准备环境

1. 安装Android Studio（推荐 2024.3+）
2. 配置Android SDK（API 24-35）
3. 准备AI API密钥（OpenAI或兼容API）

### 第二步：导入项目

```bash
# 1. 复制Rhino库
cp /workspace/libs/org.mozilla.rhino-1.8.1-SNAPSHOT.jar xihe-app/app/libs/

# 2. 创建local.properties
cd xihe-app
cat > local.properties << EOF
sdk.dir=/Users/YourName/Library/Android/sdk
ai.api.key=YOUR_API_KEY
ai.api.url=https://api.openai.com/v1/chat/completions
EOF

# 3. 在Android Studio中打开项目
# File > Open > 选择xihe-app文件夹
```

### 第三步：运行应用

1. 连接Android设备或启动模拟器
2. 等待Gradle同步完成
3. 点击Run按钮（或按Shift+F10）
4. 首次运行时授予所需权限

## 📖 使用指南

### 基础对话示例

**示例1：简单点击**
```
用户: 帮我点击屏幕上的"确定"按钮
AI: [生成脚本]
auto();
sleep(2000);
var target = text("确定").findOne(5000);
if (target) {
    target.click();
    toast("点击成功");
}
```

**示例2：自动签到**
```
用户: 写一个每日自动签到的脚本
AI: [生成脚本，包含查找签到按钮、点击、等待等逻辑]
```

**示例3：屏幕分析**
```
用户: [点击截图按钮]
AI: 分析结果：
   - 识别到3个按钮
   - 识别到文字："登录"、"注册"、"忘记密码"
用户: 点击登录按钮
AI: [生成针对性脚本]
```

## 🔧 自定义开发

### 修改AI提示词

位置：`AIConversationManager.kt`
```kotlin
companion object {
    private const val SYSTEM_PROMPT = """
    你是羲和AI助手...
    [在这里修改AI的行为指令]
    """
}
```

### 添加脚本模板

位置：`AIScriptGenerator.kt`
```kotlin
private fun generateCustomScript(intent: String): String {
    return """
    // 你的自定义脚本模板
    """
}
```

### 扩展屏幕分析

位置：`ScreenAnalyzer.kt`
```kotlin
suspend fun customAnalysis(screenshot: ByteArray): AnalysisResult {
    // 添加自定义分析逻辑
}
```

## 📋 技术栈

- **语言**: Kotlin 2.1.21
- **最小SDK**: Android 7.0 (API 24)
- **目标SDK**: Android 15 (API 35)
- **UI框架**: Material Design 3
- **架构**: MVVM
- **异步**: Kotlin Coroutines
- **网络**: Retrofit2 + OkHttp3
- **脚本引擎**: Rhino 1.8.1
- **OCR**: MLKit Text Recognition
- **JSON**: Gson

## 🔐 权限说明

### 必需权限
- `INTERNET` - AI API通信
- `ACCESS_NETWORK_STATE` - 网络状态检测

### 功能权限
- `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` - 脚本存储
- `MANAGE_EXTERNAL_STORAGE` - 完整文件访问
- `BIND_ACCESSIBILITY_SERVICE` - 无障碍服务（核心功能）
- `SYSTEM_ALERT_WINDOW` - 悬浮窗显示
- `POST_NOTIFICATIONS` - 通知权限

## 🐛 常见问题

### Q1: 编译失败 - 找不到Rhino库
**解决**：确保已复制 `rhino-1.8.1-SNAPSHOT.jar` 到 `app/libs/` 目录

### Q2: AI无响应
**解决**：
1. 检查 `local.properties` 中的API配置
2. 确认API密钥有效
3. 检查网络连接

### Q3: 脚本无法执行
**解决**：
1. 确保已启用无障碍服务
2. 检查脚本语法（使用validateScript方法）
3. 查看Logcat日志

### Q4: 屏幕分析不工作
**解决**：
1. 授予无障碍服务权限
2. 授予悬浮窗权限
3. 确保MLKit库正常加载

## 📊 性能优化建议

1. **AI请求优化**：实现请求缓存，避免重复请求
2. **脚本执行**：使用Worker线程避免阻塞UI
3. **内存管理**：及时释放Bitmap资源
4. **数据库**：使用Room存储聊天历史

## 🔮 未来扩展方向

- [ ] 脚本模板库
- [ ] 云端脚本同步
- [ ] 多轮对话优化
- [ ] 更强大的屏幕分析（图像识别）
- [ ] 脚本市场
- [ ] 协同编辑
- [ ] 定时任务
- [ ] 脚本调试器

## 📞 技术支持

- **AutoJs6文档**: https://docs.autojs6.com
- **项目Issues**: 在原AutoJs6项目提交问题
- **社区论坛**: 参考AutoJs6社区资源

## 📄 许可证

本项目基于AutoJs6，遵循相同的开源协议。

---

**项目创建完成时间**: 2025年10月20日
**项目版本**: 1.0.0
**开发环境**: Android Studio 2024.3+
**总代码量**: 约3500行（不含注释）
**总文件数**: 39个核心文件

祝你开发顺利！🎉
