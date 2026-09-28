package com.mail2dev.planfora.ui.profile

import com.mail2dev.planfora.data.local.entity.DiySupplyEntity
import com.mail2dev.planfora.data.local.entity.JournalLogEntity
import com.mail2dev.planfora.data.local.entity.PlantAssetEntity

data class LabAnalytics(
    val totalPlants: Int = 0,
    val totalLogs: Int = 0,
    val activeFerments: Int = 0,
    val topTags: List<TagCount> = emptyList(),
    val maturingSupplies: List<DiySupplyEntity> = emptyList()
)

data class TagCount(val tag: String, val count: Int)

class LabAnalyticsManager {

    fun calculate(
        plants: List<PlantAssetEntity>,
        logs: List<JournalLogEntity>,
        supplies: List<DiySupplyEntity>
    ): LabAnalytics {
        val currentTime = System.currentTimeMillis()
        
        // Summaries
        val totalPlants = plants.size
        val totalLogs = logs.size
        val maturingSupplies = supplies.filter { it.targetMaturityDate > it.startDate && it.targetMaturityDate > currentTime }
        val activeFerments = maturingSupplies.size

        // Tag Analysis
        val allTags = (plants.flatMap { it.tags.split(",") } + logs.flatMap { it.tags.split(",") })
            .filter { it.isNotBlank() }
            .map { it.trim() }
        
        val topTags = allTags.groupingBy { it }
            .eachCount()
            .entries
            .map { TagCount(it.key, it.value) }
            .sortedByDescending { it.count }
            .take(5)

        return LabAnalytics(
            totalPlants = totalPlants,
            totalLogs = totalLogs,
            activeFerments = activeFerments,
            topTags = topTags,
            maturingSupplies = maturingSupplies.sortedBy { it.targetMaturityDate }
        )
    }
}
