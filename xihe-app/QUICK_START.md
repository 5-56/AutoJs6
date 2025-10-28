# 🚀 羲和 - 快速启动指南

## ⚡ 一分钟快速启动

### 步骤1: 复制项目到Android Studio

```bash
# 将整个 xihe-app 文件夹复制到你的工作目录
# 例如: D:\Projects\xihe-app 或 ~/Projects/xihe-app
```

### 步骤2: 配置API密钥

1. **复制模板文件**
   ```bash
   cd xihe-app
   cp local.properties.template local.properties
   ```

2. **编辑 local.properties 文件**
   ```properties
   # 修改为你的Android SDK路径
   sdk.dir=/Users/YourName/Library/Android/sdk
   
   # 填入你的AI API密钥
   ai.api.key=sk-your-actual-api-key-here
   ai.api.url=https://api.openai.com/v1/chat/completions
   ```

   **如何获取API密钥？**
   - OpenAI: https://platform.openai.com/api-keys
   - 其他兼容服务: 查看对应服务商文档

### 步骤3: 在Android Studio中打开

1. 打开 Android Studio
2. 选择 `File` > `Open`
3. 选择 `xihe-app` 文件夹
4. 点击 `OK`
5. 等待 Gradle 同步完成（首次可能需要几分钟）

### 步骤4: 运行应用

1. 连接Android设备 或 启动模拟器
2. 点击工具栏的 `Run` 按钮（绿色三角形）
3. 或按快捷键 `Shift + F10`

---

## 📋 已包含的文件清单

✅ **所有核心代码** (11个Kotlin文件)
✅ **所有布局文件** (6个XML)
✅ **所有资源文件** (配置、图标、菜单等)
✅ **Rhino JavaScript引擎** (已包含)
✅ **完整的Gradle配置**
✅ **详细文档** (4个Markdown)

---

## ⚠️ 重要提示

### 必须完成的配置

1. **创建 local.properties 文件** ⭐ 最重要！
   - 复制 `local.properties.template` 为 `local.properties`
   - 填写你的 Android SDK 路径
   - 填写你的 AI API 密钥

2. **生成应用图标**（可选，不影响运行）
   - 在Android Studio中右键点击 `res` 文件夹
   - 选择 `New` > `Image Asset`
   - 选择 `Launcher Icons`
   - 设计并生成图标

### 首次运行注意事项

1. **授予权限**
   - 应用启动后会请求必要权限
   - 存储权限（用于保存脚本）
   - 网络权限（用于AI通信）

2. **启用无障碍服务**
   - 打开系统设置
   - 进入 `无障碍` > `已安装的服务`
   - 找到并启用 `羲和无障碍服务`

3. **测试AI功能**
   - 打开应用
   - 在输入框输入: "你好"
   - 如果收到AI回复，说明配置成功！

---

## 🎯 使用示例

### 示例1: 简单对话
```
你: 帮我写一个点击屏幕上"确定"按钮的脚本
羲和: [生成脚本并显示]
```

### 示例2: 屏幕分析
```
点击截图按钮 → 羲和分析屏幕内容 → 显示识别结果
```

### 示例3: 脚本执行
```
羲和生成脚本 → 点击"执行"按钮 → 查看执行结果
```

---

## 🔧 常见问题

### Q1: Gradle同步失败
**解决方案:**
- 检查网络连接
- 等待下载完成（首次可能需要较长时间）
- 尝试 `File` > `Invalidate Caches / Restart`

### Q2: 找不到SDK路径
**解决方案:**
- Windows: 通常在 `C:\Users\YourName\AppData\Local\Android\Sdk`
- macOS: 通常在 `/Users/YourName/Library/Android/sdk`
- Linux: 通常在 `/home/YourName/Android/Sdk`

### Q3: AI没有回复
**检查清单:**
- [ ] local.properties 文件已创建
- [ ] API密钥填写正确
- [ ] API URL填写正确
- [ ] 设备已连接网络
- [ ] API密钥额度充足

### Q4: 应用无法安装
**解决方案:**
- 确保设备已开启USB调试（实体设备）
- 确保设备已授权此电脑进行调试
- 检查设备存储空间

---

## 📁 项目文件结构

```
xihe-app/
├── app/
│   ├── libs/
│   │   └── rhino-1.8.1-SNAPSHOT.jar  ✅ 已包含
│   ├── src/main/
│   │   ├── java/                     ✅ 所有Kotlin代码
│   │   ├── res/                      ✅ 所有资源文件
│   │   └── AndroidManifest.xml       ✅ 清单文件
│   └── build.gradle.kts              ✅ 应用配置
├── build.gradle.kts                  ✅ 项目配置
├── settings.gradle.kts               ✅ 设置配置
├── local.properties.template         ⚠️ 需复制为local.properties
└── README.md                         ✅ 项目说明
```

---

## 🎉 完成！

现在你可以开始使用羲和了！

**下一步:**
1. 查看 `README.md` 了解更多功能
2. 查看 `DEPLOYMENT_GUIDE.md` 了解详细配置
3. 查看 `CODE_LOCATION_MAP.md` 了解代码结构

**需要帮助?**
- 查看 `PROJECT_SUMMARY.md` 了解技术细节
- 参考AutoJs6文档: https://docs.autojs6.com

---

**祝你使用愉快！🚀**
