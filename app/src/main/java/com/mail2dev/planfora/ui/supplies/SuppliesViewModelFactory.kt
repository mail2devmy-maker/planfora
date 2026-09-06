package com.mail2dev.planfora.ui.supplies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mail2dev.planfora.data.repository.SupplyRepository

class SuppliesViewModelFactory(private val repository: SupplyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SuppliesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SuppliesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
