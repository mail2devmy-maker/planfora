package com.mail2dev.planfora.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "journal_logs",
    foreignKeys = [
        ForeignKey(
            entity = PlantAssetEntity::class,
            parentColumns = ["id"],
            childColumns = ["assetId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = JournalLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentLogId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = DiySupplyEntity::class,
            parentColumns = ["id"],
            childColumns = ["supplyId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["assetId"]),
        Index(value = ["parentLogId"]),
        Index(value = ["supplyId"])
    ]
)
@Serializable
data class JournalLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assetId: Long? = null,
    val title: String,
    val note: String,
    val timestamp: Long,
    val photoPath: String?,
    val audioFilePath: String?,
    val ecValue: Double?,
    val phValue: Double?,
    val tags: String = "", // Comma-separated tags
    val parameters: String = "", // key:value|key2:value2
    val imageUris: String = "", // Comma-separated internal URIs/Paths
    val activityType: String = "Observation",
    val parentLogId: Long? = null,
    val supplyId: Long? = null,
    val customInputName: String? = null,
    val batchGroupId: String? = null,
    val targetZones: String? = null, // Comma-separated zones targeted by this activity
    val displayId: String = "" // Added in v21 for human-readable IDs like OB1, FE1.1
)
