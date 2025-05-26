package org.shengj.jobscan.database

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime

/**
 * 招聘网站表
 */
object JobSitesTable : LongIdTable("job_sites") {
    val name = varchar("name", 255)
    val url = text("url")
    val selector = text("selector").nullable()
    val isActive = bool("is_active").default(true)
    val checkInterval = integer("check_interval").default(60)
    val lastChecked = datetime("last_checked").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

/**
 * 页面快照表
 */
object PageSnapshotsTable : LongIdTable("page_snapshots") {
    val siteId = long("site_id")
    val contentHash = varchar("content_hash", 64)
    val content = text("content")
    val createdAt = datetime("created_at")
}

/**
 * 招聘信息表
 */
object JobPostingsTable : LongIdTable("job_postings") {
    val siteId = long("site_id")
    val title = varchar("title", 500)
    val company = varchar("company", 255).nullable()
    val location = varchar("location", 255).nullable()
    val salary = varchar("salary", 255).nullable()
    val description = text("description").nullable()
    val url = text("url").nullable()
    val publishedAt = datetime("published_at").nullable()
    val discoveredAt = datetime("discovered_at")
} 