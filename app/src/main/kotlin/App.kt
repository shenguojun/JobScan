package org.shengj.app

import org.shengj.jobscan.JobScanApplication

/**
 * 求职信息监控系统主入口
 */
fun main() {
    val application = JobScanApplication()
    
    try {
        // 初始化应用程序
        application.initialize()
        
        // 启动应用程序
        application.start()
        
    } catch (e: Exception) {
        println("应用程序启动失败: ${e.message}")
        e.printStackTrace()
    } finally {
        // 确保应用程序正确关闭
        application.stop()
    }
}
