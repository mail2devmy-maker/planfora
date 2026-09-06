package com.mail2dev.planfora.data.repository

import com.mail2dev.planfora.data.local.dao.CustomFieldDao
import com.mail2dev.planfora.data.local.dao.JournalLogDao
import com.mail2dev.planfora.data.local.dao.MasterDao
import com.mail2dev.planfora.data.local.dao.PlantAssetDao
import com.mail2dev.planfora.data.local.entity.*
import kotlinx.coroutines.flow.Flow

class JournalRepository(
    private val journalLogDao: JournalLogDao,
    private val plantAssetDao: PlantAssetDao,
    private val masterDao: MasterDao,
    private val customFieldDao: CustomFieldDao
) {
    fun getAllLogs(): Flow<List<JournalLogEntity>> =
        journalLogDao.getAllLogs()

    fun getLogsForAsset(assetId: Long): Flow<List<JournalLogEntity>> =
        journalLogDao.getLogsForAsset(assetId)

    fun getAllAssets(): Flow<List<PlantAssetEntity>> =
        plantAssetDao.getAllAssets()

    suspend fun getLogById(id: Long): JournalLogEntity? =
        journalLogDao.getLogById(id)

    suspend fun insertAsset(asset: PlantAssetEntity) {
        plantAssetDao.insertAsset(asset)
    }

    suspend fun updateAsset(asset: PlantAssetEntity) {
        plantAssetDao.updateAsset(asset)
    }

    suspend fun deleteAsset(asset: PlantAssetEntity) {
        plantAssetDao.deleteAsset(asset)
    }

    suspend fun getAssetById(id: Long): PlantAssetEntity? =
        plantAssetDao.getAssetById(id)

    suspend fun insertLog(log: JournalLogEntity) {
        journalLogDao.insertLog(log)
        // Update log count for the asset
        plantAssetDao.getAssetById(log.assetId)?.let { asset ->
            plantAssetDao.updateAsset(
                asset.copy(
                    totalLogsCount = asset.totalLogsCount + 1,
                    lastActionDate = log.timestamp
                )
            )
        }
    }

    suspend fun updateLog(log: JournalLogEntity) {
        journalLogDao.updateLog(log)
    }

    suspend fun deleteLog(log: JournalLogEntity) {
        journalLogDao.deleteLog(log)
        // Update log count for the asset
        plantAssetDao.getAssetById(log.assetId)?.let { asset ->
            plantAssetDao.updateAsset(
                asset.copy(
                    totalLogsCount = (asset.totalLogsCount - 1).coerceAtLeast(0)
                )
            )
        }
    }

    suspend fun getLogsBySupply(supplyId: Long): List<JournalLogEntity> =
        journalLogDao.getLogsBySupply(supplyId)

    // Master List Operations
    fun getAllLocations(): Flow<List<MasterLocationEntity>> = masterDao.getAllLocations()
    fun getLocationsByScope(scope: String): Flow<List<MasterLocationEntity>> = masterDao.getLocationsByScope(scope)
    suspend fun insertLocation(location: MasterLocationEntity) = masterDao.insertLocation(location)
    suspend fun deleteLocation(location: MasterLocationEntity) = masterDao.deleteLocation(location)
    suspend fun updateLocationName(oldName: String, newName: String, scope: String) = masterDao.renameLocationCascading(oldName, newName, scope)
    suspend fun deleteLocationByName(name: String, scope: String) = masterDao.deleteLocationCascading(name, scope)

    fun getAllTags(): Flow<List<MasterTagEntity>> = masterDao.getAllTags()
    fun getTagsByScope(scope: String): Flow<List<MasterTagEntity>> = masterDao.getTagsByScope(scope)
    suspend fun insertTag(tag: MasterTagEntity) = masterDao.insertTag(tag)
    suspend fun deleteTag(tag: MasterTagEntity) = masterDao.deleteTag(tag)
    suspend fun updateTagName(oldTag: String, newTag: String, scope: String) = masterDao.renameTagCascading(oldTag, newTag, scope)
    suspend fun deleteTagByName(name: String, scope: String) = masterDao.deleteTagCascading(name, scope)

    fun getAllIngredients(): Flow<List<MasterIngredientEntity>> = masterDao.getAllIngredients()
    suspend fun insertIngredient(ingredient: MasterIngredientEntity) = masterDao.insertIngredient(ingredient)
    suspend fun updateIngredientName(oldName: String, newName: String) = masterDao.renameIngredientCascading(oldName, newName)
    suspend fun deleteIngredientByName(name: String) = masterDao.deleteIngredientCascading(name)

    fun getAllParameters(): Flow<List<MasterParameterEntity>> = masterDao.getAllParameters()
    suspend fun insertParameter(parameter: MasterParameterEntity) = masterDao.insertParameter(parameter)
    suspend fun updateParameterName(oldName: String, newName: String) = masterDao.renameParameterCascading(oldName, newName)
    suspend fun deleteParameterByName(name: String) = masterDao.deleteParameterCascading(name)

    // Custom Fields
    fun getCustomFieldDefinitions(category: String) = customFieldDao.getDefinitionsByCategory(category)
    suspend fun insertCustomFieldDefinition(definition: CustomFieldDefinitionEntity) = customFieldDao.insertDefinition(definition)
    fun getCustomFieldValues(assetId: Long) = customFieldDao.getValuesByAsset(assetId)
    suspend fun insertCustomFieldValues(values: List<CustomFieldValueEntity>) = customFieldDao.insertValues(values)
}
