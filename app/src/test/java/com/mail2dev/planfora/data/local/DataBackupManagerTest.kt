package com.mail2dev.planfora.data.local

import com.mail2dev.planfora.data.local.dao.DiySupplyDao
import com.mail2dev.planfora.data.local.dao.JournalLogDao
import com.mail2dev.planfora.data.local.dao.PlantAssetDao
import com.mail2dev.planfora.data.local.entity.DiySupplyEntity
import com.mail2dev.planfora.data.local.entity.JournalLogEntity
import com.mail2dev.planfora.data.local.entity.PlantAssetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DataBackupManagerTest {

    private lateinit var backupManager: DataBackupManager
    
    private val mockPlants = listOf(PlantAssetEntity(id = 1, name = "Ficus", category = "Trees", plantedDate = 1000, lastActionDate = 1000))
    private val mockLogs = listOf(JournalLogEntity(id = 1, assetId = 1, title = "Log", note = "Note", timestamp = 1000, photoPath = null, audioFilePath = null, ecValue = null, phValue = null, tags = "", parameters = "", imageUris = "", activityType = "Observation", parentLogId = null, supplyId = null, customInputName = null))
    private val mockSupplies = listOf(DiySupplyEntity(id = 1, name = "Soil", category = "Substrate", batchNumber = 1, batchCode = "B1", startDate = 1000, targetMaturityDate = 1000, currentVolume = 10.0, originalVolume = 10.0, unit = "L", notifyOnMaturity = false, activeIngredient = null, activePercentage = null))

    @Before
    fun setup() {
        val fakeDb = object : AppDatabase() {
            override fun plantAssetDao() = object : PlantAssetDao {
                override fun getAllAssets(): Flow<List<PlantAssetEntity>> = TODO()
                override suspend fun getAllAssetsOnce() = mockPlants
                override suspend fun getAssetById(id: Long): PlantAssetEntity? = TODO()
                override suspend fun insertAsset(asset: PlantAssetEntity): Long = TODO()
                override suspend fun insertAssets(assets: List<PlantAssetEntity>) = TODO()
                override suspend fun updateAsset(asset: PlantAssetEntity): Int = TODO()
                override suspend fun deleteAsset(asset: PlantAssetEntity): Int = TODO()
            }
            override fun journalLogDao() = object : JournalLogDao {
                override fun getAllLogs(): Flow<List<JournalLogEntity>> = TODO()
                override suspend fun getAllLogsOnce() = mockLogs
                override fun getLogsForAsset(assetId: Long): Flow<List<JournalLogEntity>> = TODO()
                override suspend fun getLogById(id: Long): JournalLogEntity? = TODO()
                override suspend fun insertLog(log: JournalLogEntity): Long = TODO()
                override suspend fun insertLogs(logs: List<JournalLogEntity>) = TODO()
                override suspend fun updateLog(log: JournalLogEntity): Int = TODO()
                override suspend fun deleteLog(log: JournalLogEntity): Int = TODO()
            }
            override fun diySupplyDao() = object : DiySupplyDao {
                override fun getAllSupplies(): Flow<List<DiySupplyEntity>> = TODO()
                override suspend fun getAllSuppliesOnce() = mockSupplies
                override suspend fun getSupplyById(id: Long): DiySupplyEntity? = TODO()
                override suspend fun insertSupply(supply: DiySupplyEntity): Long = TODO()
                override suspend fun insertSupplies(supplies: List<DiySupplyEntity>) = TODO()
                override suspend fun updateSupply(supply: DiySupplyEntity): Int = TODO()
                override suspend fun deleteSupply(supply: DiySupplyEntity): Int = TODO()
                override suspend fun getMaxBatchNumber(supplyTypeName: String): Int? = TODO()
            }
            override fun createInvalidationTracker(): androidx.room.InvalidationTracker = TODO()
            override fun clearAllTables() = TODO()
        }
        
        backupManager = DataBackupManager(fakeDb)
    }

    @Test
    fun `test serialization accuracy`() = runBlocking {
        val json = backupManager.generateBackupJson()
        
        assertTrue(json.contains("\"version\": 7"))
        assertTrue(json.contains("\"name\": \"Ficus\""))
        assertTrue(json.contains("\"title\": \"Log\""))
        assertTrue(json.contains("\"batchCode\": \"B1\""))
    }
}
