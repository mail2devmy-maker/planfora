package com.mail2dev.planfora.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "custom_field_definitions")
@Serializable
data class CustomFieldDefinitionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // Tree, Crop/Veggie, Seedling, Cutting
    val fieldName: String,
    val fieldType: String, // TEXT, NUMBER, RADIO
    val radioOptionsJson: String? = null // Serialized list of options for RADIO type
)

@Entity(tableName = "custom_field_values")
@Serializable
data class CustomFieldValueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assetId: Long,
    val fieldDefId: Long,
    val value: String
)
