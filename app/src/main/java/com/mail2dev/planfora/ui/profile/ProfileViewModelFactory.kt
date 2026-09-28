package com.mail2dev.planfora.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mail2dev.planfora.data.local.AppDatabase
import com.mail2dev.planfora.data.repository.JournalRepository
import com.mail2dev.planfora.data.repository.SupplyRepository

class ProfileViewModelFactory(
    private val journalRepository: JournalRepository,
    private val supplyRepository: SupplyRepository,
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(journalRepository, supplyRepository, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
