package com.mail2dev.planfora.data.local.dao

import androidx.room.*
import com.mail2dev.planfora.data.local.entity.CustomFieldDefinitionEntity
import com.mail2dev.planfora.data.local.entity.CustomFieldValueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomFieldDao {
    @Query("SELECT * FROM custom_field_definitions WHERE category = :category")
    fun getDefinitionsByCategory(category: String): Flow<List<CustomFieldDefinitionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDefinition(definition: CustomFieldDefinitionEntity): Long

    @Query("SELECT * FROM custom_field_values WHERE assetId = :assetId")
    fun getValuesByAsset(assetId: Long): Flow<List<CustomFieldValueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertValue(value: CustomFieldValueEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertValues(values: List<CustomFieldValueEntity>)
}
