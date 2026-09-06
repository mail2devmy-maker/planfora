package com.mail2dev.planfora.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "diy_supplies")
@Serializable
data class DiySupplyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // PESTICIDE, FERTILIZER, SUBSTRATE, HARDWARE, etc.
    val batchNumber: Int = 0,
    val batchCode: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val targetMaturityDate: Long = 0,
    val currentVolume: Double = 0.0,
    val originalVolume: Double = 0.0,
    val unit: String = "",
    val notifyOnMaturity: Boolean = false,
    val isArchived: Boolean = false,
    val activeIngredient: String? = null,
    val activePercentage: String? = null,
    val formType: String = "LIQUID", // LIQUID, POWDER, GRANULAR, SOLID
    val formulationCode: String? = null, // SL, SC, EC, WP, WG, SP, GR
    val notes: String = "",
    val phiDays: Int? = null,
    val stockQuantity: Float? = null,
    val stockUnit: String? = null,
    val imageUris: String = "",
    val audioPath: String? = null,
    val locationNote: String = "",
    val tags: String = ""
)

enum class SupplyCategory(val displayName: String) {
    INSECTICIDE("Insecticides"),
    FUNGICIDE("Fungicides"),
    HERBICIDE("Herbicides"),
    RODENTICIDE("Rodenticides"),
    MITICIDE("Miticides"),
    NEMATICIDE("Nematicides"),
    MOLLUSCICIDE("Molluscicides"),
    BACTERICIDE("Bactericides"),
    FERTILIZER("Fertilizer"),
    SUBSTRATE("Substrate"),
    HARDWARE("Hardware"),
    OTHER("Other")
}

enum class SupplyFormType(val displayName: String) {
    LIQUID("Liquid"),
    POWDER("Powder"),
    GRANULAR("Granular"),
    SOLID("Solid")
}
