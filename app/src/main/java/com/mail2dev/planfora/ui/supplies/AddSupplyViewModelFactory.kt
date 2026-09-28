package com.mail2dev.planfora.ui.supplies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mail2dev.planfora.data.repository.JournalRepository
import com.mail2dev.planfora.data.repository.SupplyRepository

class AddSupplyViewModelFactory(
    private val supplyRepository: SupplyRepository,
    private val journalRepository: JournalRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddSupplyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddSupplyViewModel(supplyRepository, journalRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
