package com.mail2dev.planfora.data.repository

import com.mail2dev.planfora.data.local.dao.JournalLogDao
import com.mail2dev.planfora.data.local.dao.PlantAssetDao
import com.mail2dev.planfora.data.local.entity.JournalLogEntity
import com.mail2dev.planfora.data.local.entity.PlantAssetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class JournalRepositoryTest {

    private var updatedAsset: PlantAssetEntity? = null

    private val fakeAssetDao = object : PlantAssetDao {
        override fun getAllAssets(): Flow<List<PlantAssetEntity>> = TODO()
        override suspend fun getAssetById(id: Long): PlantAssetEntity? {
            return if (id == 1L) {
                PlantAssetEntity(id = 1L, name = "Mango", category = "Trees", plantedDate = 100L, totalLogsCount = 5, lastActionDate = 100L)
            } else null
        }
        override suspend fun getAllAssetsOnce(): List<PlantAssetEntity> = TODO()
        override suspend fun insertAsset(asset: PlantAssetEntity): Long = TODO()
        override suspend fun insertAssets(assets: List<PlantAssetEntity>) = TODO()
        override suspend fun updateAsset(asset: PlantAssetEntity): Int {
            updatedAsset = asset
            return 1
        }
        override suspend fun deleteAsset(asset: PlantAssetEntity): Int = TODO()
    }

    private val fakeLogDao = object : JournalLogDao {
        override fun getAllLogs(): Flow<List<JournalLogEntity>> = TODO()
        override suspend fun getAllLogsOnce(): List<JournalLogEntity> = TODO()
        override fun getLogsForAsset(assetId: Long): Flow<List<JournalLogEntity>> = TODO()
        override suspend fun getLogById(id: Long): JournalLogEntity? = TODO()
        override suspend fun insertLog(log: JournalLogEntity): Long = 1L
        override suspend fun insertLogs(logs: List<JournalLogEntity>) = TODO()
        override suspend fun updateLog(log: JournalLogEntity): Int = TODO()
        override suspend fun deleteLog(log: JournalLogEntity): Int = TODO()
    }

    private val repository = JournalRepository(fakeLogDao, fakeAssetDao)

    @Test
    fun testInsertLogIncrementsCount() = runBlocking {
        val log = JournalLogEntity(
            assetId = 1L,
            title = "New Log",
            note = "Note",
            timestamp = 200L,
            photoPath = null,
            audioFilePath = null,
            ecValue = null,
            phValue = null,
            tags = "",
            parameters = "",
            imageUris = "",
            activityType = "Observation",
            parentLogId = null,
            supplyId = null,
            customInputName = null
        )
        
        repository.insertLog(log)
        
        assertEquals(6, updatedAsset?.totalLogsCount)
        assertEquals(200L, updatedAsset?.lastActionDate)
    }
}
