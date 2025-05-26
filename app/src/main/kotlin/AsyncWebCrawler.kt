package org.shengj.app

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Paths

class AsyncWebCrawler {
    
    /**
     * 异步爬取指定URL并返回Markdown格式的内容
     * @param url 要爬取的URL
     * @param timeoutSeconds 超时时间（秒）
     * @return Markdown格式的内容，如果出错则返回错误信息
     */
    suspend fun crawlToMarkdown(url: String, timeoutSeconds: Long = 60): String = withContext(Dispatchers.IO) {
        try {
            // 获取Python脚本的路径
            val scriptPath = getScriptPath()
            
            // 构建Python命令，使用虚拟环境中的Python
            val pythonPath = getPythonPath()
            val command = listOf(pythonPath, scriptPath, url)
            
            // 创建进程
            val processBuilder = ProcessBuilder(command)
            processBuilder.redirectErrorStream(true)
            
            val process = processBuilder.start()
            
            // 使用协程超时控制
            val result = withTimeoutOrNull(timeoutSeconds * 1000) {
                // 读取输出
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val output = StringBuilder()
                
                reader.useLines { lines ->
                    lines.forEach { line ->
                        output.appendLine(line)
                    }
                }
                
                // 等待进程完成
                val exitCode = process.waitFor()
                
                if (exitCode == 0) {
                    output.toString().trim()
                } else {
                    "Error: Python script exited with code $exitCode\nOutput: ${output.toString()}"
                }
            }
            
            if (result == null) {
                process.destroyForcibly()
                "Error: 爬取超时（${timeoutSeconds}秒）"
            } else {
                result
            }
            
        } catch (e: Exception) {
            "Error executing Python script: ${e.message}"
        }
    }
    
    /**
     * 批量爬取多个URL
     * @param urls URL列表
     * @param concurrency 并发数
     * @param timeoutSeconds 每个URL的超时时间
     * @return URL到Markdown内容的映射
     */
    suspend fun crawlMultipleUrls(
        urls: List<String>, 
        concurrency: Int = 3, 
        timeoutSeconds: Long = 60
    ): Map<String, String> = withContext(Dispatchers.IO) {
        
        val semaphore = Semaphore(concurrency)
        
        val deferredResults = urls.map { url ->
            async {
                semaphore.withPermit {
                    url to crawlToMarkdown(url, timeoutSeconds)
                }
            }
        }
        
        deferredResults.awaitAll().toMap()
    }
    
    /**
     * 获取Python可执行文件路径
     */
    private fun getPythonPath(): String {
        // 首先尝试使用虚拟环境中的Python
        val venvPython = "venv/bin/python"
        val venvFile = java.io.File(venvPython)
        
        return if (venvFile.exists()) {
            venvPython
        } else {
            // 备用方案：使用系统Python
            "python3"
        }
    }
    
    /**
     * 获取Python脚本的绝对路径
     */
    private fun getScriptPath(): String {
        // 尝试从resources目录获取脚本路径
        val resource = this::class.java.classLoader.getResource("web_crawler.py")
        
        return if (resource != null) {
            // 如果在JAR中运行，需要提取到临时文件
            if (resource.protocol == "jar") {
                extractScriptToTemp()
            } else {
                // 开发环境中直接使用文件路径
                Paths.get(resource.toURI()).toString()
            }
        } else {
            // 备用方案：假设脚本在当前工作目录
            "app/src/main/resources/web_crawler.py"
        }
    }
    
    /**
     * 将脚本提取到临时文件（用于JAR部署）
     */
    private fun extractScriptToTemp(): String {
        val inputStream = this::class.java.classLoader.getResourceAsStream("web_crawler.py")
            ?: throw RuntimeException("Cannot find web_crawler.py in resources")
        
        val tempFile = kotlin.io.path.createTempFile("web_crawler", ".py")
        tempFile.toFile().writeBytes(inputStream.readBytes())
        tempFile.toFile().deleteOnExit()
        
        return tempFile.toString()
    }
} 