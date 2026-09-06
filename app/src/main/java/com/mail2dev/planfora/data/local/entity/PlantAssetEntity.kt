package com.mail2dev.planfora.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "plant_assets")
@Serializable
data class PlantAssetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val plantedDate: Long? = null,
    val totalLogsCount: Int = 0,
    val lastActionDate: Long,
    val tags: String = "", // #Perennial, #Indoor
    val locationNote: String = "",
    val acquisitionDate: Long? = null,
    val notes: String = "",
    val zones: String = "", // NEW: Comma-separated zone names, e.g., "Row 1,Row 2,Row 3"
    val imageUris: String = "",
    val audioPath: String? = null
)
