package com.mail2dev.planfora.ui.supplies

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.planfora.data.local.entity.DiySupplyEntity
import com.mail2dev.planfora.data.local.entity.CustomFieldDefinitionEntity
import com.mail2dev.planfora.data.local.entity.CustomFieldValueEntity
import com.mail2dev.planfora.data.local.entity.FieldTargetType
import com.mail2dev.planfora.data.local.entity.MasterTagEntity
import com.mail2dev.planfora.data.local.entity.SupplyCategory
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

    private val _subCategory = MutableStateFlow("")
    val subCategory = _subCategory.asStateFlow()

    private val _maturityDays = MutableStateFlow("")
    val maturityDays = _maturityDays.asStateFlow()

    private val _containerId = MutableStateFlow("")
    val containerId = _containerId.asStateFlow()

    private val _targetBenefit = MutableStateFlow("")
    val targetBenefit = _targetBenefit.asStateFlow()

    private val _materialLedger = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val materialLedger = _materialLedger.asStateFlow()

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

    private val _isLabMode = MutableStateFlow(false)
    val isLabMode = _isLabMode.asStateFlow()

    private val _isProductionUpdate = MutableStateFlow(false)
    val isProductionUpdate = _isProductionUpdate.asStateFlow()

    private val _historicalMaterialCount = MutableStateFlow(0)
    val historicalMaterialCount = _historicalMaterialCount.asStateFlow()

    private val _customTimestamp = MutableStateFlow(System.currentTimeMillis())
    val customTimestamp = _customTimestamp.asStateFlow()

    fun updateCustomTimestamp(v: Long) {
        _customTimestamp.value = v
    }

    val masterTags: StateFlow<List<String>> = journalRepository.getTagsByScope("SUPPLY")
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val masterLocations: StateFlow<List<String>> = journalRepository.getLocationsByScope("SUPPLY")
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val masterIngredients: StateFlow<List<String>> = journalRepository.getAllIngredients()
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateName(v: String) { 
        _name.value = v 
        
        // Smart Formulation Detection from Product Name
        val uppercaseName = v.uppercase()
        
        // Check Liquids
        val liquidCodes = listOf("SL", "SC", "EC")
        val matchedLiquidCode = liquidCodes.find { code ->
            uppercaseName.contains(code) || uppercaseName.contains("\\b$code\\b".toRegex()) || uppercaseName.contains("[0-9]$code".toRegex())
        }
        val hasLiquidText = uppercaseName.contains("LIQUID") || uppercaseName.contains("FLOWABLE")
        
        // Check Powders/Granules/Solids
        val powderCodes = listOf("WP", "SP")
        val granularCodes = listOf("WG", "GR")
        val matchedPowderCode = powderCodes.find { code ->
            uppercaseName.contains(code) || uppercaseName.contains("\\b$code\\b".toRegex()) || uppercaseName.contains("[0-9]$code".toRegex())
        }
        val matchedGranularCode = granularCodes.find { code ->
            uppercaseName.contains(code) || uppercaseName.contains("\\b$code\\b".toRegex()) || uppercaseName.contains("[0-9]$code".toRegex())
        }
        val hasPowderText = uppercaseName.contains("POWDER")
        val hasGranularText = uppercaseName.contains("GRANULAR")
        val hasSolidText = uppercaseName.contains("SOLID")

        when {
            matchedLiquidCode != null -> {
                _formType.value = com.mail2dev.planfora.data.local.entity.SupplyFormType.LIQUID
                _formulationCode.value = matchedLiquidCode
                _stockUnit.value = "L"
            }
            hasLiquidText -> {
                _formType.value = com.mail2dev.planfora.data.local.entity.SupplyFormType.LIQUID
                _formulationCode.value = "Other"
                _stockUnit.value = "L"
            }
            matchedPowderCode != null -> {
                _formType.value = com.mail2dev.planfora.data.local.entity.SupplyFormType.POWDER
                _formulationCode.value = matchedPowderCode
                _stockUnit.value = "kg"
            }
            hasPowderText -> {
                _formType.value = com.mail2dev.planfora.data.local.entity.SupplyFormType.POWDER
                _formulationCode.value = "Other"
                _stockUnit.value = "kg"
            }
            matchedGranularCode != null -> {
                _formType.value = com.mail2dev.planfora.data.local.entity.SupplyFormType.GRANULAR
                _formulationCode.value = matchedGranularCode
                _stockUnit.value = "kg"
            }
            hasGranularText -> {
                _formType.value = com.mail2dev.planfora.data.local.entity.SupplyFormType.GRANULAR
                _formulationCode.value = "Other"
                _stockUnit.value = "kg"
            }
            hasSolidText -> {
                _formType.value = com.mail2dev.planfora.data.local.entity.SupplyFormType.SOLID
                _formulationCode.value = "Other"
                _stockUnit.value = "kg"
            }
        }
    }
    fun updateCategory(v: SupplyCategory) { _category.value = v }
    fun updateSubCategory(v: String) { _subCategory.value = v }
    fun updateMaturityDays(v: String) { _maturityDays.value = v }
    fun updateContainerId(v: String) { _containerId.value = v }
    fun updateTargetBenefit(v: String) { _targetBenefit.value = v }

    fun addMaterialToLedger() {
        _materialLedger.update { it + ("" to "") }
    }

    fun updateMaterialInLedger(index: Int, name: String, amount: String) {
        _materialLedger.update { list ->
            list.toMutableList().apply {
                this[index] = name to amount
            }
        }
    }

    fun removeMaterialFromLedger(index: Int) {
        _materialLedger.update { list ->
            list.toMutableList().apply { removeAt(index) }
        }
    }

    fun updateFormType(v: com.mail2dev.planfora.data.local.entity.SupplyFormType) { 
        _formType.value = v 
        // Smart unit defaults based on form type
        when (v) {
            com.mail2dev.planfora.data.local.entity.SupplyFormType.LIQUID -> {
                _stockUnit.value = "L"
                if (_formulationCode.value !in listOf("SL", "SC", "EC", "Other")) {
                    _formulationCode.value = null
                }
            }
            com.mail2dev.planfora.data.local.entity.SupplyFormType.POWDER -> {
                _stockUnit.value = "kg"
                if (_formulationCode.value !in listOf("WP", "SP", "Other")) {
                    _formulationCode.value = null
                }
            }
            com.mail2dev.planfora.data.local.entity.SupplyFormType.GRANULAR -> {
                _stockUnit.value = "kg"
                if (_formulationCode.value !in listOf("WG", "GR", "Other")) {
                    _formulationCode.value = null
                }
            }
            com.mail2dev.planfora.data.local.entity.SupplyFormType.SOLID -> {
                _stockUnit.value = "kg"
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

    fun startNewSupply(defaultCategory: SupplyCategory = SupplyCategory.INSECTICIDE, isLab: Boolean = false) {
        _editingSupplyId.value = null
        _isProductionUpdate.value = false
        _historicalMaterialCount.value = 0
        _customTimestamp.value = System.currentTimeMillis()
        reset()
        _category.value = defaultCategory
        _isLabMode.value = isLab
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

    fun loadSupply(supplyId: Long, isProductionUpdate: Boolean = false) {
        // Reset state synchronously to avoid "state leakage" from previous sessions
        reset()
        _isProductionUpdate.value = isProductionUpdate
        _editingSupplyId.value = supplyId
        _customTimestamp.value = System.currentTimeMillis()

        viewModelScope.launch {
            repository.getAllSupplies().firstOrNull()?.find { it.id == supplyId }?.let { supply ->
                _name.value = supply.name
                _category.value = SupplyCategory.entries.find { it.displayName == supply.category } ?: SupplyCategory.DIY
                _subCategory.value = supply.subCategory ?: ""
                _containerId.value = supply.containerId
                _targetBenefit.value = supply.targetBenefit ?: ""
                
                // Parse material list into ledger
                val materials = supply.materialList.split("|").filter { it.contains(":") }.map { 
                    val parts = it.split(":")
                    parts[0] to parts[1]
                }
                _materialLedger.value = materials
                _historicalMaterialCount.value = if (isProductionUpdate) materials.size else 0

                if (supply.targetMaturityDate > supply.startDate) {
                    val days = (supply.targetMaturityDate - supply.startDate) / (24L * 60 * 60 * 1000)
                    _maturityDays.value = days.toString()
                } else {
                    _maturityDays.value = ""
                }
                _formType.value = com.mail2dev.planfora.data.local.entity.SupplyFormType.entries.find { it.name == supply.formType } ?: com.mail2dev.planfora.data.local.entity.SupplyFormType.LIQUID
                _formulationCode.value = supply.formulationCode
                _notes.value = "" // Fresh notes for the update entry
                _activeIngredient.value = supply.activeIngredient ?: ""
                _phiDays.value = supply.phiDays?.toString() ?: ""
                _reiHours.value = supply.reiHours?.toString() ?: ""
                _stockQuantity.value = supply.stockQuantity?.toString() ?: ""
                _stockUnit.value = supply.stockUnit ?: "L"
                
                // Media is only loaded for "Edit Profile", not for "Update Progress" (Research Diary style)
                if (!isProductionUpdate) {
                    _audioPath.value = supply.audioPath
                    _imageUris.value = supply.imageUris.split(",").filter { it.isNotBlank() }.map { Uri.parse(it) }
                }

                _location.value = supply.locationNote
                _selectedTags.value = supply.tags.split(",").filter { it.isNotBlank() }.toSet()
                _isLabMode.value = (supply.category == "DIY" && !supply.isArchived)
                
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
            val chosenTime = _customTimestamp.value
            val days = _maturityDays.value.toLongOrNull() ?: 0L
            val targetMaturity = if (days > 0) chosenTime + (days * 24 * 60 * 60 * 1000) else 0L
            
            val newMaterialList = _materialLedger.value.joinToString("|") { "${it.first}:${it.second}" }
            
            // Fetch previous state for logging
            val previousSupply = _editingSupplyId.value?.let { repository.getAllSupplies().firstOrNull()?.find { s -> s.id == it } }

            val entity = DiySupplyEntity(
                id = _editingSupplyId.value ?: 0,
                name = _name.value,
                category = _category.value.displayName,
                subCategory = if (_category.value == SupplyCategory.DIY) _subCategory.value else null,
                containerId = _containerId.value,
                materialList = newMaterialList,
                targetBenefit = _targetBenefit.value,
                formType = _formType.value.name,
                formulationCode = _formulationCode.value,
                notes = if (_isProductionUpdate.value) (previousSupply?.notes ?: "") else _notes.value,
                activeIngredient = _activeIngredient.value.ifBlank { null },
                phiDays = _phiDays.value.toIntOrNull(),
                reiHours = _reiHours.value.toIntOrNull(),
                stockQuantity = _stockQuantity.value.toFloatOrNull(),
                stockUnit = _stockUnit.value,
                batchCode = if (_editingSupplyId.value == null) "" else _name.value,
                startDate = previousSupply?.startDate ?: chosenTime,
                targetMaturityDate = if (_editingSupplyId.value == null) targetMaturity else previousSupply?.targetMaturityDate ?: 0L,
                imageUris = if (_isProductionUpdate.value) (previousSupply?.imageUris ?: "") else _imageUris.value.joinToString(",") { it.toString() },
                audioPath = if (_isProductionUpdate.value) (previousSupply?.audioPath) else _audioPath.value,
                locationNote = _location.value,
                tags = _selectedTags.value.joinToString(",")
            )
            
            val supplyId = if (_editingSupplyId.value == null) {
                repository.insertSupply(entity)
            } else {
                repository.updateSupply(entity)
                _editingSupplyId.value!!
            }

            // --- RESEARCH LOGGING LOGIC ---
            val isNewProject = _editingSupplyId.value == null
            val logTitle = if (isNewProject) "Project Started: ${_subCategory.value ?: "DIY"}" else "Production Update"
            
            // Build log notes based on what changed/was added
            val logNoteBuilder = StringBuilder()
            if (isNewProject) {
                logNoteBuilder.append("Started batch with: \n")
                _materialLedger.value.forEach { logNoteBuilder.append("• ${it.first}: ${it.second}\n") }
            } else {
                // Find new materials added in this update
                val oldMaterials = previousSupply?.materialList?.split("|")?.toSet() ?: emptySet()
                val newlyAdded = _materialLedger.value.filter { "${it.first}:${it.second}" !in oldMaterials }
                if (newlyAdded.isNotEmpty()) {
                    logNoteBuilder.append("Added materials: \n")
                    newlyAdded.forEach { logNoteBuilder.append("• ${it.first}: ${it.second}\n") }
                }
                
                if (previousSupply?.containerId != _containerId.value) {
                    logNoteBuilder.append("Moved to vessel: ${_containerId.value}\n")
                }
            }
            
            if (_notes.value.isNotBlank()) {
                logNoteBuilder.append("\nNote: ${_notes.value}")
            }

            journalRepository.insertLog(
                com.mail2dev.planfora.data.local.entity.JournalLogEntity(
                    assetId = null,
                    supplyId = supplyId,
                    title = logTitle,
                    note = logNoteBuilder.toString(),
                    timestamp = chosenTime,
                    imageUris = _imageUris.value.joinToString(",") { it.toString() },
                    audioFilePath = _audioPath.value,
                    activityType = "PRODUCTION",
                    photoPath = null,
                    ecValue = null,
                    phValue = null
                )
            )

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
        _subCategory.value = ""
        _maturityDays.value = ""
        _containerId.value = ""
        _targetBenefit.value = ""
        _materialLedger.value = emptyList()
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
