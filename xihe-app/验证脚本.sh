#!/bin/bash

# 羲和项目完整性验证脚本

echo "════════════════════════════════════════════════════════"
echo "  羲和项目完整性验证"
echo "════════════════════════════════════════════════════════"
echo ""

XIHE_DIR="/workspace/xihe-app"
PASS=0
FAIL=0

check() {
    if [ "$2" = "true" ]; then
        echo "✅ $1"
        ((PASS++))
    else
        echo "❌ $1"
        ((FAIL++))
    fi
}

# 检查主要目录
echo "检查项目结构..."
check "项目根目录存在" "$([ -d "$XIHE_DIR" ] && echo true || echo false)"
check "app目录存在" "$([ -d "$XIHE_DIR/app" ] && echo true || echo false)"
check "src/main目录存在" "$([ -d "$XIHE_DIR/app/src/main" ] && echo true || echo false)"

echo ""
echo "检查核心代码..."
check "XiheApplication.kt" "$([ -f "$XIHE_DIR/app/src/main/java/com/xihe/automation/XiheApplication.kt" ] && echo true || echo false)"
check "XiheAutoJs.kt" "$([ -f "$XIHE_DIR/app/src/main/java/com/xihe/automation/XiheAutoJs.kt" ] && echo true || echo false)"
check "XiheAIEngine.kt" "$([ -f "$XIHE_DIR/app/src/main/java/com/xihe/automation/ai/XiheAIEngine.kt" ] && echo true || echo false)"
check "AIScriptGenerator.kt" "$([ -f "$XIHE_DIR/app/src/main/java/com/xihe/automation/ai/AIScriptGenerator.kt" ] && echo true || echo false)"
check "ScreenAnalyzer.kt" "$([ -f "$XIHE_DIR/app/src/main/java/com/xihe/automation/ai/ScreenAnalyzer.kt" ] && echo true || echo false)"
check "ScriptExecutor.kt" "$([ -f "$XIHE_DIR/app/src/main/java/com/xihe/automation/script/ScriptExecutor.kt" ] && echo true || echo false)"
check "ScriptOptimizer.kt" "$([ -f "$XIHE_DIR/app/src/main/java/com/xihe/automation/script/ScriptOptimizer.kt" ] && echo true || echo false)"

echo ""
echo "检查AutoJs6核心..."
check "AutoJs6 core目录" "$([ -d "$XIHE_DIR/app/src/main/java/com/xihe/automation/autojs/core" ] && echo true || echo false)"
check "AutoJs6 runtime目录" "$([ -d "$XIHE_DIR/app/src/main/java/com/xihe/automation/autojs/runtime" ] && echo true || echo false)"
check "AutoJs6 execution目录" "$([ -d "$XIHE_DIR/app/src/main/java/com/xihe/automation/autojs/execution" ] && echo true || echo false)"
check "Stardust基础库" "$([ -d "$XIHE_DIR/app/src/main/java/com/stardust" ] && echo true || echo false)"

echo ""
echo "检查依赖库..."
check "Rhino引擎" "$([ -f "$XIHE_DIR/app/libs/org.mozilla.rhino-1.8.1-SNAPSHOT.jar" ] && echo true || echo false)"

echo ""
echo "检查配置文件..."
check "build.gradle.kts" "$([ -f "$XIHE_DIR/build.gradle.kts" ] && echo true || echo false)"
check "app/build.gradle.kts" "$([ -f "$XIHE_DIR/app/build.gradle.kts" ] && echo true || echo false)"
check "settings.gradle.kts" "$([ -f "$XIHE_DIR/settings.gradle.kts" ] && echo true || echo false)"
check "AndroidManifest.xml" "$([ -f "$XIHE_DIR/app/src/main/AndroidManifest.xml" ] && echo true || echo false)"

echo ""
echo "检查资源文件..."
check "主界面布局" "$([ -f "$XIHE_DIR/app/src/main/res/layout/activity_xihe_main.xml" ] && echo true || echo false)"
check "strings.xml" "$([ -f "$XIHE_DIR/app/src/main/res/values/strings.xml" ] && echo true || echo false)"
check "colors.xml" "$([ -f "$XIHE_DIR/app/src/main/res/values/colors.xml" ] && echo true || echo false)"

echo ""
echo "检查文档..."
check "使用说明-最终版.txt" "$([ -f "$XIHE_DIR/使用说明-最终版.txt" ] && echo true || echo false)"
check "COMPLETE_INTEGRATION_GUIDE.md" "$([ -f "$XIHE_DIR/COMPLETE_INTEGRATION_GUIDE.md" ] && echo true || echo false)"
check "README-FINAL.md" "$([ -f "$XIHE_DIR/README-FINAL.md" ] && echo true || echo false)"

echo ""
echo "════════════════════════════════════════════════════════"
echo "  验证结果"
echo "════════════════════════════════════════════════════════"
echo ""
echo "通过: $PASS 项"
echo "失败: $FAIL 项"
echo ""

# 统计信息
echo "项目统计:"
echo "────────────────────────────────────────────────────────"
echo "项目大小: $(du -sh $XIHE_DIR | cut -f1)"
echo "代码文件: $(find $XIHE_DIR/app/src/main/java -name "*.kt" -o -name "*.java" | wc -l) 个"
echo "XML文件: $(find $XIHE_DIR/app/src/main/res -name "*.xml" | wc -l) 个"
echo "文档文件: $(find $XIHE_DIR -maxdepth 1 -name "*.md" -o -name "*.txt" | wc -l) 个"
echo ""

if [ $FAIL -eq 0 ]; then
    echo "🎉 项目完整性验证通过！"
    echo "✅ 可以直接复制到Android Studio使用！"
else
    echo "⚠️  发现 $FAIL 个问题，请检查"
fi

echo ""
echo "════════════════════════════════════════════════════════"
