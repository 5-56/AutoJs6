# 羲和应用部署指南

## 项目说明

羲和是一款基于AutoJs6的AI驱动自动化脚本应用，主要特点：

- **AI聊天界面**：通过自然语言与AI交互
- **自动脚本生成**：AI根据需求自动生成AutoJs脚本
- **屏幕内容识别**：实时分析屏幕元素和文本
- **智能执行优化**：根据执行结果自动优化脚本
- **全流程AI接管**：支持完全由AI接管自动化流程

## 文件结构说明

### 完整目录结构

```
xihe-app/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/xihe/automation/
│   │       │   ├── XiheApplication.kt          # Application类
│   │       │   ├── ui/                          # UI层
│   │       │   │   ├── main/
│   │       │   │   │   └── XiheMainActivity.kt # 主Activity
│   │       │   │   ├── viewmodel/
│   │       │   │   │   └── ChatViewModel.kt     # ViewModel
│   │       │   │   ├── adapter/
│   │       │   │   │   └── ChatMessageAdapter.kt # 消息适配器
│   │       │   │   └── settings/
│   │       │   │       └── SettingsActivity.kt  # 设置Activity
│   │       │   ├── ai/                          # AI引擎
│   │       │   │   ├── AIConversationManager.kt # AI对话管理
│   │       │   │   ├── AIScriptGenerator.kt     # 脚本生成器
│   │       │   │   └── ScreenAnalyzer.kt        # 屏幕分析器
│   │       │   ├── script/                      # 脚本引擎
│   │       │   │   └── ScriptExecutor.kt        # 脚本执行器
│   │       │   ├── core/                        # 核心模块
│   │       │   │   └── accessibility/
│   │       │   │       └── XiheAccessibilityService.kt # 无障碍服务
│   │       │   └── data/                        # 数据模型
│   │       │       └── model/
│   │       │           ├── ChatMessage.kt       # 消息模型
│   │       │           └── AIResponse.kt        # AI响应模型
│   │       ├── res/                             # 资源文件
│   │       │   ├── layout/                      # 布局文件
│   │       │   │   ├── activity_xihe_main.xml   # 主界面布局
│   │       │   │   ├── activity_settings.xml    # 设置界面布局
│   │       │   │   ├── item_message_user.xml    # 用户消息布局
│   │       │   │   ├── item_message_ai.xml      # AI消息布局
│   │       │   │   ├── item_message_script.xml  # 脚本消息布局
│   │       │   │   └── item_message_system.xml  # 系统消息布局
│   │       │   ├── values/                      # 配置值
│   │       │   │   ├── strings.xml              # 字符串资源
│   │       │   │   ├── colors.xml               # 颜色资源
│   │       │   │   ├── themes.xml               # 主题资源
│   │       │   │   ├── styles.xml               # 样式资源
│   │       │   │   └── arrays.xml               # 数组资源
│   │       │   ├── drawable/                    # 图形资源
│   │       │   ├── menu/                        # 菜单
│   │       │   │   └── menu_main.xml            # 主菜单
│   │       │   └── xml/                         # XML配置
│   │       │       ├── accessibility_service_config.xml
│   │       │       ├── file_paths.xml
│   │       │       └── preferences.xml
│   │       ├── AndroidManifest.xml              # 应用清单文件
│   │       └── libs/                            # 本地库（需要手动添加）
│   │           └── rhino-1.8.1-SNAPSHOT.jar
│   ├── build.gradle.kts                         # 应用级构建脚本
│   └── proguard-rules.pro                       # 混淆规则
├── build.gradle.kts                             # 项目级构建脚本
├── settings.gradle.kts                          # 设置脚本
├── gradle.properties                            # Gradle属性
├── local.properties                             # 本地配置（需创建）
└── README.md                                    # 项目说明
```

## 在Android Studio中部署步骤

### 1. 复制项目文件

将整个 `xihe-app/` 文件夹复制到你的工作目录。

### 2. 创建必需的本地库文件

需要从原AutoJs6项目中复制以下文件：

```bash
# 从AutoJs6项目复制Rhino库
cp /workspace/libs/org.mozilla.rhino-1.8.1-SNAPSHOT.jar xihe-app/app/libs/
```

### 3. 创建 local.properties 文件

在项目根目录创建 `local.properties` 文件：

