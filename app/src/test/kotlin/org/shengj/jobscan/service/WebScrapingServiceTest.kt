package org.shengj.jobscan.service

import org.shengj.jobscan.model.JobSite
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WebScrapingServiceTest {
    
    private val webScrapingService = WebScrapingService()
    
    @Test
    fun testCalculateContentHash() {
        val content1 = "Hello World"
        val content2 = "Hello World"
        val content3 = "Hello Kotlin"
        
        val hash1 = webScrapingService.calculateContentHash(content1)
        val hash2 = webScrapingService.calculateContentHash(content2)
        val hash3 = webScrapingService.calculateContentHash(content3)
        
        // 相同内容应该产生相同的哈希
        assertTrue(hash1 == hash2)
        
        // 不同内容应该产生不同的哈希
        assertTrue(hash1 != hash3)
        
        // 哈希长度应该是64个字符（SHA-256）
        assertTrue(hash1.length == 64)
    }
    
    @Test
    fun testParseJobPostings() {
        val htmlContent = """
            <html>
                <body>
                    <div class="job-item">
                        <h3 class="title">Kotlin开发工程师</h3>
                        <div class="company">科技公司</div>
                        <div class="location">北京</div>
                        <div class="salary">15k-25k</div>
                        <a href="/job/123">查看详情</a>
                    </div>
                    <div class="job-item">
                        <h3 class="title">Java后端开发</h3>
                        <div class="company">互联网公司</div>
                        <div class="location">上海</div>
                        <div class="salary">20k-30k</div>
                        <a href="/job/456">查看详情</a>
                    </div>
                </body>
            </html>
        """.trimIndent()
        
        val jobSite = JobSite(
            id = 1,
            name = "测试网站",
            url = "https://example.com",
            selector = ".job-item"
        )
        
        val jobPostings = webScrapingService.parseJobPostings(htmlContent, jobSite)
        
        // 应该解析出2个职位
        assertTrue(jobPostings.size == 2)
        
        // 检查第一个职位
        val firstJob = jobPostings[0]
        assertTrue(firstJob.title == "Kotlin开发工程师")
        assertTrue(firstJob.company == "科技公司")
        assertTrue(firstJob.location == "北京")
        assertTrue(firstJob.salary == "15k-25k")
        assertNotNull(firstJob.url)
        
        // 检查第二个职位
        val secondJob = jobPostings[1]
        assertTrue(secondJob.title == "Java后端开发")
        assertTrue(secondJob.company == "互联网公司")
        assertTrue(secondJob.location == "上海")
        assertTrue(secondJob.salary == "20k-30k")
        assertNotNull(secondJob.url)
    }
} 