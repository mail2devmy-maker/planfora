package com.mail2dev.planfora.data.local

import androidx.room.withTransaction
import com.mail2dev.planfora.data.local.entity.DiySupplyEntity
import com.mail2dev.planfora.data.local.entity.JournalLogEntity
import com.mail2dev.planfora.data.local.entity.PlantAssetEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class PlanForaBackup(
    val version: Int,
    val exportTimestamp: Long,
    val plants: List<PlantAssetEntity>,
    val logs: List<JournalLogEntity>,
    val supplies: List<DiySupplyEntity>
)

class DataBackupManager(private val database: AppDatabase) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun generateBackupJson(): String {
        val plants = database.plantAssetDao().getAllAssetsOnce()
        val logs = database.journalLogDao().getAllLogsOnce()
        val supplies = database.diySupplyDao().getAllSuppliesOnce()

        val backup = PlanForaBackup(
            version = 7,
            exportTimestamp = System.currentTimeMillis(),
            plants = plants,
            logs = logs,
            supplies = supplies
        )

        return json.encodeToString(backup)
    }

    suspend fun restoreFromJson(jsonString: String): Boolean {
        return try {
            val backup = json.decodeFromString<PlanForaBackup>(jsonString)
            
            if (backup.version > 7) return false

            database.withTransaction {
                database.plantAssetDao().insertAssets(backup.plants)
                database.journalLogDao().insertLogs(backup.logs)
                database.diySupplyDao().insertSupplies(backup.supplies)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
