# 🎉 欢迎使用羲和 (Xihe)

> **这是一个完整可用的Android项目，可以直接复制到Android Studio使用！**

---

## ✅ 项目已准备就绪

### 已包含的内容

✨ **完整的源代码** - 12个Kotlin文件，全部功能代码
📱 **完整的UI布局** - 6个界面布局，Material Design 3风格
🎨 **完整的资源文件** - 所有图标、主题、颜色配置
🔧 **JavaScript引擎** - Rhino库已包含（1.7MB）
📚 **详细文档** - 5个文档文件，覆盖所有使用场景

**总计**: 52个文件，2MB大小，开箱即用！

---

## 🚀 三步启动

### 1️⃣ 配置API密钥（1分钟）

```bash
# 复制模板文件
cp local.properties.template local.properties

# 编辑文件，填写以下两项：
# 1. sdk.dir = 你的Android SDK路径
# 2. ai.api.key = 你的AI API密钥
```

**获取API密钥**: https://platform.openai.com/api-keys

### 2️⃣ 打开项目（30秒）

1. 启动 Android Studio
2. File > Open
3. 选择 `xihe-app` 文件夹
4. 等待Gradle同步

### 3️⃣ 运行应用（10秒）

1. 连接设备或启动模拟器
2. 点击 Run 按钮（绿色三角形）
3. 完成！

---

## 📖 项目文档导航

根据你的需求，选择相应的文档：

### 🔰 新手必读
- **`QUICK_START.md`** ⭐ 快速启动指南，一步步教你配置和运行

### 📘 功能使用
- **`README.md`** - 项目功能介绍和特性说明

### 🛠️ 详细配置
- **`DEPLOYMENT_GUIDE.md`** - 完整的部署、配置、问题排查指南

### 💻 代码开发
- **`CODE_LOCATION_MAP.md`** - 所有代码文件的位置索引
- **`PROJECT_SUMMARY.md`** - 技术架构和扩展开发指南

### 📋 文件清单
- **`FILE_LIST.md`** - 完整的文件列表和说明

---

## 🎯 核心功能

### 1. AI聊天交互
```
你: 帮我写一个点击"确定"按钮的脚本
羲和: 好的，我已经生成了脚本...
[显示可执行的AutoJs脚本]
```

### 2. 屏幕分析
```
[点击截图按钮]
羲和: 分析完成，识别到以下元素：
• 按钮: "登录"
• 输入框: "用户名"
• 文字: "欢迎使用"
```

### 3. 智能执行
```
[点击执行按钮]
羲和: ✅ 脚本执行成功
输出: 已成功点击确定按钮
```

---

## 💡 使用示例

### 示例1: 自动签到
```
用户: 帮我写一个自动签到的脚本
羲和: [生成签到脚本]
     包含：查找签到按钮 > 点击 > 等待确认
用户: [点击执行]
羲和: ✅ 签到成功！
```

### 示例2: 批量操作
```
用户: 循环点击"下一页"按钮5次
羲和: [生成循环脚本]
     使用for循环，每次点击后等待2秒
用户: [点击执行]
羲和: ✅ 已完成5次点击操作
```

---

## ⚠️ 重要提示

### ✅ 已经完成的

- [x] 所有代码文件已创建
- [x] Rhino引擎已包含（无需下载）
- [x] 所有资源文件已配置
- [x] 项目结构完整
- [x] Gradle配置正确

### ⚡ 需要你做的

- [ ] 创建 `local.properties` 文件（复制模板）
- [ ] 填写 Android SDK 路径
- [ ] 填写 AI API 密钥
- [ ] （可选）生成应用图标

---

## 🔧 系统要求

- **Android Studio**: 2022.1+ (推荐 2024.3+)
- **JDK**: 17+ (推荐 19)
- **Android SDK**: API 24-35
- **最小Android版本**: Android 7.0 (API 24)
- **目标Android版本**: Android 15 (API 35)

---

## 📊 项目统计

| 项目 | 数量 |
|------|------|
| Kotlin代码文件 | 12个 |
| XML文件 | 26个 |
| 配置文件 | 6个 |
| 文档文件 | 6个 |
| 总文件数 | 52个 |
| 代码行数 | ~3500行 |
| 项目大小 | 2MB |

---

## 🆘 遇到问题？

### 常见问题快速解决

**Q: Gradle同步失败？**
→ 查看 `QUICK_START.md` 的"常见问题"部分

**Q: AI不回复？**
→ 检查 `local.properties` 中的API配置

**Q: 脚本无法执行？**
→ 确保已启用无障碍服务

**Q: 找不到文件？**
→ 查看 `CODE_LOCATION_MAP.md`

---

## 🎓 学习路径

### 初学者
1. 阅读 `QUICK_START.md`
2. 运行应用并尝试基础功能
3. 查看 `README.md` 了解更多特性

### 开发者
1. 阅读 `CODE_LOCATION_MAP.md` 了解代码结构
2. 查看 `PROJECT_SUMMARY.md` 了解架构设计
3. 参考 `DEPLOYMENT_GUIDE.md` 进行自定义开发

---

## 📞 获取帮助

- **AutoJs6官方文档**: https://docs.autojs6.com
- **AutoJs6 GitHub**: https://github.com/SuperMonster003/AutoJs6
- **本项目文档**: 查看上述各个Markdown文件

---

## 🎉 开始使用

**现在就开始吧！**

```bash
# 1. 配置API
cp local.properties.template local.properties
# 编辑 local.properties

# 2. 打开Android Studio
# File > Open > 选择 xihe-app

# 3. 运行！
# 点击 Run 按钮
```

**祝你使用愉快！** 🚀

---

<p align="center">
  <b>羲和 - AI驱动的Android自动化脚本引擎</b><br>
  <i>让自动化变得简单而智能</i>
</p>
