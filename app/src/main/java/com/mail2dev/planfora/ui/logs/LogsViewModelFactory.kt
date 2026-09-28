package com.mail2dev.planfora.ui.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mail2dev.planfora.data.repository.JournalRepository
import com.mail2dev.planfora.data.repository.SupplyRepository

class LogsViewModelFactory(
    private val repository: JournalRepository,
    private val supplyRepository: SupplyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LogsViewModel(repository, supplyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
