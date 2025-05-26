package org.shengj.app

object CrawlerDemo {
    
    /**
     * 演示同步爬虫
     */
    fun demonstrateSyncCrawler() {
        println("\n=== 同步爬虫演示 ===")
        val crawler = WebCrawler()
        
        val url = "https://www.gd.gov.cn/xxts/content/post_4663415.html"
        println("正在爬取URL: $url")
        println("请稍等...")
        
        val startTime = System.currentTimeMillis()
        val markdown = crawler.crawlToMarkdown(url)
        val endTime = System.currentTimeMillis()
        
        println("\n爬取完成，耗时: ${endTime - startTime}ms")
        println("结果长度: ${markdown.length} 字符")
        println("前500字符预览:")
        println("-".repeat(50))
        println(markdown.take(500))
        if (markdown.length > 500) {
            println("...")
        }
        println("-".repeat(50))
    }
    
    /**
     * 演示异步爬虫
     */
    suspend fun demonstrateAsyncCrawler() {
        println("\n=== 异步爬虫演示 ===")
        val crawler = AsyncWebCrawler()
        
        val url = "https://www.gd.gov.cn/xxts/content/post_4663415.html"
        println("正在异步爬取URL: $url")
        println("请稍等...")
        
        val startTime = System.currentTimeMillis()
        val markdown = crawler.crawlToMarkdown(url, timeoutSeconds = 30)
        val endTime = System.currentTimeMillis()
        
        println("\n异步爬取完成，耗时: ${endTime - startTime}ms")
        println("结果长度: ${markdown.length} 字符")
        println("前500字符预览:")
        println("-".repeat(50))
        println(markdown.take(500))
        if (markdown.length > 500) {
            println("...")
        }
        println("-".repeat(50))
    }
    
    /**
     * 演示批量爬取
     */
    suspend fun demonstrateBatchCrawling() {
        println("\n=== 批量爬取演示 ===")
        val crawler = AsyncWebCrawler()
        
        val urls = listOf(
            "https://www.gd.gov.cn/xxts/content/post_4663415.html",
            "https://example.com",  // 这个可能会失败，用于演示错误处理
        )
        
        println("正在批量爬取 ${urls.size} 个URL...")
        println("URL列表:")
        urls.forEachIndexed { index, url ->
            println("${index + 1}. $url")
        }
        
        val startTime = System.currentTimeMillis()
        val results = crawler.crawlMultipleUrls(urls, concurrency = 2, timeoutSeconds = 30)
        val endTime = System.currentTimeMillis()
        
        println("\n批量爬取完成，总耗时: ${endTime - startTime}ms")
        println("成功爬取: ${results.count { !it.value.startsWith("Error") }} / ${urls.size}")
        
        results.forEach { (url, content) ->
            println("\n--- $url ---")
            if (content.startsWith("Error")) {
                println("❌ 爬取失败: $content")
            } else {
                println("✅ 爬取成功，内容长度: ${content.length} 字符")
                println("前200字符预览: ${content.take(200)}...")
            }
        }
    }
    
    /**
     * 运行所有演示
     */
    suspend fun runAllDemos() {
        try {
            demonstrateSyncCrawler()
            demonstrateAsyncCrawler()
            demonstrateBatchCrawling()
        } catch (e: Exception) {
            println("演示过程中出现错误: ${e.message}")
            e.printStackTrace()
        }
    }
} 