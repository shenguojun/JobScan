#!/bin/bash

echo "=== 环境版本检查 ==="

# 检查Java版本
echo "Java版本:"
java -version 2>&1 | head -1

# 检查Python版本
echo ""
echo "Python版本:"
python3 --version

# 检查虚拟环境是否存在
if [ -d "venv" ]; then
    echo ""
    echo "虚拟环境: ✅ 已创建"
    
    # 激活虚拟环境并检查包版本
    source venv/bin/activate
    
    echo ""
    echo "Python包版本:"
    echo "- crawl4ai: $(pip show crawl4ai 2>/dev/null | grep Version | cut -d' ' -f2 || echo '未安装')"
    echo "- playwright: $(pip show playwright 2>/dev/null | grep Version | cut -d' ' -f2 || echo '未安装')"
    
    # 检查Playwright浏览器
    echo ""
    echo "Playwright浏览器状态:"
    playwright --version 2>/dev/null || echo "Playwright CLI未找到"
    
else
    echo ""
    echo "虚拟环境: ❌ 未创建"
    echo "请运行: ./setup_python_env.sh"
fi

echo ""
echo "=== 检查完成 ===" 