package com.mail2dev.planfora.ui.supplies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.planfora.data.local.entity.DiySupplyEntity
import com.mail2dev.planfora.data.repository.SupplyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SupplyCategory(val displayName: String) {
    ALL("All"),
    INSECTICIDE("Insecticides"),
    FUNGICIDE("Fungicides"),
    HERBICIDE("Herbicides"),
    FERTILIZER("Fertilizer"),
    DIY("DIY"),
    SUPPLIES_TOOLS("Supplies & Tools")
}

class SuppliesViewModel(private val repository: SupplyRepository) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(SupplyCategory.ALL)
    val selectedCategory: StateFlow<SupplyCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val supplies: StateFlow<List<DiySupplyEntity>> = repository.getAllSupplies()
        .combine(_selectedCategory) { supplies, category ->
            if (category == SupplyCategory.ALL) {
                supplies
            } else {
                supplies.filter { it.category == category.displayName }
            }
        }.combine(_searchQuery) { filtered, query ->
            if (query.isBlank()) {
                filtered
            } else {
                filtered.filter { 
                    it.name.contains(query, ignoreCase = true) || 
                    it.category.contains(query, ignoreCase = true) ||
                    it.batchCode.contains(query, ignoreCase = true)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showAddBottomSheet = MutableStateFlow(false)
    val showAddBottomSheet: StateFlow<Boolean> = _showAddBottomSheet.asStateFlow()

    fun setShowAddBottomSheet(show: Boolean) {
        _showAddBottomSheet.value = show
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: SupplyCategory) {
        _selectedCategory.value = category
    }

    fun deleteSupply(supply: DiySupplyEntity) {
        viewModelScope.launch {
            repository.deleteSupply(supply)
        }
    }

    fun updateSupply(supply: DiySupplyEntity) {
        viewModelScope.launch {
            repository.updateSupply(supply)
        }
    }

    fun addSupply(
        name: String,
        category: String,
        volume: Double,
        unit: String,
        targetMaturityDate: Long?,
        activeIngredient: String? = null,
        activePercentage: String? = null
    ) {
        viewModelScope.launch {
            val maxBatch = repository.generateNextBatchName(name) // This gets "Name #Num"
            val batchNumber = maxBatch.substringAfterLast("#").toIntOrNull() ?: 1
            
            val entity = DiySupplyEntity(
                name = name,
                category = category,
                batchNumber = batchNumber,
                batchCode = "$name #$batchNumber",
                startDate = System.currentTimeMillis(),
                targetMaturityDate = targetMaturityDate ?: 0L,
                currentVolume = volume,
                originalVolume = volume,
                unit = unit,
                notifyOnMaturity = targetMaturityDate != null,
                activeIngredient = activeIngredient,
                activePercentage = activePercentage
            )
            repository.insertSupply(entity)
            setShowAddBottomSheet(false)
        }
    }
}
