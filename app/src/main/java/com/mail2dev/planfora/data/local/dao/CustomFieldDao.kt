package com.mail2dev.planfora.data.local.dao

import androidx.room.*
import com.mail2dev.planfora.data.local.entity.CustomFieldDefinitionEntity
import com.mail2dev.planfora.data.local.entity.CustomFieldValueEntity
import com.mail2dev.planfora.data.local.entity.FieldTargetType
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomFieldDao {
    @Query("SELECT * FROM custom_field_definitions WHERE targetType = :targetType AND scope = :scope AND isArchived = 0")
    fun getDefinitions(targetType: FieldTargetType, scope: String): Flow<List<CustomFieldDefinitionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDefinition(definition: CustomFieldDefinitionEntity): Long

    @Update
    suspend fun updateDefinition(definition: CustomFieldDefinitionEntity)

    @Query("UPDATE custom_field_definitions SET isArchived = 1 WHERE id = :definitionId")
    suspend fun archiveDefinition(definitionId: Long)

    @Query("SELECT * FROM custom_field_values WHERE entityId = :entityId")
    fun getValuesForEntity(entityId: Long): Flow<List<CustomFieldValueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertValue(value: CustomFieldValueEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertValues(values: List<CustomFieldValueEntity>)

    @Query("DELETE FROM custom_field_values WHERE entityId = :entityId")
    suspend fun deleteValuesForEntity(entityId: Long)
}
