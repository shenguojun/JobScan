package org.shengj.jobscan.cli

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.shengj.jobscan.scheduler.JobScanScheduler
import org.shengj.jobscan.service.JobScanService
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

/**
 * 求职扫描命令行界面
 */
class JobScanCLI(
    private val jobScanService: JobScanService,
    private val scheduler: JobScanScheduler
) {
    
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    
    fun start() {
        println("=== 求职信息监控系统 ===")
        println("欢迎使用求职信息监控系统！")
        println()
        
        while (true) {
            showMenu()
            val choice = readLine()?.trim()
            
            when (choice) {
                "1" -> addJobSite()
                "2" -> listJobSites()
                "3" -> runScanNow()
                "4" -> viewRecentJobs()
                "5" -> startScheduler()
                "6" -> stopScheduler()
                "7" -> triggerScanNow()
                "8" -> updateScheduleInterval()
                "9" -> viewJobsBySite()
                "0" -> {
                    println("正在退出系统...")
                    scheduler.stop()
                    break
                }
                else -> println("无效的选择，请重新输入")
            }
            
            println()
        }
    }
    
    private fun showMenu() {
        println("请选择操作:")
        println("1. 添加招聘网站")
        println("2. 查看所有招聘网站")
        println("3. 立即执行扫描")
        println("4. 查看最近的招聘信息")
        println("5. 启动定时调度器")
        println("6. 停止定时调度器")
        println("7. 手动触发扫描")
        println("8. 更新调度间隔")
        println("9. 查看指定网站的招聘信息")
        println("0. 退出")
        print("请输入选择 (0-9): ")
    }
    
    private fun addJobSite() {
        println("\n=== 添加招聘网站 ===")
        
        print("请输入网站名称: ")
        val name = readLine()?.trim()
        if (name.isNullOrBlank()) {
            println("网站名称不能为空")
            return
        }
        
        print("请输入网站URL: ")
        val url = readLine()?.trim()
        if (url.isNullOrBlank()) {
            println("网站URL不能为空")
            return
        }
        
        print("请输入CSS选择器 (可选，直接回车跳过): ")
        val selector = readLine()?.trim()?.takeIf { it.isNotBlank() }
        
        print("请输入检查间隔(分钟，默认60): ")
        val intervalInput = readLine()?.trim()
        val interval = intervalInput?.toIntOrNull() ?: 60
        
        try {
            val siteId = jobScanService.addJobSite(name, url, selector, interval)
            println("成功添加招聘网站，ID: $siteId")
        } catch (e: Exception) {
            println("添加招聘网站失败: ${e.message}")
            logger.error(e) { "添加招聘网站失败" }
        }
    }
    
    private fun listJobSites() {
        println("\n=== 所有招聘网站 ===")
        
        try {
            val sites = jobScanService.getAllJobSites()
            if (sites.isEmpty()) {
                println("暂无招聘网站")
                return
            }
            
            sites.forEach { site ->
                println("ID: ${site.id}")
                println("名称: ${site.name}")
                println("URL: ${site.url}")
                println("选择器: ${site.selector ?: "无"}")
                println("状态: ${if (site.isActive) "活跃" else "禁用"}")
                println("检查间隔: ${site.checkInterval} 分钟")
                println("最后检查: ${site.lastChecked?.format(dateFormatter) ?: "从未检查"}")
                println("创建时间: ${site.createdAt.format(dateFormatter)}")
                println("---")
            }
        } catch (e: Exception) {
            println("获取招聘网站列表失败: ${e.message}")
            logger.error(e) { "获取招聘网站列表失败" }
        }
    }
    
    private fun runScanNow() {
        println("\n=== 立即执行扫描 ===")
        
        try {
            runBlocking {
                val scanResults = jobScanService.scanAllActiveSites()
                val report = jobScanService.generateScanReport(scanResults)
                println(report)
            }
        } catch (e: Exception) {
            println("执行扫描失败: ${e.message}")
            logger.error(e) { "执行扫描失败" }
        }
    }
    
    private fun viewRecentJobs() {
        println("\n=== 最近的招聘信息 ===")
        
        print("请输入要显示的数量 (默认20): ")
        val limitInput = readLine()?.trim()
        val limit = limitInput?.toIntOrNull() ?: 20
        
        try {
            val jobs = jobScanService.getRecentJobPostings(limit)
            if (jobs.isEmpty()) {
                println("暂无招聘信息")
                return
            }
            
            jobs.forEach { job ->
                println("标题: ${job.title}")
                if (job.company != null) println("公司: ${job.company}")
                if (job.location != null) println("地点: ${job.location}")
                if (job.salary != null) println("薪资: ${job.salary}")
                if (job.url != null) println("链接: ${job.url}")
                println("发现时间: ${job.discoveredAt.format(dateFormatter)}")
                println("---")
            }
        } catch (e: Exception) {
            println("获取招聘信息失败: ${e.message}")
            logger.error(e) { "获取招聘信息失败" }
        }
    }
    
    private fun startScheduler() {
        println("\n=== 启动定时调度器 ===")
        
        try {
            scheduler.start()
            println("定时调度器已启动")
        } catch (e: Exception) {
            println("启动定时调度器失败: ${e.message}")
            logger.error(e) { "启动定时调度器失败" }
        }
    }
    
    private fun stopScheduler() {
        println("\n=== 停止定时调度器 ===")
        
        try {
            scheduler.stop()
            println("定时调度器已停止")
        } catch (e: Exception) {
            println("停止定时调度器失败: ${e.message}")
            logger.error(e) { "停止定时调度器失败" }
        }
    }
    
    private fun triggerScanNow() {
        println("\n=== 手动触发扫描 ===")
        
        try {
            scheduler.triggerScanNow()
            println("已触发扫描任务，请查看日志获取结果")
        } catch (e: Exception) {
            println("触发扫描失败: ${e.message}")
            logger.error(e) { "触发扫描失败" }
        }
    }
    
    private fun updateScheduleInterval() {
        println("\n=== 更新调度间隔 ===")
        
        print("请输入新的调度间隔(分钟): ")
        val intervalInput = readLine()?.trim()
        val interval = intervalInput?.toIntOrNull()
        
        if (interval == null || interval <= 0) {
            println("无效的间隔时间")
            return
        }
        
        try {
            scheduler.updateScheduleInterval(interval)
            println("调度间隔已更新为 $interval 分钟")
        } catch (e: Exception) {
            println("更新调度间隔失败: ${e.message}")
            logger.error(e) { "更新调度间隔失败" }
        }
    }
    
    private fun viewJobsBySite() {
        println("\n=== 查看指定网站的招聘信息 ===")
        
        // 先显示所有网站
        val sites = jobScanService.getAllJobSites()
        if (sites.isEmpty()) {
            println("暂无招聘网站")
            return
        }
        
        println("可用的招聘网站:")
        sites.forEach { site ->
            println("${site.id}. ${site.name}")
        }
        
        print("请输入网站ID: ")
        val siteIdInput = readLine()?.trim()
        val siteId = siteIdInput?.toLongOrNull()
        
        if (siteId == null) {
            println("无效的网站ID")
            return
        }
        
        print("请输入要显示的数量 (默认20): ")
        val limitInput = readLine()?.trim()
        val limit = limitInput?.toIntOrNull() ?: 20
        
        try {
            val jobs = jobScanService.getJobPostingsBySite(siteId, limit)
            if (jobs.isEmpty()) {
                println("该网站暂无招聘信息")
                return
            }
            
            val siteName = sites.find { it.id == siteId }?.name ?: "未知网站"
            println("\n=== $siteName 的招聘信息 ===")
            
            jobs.forEach { job ->
                println("标题: ${job.title}")
                if (job.company != null) println("公司: ${job.company}")
                if (job.location != null) println("地点: ${job.location}")
                if (job.salary != null) println("薪资: ${job.salary}")
                if (job.url != null) println("链接: ${job.url}")
                println("发现时间: ${job.discoveredAt.format(dateFormatter)}")
                println("---")
            }
        } catch (e: Exception) {
            println("获取招聘信息失败: ${e.message}")
            logger.error(e) { "获取招聘信息失败" }
        }
    }
} 