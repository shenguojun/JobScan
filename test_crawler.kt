import org.shengj.app.WebCrawler
import org.shengj.app.AsyncWebCrawler
import kotlinx.coroutines.*

fun main() {
    println("=== Web爬虫功能测试 ===")

    // 测试同步爬虫
    println("\n1. 测试同步爬虫:")
    val syncCrawler = WebCrawler()
    val syncResult = syncCrawler.crawlToMarkdown("https://example.com")
    println("同步爬虫结果长度: ${syncResult.length}")
    println("前400字符: ${syncResult.take(400)}")

    // 测试异步爬虫
    println("\n2. 测试异步爬虫:")
    runBlocking {
        val asyncCrawler = AsyncWebCrawler()
        val asyncResult = asyncCrawler.crawlToMarkdown("https://example.com")
        println("异步爬虫结果长度: ${asyncResult.length}")
        println("前400字符: ${asyncResult.take(400)}")
    }

    println("\n测试完成！")
}