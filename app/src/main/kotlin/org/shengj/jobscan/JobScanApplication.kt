package org.shengj.jobscan

import mu.KotlinLogging
import org.shengj.jobscan.cli.JobScanCLI
import org.shengj.jobscan.database.DatabaseManager
import org.shengj.jobscan.repository.JobScanRepository
import org.shengj.jobscan.scheduler.JobScanScheduler
import org.shengj.jobscan.service.JobScanService
import org.shengj.jobscan.service.WebScrapingService

private val logger = KotlinLogging.logger {}

/**
 * 求职扫描应用程序主类
 */
class JobScanApplication {
    
    private lateinit var databaseManager: DatabaseManager
    private lateinit var repository: JobScanRepository
    private lateinit var webScrapingService: WebScrapingService
    private lateinit var jobScanService: JobScanService
    private lateinit var scheduler: JobScanScheduler
    private lateinit var cli: JobScanCLI
    
    /**
     * 初始化应用程序
     */
    fun initialize() {
        logger.info { "正在初始化求职扫描应用程序..." }
        
        try {
            // 初始化数据库
            databaseManager = DatabaseManager()
            databaseManager.initialize()
            
            // 初始化仓库
            repository = JobScanRepository()
            
            // 初始化服务
            webScrapingService = WebScrapingService()
            jobScanService = JobScanService(repository, webScrapingService)
            
            // 初始化调度器
            scheduler = JobScanScheduler(jobScanService)
            
            // 初始化命令行界面
            cli = JobScanCLI(jobScanService, scheduler)
            
            logger.info { "求职扫描应用程序初始化完成" }
            
        } catch (e: Exception) {
            logger.error(e) { "初始化应用程序失败" }
            throw e
        }
    }
    
    /**
     * 启动应用程序
     */
    fun start() {
        logger.info { "启动求职扫描应用程序" }
        
        try {
            // 添加一些示例网站（如果数据库为空）
            initializeSampleSites()
            
            // 启动命令行界面
            cli.start()
            
        } catch (e: Exception) {
            logger.error(e) { "启动应用程序失败" }
            throw e
        }
    }
    
    /**
     * 初始化示例网站
     */
    private fun initializeSampleSites() {
        val existingSites = jobScanService.getAllJobSites()
        if (existingSites.isEmpty()) {
            logger.info { "数据库为空，添加示例招聘网站" }
            
            // 添加一些示例网站
            val sampleSites = listOf(
                Triple("拉勾网-Kotlin", "https://www.lagou.com/jobs/list_kotlin", ".con_list_item"),
                Triple("Boss直聘-Java", "https://www.zhipin.com/job_detail/?query=java", ".job-list li"),
                Triple("智联招聘-Python", "https://sou.zhaopin.com/jobs/searchresult.ashx?kw=python", ".newlist li")
            )
            
            sampleSites.forEach { (name, url, selector) ->
                try {
                    jobScanService.addJobS2ite(name, url, selector, 60)
                    logger.info { "添加示例网站: $name" }
                } catch (e: Exception) {
                    logger.warn(e) { "添加示例网站失败: $name" }
                }
            }
        }
    }
    
    /**
     * 停止应用程序
     */
    fun stop() {
        logger.info { "停止求职扫描应用程序" }
        
        try {
            scheduler.stop()
            logger.info { "应用程序已停止" }
        } catch (e: Exception) {
            logger.error(e) { "停止应用程序时发生错误" }
        }
    }
} 