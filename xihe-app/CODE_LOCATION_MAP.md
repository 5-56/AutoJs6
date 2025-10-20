# 羲和应用代码位置索引

本文档详细说明了羲和应用中所有代码文件的位置及其功能。

## 📁 项目根目录

```
xihe-app/
├── README.md                    # 项目总体说明文档
├── DEPLOYMENT_GUIDE.md          # 部署指南（详细步骤）
├── CODE_LOCATION_MAP.md         # 本文件 - 代码位置索引
├── build.gradle.kts             # 项目级Gradle构建脚本
├── settings.gradle.kts          # Gradle设置文件
├── gradle.properties            # Gradle配置属性
└── local.properties             # 本地配置（需手动创建，包含API密钥）
```

## 📱 应用模块 (app/)

### 构建配置
```
app/
├── build.gradle.kts             # 应用级Gradle构建脚本
│                                # 包含：依赖项、编译配置、构建类型
└── proguard-rules.pro           # ProGuard混淆规则
```

### 源代码 (app/src/main/java/com/xihe/automation/)

#### 1. 应用入口
```
├── XiheApplication.kt           # Application类
│                                # 位置: app/src/main/java/com/xihe/automation/
│                                # 功能: 应用初始化、全局Context、日志配置
```

#### 2. UI层 (ui/)
```
├── ui/
│   ├── main/
│   │   └── XiheMainActivity.kt  # 主Activity - AI聊天界面
│   │                            # 位置: app/src/main/java/com/xihe/automation/ui/main/
│   │                            # 功能: 聊天界面、权限管理、菜单处理
│   │
│   ├── viewmodel/
│   │   └── ChatViewModel.kt     # 聊天ViewModel
│   │                            # 位置: app/src/main/java/com/xihe/automation/ui/viewmodel/
│   │                            # 功能: 消息管理、AI交互逻辑、状态管理
│   │
│   ├── adapter/
│   │   └── ChatMessageAdapter.kt # RecyclerView适配器
│   │                            # 位置: app/src/main/java/com/xihe/automation/ui/adapter/
│   │                            # 功能: 聊天消息列表展示、多种消息类型
│   │
│   └── settings/
│       └── SettingsActivity.kt  # 设置Activity
│                                # 位置: app/src/main/java/com/xihe/automation/ui/settings/
│                                # 功能: 应用设置界面、API配置
```

#### 3. AI引擎 (ai/)
```
├── ai/
│   ├── AIConversationManager.kt # AI对话管理器
│   │                            # 位置: app/src/main/java/com/xihe/automation/ai/
│   │                            # 功能: AI API通信、对话历史管理、后备脚本生成
│   │                            # 重要方法:
│   │                            #   - sendMessage(): 发送消息给AI
│   │                            #   - buildRequestBody(): 构建API请求
│   │                            #   - parseResponse(): 解析AI响应
│   │                            #   - generateFallbackScript(): 生成后备脚本
│   │
│   ├── AIScriptGenerator.kt     # AI脚本生成器
│   │                            # 位置: app/src/main/java/com/xihe/automation/ai/
│   │                            # 功能: 根据需求生成AutoJs脚本
│   │                            # 重要方法:
│   │                            #   - generateScript(): 主生成方法
│   │                            #   - generateClickScript(): 生成点击脚本
│   │                            #   - generateSwipeScript(): 生成滑动脚本
│   │                            #   - generateInputScript(): 生成输入脚本
│   │                            #   - optimizeScript(): 优化脚本
│   │
│   └── ScreenAnalyzer.kt        # 屏幕分析器
│                                # 位置: app/src/main/java/com/xihe/automation/ai/
│                                # 功能: 屏幕截图、OCR文字识别、UI元素分析
│                                # 重要方法:
│                                #   - captureScreen(): 捕获屏幕
│                                #   - analyzeScreen(): 分析屏幕内容
│                                #   - recognizeText(): 文字识别
│                                #   - analyzeUIElements(): UI元素分析
```

#### 4. 脚本引擎 (script/)
```
├── script/
│   └── ScriptExecutor.kt        # 脚本执行器
│                                # 位置: app/src/main/java/com/xihe/automation/script/
│                                # 功能: 执行JavaScript脚本（使用Rhino引擎）
│                                # 重要方法:
│                                #   - execute(): 执行脚本
│                                #   - injectGlobalObjects(): 注入全局对象
│                                #   - validateScript(): 验证脚本语法
```

#### 5. 核心服务 (core/)
```
├── core/
│   └── accessibility/
│       └── XiheAccessibilityService.kt # 无障碍服务
│                                # 位置: app/src/main/java/com/xihe/automation/core/accessibility/
│                                # 功能: 屏幕元素获取、自动化操作
│                                # 重要方法:
│                                #   - getRootNode(): 获取根节点
│                                #   - findNodeByText(): 查找文本节点
│                                #   - findNodeById(): 查找ID节点
│                                #   - clickNode(): 点击节点
│                                #   - clickAt(): 点击坐标
│                                #   - swipe(): 滑动操作
│                                #   - setText(): 输入文本
```

#### 6. 数据模型 (data/model/)
```
└── data/
    └── model/
        ├── ChatMessage.kt       # 聊天消息模型
        │                        # 位置: app/src/main/java/com/xihe/automation/data/model/
        │                        # 包含: 消息ID、内容、类型、时间戳、脚本内容
        │
        └── AIResponse.kt        # AI响应和其他数据模型
                                 # 位置: app/src/main/java/com/xihe/automation/data/model/
                                 # 包含:
                                 #   - AIResponse: AI响应数据
                                 #   - ScreenAnalysis: 屏幕分析结果
                                 #   - UIElement: UI元素信息
                                 #   - Bounds: 元素边界
                                 #   - ScriptExecutionResult: 脚本执行结果
```

