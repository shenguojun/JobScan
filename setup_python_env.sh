#!/bin/bash

echo "设置Python环境..."

# 检查Python3是否安装
if ! command -v python3 &> /dev/null; then
    echo "错误: 未找到python3，请先安装Python 3.7+"
    exit 1
fi

# 检查pip是否安装
if ! command -v pip3 &> /dev/null; then
    echo "错误: 未找到pip3，请先安装pip"
    exit 1
fi

# 创建虚拟环境（如果不存在）
if [ ! -d "venv" ]; then
    echo "创建Python虚拟环境..."
    python3 -m venv venv
fi

# 激活虚拟环境并安装依赖
echo "激活虚拟环境并安装Python依赖..."
source venv/bin/activate

# 升级pip到最新版本
echo "升级pip到最新版本..."
pip install --upgrade pip

# 安装或升级crawl4ai和其他依赖
echo "安装/升级crawl4ai和依赖..."
pip install --upgrade -r requirements.txt

# 安装Playwright浏览器
echo "安装Playwright浏览器..."
playwright install

# 显示安装的版本信息
echo ""
echo "=== 安装完成 ==="
echo "Python版本: $(python --version)"
echo "pip版本: $(pip --version)"
echo "crawl4ai版本: $(pip show crawl4ai | grep Version | cut -d' ' -f2)"

echo ""
echo "Python环境设置完成！"
echo "现在可以运行Kotlin应用程序了：./gradlew run" 