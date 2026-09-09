package com.mail2dev.planfora.ui.supplies

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.planfora.data.local.entity.DiySupplyEntity
import com.mail2dev.planfora.data.local.entity.CustomFieldDefinitionEntity
import com.mail2dev.planfora.data.local.entity.CustomFieldValueEntity
import com.mail2dev.planfora.data.local.entity.FieldTargetType
import com.mail2dev.planfora.data.local.entity.MasterTagEntity
import com.mail2dev.planfora.data.repository.JournalRepository
import com.mail2dev.planfora.data.repository.SupplyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AddSupplyViewModel(
    private val repository: SupplyRepository,
    private val journalRepository: JournalRepository
) : ViewModel() {
    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _category = MutableStateFlow(SupplyCategory.INSECTICIDE)
    val category = _category.asStateFlow()

    private val _formType = MutableStateFlow(com.mail2dev.planfora.data.local.entity.SupplyFormType.LIQUID)
    val formType = _formType.asStateFlow()

    private val _formulationCode = MutableStateFlow<String?>(null)
    val formulationCode = _formulationCode.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes = _notes.asStateFlow()

    private val _location = MutableStateFlow("")
    val location = _location.asStateFlow()

    private val _selectedTags = MutableStateFlow(setOf<String>())
    val selectedTags = _selectedTags.asStateFlow()

    private val _imageUris = MutableStateFlow(listOf<Uri>())
    val imageUris = _imageUris.asStateFlow()

    private val _audioPath = MutableStateFlow<String?>(null)
    val audioPath = _audioPath.asStateFlow()

    private val _visibleOptionalFields = MutableStateFlow(setOf<OptionalSupplyField>())
    val visibleOptionalFields = _visibleOptionalFields.asStateFlow()

    // Custom Fields State (Scoped to SUPPLY)
    private val _customFieldDefinitions = _category.flatMapLatest { cat ->
        journalRepository.getCustomFieldDefinitions(FieldTargetType.SUPPLY_CATEGORY, cat.displayName)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customFieldDefinitions = _customFieldDefinitions

    private val _customFieldValues = MutableStateFlow(mutableMapOf<Long, String>())
    val customFieldValues = _customFieldValues.asStateFlow()

    // Optional fields
    private val _activeIngredient = MutableStateFlow("")
    val activeIngredient = _activeIngredient.asStateFlow()

    private val _phiDays = MutableStateFlow("")
    val phiDays = _phiDays.asStateFlow()

    private val _reiHours = MutableStateFlow("")
    val reiHours = _reiHours.asStateFlow()

    private val _stockQuantity = MutableStateFlow("")
    val stockQuantity = _stockQuantity.asStateFlow()

    private val _stockUnit = MutableStateFlow("L")
    val stockUnit = _stockUnit.asStateFlow()

    private val _editingSupplyId = MutableStateFlow<Long?>(null)
    val editingSupplyId = _editingSupplyId.asStateFlow()

    val masterTags: StateFlow<List<String>> = journalRepository.getTagsByScope("SUPPLY")
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val masterLocations: StateFlow<List<String>> = journalRepository.getLocationsByScope("SUPPLY")
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val masterIngredients: StateFlow<List<String>> = journalRepository.getAllIngredients()
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateName(v: String) { _name.value = v }
    fun updateCategory(v: SupplyCategory) { _category.value = v }
    fun updateFormType(v: com.mail2dev.planfora.data.local.entity.SupplyFormType) { 
        _formType.value = v 
        // Smart unit defaults based on form type
        when (v) {
            com.mail2dev.planfora.data.local.entity.SupplyFormType.LIQUID -> {
                updateStockUnit("L")
                if (_formulationCode.value !in listOf("SL", "SC", "EC", "Other")) {
                    _formulationCode.value = null
                }
            }
            com.mail2dev.planfora.data.local.entity.SupplyFormType.POWDER -> {
                updateStockUnit("kg")
                if (_formulationCode.value !in listOf("WP", "SP", "Other")) {
                    _formulationCode.value = null
                }
            }
            com.mail2dev.planfora.data.local.entity.SupplyFormType.GRANULAR -> {
                updateStockUnit("kg")
                if (_formulationCode.value !in listOf("WG", "GR", "Other")) {
                    _formulationCode.value = null
                }
            }
            com.mail2dev.planfora.data.local.entity.SupplyFormType.SOLID -> {
                updateStockUnit("kg")
                if (_formulationCode.value != "Other") {
                    _formulationCode.value = null
                }
            }
        }
    }
    fun updateFormulationCode(v: String?) { 
        _formulationCode.value = v 
        // Smart unit defaults based on formulation code
        when (v) {
            "SL", "SC", "EC" -> {
                updateFormType(com.mail2dev.planfora.data.local.entity.SupplyFormType.LIQUID)
                updateStockUnit("L")
            }
            "WP", "WG", "SP" -> {
                updateFormType(com.mail2dev.planfora.data.local.entity.SupplyFormType.POWDER)
                updateStockUnit("kg")
            }
            "GR" -> {
                updateFormType(com.mail2dev.planfora.data.local.entity.SupplyFormType.GRANULAR)
                updateStockUnit("kg")
            }
        }
    }
    fun updateNotes(v: String) { _notes.value = v }
    fun updateLocation(v: String) { _location.value = v }
    
    fun toggleTag(tag: String) {
        _selectedTags.update { tags ->
            if (tags.contains(tag)) tags - tag else tags + tag
        }
    }

    fun addMasterTag(tag: String) {
        viewModelScope.launch {
            journalRepository.insertTag(MasterTagEntity(tag, "SUPPLY"))
            toggleTag(tag)
        }
    }

    fun updateMasterTag(oldTag: String, newTag: String) {
        viewModelScope.launch {
            journalRepository.updateTagName(oldTag, newTag, "SUPPLY")
            _selectedTags.update { tags ->
                if (tags.contains(oldTag)) tags - oldTag + newTag else tags
            }
        }
    }

    fun deleteMasterTag(tag: String) {
        viewModelScope.launch {
            journalRepository.deleteTagByName(tag, "SUPPLY")
            _selectedTags.update { it - tag }
        }
    }

    fun addMasterLocation(loc: String) {
        viewModelScope.launch {
            journalRepository.insertLocation(com.mail2dev.planfora.data.local.entity.MasterLocationEntity(loc, "SUPPLY"))
            updateLocation(loc)
        }
    }

    fun updateMasterLocation(oldName: String, newName: String) {
        viewModelScope.launch {
            journalRepository.updateLocationName(oldName, newName, "SUPPLY")
            if (location.value == oldName) updateLocation(newName)
        }
    }

    fun deleteMasterLocation(name: String) {
        viewModelScope.launch {
            journalRepository.deleteLocationByName(name, "SUPPLY")
            if (location.value == name) updateLocation("")
        }
    }

    fun startNewSupply() {
        _editingSupplyId.value = null
        reset()
    }

    fun addImageUri(uri: Uri) { _imageUris.update { it + uri } }
    fun removeImageUri(uri: Uri) { _imageUris.update { it - uri } }
    fun setAudioPath(path: String?) { _audioPath.value = path }

    fun toggleOptionalField(field: OptionalSupplyField) {
        _visibleOptionalFields.update { fields ->
            if (fields.contains(field)) fields - field else fields + field
        }
    }

    fun updateActiveIngredient(v: String) { _activeIngredient.value = v }

    fun addMasterIngredient(name: String) {
        viewModelScope.launch {
            journalRepository.insertIngredient(com.mail2dev.planfora.data.local.entity.MasterIngredientEntity(name))
            updateActiveIngredient(name)
        }
    }

    fun updateMasterIngredient(oldName: String, newName: String) {
        viewModelScope.launch {
            journalRepository.updateIngredientName(oldName, newName)
            if (activeIngredient.value == oldName) updateActiveIngredient(newName)
        }
    }

    fun deleteMasterIngredient(name: String) {
        viewModelScope.launch {
            journalRepository.deleteIngredientByName(name)
            if (activeIngredient.value == name) updateActiveIngredient("")
        }
    }

    fun updatePhiDays(v: String) { _phiDays.value = v }
    fun updateReiHours(v: String) { _reiHours.value = v }
    fun updateStockQuantity(v: String) { _stockQuantity.value = v }
    fun updateStockUnit(v: String) { _stockUnit.value = v }

    fun loadSupply(supplyId: Long) {
        viewModelScope.launch {
            repository.getAllSupplies().firstOrNull()?.find { it.id == supplyId }?.let { supply ->
                _editingSupplyId.value = supplyId
                _name.value = supply.name
                _category.value = SupplyCategory.entries.find { it.displayName == supply.category } ?: SupplyCategory.OTHER
                _formType.value = com.mail2dev.planfora.data.local.entity.SupplyFormType.entries.find { it.name == supply.formType } ?: com.mail2dev.planfora.data.local.entity.SupplyFormType.LIQUID
                _formulationCode.value = supply.formulationCode
                _notes.value = supply.notes
                _activeIngredient.value = supply.activeIngredient ?: ""
                _phiDays.value = supply.phiDays?.toString() ?: ""
                _reiHours.value = supply.reiHours?.toString() ?: ""
                _stockQuantity.value = supply.stockQuantity?.toString() ?: ""
                _stockUnit.value = supply.stockUnit ?: "L"
                _audioPath.value = supply.audioPath
                _imageUris.value = supply.imageUris.split(",").filter { it.isNotBlank() }.map { Uri.parse(it) }
                _location.value = supply.locationNote
                _selectedTags.value = supply.tags.split(",").filter { it.isNotBlank() }.toSet()
                
                // Load custom field values
                journalRepository.getCustomFieldValues(supplyId).firstOrNull()?.let { values ->
                    _customFieldValues.value = values.associate { it.fieldDefId to it.value }.toMutableMap()
                }
            }
        }
    }

    fun updateCustomFieldValue(fieldDefId: Long, value: String) {
        _customFieldValues.update { map ->
            val newMap = map.toMutableMap()
            newMap[fieldDefId] = value
            newMap
        }
    }

    fun addCustomFieldDefinition(definition: CustomFieldDefinitionEntity) {
        viewModelScope.launch {
            journalRepository.insertCustomFieldDefinition(definition)
        }
    }

    fun archiveCustomFieldDefinition(definitionId: Long) {
        viewModelScope.launch {
            journalRepository.archiveCustomFieldDefinition(definitionId)
        }
    }

    fun updateCustomFieldDefinition(definition: CustomFieldDefinitionEntity) {
        viewModelScope.launch {
            journalRepository.updateCustomFieldDefinition(definition)
        }
    }

    fun saveSupply() {
        viewModelScope.launch {
            val entity = DiySupplyEntity(
                id = _editingSupplyId.value ?: 0,
                name = _name.value,
                category = _category.value.displayName,
                formType = _formType.value.name,
                formulationCode = _formulationCode.value,
                notes = _notes.value,
                activeIngredient = _activeIngredient.value.ifBlank { null },
                phiDays = _phiDays.value.toIntOrNull(),
                reiHours = _reiHours.value.toIntOrNull(),
                stockQuantity = _stockQuantity.value.toFloatOrNull(),
                stockUnit = _stockUnit.value,
                batchCode = _name.value,
                imageUris = _imageUris.value.joinToString(",") { it.toString() },
                audioPath = _audioPath.value,
                locationNote = _location.value,
                tags = _selectedTags.value.joinToString(",")
            )
            
            val supplyId = if (_editingSupplyId.value == null) {
                repository.insertSupply(entity)
            } else {
                repository.updateSupply(entity)
                _editingSupplyId.value!!
            }

            // Save custom field values
            journalRepository.deleteCustomFieldValues(supplyId)
            val values = _customFieldValues.value.map { (defId, value) ->
                CustomFieldValueEntity(entityId = supplyId, fieldDefId = defId, value = value)
            }
            journalRepository.insertCustomFieldValues(values)

            reset()
        }
    }

    private fun reset() {
        _name.value = ""
        _notes.value = ""
        _activeIngredient.value = ""
        _phiDays.value = ""
        _reiHours.value = ""
        _stockQuantity.value = ""
        _stockUnit.value = "L"
        _location.value = ""
        _selectedTags.value = emptySet()
        _imageUris.value = emptyList()
        _audioPath.value = null
        _customFieldValues.value = mutableMapOf()
        _visibleOptionalFields.value = emptySet()
    }
}

enum class OptionalSupplyField(val displayName: String) {
    AI("Active Ingredient (A.I.)"),
    PHI("Pre-Harvest Interval (PHI)"),
    STOCK("Stock & Inventory"),
    REI("Re-Entry Interval (REI)"),
    NPK("NPK Ratio"),
    DILUTION("Dilution Rate"),
    TARGET_PESTS("Target Pests")
}
