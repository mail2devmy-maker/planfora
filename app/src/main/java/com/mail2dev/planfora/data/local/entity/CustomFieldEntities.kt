package com.mail2dev.planfora.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

enum class FieldTargetType {
    LOG_ACTIVITY,
    ASSET_CATEGORY,
    SUPPLY_CATEGORY
}

enum class CustomFieldType {
    TEXT,
    NUMBER,
    RADIO,
    MULTI_SELECT
}

@Entity(tableName = "custom_field_definitions")
@Serializable
data class CustomFieldDefinitionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetType: FieldTargetType,
    val scope: String, // e.g., "Observation", "Feeding", "Tree", "Fertilizer"
    val fieldName: String,
    val fieldType: CustomFieldType,
    val optionsJson: String? = null, // Serialized list of options for select types
    val isArchived: Boolean = false
)

@Entity(tableName = "custom_field_values")
@Serializable
data class CustomFieldValueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityId: Long, // Generic ID for logId, assetId, or supplyId
    val fieldDefId: Long,
    val value: String
)
