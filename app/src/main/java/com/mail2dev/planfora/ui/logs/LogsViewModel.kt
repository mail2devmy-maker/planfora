package com.mail2dev.planfora.ui.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.planfora.data.local.entity.JournalLogEntity
import com.mail2dev.planfora.data.local.entity.DiySupplyEntity
import com.mail2dev.planfora.data.local.entity.PlantAssetEntity
import com.mail2dev.planfora.data.local.entity.CustomFieldDefinitionEntity
import com.mail2dev.planfora.data.local.entity.CustomFieldValueEntity
import com.mail2dev.planfora.data.local.entity.FieldTargetType
import com.mail2dev.planfora.data.repository.JournalRepository
import com.mail2dev.planfora.data.repository.SupplyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class CalendarMode { MONTH, WEEK, DAY }
enum class LayoutMode { COMPACT_LIST, EXPANDED_CARD }

class LogsViewModel(
    private val repository: JournalRepository,
    private val supplyRepository: SupplyRepository
) : ViewModel() {

    private val _calendarMode = MutableStateFlow(CalendarMode.WEEK)
    val calendarMode: StateFlow<CalendarMode> = _calendarMode.asStateFlow()

    private val _layoutMode = MutableStateFlow(LayoutMode.EXPANDED_CARD)
    val layoutMode: StateFlow<LayoutMode> = _layoutMode.asStateFlow()

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _locationFilter = MutableStateFlow<String?>(null)
    val locationFilter: StateFlow<String?> = _locationFilter.asStateFlow()

    private val _phiFilterActive = MutableStateFlow(false)
    val phiFilterActive: StateFlow<Boolean> = _phiFilterActive.asStateFlow()

    val assets: StateFlow<List<PlantAssetEntity>> = repository.getAllAssets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val supplies: StateFlow<List<DiySupplyEntity>> = supplyRepository.getAllSupplies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val masterLocations: StateFlow<List<String>> = repository.getAllLocations()
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val masterTags: StateFlow<List<String>> = repository.getTagsByScope("ASSET")
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val masterParameters: StateFlow<List<String>> = repository.getAllParameters()
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _allLogs = repository.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredLogs: StateFlow<List<JournalLogEntity>> = combine(
        _allLogs, 
        _selectedDate, 
        _locationFilter, 
        _phiFilterActive
    ) { logs, date, location, phiOnly ->
        logs.filter { log -> 
            val dateMatch = isSameDay(log.timestamp, date)
            val locationMatch = location == null || assets.value.find { it.id == log.assetId }?.locationNote == location
            val phiMatch = !phiOnly || isPhiActive(log)
            dateMatch && locationMatch && phiMatch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun isPhiActive(log: JournalLogEntity): Boolean {
        val phiExpiry = log.parameters.split("|")
            .find { it.startsWith("phi_expiry:") }
            ?.substringAfter("phi_expiry:")
            ?.toLongOrNull() ?: 0L
        return System.currentTimeMillis() < phiExpiry
    }

    val logEventDates: StateFlow<Set<Long>> = _allLogs.map { logs ->
        logs.map { normalizeToStartOfDay(it.timestamp) }.toSet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val allLogs: StateFlow<List<JournalLogEntity>> = _allLogs

    private val _showAddBottomSheet = MutableStateFlow(false)
    val showAddBottomSheet: StateFlow<Boolean> = _showAddBottomSheet.asStateFlow()

    fun setCalendarMode(mode: CalendarMode) { _calendarMode.value = mode }
    fun setLayoutMode(mode: LayoutMode) { _layoutMode.value = mode }
    fun setSelectedDate(timestamp: Long) { _selectedDate.value = timestamp }
    fun setLocationFilter(location: String?) { _locationFilter.value = location }
    fun togglePhiFilter() { _phiFilterActive.value = !_phiFilterActive.value }
    fun setShowAddBottomSheet(show: Boolean) { _showAddBottomSheet.value = show }

    fun addMasterTag(tag: String) {
        viewModelScope.launch {
            repository.insertTag(com.mail2dev.planfora.data.local.entity.MasterTagEntity(tag, "ASSET"))
        }
    }

    fun updateMasterTag(oldTag: String, newTag: String) {
        viewModelScope.launch {
            repository.updateTagName(oldTag, newTag, "ASSET")
        }
    }

    fun deleteMasterTag(tag: String) {
        viewModelScope.launch {
            repository.deleteTagByName(tag, "ASSET")
        }
    }

    fun addMasterParameter(name: String) {
        viewModelScope.launch {
            repository.insertParameter(com.mail2dev.planfora.data.local.entity.MasterParameterEntity(name))
        }
    }

    fun updateMasterParameter(oldName: String, newName: String) {
        viewModelScope.launch {
            repository.updateParameterName(oldName, newName)
        }
    }

    fun deleteMasterParameter(name: String) {
        viewModelScope.launch {
            repository.deleteParameterByName(name)
        }
    }

    // Dynamic Custom Fields
    fun getCustomFieldDefinitions(targetType: FieldTargetType, scope: String) = 
        repository.getCustomFieldDefinitions(targetType, scope)

    fun addCustomFieldDefinition(definition: CustomFieldDefinitionEntity) {
        viewModelScope.launch {
            repository.insertCustomFieldDefinition(definition)
        }
    }

    fun archiveCustomFieldDefinition(definitionId: Long) {
        viewModelScope.launch {
            repository.archiveCustomFieldDefinition(definitionId)
        }
    }

    fun updateCustomFieldDefinition(definition: CustomFieldDefinitionEntity) {
        viewModelScope.launch {
            repository.updateCustomFieldDefinition(definition)
        }
    }

    fun getCustomFieldValues(entityId: Long) = repository.getCustomFieldValues(entityId)

    suspend fun getLogById(id: Long): JournalLogEntity? {
        return repository.getLogById(id)
    }

    suspend fun getLatestLogForSupply(supplyId: Long): JournalLogEntity? {
        return repository.getLogsBySupply(supplyId).firstOrNull()
    }

    suspend fun getLogsBySupply(supplyId: Long): List<JournalLogEntity> {
        return repository.getLogsBySupply(supplyId)
    }

    fun deleteLog(log: JournalLogEntity) {
        viewModelScope.launch {
            repository.deleteLog(log)
        }
    }

    fun updateLog(log: JournalLogEntity) {
        viewModelScope.launch {
            repository.updateLog(log)
        }
    }

    private fun isSameDay(t1: Long, t2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun normalizeToStartOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun addJournalLog(
        assetId: Long,
        title: String,
        note: String,
        photoPath: String?,
        audioFilePath: String?,
        ecValue: Double?,
        phValue: Double?,
        tags: String = "",
        parameters: String = "",
        imageUris: String = "",
        activityType: String = "Observation",
        parentLogId: Long? = null,
        timestamp: Long = System.currentTimeMillis(),
        supplyId: Long? = null,
        customInputName: String? = null,
        batchGroupId: String? = null,
        targetZones: String? = null,
        customFieldValues: Map<Long, String> = emptyMap()
    ) {
        viewModelScope.launch {
            val log = JournalLogEntity(
                assetId = assetId,
                title = title,
                note = note,
                timestamp = timestamp,
                photoPath = photoPath,
                audioFilePath = audioFilePath,
                ecValue = ecValue,
                phValue = phValue,
                tags = tags,
                parameters = parameters,
                imageUris = imageUris,
                activityType = activityType,
                parentLogId = parentLogId,
                supplyId = supplyId,
                customInputName = customInputName,
                batchGroupId = batchGroupId,
                targetZones = targetZones
            )
            val logId = repository.insertLog(log)
            
            // Save dynamic values
            val values = customFieldValues.map { (defId, value) ->
                CustomFieldValueEntity(entityId = logId, fieldDefId = defId, value = value)
            }
            repository.insertCustomFieldValues(values)

            setShowAddBottomSheet(false)
        }
    }
}
