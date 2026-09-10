package com.mail2dev.planfora.ui.assets

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.planfora.data.local.entity.CustomFieldDefinitionEntity
import com.mail2dev.planfora.data.local.entity.CustomFieldValueEntity
import com.mail2dev.planfora.data.local.entity.FieldTargetType
import com.mail2dev.planfora.data.local.entity.CustomFieldType
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

    private val _subLocation = MutableStateFlow("")
    val subLocation = _subLocation.asStateFlow()

    private val _totalPlants = MutableStateFlow("")
    val totalPlants = _totalPlants.asStateFlow()

    private val _selectedTags = MutableStateFlow(setOf<String>())
    val selectedTags = _selectedTags.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes = _notes.asStateFlow()

    private val _imageUris = MutableStateFlow(listOf<Uri>())
    val imageUris = _imageUris.asStateFlow()

    private val _audioPath = MutableStateFlow<String?>(null)
    val audioPath = _audioPath.asStateFlow()

    // Custom Fields State
    private val _customFieldDefinitions = _selectedCategory.flatMapLatest { cat ->
        repository.getCustomFieldDefinitions(FieldTargetType.ASSET_CATEGORY, cat.displayName)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customFieldDefinitions = _customFieldDefinitions

    private val _customFieldValues = MutableStateFlow(mutableMapOf<Long, String>())
    val customFieldValues = _customFieldValues.asStateFlow()

    // Internal hidden state for preserved DB columns
    private val _plantedDate = MutableStateFlow<Long?>(null)
    private val _acquisitionDate = MutableStateFlow<Long?>(null)
    private val _costValue = MutableStateFlow("")
    private val _batchTrayId = MutableStateFlow("")
    private val _motherPlantLink = MutableStateFlow("")
    private val _physicalId = MutableStateFlow("")
    private val _quantity = MutableStateFlow("")
    private val _propagatedDate = MutableStateFlow<Long?>(null)
    private val _rootstock = MutableStateFlow("")
    private val _plotRowId = MutableStateFlow("")
    private val _zones = MutableStateFlow("")
    private val _expectedHarvestDate = MutableStateFlow<Long?>(null)

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
    fun updateCategory(category: AssetCategory) { _selectedCategory.value = category }
    fun updateLocation(newLocation: String) { _location.value = newLocation }
    fun updateSubLocation(v: String) { _subLocation.value = v }
    fun updateTotalPlants(v: String) { _totalPlants.value = v }
    
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

    fun saveAsset() {
        viewModelScope.launch {
            val combinedTags = _selectedTags.value.toMutableList()
            val asset = PlantAssetEntity(
                id = _editingAssetId.value ?: 0,
                name = _name.value,
                category = _selectedCategory.value.displayName,
                plantedDate = _plantedDate.value,
                totalLogsCount = 0,
                lastActionDate = System.currentTimeMillis(),
                tags = combinedTags.joinToString(","),
                locationNote = _location.value,
                subLocation = _subLocation.value,
                totalPlants = _totalPlants.value.toIntOrNull() ?: 0,
                acquisitionDate = _acquisitionDate.value,
                notes = _notes.value,
                zones = _zones.value,
                imageUris = _imageUris.value.joinToString(",") { it.toString() },
                audioPath = _audioPath.value
            )
            
            val assetId = if (_editingAssetId.value == null) {
                repository.insertAsset(asset)
            } else {
                repository.updateAsset(asset)
                _editingAssetId.value!!
            }
            
            repository.deleteCustomFieldValues(assetId)
            val values = _customFieldValues.value.map { (defId, value) ->
                CustomFieldValueEntity(entityId = assetId, fieldDefId = defId, value = value)
            }
            repository.insertCustomFieldValues(values)
            
            reset()
        }
    }

    fun loadAsset(assetId: Long) {
        viewModelScope.launch {
            repository.getAssetById(assetId)?.let { asset ->
                _editingAssetId.value = assetId
                _name.value = asset.name
                _selectedCategory.value = AssetCategory.entries.find { it.displayName == asset.category } ?: AssetCategory.TREE
                _location.value = asset.locationNote
                _subLocation.value = asset.subLocation
                _totalPlants.value = if (asset.totalPlants > 0) asset.totalPlants.toString() else ""
                _notes.value = asset.notes
                _zones.value = asset.zones
                _plantedDate.value = if ((asset.plantedDate ?: 0L) > 0L) asset.plantedDate else null
                _acquisitionDate.value = if ((asset.acquisitionDate ?: 0L) > 0L) asset.acquisitionDate else null
                _audioPath.value = asset.audioPath
                _imageUris.value = asset.imageUris.split(",").filter { it.isNotBlank() }.map { Uri.parse(it) }
                
                val tagList = asset.tags.split(",")
                val userTags = tagList.filter { !it.contains(":") }.toSet()
                _selectedTags.value = userTags
                
                // Load custom field values
                repository.getCustomFieldValues(assetId).firstOrNull()?.let { values ->
                    _customFieldValues.value = values.associate { it.fieldDefId to it.value }.toMutableMap()
                }
            }
        }
    }

    private fun reset() {
        _name.value = ""
        _notes.value = ""
        _location.value = ""
        _subLocation.value = ""
        _totalPlants.value = ""
        _selectedTags.value = emptySet()
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