### 资源文件 (app/src/main/res/)

#### 布局文件 (layout/)
```
├── layout/
│   ├── activity_xihe_main.xml   # 主界面布局
│   │                            # 位置: app/src/main/res/layout/
│   │                            # 包含: Toolbar、聊天列表、输入区域
│   │
│   ├── activity_settings.xml    # 设置界面布局
│   │                            # 位置: app/src/main/res/layout/
│   │
│   ├── item_message_user.xml    # 用户消息项布局
│   │                            # 位置: app/src/main/res/layout/
│   │
│   ├── item_message_ai.xml      # AI消息项布局
│   │                            # 位置: app/src/main/res/layout/
│   │
│   ├── item_message_script.xml  # 脚本消息项布局
│   │                            # 位置: app/src/main/res/layout/
│   │                            # 包含: 脚本展示、复制按钮、执行按钮
│   │
│   └── item_message_system.xml  # 系统消息项布局
│                                # 位置: app/src/main/res/layout/
```

#### 配置文件 (values/)
```
├── values/
│   ├── strings.xml              # 字符串资源
│   │                            # 位置: app/src/main/res/values/
│   │                            # 包含: 所有UI文本、提示信息、错误消息
│   │
│   ├── colors.xml               # 颜色资源
│   │                            # 位置: app/src/main/res/values/
│   │                            # 包含: 主题色、消息颜色、状态颜色
│   │
│   ├── themes.xml               # 主题定义
│   │                            # 位置: app/src/main/res/values/
│   │                            # 包含: 应用主题、启动主题
│   │
│   ├── styles.xml               # 样式定义
│   │                            # 位置: app/src/main/res/values/
│   │                            # 包含: 按钮样式、卡片样式
│   │
│   └── arrays.xml               # 数组资源
│                                # 位置: app/src/main/res/values/
│                                # 包含: 主题选项、语言选项
```

#### 其他资源
```
├── drawable/
│   ├── bg_input_message.xml     # 输入框背景
│   │                            # 位置: app/src/main/res/drawable/
│   │
│   └── splash_background.xml    # 启动屏背景
│                                # 位置: app/src/main/res/drawable/
│
├── menu/
│   └── menu_main.xml            # 主菜单
│                                # 位置: app/src/main/res/menu/
│                                # 包含: 清空聊天、设置、关于
│
└── xml/
    ├── accessibility_service_config.xml # 无障碍服务配置
    │                            # 位置: app/src/main/res/xml/
    │
    ├── file_paths.xml           # 文件共享路径
    │                            # 位置: app/src/main/res/xml/
    │
    └── preferences.xml          # 设置项配置
                                 # 位置: app/src/main/res/xml/
```

### 清单文件
```
└── AndroidManifest.xml          # 应用清单文件
                                 # 位置: app/src/main/
                                 # 包含:
                                 #   - 权限声明
                                 #   - Activity声明
                                 #   - Service声明
                                 #   - Application配置
```

## 🔑 关键代码位置快速索引

### AI功能
- **AI对话**: `app/src/main/java/com/xihe/automation/ai/AIConversationManager.kt`
- **脚本生成**: `app/src/main/java/com/xihe/automation/ai/AIScriptGenerator.kt`
- **屏幕分析**: `app/src/main/java/com/xihe/automation/ai/ScreenAnalyzer.kt`

### 界面
- **主界面**: `app/src/main/java/com/xihe/automation/ui/main/XiheMainActivity.kt`
- **ViewModel**: `app/src/main/java/com/xihe/automation/ui/viewmodel/ChatViewModel.kt`
- **消息适配器**: `app/src/main/java/com/xihe/automation/ui/adapter/ChatMessageAdapter.kt`

### 脚本执行
- **执行器**: `app/src/main/java/com/xihe/automation/script/ScriptExecutor.kt`

### 系统服务
- **无障碍服务**: `app/src/main/java/com/xihe/automation/core/accessibility/XiheAccessibilityService.kt`

### 数据模型
- **所有模型**: `app/src/main/java/com/xihe/automation/data/model/`

### 配置文件
- **应用配置**: `app/build.gradle.kts`
- **API配置**: `local.properties` (需手动创建)
- **清单文件**: `app/src/main/AndroidManifest.xml`

## 📝 重要说明

1. **所有路径都是相对于项目根目录** `xihe-app/`

2. **需要手动创建的文件**:
   - `local.properties` (必需，包含AI API配置)
   - `app/libs/rhino-1.8.1-SNAPSHOT.jar` (从AutoJs6复制)
   - 各种图标文件 (ic_*.xml)

3. **包名**: `com.xihe.automation`

4. **最小SDK**: API 24 (Android 7.0)

5. **目标SDK**: API 35

## 🚀 快速定位

在Android Studio中：
1. 使用 `Ctrl+N` (Windows/Linux) 或 `Cmd+O` (Mac) 快速查找类
2. 使用 `Ctrl+Shift+N` (Windows/Linux) 或 `Cmd+Shift+O` (Mac) 快速查找文件
3. 使用项目结构视图（左侧）浏览文件树

## 📚 相关文档

- **部署指南**: `DEPLOYMENT_GUIDE.md`
- **项目说明**: `README.md`
- **AutoJs6文档**: https://docs.autojs6.com
