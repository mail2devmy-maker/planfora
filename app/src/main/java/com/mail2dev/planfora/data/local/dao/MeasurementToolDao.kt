package com.mail2dev.planfora.data.local.dao

import androidx.room.*
import com.mail2dev.planfora.data.local.entity.MeasurementToolEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementToolDao {
    @Query("SELECT * FROM measurement_tools ORDER BY name ASC")
    fun getAllTools(): Flow<List<MeasurementToolEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTool(tool: MeasurementToolEntity)

    @Update
    suspend fun updateTool(tool: MeasurementToolEntity)

    @Delete
    suspend fun deleteTool(tool: MeasurementToolEntity)

    @Query("SELECT * FROM measurement_tools WHERE id = :id")
    suspend fun getToolById(id: Long): MeasurementToolEntity?
}
