#!/bin/bash

# AutoJs6 Agent系统部署脚本
echo "=== AutoJs6 Agent系统部署脚本 ==="

# 检查Git状态
echo "检查Git状态..."
git status

# 添加文件到Git
echo "添加Agent系统文件到Git..."
git add app/src/main/java/org/autojs/autojs/agent/
git add app/src/main/java/org/autojs/autojs/ui/main/AgentControlActivity.kt
git add app/src/main/res/layout/activity_agent_control.xml
git add app/src/main/res/layout/item_agent_status.xml
git add app/src/test/java/org/autojs/autojs/agent/
git add app/src/main/java/org/autojs/autojs/App.kt
git add *.md

# 提交更改
echo "提交更改..."
git commit -m "feat: 添加Agent系统 - 智能化改进

- 实现5个核心Agent：脚本生成、执行监控、行为学习、UI适配、对话交互
- 添加Agent管理器和基础架构
- 集成到AutoJs6应用程序
- 添加用户界面和控制功能
- 完整的测试套件和文档"

# 推送到远程仓库
echo "推送到远程仓库..."
git push origin HEAD

echo "部署完成！"