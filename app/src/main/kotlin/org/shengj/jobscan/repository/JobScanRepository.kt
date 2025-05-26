package org.shengj.jobscan.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.shengj.jobscan.database.JobPostingsTable
import org.shengj.jobscan.database.JobSitesTable
import org.shengj.jobscan.database.PageSnapshotsTable
import org.shengj.jobscan.model.JobPosting
import org.shengj.jobscan.model.JobSite
import org.shengj.jobscan.model.PageSnapshot
import java.time.LocalDateTime

/**
 * 求职扫描数据仓库
 */
class JobScanRepository {
    
    // ========== JobSite 相关操作 ==========
    
    fun createJobSite(jobSite: JobSite): Long = transaction {
        JobSitesTable.insertAndGetId {
            it[name] = jobSite.name
            it[url] = jobSite.url
            it[selector] = jobSite.selector
            it[isActive] = jobSite.isActive
            it[checkInterval] = jobSite.checkInterval
            it[lastChecked] = jobSite.lastChecked
            it[createdAt] = jobSite.createdAt
            it[updatedAt] = jobSite.updatedAt
        }.value
    }
    
    fun getAllJobSites(): List<JobSite> = transaction {
        JobSitesTable.selectAll().map { row ->
            JobSite(
                id = row[JobSitesTable.id].value,
                name = row[JobSitesTable.name],
                url = row[JobSitesTable.url],
                selector = row[JobSitesTable.selector],
                isActive = row[JobSitesTable.isActive],
                checkInterval = row[JobSitesTable.checkInterval],
                lastChecked = row[JobSitesTable.lastChecked],
                createdAt = row[JobSitesTable.createdAt],
                updatedAt = row[JobSitesTable.updatedAt]
            )
        }
    }
    
    fun getActiveJobSites(): List<JobSite> = transaction {
        JobSitesTable.select { JobSitesTable.isActive eq true }.map { row ->
            JobSite(
                id = row[JobSitesTable.id].value,
                name = row[JobSitesTable.name],
                url = row[JobSitesTable.url],
                selector = row[JobSitesTable.selector],
                isActive = row[JobSitesTable.isActive],
                checkInterval = row[JobSitesTable.checkInterval],
                lastChecked = row[JobSitesTable.lastChecked],
                createdAt = row[JobSitesTable.createdAt],
                updatedAt = row[JobSitesTable.updatedAt]
            )
        }
    }
    
    fun updateJobSiteLastChecked(siteId: Long, lastChecked: LocalDateTime) = transaction {
        JobSitesTable.update({ JobSitesTable.id eq siteId }) {
            it[JobSitesTable.lastChecked] = lastChecked
            it[updatedAt] = LocalDateTime.now()
        }
    }
    
    // ========== PageSnapshot 相关操作 ==========
    
    fun savePageSnapshot(snapshot: PageSnapshot): Long = transaction {
        PageSnapshotsTable.insertAndGetId {
            it[siteId] = snapshot.siteId
            it[contentHash] = snapshot.contentHash
            it[content] = snapshot.content
            it[createdAt] = snapshot.createdAt
        }.value
    }
    
    fun getLatestPageSnapshot(siteId: Long): PageSnapshot? = transaction {
        PageSnapshotsTable
            .select { PageSnapshotsTable.siteId eq siteId }
            .orderBy(PageSnapshotsTable.createdAt, SortOrder.DESC)
            .limit(1)
            .map { row ->
                PageSnapshot(
                    id = row[PageSnapshotsTable.id].value,
                    siteId = row[PageSnapshotsTable.siteId],
                    contentHash = row[PageSnapshotsTable.contentHash],
                    content = row[PageSnapshotsTable.content],
                    createdAt = row[PageSnapshotsTable.createdAt]
                )
            }
            .firstOrNull()
    }
    
    // ========== JobPosting 相关操作 ==========
    
    fun saveJobPosting(jobPosting: JobPosting): Long = transaction {
        JobPostingsTable.insertAndGetId {
            it[siteId] = jobPosting.siteId
            it[title] = jobPosting.title
            it[company] = jobPosting.company
            it[location] = jobPosting.location
            it[salary] = jobPosting.salary
            it[description] = jobPosting.description
            it[url] = jobPosting.url
            it[publishedAt] = jobPosting.publishedAt
            it[discoveredAt] = jobPosting.discoveredAt
        }.value
    }
    
    fun saveJobPostings(jobPostings: List<JobPosting>): List<Long> = transaction {
        jobPostings.map { jobPosting ->
            JobPostingsTable.insertAndGetId {
                it[siteId] = jobPosting.siteId
                it[title] = jobPosting.title
                it[company] = jobPosting.company
                it[location] = jobPosting.location
                it[salary] = jobPosting.salary
                it[description] = jobPosting.description
                it[url] = jobPosting.url
                it[publishedAt] = jobPosting.publishedAt
                it[discoveredAt] = jobPosting.discoveredAt
            }.value
        }
    }
    
    fun getRecentJobPostings(limit: Int = 50): List<JobPosting> = transaction {
        JobPostingsTable
            .selectAll()
            .orderBy(JobPostingsTable.discoveredAt, SortOrder.DESC)
            .limit(limit)
            .map { row ->
                JobPosting(
                    id = row[JobPostingsTable.id].value,
                    siteId = row[JobPostingsTable.siteId],
                    title = row[JobPostingsTable.title],
                    company = row[JobPostingsTable.company],
                    location = row[JobPostingsTable.location],
                    salary = row[JobPostingsTable.salary],
                    description = row[JobPostingsTable.description],
                    url = row[JobPostingsTable.url],
                    publishedAt = row[JobPostingsTable.publishedAt],
                    discoveredAt = row[JobPostingsTable.discoveredAt]
                )
            }
    }
    
    fun getJobPostingsBySite(siteId: Long, limit: Int = 20): List<JobPosting> = transaction {
        JobPostingsTable
            .select { JobPostingsTable.siteId eq siteId }
            .orderBy(JobPostingsTable.discoveredAt, SortOrder.DESC)
            .limit(limit)
            .map { row ->
                JobPosting(
                    id = row[JobPostingsTable.id].value,
                    siteId = row[JobPostingsTable.siteId],
                    title = row[JobPostingsTable.title],
                    company = row[JobPostingsTable.company],
                    location = row[JobPostingsTable.location],
                    salary = row[JobPostingsTable.salary],
                    description = row[JobPostingsTable.description],
                    url = row[JobPostingsTable.url],
                    publishedAt = row[JobPostingsTable.publishedAt],
                    discoveredAt = row[JobPostingsTable.discoveredAt]
                )
            }
    }
} 