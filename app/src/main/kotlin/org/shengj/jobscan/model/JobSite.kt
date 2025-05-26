package org.shengj.jobscan.model

import java.time.LocalDateTime

/**
 * 招聘网站配置
 */
data class JobSite(
    val id: Long = 0,
    val name: String,
    val url: String,
    val selector: String? = null, // CSS选择器，用于提取招聘信息
    val isActive: Boolean = true,
    val checkInterval: Int = 60, // 检查间隔（分钟）
    val lastChecked: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * 页面内容快照
 */
data class PageSnapshot(
    val id: Long = 0,
    val siteId: Long,
    val contentHash: String,
    val content: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * 新增的招聘信息
 */
data class JobPosting(
    val id: Long = 0,
    val siteId: Long,
    val title: String,
    val company: String? = null,
    val location: String? = null,
    val salary: String? = null,
    val description: String? = null,
    val url: String? = null,
    val publishedAt: LocalDateTime? = null,
    val discoveredAt: LocalDateTime = LocalDateTime.now()
)

/**
 * 检查结果
 */
data class ScanResult(
    val siteId: Long,
    val siteName: String,
    val hasChanges: Boolean,
    val newJobsCount: Int,
    val newJobs: List<JobPosting> = emptyList(),
    val error: String? = null,
    val scanTime: LocalDateTime = LocalDateTime.now()
) 