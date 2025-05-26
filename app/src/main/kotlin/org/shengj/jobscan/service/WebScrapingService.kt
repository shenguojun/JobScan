package org.shengj.jobscan.service

import kotlinx.coroutines.delay
import mu.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.shengj.jobscan.model.JobPosting
import org.shengj.jobscan.model.JobSite
import java.io.IOException
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

/**
 * 网页爬虫服务
 */
class WebScrapingService {
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * 获取网页内容
     */
    suspend fun fetchPageContent(url: String): String? {
        return try {
            logger.info { "正在获取页面内容: $url" }
            
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val content = response.body?.string()
                logger.info { "成功获取页面内容，长度: ${content?.length ?: 0}" }
                content
            } else {
                logger.error { "获取页面失败，状态码: ${response.code}" }
                null
            }
        } catch (e: IOException) {
            logger.error(e) { "获取页面内容时发生网络错误: $url" }
            null
        } catch (e: Exception) {
            logger.error(e) { "获取页面内容时发生未知错误: $url" }
            null
        }
    }
    
    /**
     * 计算内容哈希值
     */
    fun calculateContentHash(content: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(content.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * 解析招聘信息
     */
    fun parseJobPostings(content: String, jobSite: JobSite): List<JobPosting> {
        return try {
            val document = Jsoup.parse(content)
            val jobPostings = mutableListOf<JobPosting>()
            
            // 如果有自定义选择器，使用自定义解析
            if (!jobSite.selector.isNullOrBlank()) {
                jobPostings.addAll(parseWithCustomSelector(document, jobSite))
            } else {
                // 使用通用解析策略
                jobPostings.addAll(parseWithGenericStrategy(document, jobSite))
            }
            
            logger.info { "从 ${jobSite.name} 解析出 ${jobPostings.size} 个招聘信息" }
            jobPostings
        } catch (e: Exception) {
            logger.error(e) { "解析招聘信息时发生错误: ${jobSite.name}" }
            emptyList()
        }
    }
    
    /**
     * 使用自定义选择器解析
     */
    private fun parseWithCustomSelector(document: Document, jobSite: JobSite): List<JobPosting> {
        val jobPostings = mutableListOf<JobPosting>()
        val selector = jobSite.selector ?: return jobPostings
        
        try {
            val elements = document.select(selector)
            
            for (element in elements) {
                val jobPosting = extractJobInfoFromElement(element, jobSite)
                if (jobPosting != null) {
                    jobPostings.add(jobPosting)
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "使用自定义选择器解析失败: ${jobSite.selector}" }
        }
        
        return jobPostings
    }
    
    /**
     * 使用通用策略解析
     */
    private fun parseWithGenericStrategy(document: Document, jobSite: JobSite): List<JobPosting> {
        val jobPostings = mutableListOf<JobPosting>()
        
        // 尝试常见的招聘信息选择器
        val commonSelectors = listOf(
            ".job-item", ".job-listing", ".position", ".vacancy",
            "[class*='job']", "[class*='position']", "[class*='career']",
            "li:has(a[href*='job'])", "div:has(a[href*='position'])"
        )
        
        for (selector in commonSelectors) {
            try {
                val elements = document.select(selector)
                if (elements.isNotEmpty()) {
                    logger.info { "使用选择器 '$selector' 找到 ${elements.size} 个元素" }
                    
                    for (element in elements.take(50)) { // 限制最多50个
                        val jobPosting = extractJobInfoFromElement(element, jobSite)
                        if (jobPosting != null) {
                            jobPostings.add(jobPosting)
                        }
                    }
                    
                    if (jobPostings.isNotEmpty()) {
                        break // 找到有效内容就停止尝试其他选择器
                    }
                }
            } catch (e: Exception) {
                logger.debug(e) { "选择器 '$selector' 解析失败" }
            }
        }
        
        return jobPostings
    }
    
    /**
     * 从元素中提取招聘信息
     */
    private fun extractJobInfoFromElement(element: Element, jobSite: JobSite): JobPosting? {
        return try {
            // 提取标题
            val title = extractTitle(element) ?: return null
            
            // 提取公司名称
            val company = extractCompany(element)
            
            // 提取地点
            val location = extractLocation(element)
            
            // 提取薪资
            val salary = extractSalary(element)
            
            // 提取描述
            val description = extractDescription(element)
            
            // 提取链接
            val url = extractUrl(element, jobSite.url)
            
            JobPosting(
                siteId = jobSite.id,
                title = title,
                company = company,
                location = location,
                salary = salary,
                description = description,
                url = url,
                discoveredAt = LocalDateTime.now()
            )
        } catch (e: Exception) {
            logger.debug(e) { "提取招聘信息失败" }
            null
        }
    }
    
    private fun extractTitle(element: Element): String? {
        val titleSelectors = listOf(
            "h1", "h2", "h3", "h4", ".title", ".job-title", ".position-title",
            "a[href*='job']", "a[href*='position']", "[class*='title']"
        )
        
        for (selector in titleSelectors) {
            val titleElement = element.selectFirst(selector)
            if (titleElement != null) {
                val title = titleElement.text().trim()
                if (title.isNotBlank() && title.length > 3) {
                    return title
                }
            }
        }
        
        // 如果没找到，尝试使用元素本身的文本
        val text = element.text().trim()
        if (text.isNotBlank() && text.length in 5..200) {
            return text.split("\n").firstOrNull()?.trim()
        }
        
        return null
    }
    
    private fun extractCompany(element: Element): String? {
        val companySelectors = listOf(
            ".company", ".company-name", ".employer", "[class*='company']"
        )
        
        for (selector in companySelectors) {
            val companyElement = element.selectFirst(selector)
            if (companyElement != null) {
                val company = companyElement.text().trim()
                if (company.isNotBlank()) {
                    return company
                }
            }
        }
        
        return null
    }
    
    private fun extractLocation(element: Element): String? {
        val locationSelectors = listOf(
            ".location", ".city", ".address", "[class*='location']", "[class*='city']"
        )
        
        for (selector in locationSelectors) {
            val locationElement = element.selectFirst(selector)
            if (locationElement != null) {
                val location = locationElement.text().trim()
                if (location.isNotBlank()) {
                    return location
                }
            }
        }
        
        return null
    }
    
    private fun extractSalary(element: Element): String? {
        val salarySelectors = listOf(
            ".salary", ".pay", ".wage", "[class*='salary']", "[class*='pay']"
        )
        
        for (selector in salarySelectors) {
            val salaryElement = element.selectFirst(selector)
            if (salaryElement != null) {
                val salary = salaryElement.text().trim()
                if (salary.isNotBlank()) {
                    return salary
                }
            }
        }
        
        // 在文本中查找薪资信息
        val text = element.text()
        val salaryPattern = Regex("""(\d+[kK]?[-~]\d+[kK]?|\d+[kK]?\+|面议|薪资面议)""")
        val match = salaryPattern.find(text)
        return match?.value
    }
    
    private fun extractDescription(element: Element): String? {
        val descriptionSelectors = listOf(
            ".description", ".summary", ".content", "[class*='desc']"
        )
        
        for (selector in descriptionSelectors) {
            val descElement = element.selectFirst(selector)
            if (descElement != null) {
                val desc = descElement.text().trim()
                if (desc.isNotBlank() && desc.length > 10) {
                    return desc.take(500) // 限制长度
                }
            }
        }
        
        return null
    }
    
    private fun extractUrl(element: Element, baseUrl: String): String? {
        val linkElement = element.selectFirst("a[href]")
        if (linkElement != null) {
            val href = linkElement.attr("href")
            if (href.isNotBlank()) {
                return if (href.startsWith("http")) {
                    href
                } else {
                    // 相对链接转绝对链接
                    val base = if (baseUrl.endsWith("/")) baseUrl.dropLast(1) else baseUrl
                    val path = if (href.startsWith("/")) href else "/$href"
                    "$base$path"
                }
            }
        }
        
        return null
    }
    
    /**
     * 添加延迟以避免过于频繁的请求
     */
    suspend fun addDelay(delayMs: Long = 1000) {
        delay(delayMs)
    }
} 