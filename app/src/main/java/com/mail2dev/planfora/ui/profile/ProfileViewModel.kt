package com.mail2dev.planfora.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mail2dev.planfora.data.local.AppDatabase
import com.mail2dev.planfora.data.local.DataBackupManager
import com.mail2dev.planfora.data.repository.JournalRepository
import com.mail2dev.planfora.data.repository.SupplyRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class UnitSystem { METRIC, IMPERIAL }
enum class SubscriptionTier { FREE, PRO }

class ProfileViewModel(
    private val journalRepository: JournalRepository,
    private val supplyRepository: SupplyRepository,
    private val database: AppDatabase
) : ViewModel() {

    private val backupManager = DataBackupManager(database)
    private val analyticsManager = LabAnalyticsManager()

    private val _unitSystem = MutableStateFlow(UnitSystem.METRIC)
    val unitSystem: StateFlow<UnitSystem> = _unitSystem.asStateFlow()

    private val _use24HourFormat = MutableStateFlow(false)
    val use24HourFormat: StateFlow<Boolean> = _use24HourFormat.asStateFlow()

    private val _subscriptionTier = MutableStateFlow(SubscriptionTier.FREE)
    val subscriptionTier: StateFlow<SubscriptionTier> = _subscriptionTier.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<Long?>(null)
    val lastSyncTimestamp: StateFlow<Long?> = _lastSyncTimestamp.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val analytics: StateFlow<LabAnalytics> = combine(
        journalRepository.getAllAssets(),
        journalRepository.getAllLogs(),
        supplyRepository.getAllSupplies()
    ) { plants, logs, supplies ->
        analyticsManager.calculate(plants, logs, supplies)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LabAnalytics())

    fun setUnitSystem(system: UnitSystem) {
        _unitSystem.value = system
    }

    fun setUse24HourFormat(use24Hour: Boolean) {
        _use24HourFormat.value = use24Hour
    }

    fun triggerGoogleDriveSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            // Simulate sync delay
            delay(2000)
            _lastSyncTimestamp.value = System.currentTimeMillis()
            _isSyncing.value = false
        }
    }

    suspend fun exportLocalBackup(): String {
        return backupManager.generateBackupJson()
    }

    suspend fun restoreLocalBackup(jsonString: String): Boolean {
        return backupManager.restoreFromJson(jsonString)
    }

    fun purchaseProTier() {
        // RevenueCat SDK integration hook
        viewModelScope.launch {
            // Simulate purchase success
            delay(1000)
            _subscriptionTier.value = SubscriptionTier.PRO
        }
    }

    fun resetDatabase() {
        // TODO: Call Room database clearAllTables()
    }
}
