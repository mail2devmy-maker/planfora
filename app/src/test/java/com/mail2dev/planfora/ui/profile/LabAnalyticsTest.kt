package com.mail2dev.planfora.ui.profile

import com.mail2dev.planfora.data.local.entity.DiySupplyEntity
import com.mail2dev.planfora.data.local.entity.JournalLogEntity
import com.mail2dev.planfora.data.local.entity.PlantAssetEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class LabAnalyticsTest {

    private val analyticsManager = LabAnalyticsManager()

    @Test
    fun `test tag frequency parsing and ranking`() {
        val plants = listOf(
            PlantAssetEntity(name = "P1", category = "C1", tags = "Indoor, Perennial", plantedDate = 0, lastActionDate = 0)
        )
        val logs = listOf(
            JournalLogEntity(assetId = 1, title = "L1", note = "", timestamp = 0, photoPath = null, audioFilePath = null, ecValue = null, phValue = null, tags = "Indoor, Trial", parameters = "", imageUris = "", activityType = "Observation", parentLogId = null, supplyId = null, customInputName = null),
            JournalLogEntity(assetId = 1, title = "L2", note = "", timestamp = 0, photoPath = null, audioFilePath = null, ecValue = null, phValue = null, tags = "Trial", parameters = "", imageUris = "", activityType = "Observation", parentLogId = null, supplyId = null, customInputName = null)
        )

        val result = analyticsManager.calculate(plants, logs, emptyList())

        // Top tags should be: Indoor (2), Trial (2), Perennial (1)
        assertEquals(3, result.topTags.size)
        assertEquals("Indoor", result.topTags[0].tag)
        assertEquals(2, result.topTags[0].count)
        assertEquals("Trial", result.topTags[1].tag)
        assertEquals(2, result.topTags[1].count)
    }

    @Test
    fun `test maturity state counting for DIY supplies`() {
        val now = System.currentTimeMillis()
        val supplies = listOf(
            // Maturing
            DiySupplyEntity(name = "S1", category = "DIY", batchNumber = 1, batchCode = "B1", startDate = now - 1000, targetMaturityDate = now + 100000, currentVolume = 1.0, originalVolume = 1.0, unit = "L", notifyOnMaturity = false, activeIngredient = null, activePercentage = null),
            // Already Mature
            DiySupplyEntity(name = "S2", category = "DIY", batchNumber = 2, batchCode = "B2", startDate = now - 2000, targetMaturityDate = now - 1000, currentVolume = 1.0, originalVolume = 1.0, unit = "L", notifyOnMaturity = false, activeIngredient = null, activePercentage = null),
            // Not DIY (store)
            DiySupplyEntity(name = "S3", category = "Store", batchNumber = 0, batchCode = "", startDate = now, targetMaturityDate = now, currentVolume = 1.0, originalVolume = 1.0, unit = "L", notifyOnMaturity = false, activeIngredient = null, activePercentage = null)
        )

        val result = analyticsManager.calculate(emptyList(), emptyList(), supplies)

        assertEquals(1, result.activeFerments)
        assertEquals("S1", result.maturingSupplies[0].name)
    }
}
