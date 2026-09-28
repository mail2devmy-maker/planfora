package com.mail2dev.planfora.ui.assets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mail2dev.planfora.data.repository.JournalRepository

class AssetsViewModelFactory(private val repository: JournalRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AssetsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AssetsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
