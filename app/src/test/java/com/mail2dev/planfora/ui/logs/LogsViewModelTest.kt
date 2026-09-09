package com.mail2dev.planfora.ui.logs

import com.mail2dev.planfora.data.local.dao.*
import com.mail2dev.planfora.data.local.entity.*
import com.mail2dev.planfora.data.repository.JournalRepository
import com.mail2dev.planfora.data.repository.SupplyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class LogsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    
    private lateinit var repository: JournalRepository
    private lateinit var supplyRepository: SupplyRepository
    private lateinit var viewModel: LogsViewModel

    private val now = System.currentTimeMillis()
    private val yesterday = now - 24 * 60 * 60 * 1000L

    private val mockLogs = listOf(
        JournalLogEntity(id = 1, assetId = 1, title = "Log 1", note = "", timestamp = now, photoPath = null, audioFilePath = null, ecValue = null, phValue = null, tags = "", parameters = "", imageUris = "", activityType = "Observation", parentLogId = null, supplyId = null, customInputName = null),
        JournalLogEntity(id = 2, assetId = 1, title = "Log 2", note = "", timestamp = yesterday, photoPath = null, audioFilePath = null, ecValue = null, phValue = null, tags = "", parameters = "", imageUris = "", activityType = "Observation", parentLogId = null, supplyId = null, customInputName = null)
    )

    private val fakeLogDao = object : JournalLogDao {
        override fun getAllLogs(): Flow<List<JournalLogEntity>> = flowOf(mockLogs)
        override suspend fun getAllLogsOnce(): List<JournalLogEntity> = mockLogs
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

    private val fakeAssetDao = object : PlantAssetDao {
        override fun getAllAssets(): Flow<List<PlantAssetEntity>> = flowOf(emptyList())
        override suspend fun getAllAssetsOnce(): List<PlantAssetEntity> = TODO()
        override suspend fun getAssetById(id: Long): PlantAssetEntity? = null
        override suspend fun insertAsset(asset: PlantAssetEntity): Long = 0
        override suspend fun insertAssets(assets: List<PlantAssetEntity>) = TODO()
        override suspend fun updateAsset(asset: PlantAssetEntity): Int = 0
        override suspend fun deleteAsset(asset: PlantAssetEntity): Int = 0
    }

    private val fakeSupplyDao = object : DiySupplyDao {
        override fun getAllSupplies(): Flow<List<DiySupplyEntity>> = flowOf(emptyList())
        override suspend fun getAllSuppliesOnce(): List<DiySupplyEntity> = TODO()
        override suspend fun getSupplyById(id: Long): DiySupplyEntity? = TODO()
        override suspend fun insertSupply(supply: DiySupplyEntity): Long = TODO()
        override suspend fun insertSupplies(supplies: List<DiySupplyEntity>) = TODO()
        override suspend fun updateSupply(supply: DiySupplyEntity): Int = TODO()
        override suspend fun deleteSupply(supply: DiySupplyEntity): Int = TODO()
        override suspend fun getMaxBatchNumber(supplyTypeName: String): Int? = TODO()
        override fun getDistinctActiveIngredients(): Flow<List<String>> = TODO()
    }

    private val fakeMasterDao = object : MasterDao {
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

    private val fakeCustomFieldDao = object : CustomFieldDao {
        override fun getDefinitionsByCategory(category: String): Flow<List<CustomFieldDefinitionEntity>> = flowOf(emptyList())
        override suspend fun insertDefinition(definition: CustomFieldDefinitionEntity): Long = 0
        override suspend fun deleteDefinition(definition: CustomFieldDefinitionEntity) {}
        override suspend fun deleteDefinitionsByCategory(category: String) {}
        override fun getValuesByAsset(assetId: Long): Flow<List<CustomFieldValueEntity>> = flowOf(emptyList())
        override suspend fun insertValue(value: CustomFieldValueEntity): Long = 0
        override suspend fun insertValues(values: List<CustomFieldValueEntity>) {}
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = JournalRepository(fakeLogDao, fakeAssetDao, fakeMasterDao, fakeCustomFieldDao)
        supplyRepository = SupplyRepository(fakeSupplyDao)
        viewModel = LogsViewModel(repository, supplyRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `filteredLogs should only contain logs for selected date`() = runTest {
        viewModel.setSelectedDate(now)
        val filtered = viewModel.filteredLogs.first()
        assertEquals(1, filtered.size)
        assertEquals("Log 1", filtered[0].title)

        viewModel.setSelectedDate(yesterday)
        val filteredYesterday = viewModel.filteredLogs.first()
        assertEquals(1, filteredYesterday.size)
        assertEquals("Log 2", filteredYesterday[0].title)
    }

    @Test
    fun `logEventDates should contain unique start-of-day timestamps`() = runTest {
        val eventDates = viewModel.logEventDates.first()
        assertEquals(2, eventDates.size)
        
        val startOfNow = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        assertTrue(eventDates.contains(startOfNow))
    }
}
