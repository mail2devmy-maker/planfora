package com.mail2dev.planfora.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "measurement_tools")
@Serializable
data class MeasurementToolEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val capacity: Double,
    val unit: String
)
