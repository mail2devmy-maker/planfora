package com.mail2dev.planfora.ui.assets

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.planfora.data.local.entity.CustomFieldDefinitionEntity
import com.mail2dev.planfora.data.local.entity.CustomFieldValueEntity
import com.mail2dev.planfora.data.local.entity.MasterLocationEntity
import com.mail2dev.planfora.data.local.entity.MasterTagEntity
import com.mail2dev.planfora.data.local.entity.PlantAssetEntity
import com.mail2dev.planfora.data.repository.JournalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddAssetViewModel(private val repository: JournalRepository) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _selectedCategory = MutableStateFlow(AssetCategory.TREE)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _location = MutableStateFlow("")
    val location = _location.asStateFlow()

    private val _selectedTags = MutableStateFlow(setOf<String>())
    val selectedTags = _selectedTags.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes = _notes.asStateFlow()

    private val _imageUris = MutableStateFlow(listOf<Uri>())
    val imageUris = _imageUris.asStateFlow()

    private val _audioPath = MutableStateFlow<String?>(null)
    val audioPath = _audioPath.asStateFlow()

    private val _visibleOptionalFields = MutableStateFlow(setOf<OptionalField>())
    val visibleOptionalFields = _visibleOptionalFields.asStateFlow()

    // Custom Fields State
    private val _customFieldDefinitions = _selectedCategory.flatMapLatest { cat ->
        repository.getCustomFieldDefinitions(cat.displayName)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customFieldDefinitions = _customFieldDefinitions

    private val _customFieldValues = MutableStateFlow(mutableMapOf<Long, String>())
    val customFieldValues = _customFieldValues.asStateFlow()

    // Optional field values
    private val _plantedDate = MutableStateFlow<Long?>(null)
    val plantedDate = _plantedDate.asStateFlow()

    private val _acquisitionDate = MutableStateFlow<Long?>(null)
    val acquisitionDate = _acquisitionDate.asStateFlow()

    private val _costValue = MutableStateFlow("")
    val costValue = _costValue.asStateFlow()

    private val _batchTrayId = MutableStateFlow("")
    val batchTrayId = _batchTrayId.asStateFlow()

    private val _motherPlantLink = MutableStateFlow("")
    val motherPlantLink = _motherPlantLink.asStateFlow()

    private val _physicalId = MutableStateFlow("")
    val physicalId = _physicalId.asStateFlow()

    private val _quantity = MutableStateFlow("")
    val quantity = _quantity.asStateFlow()

    private val _propagatedDate = MutableStateFlow<Long?>(null)
    val propagatedDate = _propagatedDate.asStateFlow()

    private val _rootstock = MutableStateFlow("")
    val rootstock = _rootstock.asStateFlow()

    private val _plotRowId = MutableStateFlow("")
    val plotRowId = _plotRowId.asStateFlow()

    private val _zones = MutableStateFlow("")
    val zones = _zones.asStateFlow()

    private val _expectedHarvestDate = MutableStateFlow<Long?>(null)
    val expectedHarvestDate = _expectedHarvestDate.asStateFlow()

    private val _editingAssetId = MutableStateFlow<Long?>(null)
    val editingAssetId = _editingAssetId.asStateFlow()

    val masterLocations: StateFlow<List<String>> = repository.getLocationsByScope("ASSET")
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val masterTags: StateFlow<List<String>> = repository.getTagsByScope("ASSET")
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateName(newName: String) { _name.value = newName }
    fun updateNotes(newNotes: String) { _notes.value = newNotes }
    fun updateCategory(category: AssetCategory) { 
        _selectedCategory.value = category 
        // Logic for auto-highlighting suggested chips could be here, 
        // but it's mainly a UI concern for highlighting.
    }
    fun updateLocation(newLocation: String) { _location.value = newLocation }
    
    fun toggleTag(tag: String) {
        _selectedTags.update { tags ->
            if (tags.contains(tag)) tags - tag else tags + tag
        }
    }

    fun addMasterTag(tag: String) {
        viewModelScope.launch {
            repository.insertTag(MasterTagEntity(tag, "ASSET"))
            toggleTag(tag)
        }
    }

    fun updateMasterTag(oldTag: String, newTag: String) {
        viewModelScope.launch {
            repository.updateTagName(oldTag, newTag, "ASSET")
            _selectedTags.update { tags ->
                if (tags.contains(oldTag)) tags - oldTag + newTag else tags
            }
        }
    }

    fun deleteMasterTag(tag: String) {
        viewModelScope.launch {
            repository.deleteTagByName(tag, "ASSET")
            _selectedTags.update { it - tag }
        }
    }

    fun addMasterLocation(loc: String) {
        viewModelScope.launch {
            repository.insertLocation(MasterLocationEntity(loc, "ASSET"))
            updateLocation(loc)
        }
    }

    fun updateMasterLocation(oldName: String, newName: String) {
        viewModelScope.launch {
            repository.updateLocationName(oldName, newName, "ASSET")
            if (location.value == oldName) updateLocation(newName)
        }
    }

    fun deleteMasterLocation(name: String) {
        viewModelScope.launch {
            repository.deleteLocationByName(name, "ASSET")
            if (location.value == name) updateLocation("")
        }
    }

    fun startNewAsset() {
        _editingAssetId.value = null
        reset()
    }

    fun toggleOptionalField(field: OptionalField) {
        _visibleOptionalFields.update { fields ->
            if (fields.contains(field)) fields - field else fields + field
        }
    }

    fun updatePlantedDate(date: Long?) { _plantedDate.value = date }
    fun updateAcquisitionDate(date: Long?) { _acquisitionDate.value = date }
    fun updateCostValue(value: String) { _costValue.value = value }
    fun updateBatchTrayId(id: String) { _batchTrayId.value = id }
    fun updateMotherPlantLink(link: String) { _motherPlantLink.value = link }
    fun updatePhysicalId(id: String) { _physicalId.value = id }
    fun updateQuantity(q: String) { _quantity.value = q }
    fun updatePropagatedDate(date: Long?) { _propagatedDate.value = date }
    fun updateRootstock(r: String) { _rootstock.value = r }
    fun updatePlotRowId(id: String) { _plotRowId.value = id }
    fun updateZones(zones: String) { _zones.value = zones }
    fun updateExpectedHarvestDate(date: Long?) { _expectedHarvestDate.value = date }

    fun loadAsset(assetId: Long) {
        viewModelScope.launch {
            repository.getAssetById(assetId)?.let { asset ->
                _editingAssetId.value = assetId
                _name.value = asset.name
                _selectedCategory.value = AssetCategory.entries.find { it.displayName == asset.category } ?: AssetCategory.TREE
                _location.value = asset.locationNote
                _notes.value = asset.notes
                _zones.value = asset.zones
                _plantedDate.value = if ((asset.plantedDate ?: 0L) > 0L) asset.plantedDate else null
                _acquisitionDate.value = if ((asset.acquisitionDate ?: 0L) > 0L) asset.acquisitionDate else null
                _audioPath.value = asset.audioPath
                _imageUris.value = asset.imageUris.split(",").filter { it.isNotBlank() }.map { Uri.parse(it) }
                
                // Parse tags
                val tagList = asset.tags.split(",")
                val userTags = tagList.filter { !it.contains(":") }.toSet()
                _selectedTags.value = userTags
                
                tagList.forEach { tag ->
                    when {
                        tag.startsWith("Batch:") -> _batchTrayId.value = tag.substringAfter(":")
                        tag.startsWith("PhysID:") -> _physicalId.value = tag.substringAfter(":")
                        tag.startsWith("Qty:") -> _quantity.value = tag.substringAfter(":")
                        tag.startsWith("Mother:") -> _motherPlantLink.value = tag.substringAfter(":")
                        tag.startsWith("Rootstock:") -> _rootstock.value = tag.substringAfter(":")
                        tag.startsWith("Plot:") -> _plotRowId.value = tag.substringAfter(":")
                        tag.startsWith("PropDate:") -> { /* TODO: parse date if needed, but current UI uses System.currentTimeMillis() or selected date */ }
                        tag.startsWith("ExpHarv:") -> { /* TODO: parse date if needed */ }
                    }
                }
                
                // If special tags exist, toggle the optional fields visibility
                val newVisibleFields = mutableSetOf<OptionalField>()
                if (_batchTrayId.value.isNotBlank()) newVisibleFields.add(OptionalField.BATCH_TRAY_ID)
                if (_physicalId.value.isNotBlank()) newVisibleFields.add(OptionalField.PHYSICAL_ID)
                if (_quantity.value.isNotBlank()) newVisibleFields.add(OptionalField.QUANTITY)
                if (_motherPlantLink.value.isNotBlank()) newVisibleFields.add(OptionalField.MOTHER_PLANT_LINK)
                if (_rootstock.value.isNotBlank()) newVisibleFields.add(OptionalField.ROOTSTOCK)
                if (_plotRowId.value.isNotBlank()) newVisibleFields.add(OptionalField.PLOT_ROW_ID)
                if ((asset.plantedDate ?: 0L) > 0L) newVisibleFields.add(OptionalField.PLANTING_DATE)
                if ((asset.acquisitionDate ?: 0L) > 0L) newVisibleFields.add(OptionalField.ACQUISITION_DETAILS)
                
                _visibleOptionalFields.value = newVisibleFields
            }
        }
    }

    fun addImageUri(uri: Uri) { _imageUris.update { it + uri } }
    fun removeImageUri(uri: Uri) { _imageUris.update { it - uri } }
    fun setAudioPath(path: String?) { _audioPath.value = path }

    fun updateCustomFieldValue(fieldDefId: Long, value: String) {
        _customFieldValues.update { map ->
            val newMap = map.toMutableMap()
            newMap[fieldDefId] = value
            newMap
        }
    }

    fun addCustomFieldDefinition(name: String, type: String, optionsJson: String?, isGlobal: Boolean) {
        viewModelScope.launch {
            repository.insertCustomFieldDefinition(
                CustomFieldDefinitionEntity(
                    category = if (isGlobal) "Global" else _selectedCategory.value.displayName,
                    fieldName = name,
                    fieldType = type,
                    radioOptionsJson = optionsJson
                )
            )
        }
    }

    fun saveAsset() {
        viewModelScope.launch {
            val combinedTags = _selectedTags.value.toMutableList()
            if (_batchTrayId.value.isNotBlank()) combinedTags.add("Batch:${_batchTrayId.value}")
            if (_physicalId.value.isNotBlank()) combinedTags.add("PhysID:${_physicalId.value}")
            if (_quantity.value.isNotBlank()) combinedTags.add("Qty:${_quantity.value}")
            if (_motherPlantLink.value.isNotBlank()) combinedTags.add("Mother:${_motherPlantLink.value}")
            if (_rootstock.value.isNotBlank()) combinedTags.add("Rootstock:${_rootstock.value}")
            if (_plotRowId.value.isNotBlank()) combinedTags.add("Plot:${_plotRowId.value}")
            
            val tagSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            _propagatedDate.value?.let { combinedTags.add("PropDate:${tagSdf.format(Date(it))}") }
            _expectedHarvestDate.value?.let { combinedTags.add("ExpHarv:${tagSdf.format(Date(it))}") }

            val asset = PlantAssetEntity(
                id = _editingAssetId.value ?: 0,
                name = _name.value,
                category = _selectedCategory.value.displayName,
                plantedDate = _plantedDate.value,
                totalLogsCount = 0, // In reality, we should keep the current count if editing
                lastActionDate = System.currentTimeMillis(),
                tags = combinedTags.joinToString(","),
                locationNote = _location.value,
                acquisitionDate = _acquisitionDate.value,
                notes = _notes.value,
                zones = _zones.value,
                imageUris = _imageUris.value.joinToString(",") { it.toString() },
                audioPath = _audioPath.value
            )
            
            if (_editingAssetId.value == null) {
                repository.insertAsset(asset)
            } else {
                repository.updateAsset(asset)
            }
            
            // We need the newly created asset ID to save custom field values.
            // Assuming repository.insertAsset returns the ID or we query it.
            // For now, let's assume we handle value persistence in repository.insertAsset or separate logic.
            // repository.insertCustomFieldValues(...)
            
            reset()
        }
    }

    private fun reset() {
        _name.value = ""
        _notes.value = ""
        _location.value = ""
        _selectedTags.value = emptySet()
        _visibleOptionalFields.value = emptySet()
        _plantedDate.value = null
        _acquisitionDate.value = null
        _propagatedDate.value = null
        _expectedHarvestDate.value = null
        _batchTrayId.value = ""
        _physicalId.value = ""
        _quantity.value = ""
        _rootstock.value = ""
        _plotRowId.value = ""
        _zones.value = ""
        _imageUris.value = emptyList()
        _audioPath.value = null
        _customFieldValues.value = mutableMapOf()
    }
}

enum class OptionalField(val displayName: String, val icon: String) {
    PLANTING_DATE("Planting Date", "📅"),
    ACQUISITION_DETAILS("Acquisition Details", "🛒"),
    GPS_COORDINATES("GPS Coordinates", "📍"),
    COST_VALUE("Cost/Value", "💰"),
    BATCH_TRAY_ID("Batch / Tray ID", "🧪"),
    MOTHER_PLANT_LINK("Mother Plant Link", "✂️"),
    PHYSICAL_ID("Physical ID / Tree #", "🔢"),
    QUANTITY("Quantity", "🔢"),
    PROPAGATED_DATE("Propagated Date", "📅"),
    ROOTSTOCK("Rootstock", "🌳"),
    PLOT_ROW_ID("Plot / Row ID", "📍"),
    EXPECTED_HARVEST_DATE("Expected Harvest", "📅")
}
