# Web爬虫集成说明

这个项目将Python的crawl4ai爬虫功能集成到了Kotlin应用中。

## 功能特性

- 🚀 **同步爬虫**: 简单的阻塞式爬虫调用
- ⚡ **异步爬虫**: 基于Kotlin协程的非阻塞爬虫
- 📦 **批量爬取**: 支持并发爬取多个URL
- ⏱️ **超时控制**: 可配置的超时机制
- 🛡️ **错误处理**: 完善的错误处理和日志记录
- 🔄 **自动重试**: 内置缓存和重试机制

## 环境要求

- **Java**: JDK 21+
- **Python**: Python 3.7+
- **pip**: Python包管理器
- **系统**: macOS, Linux, Windows

## 快速开始

### 1. 设置Python环境

```bash
# 运行自动安装脚本（推荐）
./setup_python_env.sh

# 或者手动安装
python3 -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
playwright install
```

### 2. 构建并运行Kotlin项目

```bash
# 构建项目
./gradlew build

# 运行应用（包含演示）
./gradlew run
```

## 使用方法

### 同步爬虫

```kotlin
val crawler = WebCrawler()
val markdown = crawler.crawlToMarkdown("https://example.com")
println("爬取结果: $markdown")
```

### 异步爬虫

```kotlin
runBlocking {
    val crawler = AsyncWebCrawler()
    val markdown = crawler.crawlToMarkdown("https://example.com", timeoutSeconds = 30)
    println("爬取结果: $markdown")
}
```

### 批量爬取

```kotlin
runBlocking {
    val crawler = AsyncWebCrawler()
    val urls = listOf(
        "https://example.com",
        "https://httpbin.org/html",
        "https://www.wikipedia.org"
    )
    val results = crawler.crawlMultipleUrls(urls, concurrency = 3)
    
    results.forEach { (url, content) ->
        println("$url: ${content.length} 字符")
    }
}
```

## 项目结构

```
app/
├── src/main/kotlin/
│   ├── App.kt                 # 主应用程序
│   ├── WebCrawler.kt          # 同步爬虫类
│   ├── AsyncWebCrawler.kt     # 异步爬虫类
│   └── CrawlerDemo.kt         # 演示代码
├── src/main/resources/
│   └── web_crawler.py         # Python爬虫脚本
venv/                          # Python虚拟环境
requirements.txt               # Python依赖
setup_python_env.sh           # 环境设置脚本
CRAWLER_README.md             # 本文档
```

## 配置选项

Python爬虫脚本支持以下配置（在`web_crawler.py`中修改）：

- `excluded_tags`: 排除的HTML标签（默认：nav, footer, aside）
- `remove_overlay_elements`: 是否移除覆盖元素（默认：true）
- `threshold`: 内容过滤阈值（默认：0.48）
- `ignore_links`: 是否忽略链接（默认：true）

## 实际使用示例

### 爬取新闻网站

```kotlin
val crawler = WebCrawler()
val newsContent = crawler.crawlToMarkdown("https://news.example.com/article/123")
// 处理新闻内容...
```

### 批量爬取产品页面

```kotlin
runBlocking {
    val crawler = AsyncWebCrawler()
    val productUrls = listOf(
        "https://shop.example.com/product/1",
        "https://shop.example.com/product/2",
        "https://shop.example.com/product/3"
    )
    
    val products = crawler.crawlMultipleUrls(productUrls, concurrency = 2)
    products.forEach { (url, content) ->
        // 解析产品信息...
        println("产品页面 $url 已爬取，内容长度: ${content.length}")
    }
}
```

## 故障排除

### Python脚本找不到

确保Python脚本在正确的位置：
- 开发环境: `app/src/main/resources/web_crawler.py`
- JAR包: 自动提取到临时文件

### Python依赖问题

```bash
# 重新安装依赖
source venv/bin/activate
pip install --upgrade crawl4ai
playwright install

# 检查Python版本
python3 --version
```

### 虚拟环境问题

```bash
# 删除并重新创建虚拟环境
rm -rf venv
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
playwright install
```

### 超时问题

增加超时时间：
```kotlin
val markdown = crawler.crawlToMarkdown(url, timeoutSeconds = 120)
```

### 内容为空或很少

某些网站可能需要特殊处理：
1. 检查网站是否需要JavaScript渲染
2. 尝试不同的User-Agent
3. 检查网站的反爬虫机制

## 性能优化

1. **并发控制**: 调整`concurrency`参数（建议2-5）
2. **超时设置**: 根据网站响应速度调整超时时间
3. **缓存**: crawl4ai自动启用缓存，重复URL会更快
4. **内存管理**: 大量爬取时注意内存使用

## 扩展功能

可以通过修改`web_crawler.py`来添加更多功能：

### 自定义User-Agent
```python
config = CrawlerRunConfig(
    user_agent="Mozilla/5.0 (Custom Bot)",
    # ... 其他配置
)
```

### 添加代理支持
```python
config = CrawlerRunConfig(
    proxy="http://proxy.example.com:8080",
    # ... 其他配置
)
```

### 自定义过滤规则
```python
config = CrawlerRunConfig(
    excluded_tags=['nav', 'footer', 'aside', 'script', 'style'],
    # ... 其他配置
)
```

## 注意事项

1. **遵守robots.txt**: 请确保遵守目标网站的robots.txt规则
2. **请求频率**: 避免过于频繁的请求，以免被封IP
3. **法律合规**: 确保爬取行为符合当地法律法规
4. **资源使用**: 大量爬取时注意系统资源使用

## 许可证

本项目遵循MIT许可证。 