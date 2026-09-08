package com.mail2dev.planfora.data.repository

import com.mail2dev.planfora.data.local.dao.*
import com.mail2dev.planfora.data.local.entity.*
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
        override suspend fun getLogsBySupply(supplyId: Long): List<JournalLogEntity> = TODO()
        override suspend fun deleteBatchLogs(batchGroupId: String) = TODO()
        override suspend fun getLogsByBatchGroup(batchGroupId: String): List<JournalLogEntity> = TODO()
    }

    private val fakeMasterDao = object : MasterDao {
        override fun getAllLocations(): Flow<List<MasterLocationEntity>> = TODO()
        override fun getLocationsByScope(scope: String): Flow<List<MasterLocationEntity>> = TODO()
        override suspend fun insertLocation(location: MasterLocationEntity) = TODO()
        override suspend fun deleteLocation(location: MasterLocationEntity) = TODO()
        override suspend fun updateLocationNameScoped(oldName: String, newName: String, scope: String) = TODO()
        override suspend fun deleteLocationByNameScoped(name: String, scope: String) = TODO()
        override suspend fun updateAssetsLocation(oldName: String, newName: String) = TODO()
        override suspend fun updateSuppliesLocation(oldName: String, newName: String) = TODO()
        override suspend fun clearAssetsLocation(name: String) = TODO()
        override suspend fun clearSuppliesLocation(name: String) = TODO()
        override fun getAllTags(): Flow<List<MasterTagEntity>> = TODO()
        override fun getTagsByScope(scope: String): Flow<List<MasterTagEntity>> = TODO()
        override suspend fun insertTag(tag: MasterTagEntity) = TODO()
        override suspend fun deleteTag(tag: MasterTagEntity) = TODO()
        override suspend fun updateTagNameScoped(oldTag: String, newTag: String, scope: String) = TODO()
        override suspend fun deleteTagByNameScoped(name: String, scope: String) = TODO()
        override suspend fun updateAssetsTags(oldTag: String, newTag: String) = TODO()
        override suspend fun updateLogsTags(oldTag: String, newTag: String) = TODO()
        override suspend fun updateSuppliesTags(oldTag: String, newTag: String) = TODO()
        override suspend fun removeAssetsTag(name: String) = TODO()
        override suspend fun removeLogsTag(name: String) = TODO()
        override suspend fun removeSuppliesTag(name: String) = TODO()
        override fun getAllIngredients(): Flow<List<MasterIngredientEntity>> = TODO()
        override suspend fun insertIngredient(ingredient: MasterIngredientEntity) = TODO()
        override suspend fun updateIngredientName(oldName: String, newName: String) = TODO()
        override suspend fun deleteIngredientByName(name: String) = TODO()
        override suspend fun updateSuppliesActiveIngredient(oldName: String, newName: String) = TODO()
        override suspend fun clearSuppliesActiveIngredient(name: String) = TODO()
        override fun getAllParameters(): Flow<List<MasterParameterEntity>> = TODO()
        override suspend fun insertParameter(parameter: MasterParameterEntity) = TODO()
        override suspend fun updateParameterName(oldName: String, newName: String) = TODO()
        override suspend fun deleteParameterByName(name: String) = TODO()
        override suspend fun updateLogsParameters(oldName: String, newName: String) = TODO()
        override suspend fun removeLogsParameter(name: String) = TODO()
    }

    private val fakeCustomFieldDao = object : CustomFieldDao {
        override fun getDefinitionsByCategory(category: String): Flow<List<CustomFieldDefinitionEntity>> = TODO()
        override suspend fun insertDefinition(definition: CustomFieldDefinitionEntity): Long = TODO()
        override fun getValuesByAsset(assetId: Long): Flow<List<CustomFieldValueEntity>> = TODO()
        override suspend fun insertValue(value: CustomFieldValueEntity): Long = TODO()
        override suspend fun insertValues(values: List<CustomFieldValueEntity>) = TODO()
    }

    private val repository = JournalRepository(fakeLogDao, fakeAssetDao, fakeMasterDao, fakeCustomFieldDao)

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
