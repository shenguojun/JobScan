package org.shengj.jobscan.database

import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * 数据库管理器
 */
class DatabaseManager(private val dbPath: String = "jobscan.db") {
    
    private lateinit var database: Database
    
    fun initialize() {
        logger.info { "初始化数据库: $dbPath" }
        
        // 确保数据库目录存在
        val dbFile = File(dbPath)
        dbFile.parentFile?.mkdirs()
        
        // 连接数据库
        database = Database.connect(
            url = "jdbc:sqlite:$dbPath",
            driver = "org.sqlite.JDBC"
        )
        
        // 创建表
        transaction(database) {
            SchemaUtils.create(
                JobSitesTable,
                PageSnapshotsTable,
                JobPostingsTable
            )
        }
        
        logger.info { "数据库初始化完成" }
    }
    
    fun getDatabase(): Database = database
} 