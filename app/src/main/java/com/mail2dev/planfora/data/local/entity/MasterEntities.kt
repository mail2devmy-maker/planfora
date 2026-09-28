package com.mail2dev.planfora.data.local.entity

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Entity(tableName = "master_locations", primaryKeys = ["name", "scope"])
@Serializable
data class MasterLocationEntity(
    val name: String,
    val scope: String = "GLOBAL" // ASSET, SUPPLY, GLOBAL
)

@Entity(tableName = "master_tags", primaryKeys = ["name", "scope"])
@Serializable
data class MasterTagEntity(
    val name: String,
    val scope: String = "GLOBAL" // ASSET, SUPPLY, GLOBAL
)

@Entity(tableName = "master_ingredients", primaryKeys = ["name"])
@Serializable
data class MasterIngredientEntity(
    val name: String
)

@Entity(tableName = "master_parameters", primaryKeys = ["name"])
@Serializable
data class MasterParameterEntity(
    val name: String,
    val isPreset: Boolean = false
)
