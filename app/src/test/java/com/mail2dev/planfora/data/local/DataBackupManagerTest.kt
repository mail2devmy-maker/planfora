package com.mail2dev.planfora.data.local

import com.mail2dev.planfora.data.local.dao.*
import com.mail2dev.planfora.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
                override fun getAllAssets(): Flow<List<PlantAssetEntity>> = flowOf(emptyList())
                override suspend fun getAllAssetsOnce() = mockPlants
                override suspend fun getAssetById(id: Long): PlantAssetEntity? = mockPlants.find { it.id == id }
                override suspend fun insertAsset(asset: PlantAssetEntity): Long = 0
                override suspend fun insertAssets(assets: List<PlantAssetEntity>) {}
                override suspend fun updateAsset(asset: PlantAssetEntity): Int = 0
                override suspend fun deleteAsset(asset: PlantAssetEntity): Int = 0
            }
            override fun journalLogDao() = object : JournalLogDao {
                override fun getAllLogs(): Flow<List<JournalLogEntity>> = flowOf(emptyList())
                override suspend fun getAllLogsOnce() = mockLogs
                override fun getLogsForAsset(assetId: Long): Flow<List<JournalLogEntity>> = flowOf(emptyList())
                override suspend fun getLogById(id: Long): JournalLogEntity? = mockLogs.find { it.id == id }
                override suspend fun insertLog(log: JournalLogEntity): Long = 0
                override suspend fun insertLogs(logs: List<JournalLogEntity>) {}
                override suspend fun updateLog(log: JournalLogEntity): Int = 0
                override suspend fun deleteLog(log: JournalLogEntity): Int = 0
                override suspend fun getLogsBySupply(supplyId: Long): List<JournalLogEntity> = emptyList()
                override suspend fun deleteBatchLogs(batchGroupId: String) {}
                override suspend fun getLogsByBatchGroup(batchGroupId: String): List<JournalLogEntity> = emptyList()
            }
            override fun diySupplyDao() = object : DiySupplyDao {
                override fun getAllSupplies(): Flow<List<DiySupplyEntity>> = flowOf(emptyList())
                override suspend fun getAllSuppliesOnce() = mockSupplies
                override suspend fun getSupplyById(id: Long): DiySupplyEntity? = mockSupplies.find { it.id == id }
                override suspend fun insertSupply(supply: DiySupplyEntity): Long = 0
                override suspend fun insertSupplies(supplies: List<DiySupplyEntity>) {}
                override suspend fun updateSupply(supply: DiySupplyEntity): Int = 0
                override suspend fun deleteSupply(supply: DiySupplyEntity): Int = 0
                override suspend fun getMaxBatchNumber(supplyTypeName: String): Int? = null
                override fun getDistinctActiveIngredients(): Flow<List<String>> = flowOf(emptyList())
            }
            override fun masterDao() = object : MasterDao {
                override fun getAllLocations(): Flow<List<MasterLocationEntity>> = flowOf(emptyList())
                override fun getLocationsByScope(scope: String): Flow<List<MasterLocationEntity>> = flowOf(emptyList())
                override suspend fun insertLocation(location: MasterLocationEntity) {}
                override suspend fun deleteLocation(location: MasterLocationEntity) {}
                override suspend fun updateLocationNameScoped(oldName: String, newName: String, scope: String) {}
                override suspend fun deleteLocationByNameScoped(name: String, scope: String) {}
                override suspend fun updateAssetsLocation(oldName: String, newName: String) {}
                override suspend fun updateSuppliesLocation(oldName: String, newName: String) {}
                override suspend fun clearAssetsLocation(name: String) {}
                override suspend fun clearSuppliesLocation(name: String) {}
                override fun getAllTags(): Flow<List<MasterTagEntity>> = flowOf(emptyList())
                override fun getTagsByScope(scope: String): Flow<List<MasterTagEntity>> = flowOf(emptyList())
                override suspend fun insertTag(tag: MasterTagEntity) {}
                override suspend fun deleteTag(tag: MasterTagEntity) {}
                override suspend fun updateTagNameScoped(oldTag: String, newTag: String, scope: String) {}
                override suspend fun deleteTagByNameScoped(name: String, scope: String) {}
                override suspend fun updateAssetsTags(oldTag: String, newTag: String) {}
                override suspend fun updateLogsTags(oldTag: String, newTag: String) {}
                override suspend fun updateSuppliesTags(oldTag: String, newTag: String) {}
                override suspend fun removeAssetsTag(name: String) {}
                override suspend fun removeLogsTag(name: String) {}
                override suspend fun removeSuppliesTag(name: String) {}
                override fun getAllIngredients(): Flow<List<MasterIngredientEntity>> = flowOf(emptyList())
                override suspend fun insertIngredient(ingredient: MasterIngredientEntity) {}
                override suspend fun updateIngredientName(oldName: String, newName: String) {}
                override suspend fun deleteIngredientByName(name: String) {}
                override suspend fun updateSuppliesActiveIngredient(oldName: String, newName: String) {}
                override suspend fun clearSuppliesActiveIngredient(name: String) {}
                override fun getAllParameters(): Flow<List<MasterParameterEntity>> = flowOf(emptyList())
                override suspend fun insertParameter(parameter: MasterParameterEntity) {}
                override suspend fun updateParameterName(oldName: String, newName: String) {}
                override suspend fun deleteParameterByName(name: String) {}
                override suspend fun updateLogsParameters(oldName: String, newName: String) {}
                override suspend fun removeLogsParameter(name: String) {}
            }
            override fun customFieldDao() = object : CustomFieldDao {
                override fun getDefinitionsByCategory(category: String): Flow<List<CustomFieldDefinitionEntity>> = flowOf(emptyList())
                override suspend fun insertDefinition(definition: CustomFieldDefinitionEntity): Long = 0
                override suspend fun deleteDefinition(definition: CustomFieldDefinitionEntity) {}
                override suspend fun deleteDefinitionsByCategory(category: String) {}
                override fun getValuesByAsset(assetId: Long): Flow<List<CustomFieldValueEntity>> = flowOf(emptyList())
                override suspend fun insertValue(value: CustomFieldValueEntity): Long = 0
                override suspend fun insertValues(values: List<CustomFieldValueEntity>) {}
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
