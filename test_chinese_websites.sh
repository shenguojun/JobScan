#!/bin/bash

echo "=== 中文网站爬取测试 ==="
echo ""

# 激活虚拟环境
source venv/bin/activate

# 测试URL列表
urls=(
    "https://www.gd.gov.cn/xxts/content/post_4663415.html"
    "https://www.gov.cn"
    "https://www.xinhuanet.com"
)

echo "测试URL列表:"
for i in "${!urls[@]}"; do
    echo "$((i+1)). ${urls[i]}"
done
echo ""

# 测试每个URL
for i in "${!urls[@]}"; do
    url="${urls[i]}"
    echo "--- 测试 $((i+1)): $url ---"
    
    # 获取内容并统计字符数
    content=$(python app/src/main/resources/web_crawler.py "$url" 2>/dev/null)
    char_count=$(echo "$content" | wc -c | tr -d ' ')
    
    if [ "$char_count" -gt 500 ]; then
        echo "✅ 成功: $char_count 字符"
        echo "前100字符预览: $(echo "$content" | head -c 100)..."
    elif [ "$char_count" -gt 100 ]; then
        echo "⚠️  部分成功: $char_count 字符"
        echo "内容预览: $(echo "$content" | head -c 100)..."
    else
        echo "❌ 失败: 只获取到 $char_count 字符"
        echo "内容: $content"
    fi
    echo ""
done

echo "=== 测试完成 ===" 