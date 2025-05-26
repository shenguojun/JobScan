import org.shengj.app.WebCrawler
import org.shengj.app.AsyncWebCrawler
import kotlinx.coroutines.*

fun main() {
    println("=== Web爬虫功能测试 ===")
    
    // 测试同步爬虫
    println("\n1. 测试同步爬虫:")
    val syncCrawler = WebCrawler()
    val syncResult = syncCrawler.crawlToMarkdown("https://www.gd.gov.cn/xxts/content/post_4663415.html")
    println("同步爬虫结果长度: ${syncResult.length}")
    println("前200字符: ${syncResult.take(200)}")
    
    // 测试异步爬虫
    println("\n2. 测试异步爬虫:")
    runBlocking {
        val asyncCrawler = AsyncWebCrawler()
        val asyncResult = asyncCrawler.crawlToMarkdown("https://www.gd.gov.cn/xxts/content/post_4663415.html")
        println("异步爬虫结果长度: ${asyncResult.length}")
        println("前200字符: ${asyncResult.take(200)}")
    }
    
    println("\n测试完成！")
} 