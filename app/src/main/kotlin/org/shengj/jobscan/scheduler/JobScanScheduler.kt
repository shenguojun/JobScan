package org.shengj.jobscan.scheduler

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import org.shengj.jobscan.service.JobScanService

private val logger = KotlinLogging.logger {}

/**
 * 求职扫描任务调度器
 */
class JobScanScheduler(private val jobScanService: JobScanService) {
    
    private val scheduler: Scheduler = StdSchedulerFactory.getDefaultScheduler()
    
    /**
     * 启动调度器
     */
    fun start() {
        logger.info { "启动求职扫描调度器" }
        
        // 设置JobScanService到JobDataMap中，供Job使用
        val jobDataMap = JobDataMap()
        jobDataMap["jobScanService"] = jobScanService
        
        // 创建扫描任务
        val scanJob = JobBuilder.newJob(ScanJob::class.java)
            .withIdentity("scanJob", "jobScanGroup")
            .setJobData(jobDataMap)
            .build()
        
        // 创建触发器 - 每30分钟执行一次
        val trigger = TriggerBuilder.newTrigger()
            .withIdentity("scanTrigger", "jobScanGroup")
            .startNow()
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInMinutes(30)
                    .repeatForever()
            )
            .build()
        
        // 调度任务
        scheduler.scheduleJob(scanJob, trigger)
        scheduler.start()
        
        logger.info { "求职扫描调度器启动成功，将每30分钟执行一次扫描" }
    }
    
    /**
     * 停止调度器
     */
    fun stop() {
        logger.info { "停止求职扫描调度器" }
        scheduler.shutdown(true)
    }
    
    /**
     * 立即执行一次扫描
     */
    fun triggerScanNow() {
        logger.info { "手动触发扫描任务" }
        
        val jobKey = JobKey.jobKey("scanJob", "jobScanGroup")
        scheduler.triggerJob(jobKey)
    }
    
    /**
     * 更新调度间隔
     */
    fun updateScheduleInterval(intervalMinutes: Int) {
        logger.info { "更新调度间隔为 $intervalMinutes 分钟" }
        
        val triggerKey = TriggerKey.triggerKey("scanTrigger", "jobScanGroup")
        
        val newTrigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .startNow()
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInMinutes(intervalMinutes)
                    .repeatForever()
            )
            .build()
        
        scheduler.rescheduleJob(triggerKey, newTrigger)
    }
}

/**
 * Quartz扫描任务
 */
class ScanJob : Job {
    
    override fun execute(context: JobExecutionContext) {
        val jobScanService = context.jobDetail.jobDataMap["jobScanService"] as JobScanService
        
        logger.info { "开始执行定时扫描任务" }
        
        try {
            runBlocking {
                val scanResults = jobScanService.scanAllActiveSites()
                val report = jobScanService.generateScanReport(scanResults)
                
                logger.info { "定时扫描任务完成" }
                logger.info { "\n$report" }
                
                // 这里可以添加通知逻辑，比如发送邮件、推送消息等
                // notificationService.sendReport(report)
            }
        } catch (e: Exception) {
            logger.error(e) { "执行定时扫描任务时发生错误" }
        }
    }
} 