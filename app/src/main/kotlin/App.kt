package org.shengj.app

import kotlinx.coroutines.runBlocking

fun main() {

    // 运行爬虫演示
    runBlocking {
        CrawlerDemo.runAllDemos()
    }

}
