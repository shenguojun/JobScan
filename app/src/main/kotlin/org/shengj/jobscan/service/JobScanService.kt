package org.shengj.jobscan.service

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import org.shengj.jobscan.model.JobSite
import org.shengj.jobscan.model.PageSnapshot
import org.shengj.jobscan.model.ScanResult
import org.shengj.jobscan.repository.JobScanRepository
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

/**
 * 求职扫描服务
 */
class JobScanService(
    private val repository: JobScanRepository,
    private val webScrapingService: WebScrapingService
) {
    
    /**
     * 扫描所有活跃的招聘网站
     */
    suspend fun scanAllActiveSites(): List<ScanResult> = coroutineScope {
        logger.info { "开始扫描所有活跃的招聘网站" }
        
        val activeSites = repository.getActiveJobSites()
        logger.info { "找到 ${activeSites.size} 个活跃的招聘网站" }
        
        if (activeSites.isEmpty()) {
            logger.warn { "没有找到活跃的招聘网站" }
            return@coroutineScope emptyList()
        }
        
        // 并发扫描所有网站
        val scanJobs = activeSites.map { site ->
            async {
                try {
                    scanSingleSite(site)
                } catch (e: Exception) {
                    logger.error(e) { "扫描网站 ${site.name} 时发生错误" }
                    ScanResult(
                        siteId = site.id,
                        siteName = site.name,
                        hasChanges = false,
                        newJobsCount = 0,
                        error = e.message
                    )
                }
            }
        }
        
        val results = scanJobs.awaitAll()
        
        val successCount = results.count { it.error == null }
        val totalNewJobs = results.sumOf { it.newJobsCount }
        
        logger.info { "扫描完成: $successCount/${results.size} 个网站成功，共发现 $totalNewJobs 个新职位" }
        
        results
    }
    
    /**
     * 扫描单个招聘网站
     */
    suspend fun scanSingleSite(jobSite: JobSite): ScanResult {
        logger.info { "开始扫描网站: ${jobSite.name} (${jobSite.url})" }
        
        try {
            // 获取当前页面内容
            val currentContent = webScrapingService.fetchPageContent(jobSite.url)
            if (currentContent == null) {
                return ScanResult(
                    siteId = jobSite.id,
                    siteName = jobSite.name,
                    hasChanges = false,
                    newJobsCount = 0,
                    error = "无法获取页面内容"
                )
            }
            
            // 计算内容哈希
            val currentHash = webScrapingService.calculateContentHash(currentContent)
            
            // 获取上次的页面快照
            val lastSnapshot = repository.getLatestPageSnapshot(jobSite.id)
            
            // 检查是否有变化
            val hasChanges = lastSnapshot == null || lastSnapshot.contentHash != currentHash
            
            if (!hasChanges) {
                logger.info { "网站 ${jobSite.name} 内容无变化" }
                // 更新最后检查时间
                repository.updateJobSiteLastChecked(jobSite.id, LocalDateTime.now())
                
                return ScanResult(
                    siteId = jobSite.id,
                    siteName = jobSite.name,
                    hasChanges = false,
                    newJobsCount = 0
                )
            }
            
            logger.info { "网站 ${jobSite.name} 检测到内容变化，开始解析新的招聘信息" }
            
            // 保存新的页面快照
            val newSnapshot = PageSnapshot(
                siteId = jobSite.id,
                contentHash = currentHash,
                content = currentContent
            )
            repository.savePageSnapshot(newSnapshot)
            
            // 解析招聘信息
            val newJobPostings = webScrapingService.parseJobPostings(currentContent, jobSite)
            
            // 保存新的招聘信息
            if (newJobPostings.isNotEmpty()) {
                repository.saveJobPostings(newJobPostings)
                logger.info { "保存了 ${newJobPostings.size} 个新的招聘信息" }
            }
            
            // 更新最后检查时间
            repository.updateJobSiteLastChecked(jobSite.id, LocalDateTime.now())
            
            // 添加延迟避免过于频繁的请求
            webScrapingService.addDelay(2000)
            
            return ScanResult(
                siteId = jobSite.id,
                siteName = jobSite.name,
                hasChanges = true,
                newJobsCount = newJobPostings.size,
                newJobs = newJobPostings
            )
            
        } catch (e: Exception) {
            logger.error(e) { "扫描网站 ${jobSite.name} 时发生错误" }
            return ScanResult(
                siteId = jobSite.id,
                siteName = jobSite.name,
                hasChanges = false,
                newJobsCount = 0,
                error = e.message
            )
        }
    }
    
    /**
     * 添加新的招聘网站
     */
    fun addJobSite(
        name: String,
        url: String,
        selector: String? = null,
        checkInterval: Int = 60
    ): Long {
        logger.info { "添加新的招聘网站: $name ($url)" }
        
        val jobSite = JobSite(
            name = name,
            url = url,
            selector = selector,
            checkInterval = checkInterval
        )
        
        return repository.createJobSite(jobSite)
    }
    
    /**
     * 获取所有招聘网站
     */
    fun getAllJobSites(): List<JobSite> {
        return repository.getAllJobSites()
    }
    
    /**
     * 获取最近的招聘信息
     */
    fun getRecentJobPostings(limit: Int = 50) = repository.getRecentJobPostings(limit)
    
    /**
     * 获取指定网站的招聘信息
     */
    fun getJobPostingsBySite(siteId: Long, limit: Int = 20) = 
        repository.getJobPostingsBySite(siteId, limit)
    
    /**
     * 生成扫描报告
     */
    fun generateScanReport(scanResults: List<ScanResult>): String {
        val report = StringBuilder()
        report.appendLine("=== 求职信息扫描报告 ===")
        report.appendLine("扫描时间: ${LocalDateTime.now()}")
        report.appendLine()
        
        val successfulScans = scanResults.filter { it.error == null }
        val failedScans = scanResults.filter { it.error != null }
        val totalNewJobs = successfulScans.sumOf { it.newJobsCount }
        val sitesWithChanges = successfulScans.count { it.hasChanges }
        
        report.appendLine("总体统计:")
        report.appendLine("- 扫描网站数: ${scanResults.size}")
        report.appendLine("- 成功扫描: ${successfulScans.size}")
        report.appendLine("- 失败扫描: ${failedScans.size}")
        report.appendLine("- 有变化的网站: $sitesWithChanges")
        report.appendLine("- 新增职位总数: $totalNewJobs")
        report.appendLine()
        
        if (successfulScans.isNotEmpty()) {
            report.appendLine("成功扫描的网站:")
            successfulScans.forEach { result ->
                report.appendLine("- ${result.siteName}: ${if (result.hasChanges) "发现 ${result.newJobsCount} 个新职位" else "无变化"}")
            }
            report.appendLine()
        }
        
        if (failedScans.isNotEmpty()) {
            report.appendLine("扫描失败的网站:")
            failedScans.forEach { result ->
                report.appendLine("- ${result.siteName}: ${result.error}")
            }
            report.appendLine()
        }
        
        if (totalNewJobs > 0) {
            report.appendLine("新增职位详情:")
            successfulScans.filter { it.newJobs.isNotEmpty() }.forEach { result ->
                report.appendLine("${result.siteName}:")
                result.newJobs.take(5).forEach { job -> // 只显示前5个
                    report.appendLine("  - ${job.title}")
                    if (job.company != null) report.appendLine("    公司: ${job.company}")
                    if (job.location != null) report.appendLine("    地点: ${job.location}")
                    if (job.salary != null) report.appendLine("    薪资: ${job.salary}")
                    if (job.url != null) report.appendLine("    链接: ${job.url}")
                    report.appendLine()
                }
                if (result.newJobs.size > 5) {
                    report.appendLine("  ... 还有 ${result.newJobs.size - 5} 个职位")
                }
                report.appendLine()
            }
        }
        
        return report.toString()
    }
} 