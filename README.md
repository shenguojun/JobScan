# 求职信息监控系统 (JobScan)

一个用Kotlin开发的智能求职信息监控应用，帮助你自动监控多个招聘网站的新增职位信息。

## 功能特性

- 🔍 **智能网页爬虫**: 自动获取招聘网站内容，支持自定义CSS选择器
- 📊 **内容变化检测**: 通过内容哈希对比检测页面变化
- 🗄️ **数据持久化**: 使用SQLite数据库存储招聘信息和页面快照
- ⏰ **定时调度**: 支持定时自动扫描，可自定义扫描间隔
- 📱 **命令行界面**: 友好的交互式命令行界面
- 📈 **扫描报告**: 生成详细的扫描报告，包含新增职位统计
- 🔄 **并发处理**: 支持多网站并发扫描，提高效率

## 技术架构

### 核心技术栈
- **Kotlin**: 主要开发语言
- **JSoup**: HTML解析和网页爬虫
- **OkHttp**: HTTP客户端
- **Exposed**: Kotlin数据库ORM框架
- **SQLite**: 轻量级数据库
- **Quartz**: 任务调度框架
- **Logback**: 日志框架
- **Kotlin Coroutines**: 异步编程

### 项目结构
```
app/src/main/kotlin/org/shengj/jobscan/
├── model/              # 数据模型
│   └── JobSite.kt     # 招聘网站、页面快照、招聘信息等模型
├── database/           # 数据库层
│   ├── DatabaseTables.kt    # 数据库表定义
│   └── DatabaseManager.kt  # 数据库管理器
├── repository/         # 数据访问层
│   └── JobScanRepository.kt # 数据仓库
├── service/           # 业务逻辑层
│   ├── WebScrapingService.kt # 网页爬虫服务
│   └── JobScanService.kt     # 核心扫描服务
├── scheduler/         # 任务调度
│   └── JobScanScheduler.kt   # 调度器
├── cli/              # 命令行界面
│   └── JobScanCLI.kt        # 交互式命令行
└── JobScanApplication.kt    # 应用程序主类
```

## 快速开始

### 环境要求
- Java 21+
- Gradle 8.7+

### 运行应用
```bash
# 克隆项目
git clone <repository-url>
cd JobScan

# 运行应用
./gradlew run
```

### 使用指南

1. **启动应用**: 运行后会显示交互式菜单
2. **添加招聘网站**: 选择选项1，输入网站名称、URL和可选的CSS选择器
3. **立即扫描**: 选择选项3，手动执行一次扫描
4. **启动定时调度**: 选择选项5，开启自动定时扫描
5. **查看结果**: 选择选项4查看最近发现的招聘信息

### 示例网站配置

应用启动时会自动添加一些示例网站：

| 网站名称 | URL | CSS选择器 |
|---------|-----|----------|
| 拉勾网-Kotlin | https://www.lagou.com/jobs/list_kotlin | .con_list_item |
| Boss直聘-Java | https://www.zhipin.com/job_detail/?query=java | .job-list li |
| 智联招聘-Python | https://sou.zhaopin.com/jobs/searchresult.ashx?kw=python | .newlist li |

## 核心功能详解

### 1. 网页爬虫
- 支持自定义User-Agent
- 智能重试机制
- 请求间隔控制，避免被反爬
- 支持相对链接转绝对链接

### 2. 内容解析
- **自定义选择器**: 支持CSS选择器精确提取招聘信息
- **通用解析策略**: 自动尝试常见的招聘信息选择器
- **信息提取**: 自动提取职位标题、公司、地点、薪资等信息

### 3. 变化检测
- 使用SHA-256哈希算法检测页面内容变化
- 只有内容发生变化时才进行详细解析
- 避免重复处理相同内容

### 4. 数据存储
- **招聘网站表**: 存储网站配置信息
- **页面快照表**: 存储页面内容和哈希值
- **招聘信息表**: 存储解析出的招聘信息

### 5. 任务调度
- 基于Quartz框架的定时任务
- 支持动态调整扫描间隔
- 手动触发扫描功能

## 配置说明

### 日志配置
日志配置文件位于 `app/src/main/resources/logback.xml`：
- 控制台输出: INFO级别
- 文件输出: 保存到 `logs/jobscan.log`
- 自动滚动: 按日期和大小滚动

### 数据库
- 使用SQLite数据库，文件名: `jobscan.db`
- 自动创建表结构
- 支持数据持久化

## 扩展功能

### 添加新的招聘网站
1. 通过命令行界面添加
2. 提供网站URL和可选的CSS选择器
3. 系统会自动尝试解析招聘信息

### 自定义解析规则
可以为特定网站提供CSS选择器来精确提取招聘信息：
```
# 示例选择器
.job-item                    # 职位容器
.job-item .title            # 职位标题
.job-item .company          # 公司名称
.job-item .location         # 工作地点
.job-item .salary           # 薪资信息
```

## 注意事项

1. **合规使用**: 请遵守网站的robots.txt和使用条款
2. **请求频率**: 系统已内置请求间隔，避免对目标网站造成压力
3. **网站变化**: 招聘网站可能会更改页面结构，需要相应调整选择器
4. **反爬机制**: 某些网站可能有反爬虫机制，需要适当调整策略

## 开发计划

- [ ] Web界面支持
- [ ] 邮件通知功能
- [ ] 更多招聘网站适配
- [ ] 职位去重功能
- [ ] 关键词过滤
- [ ] 数据导出功能
- [ ] Docker容器化

## 贡献指南

欢迎提交Issue和Pull Request来改进这个项目！

## 许可证

MIT License