```properties
# Android SDK路径（根据你的实际路径修改）
sdk.dir=/Users/YourName/Library/Android/sdk

# AI API配置（必需）
ai.api.key=YOUR_API_KEY_HERE
ai.api.url=https://api.openai.com/v1/chat/completions
```

**重要提示**：
- 将 `YOUR_API_KEY_HERE` 替换为你的实际AI API密钥
- 支持OpenAI API或兼容的API服务

### 4. 创建占位图标

创建以下占位文件（或使用自己的图标）：

```
app/src/main/res/
├── drawable/
│   ├── ic_screenshot.xml
│   ├── ic_send.xml
│   ├── ic_ai.xml
│   ├── ic_script.xml
│   ├── ic_copy.xml
│   ├── ic_play.xml
│   ├── ic_clear.xml
│   ├── ic_settings.xml
│   └── ic_info.xml
└── mipmap-xxhdpi/
    ├── ic_launcher.png
    └── ic_launcher_round.png
```

简单的XML图标示例（ic_send.xml）：
```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M2.01,21L23,12 2.01,3 2,10l15,2 -15,2z"/>
</vector>
```

### 5. 在Android Studio中打开项目

1. 打开Android Studio
2. 选择 `File > Open`
3. 选择 `xihe-app` 文件夹
4. 点击 `OK`

### 6. 同步Gradle

项目打开后，Android Studio会自动同步Gradle。如果没有自动同步：

1. 点击顶部工具栏的 `Sync Project with Gradle Files` 按钮
2. 或者选择 `File > Sync Project with Gradle Files`

### 7. 配置运行设备

1. 连接Android设备或启动模拟器
2. 确保设备已开启USB调试（实体设备）
3. 在顶部工具栏选择目标设备

### 8. 构建并运行

1. 点击 `Build > Make Project` 构建项目
2. 点击 `Run > Run 'app'` 或按 `Shift + F10` 运行应用

## 功能配置

### 配置AI API

应用首次运行时，需要在设置中配置AI API：

1. 打开应用
2. 点击右上角菜单 > 设置
3. 在"AI API配置"部分输入：
   - API Key
   - API URL

### 启用无障碍服务

为了使用屏幕识别和自动化功能：

1. 打开系统设置
2. 进入 `无障碍 > 已安装的服务`
3. 找到并启用 `羲和无障碍服务`

### 授予必要权限

应用运行时会请求以下权限：
- 存储权限（用于保存脚本）
- 网络权限（用于AI API通信）
- 通知权限（用于显示执行状态）

## 使用示例

### 1. 基础对话

启动应用后，在输入框输入：
```
帮我写一个点击屏幕上"确定"按钮的脚本
```

AI会生成类似以下的脚本：
```javascript
auto();
sleep(2000);
var target = text("确定").findOne(5000);
if (target) {
    target.click();
    toast("点击成功");
} else {
    toast("未找到目标元素");
}
```

### 2. 屏幕分析

1. 点击输入框左侧的截图按钮
2. AI会分析当前屏幕内容
3. 你可以基于分析结果继续对话

### 3. 脚本执行

当AI生成脚本后：
1. 点击脚本卡片上的"执行"按钮
2. 应用会运行脚本并显示结果
3. 根据结果，可以要求AI优化脚本

## 常见问题

### Q: 编译失败，提示找不到Rhino库

A: 确保已将 `rhino-1.8.1-SNAPSHOT.jar` 复制到 `app/libs/` 目录。

### Q: AI无响应

A: 检查 `local.properties` 中的AI API配置是否正确，确保API密钥有效。

### Q: 无法使用自动化功能

A: 确保已在系统设置中启用羲和的无障碍服务。

### Q: 屏幕分析功能不工作

A: 需要启用无障碍服务，并授予悬浮窗权限。

## 进阶开发

### 自定义AI提示词

修改 `AIConversationManager.kt` 中的 `SYSTEM_PROMPT` 常量来自定义AI行为。

### 扩展脚本模板

在 `AIScriptGenerator.kt` 中添加更多脚本生成模板。

### 添加新的屏幕分析功能

扩展 `ScreenAnalyzer.kt` 来支持更多的屏幕分析功能（如图像识别、元素定位等）。

## 技术支持

如有问题，请：
1. 检查上述常见问题
2. 查看日志输出（使用Logcat）
3. 参考AutoJs6文档：https://docs.autojs6.com

## 许可证

本项目基于AutoJs6，遵循相同的开源协议。
