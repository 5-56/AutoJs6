# 羲和 (Xihe) - AI驱动的Android自动化脚本应用

## 项目简介

羲和是一款基于AutoJs6的AI驱动自动化应用，通过AI聊天界面实现全自动脚本编写、运行和优化。

## 主要功能

1. **AI聊天界面** - 主界面采用对话式交互，用户通过自然语言描述需求
2. **AI自动编写脚本** - AI根据用户需求自动生成AutoJs脚本
3. **屏幕内容识别** - 实时分析屏幕内容，辅助脚本生成和优化
4. **脚本自动执行** - AI生成的脚本可立即执行
5. **智能优化** - 根据执行结果自动优化脚本
6. **全流程AI接管** - 支持完全由AI接管整个自动化流程

## 项目结构

```
xihe-app/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/xihe/
│   │   │   │   ├── ui/          # UI层
│   │   │   │   │   ├── chat/    # 聊天界面
│   │   │   │   │   ├── script/  # 脚本管理界面
│   │   │   │   ├── ai/          # AI引擎
│   │   │   │   │   ├── AIScriptGenerator.kt    # AI脚本生成器
│   │   │   │   │   ├── AIConversationManager.kt # AI对话管理
│   │   │   │   │   ├── ScreenAnalyzer.kt       # 屏幕分析器
│   │   │   │   ├── script/      # 脚本引擎
│   │   │   │   │   ├── ScriptExecutor.kt       # 脚本执行器
│   │   │   │   │   ├── ScriptOptimizer.kt      # 脚本优化器
│   │   │   │   ├── core/        # 核心模块
│   │   │   │   │   ├── AutomationEngine.kt     # 自动化引擎
│   │   │   │   │   ├── ScreenCaptureService.kt # 屏幕捕获服务
│   │   │   ├── res/
│   │   │   │   ├── layout/      # 布局文件
│   │   │   │   ├── drawable/    # 图片资源
│   │   │   │   ├── values/      # 配置值
│   │   │   ├── AndroidManifest.xml
│   ├── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 核心组件说明

### 1. 主界面 (XiheMainActivity)
- AI聊天交互界面
- 实时消息显示
- 脚本执行状态监控

### 2. AI引擎模块
- **AIScriptGenerator**: 根据用户需求生成脚本
- **AIConversationManager**: 管理AI对话上下文
- **ScreenAnalyzer**: 分析屏幕内容，提供给AI作为上下文

### 3. 脚本引擎模块
- **ScriptExecutor**: 执行AutoJs脚本
- **ScriptOptimizer**: 根据执行结果优化脚本

### 4. 核心服务
- **AutomationEngine**: 整合所有模块的核心引擎
- **ScreenCaptureService**: 屏幕捕获和分析服务

## 使用方法

1. 在Android Studio中打开项目
2. 确保已安装所有必需的依赖
3. 连接Android设备或启动模拟器
4. 点击运行

## 依赖项

- AutoJs6核心库（来自原项目）
- AI SDK（需配置API密钥）
- OCR识别库（MLKit或PaddleOCR）
- 其他AutoJs6依赖

## 配置AI API

在 `local.properties` 文件中添加：
```
ai.api.key=YOUR_API_KEY
ai.api.url=YOUR_API_URL
```

## 权限要求

- 无障碍服务权限
- 屏幕截图权限
- 网络权限
- 存储权限

## 开发计划

- [x] 基础项目结构
- [x] AI聊天界面
- [x] 脚本生成引擎
- [ ] 屏幕分析优化
- [ ] 多轮对话优化
- [ ] 脚本模板库
- [ ] 云端脚本同步

## 许可证

基于AutoJs6项目，遵循相同的开源协议。
