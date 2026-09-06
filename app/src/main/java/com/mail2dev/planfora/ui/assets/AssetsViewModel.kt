package com.mail2dev.planfora.ui.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.planfora.data.local.entity.PlantAssetEntity
import com.mail2dev.planfora.data.repository.JournalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AssetCategory(val displayName: String, val description: String = "", val icon: String = "") {
    ALL("All", "", "📁"),
    TREE("Tree", "For old orchard trees, mature perennials", "🌳"),
    CROP_OR_VEGGIE("Crop/Veggie", "For short-term produce, vegetable beds, row crops", "🌽"),
    SEEDLING("Seedling", "For seeds, germination trays, young nursery stock", "🌱"),
    CUTTING("Cutting", "For marcots, air layers, clones, stem pieces", "✂️")
}

class AssetsViewModel(private val repository: JournalRepository) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(AssetCategory.ALL)
    val selectedCategory: StateFlow<AssetCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedAssetIds = MutableStateFlow(setOf<Long>())
    val selectedAssetIds: StateFlow<Set<Long>> = _selectedAssetIds.asStateFlow()

    private val _isMultiSelectMode = MutableStateFlow(false)
    val isMultiSelectMode: StateFlow<Boolean> = _isMultiSelectMode.asStateFlow()

    private val _showAddBottomSheet = MutableStateFlow(false)
    val showAddBottomSheet: StateFlow<Boolean> = _showAddBottomSheet.asStateFlow()

    val assets: StateFlow<List<PlantAssetEntity>> = repository.getAllAssets()
        .combine(_selectedCategory) { assets, category ->
            if (category == AssetCategory.ALL) assets else assets.filter { it.category == category.displayName }
        }
        .combine(_searchQuery) { assets, query ->
            if (query.isBlank()) assets else {
                assets.filter { 
                    it.name.contains(query, ignoreCase = true) || 
                    it.tags.contains(query, ignoreCase = true) ||
                    it.notes.contains(query, ignoreCase = true) ||
                    it.tags.contains("PhysID:$query", ignoreCase = true)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<com.mail2dev.planfora.data.local.entity.JournalLogEntity>> = repository.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupedAssets: StateFlow<Map<String, List<PlantAssetEntity>>> = assets
        .map { list -> list.groupBy { it.locationNote.ifBlank { "Unassigned" } } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun setCategory(category: AssetCategory) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleAssetSelection(assetId: Long) {
        if (!_isMultiSelectMode.value) {
            _isMultiSelectMode.value = true
        }
        _selectedAssetIds.update { set ->
            if (set.contains(assetId)) set - assetId else set + assetId
        }
        if (_selectedAssetIds.value.isEmpty()) {
            _isMultiSelectMode.value = false
        }
    }

    fun selectAllInZone(assetsInZone: List<PlantAssetEntity>) {
        _isMultiSelectMode.value = true
        val ids = assetsInZone.map { it.id }.toSet()
        _selectedAssetIds.update { it + ids }
    }

    fun clearSelection() {
        _selectedAssetIds.value = emptySet()
        _isMultiSelectMode.value = false
    }

    fun setShowAddBottomSheet(show: Boolean) {
        _showAddBottomSheet.value = show
    }

    fun promoteAssetCategory(asset: PlantAssetEntity, newCategory: AssetCategory) {
        viewModelScope.launch {
            val oldCategory = asset.category
            val updatedAsset = asset.copy(category = newCategory.displayName)
            repository.updateAsset(updatedAsset)
            
            // Automated Milestone Entry
            repository.insertLog(
                com.mail2dev.planfora.data.local.entity.JournalLogEntity(
                    assetId = asset.id,
                    title = "Graduation: ${newCategory.displayName}",
                    note = "🎓 Asset Graduated: Category updated from $oldCategory to ${newCategory.displayName}.",
                    timestamp = System.currentTimeMillis(),
                    activityType = "MILESTONE",
                    photoPath = null,
                    audioFilePath = null,
                    ecValue = null,
                    phValue = null
                )
            )
        }
    }

    fun updateAsset(asset: PlantAssetEntity) {
        viewModelScope.launch {
            repository.updateAsset(asset)
        }
    }

    fun deleteAsset(asset: PlantAssetEntity) {
        viewModelScope.launch {
            repository.deleteAsset(asset)
        }
    }

    fun addAsset(
        name: String,
        category: String,
        plantedDate: Long,
        tags: String = "",
        locationNote: String = "",
        acquisitionDate: Long = 0L
    ) {
        viewModelScope.launch {
            val asset = PlantAssetEntity(
                name = name,
                category = category,
                plantedDate = plantedDate,
                totalLogsCount = 0,
                lastActionDate = plantedDate,
                tags = tags,
                locationNote = locationNote,
                acquisitionDate = acquisitionDate
            )
            repository.insertAsset(asset)
            setShowAddBottomSheet(false)
        }
    }
}
