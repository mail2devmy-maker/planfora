package com.mail2dev.planfora.data.local.dao

import androidx.room.*
import com.mail2dev.planfora.data.local.entity.MasterIngredientEntity
import com.mail2dev.planfora.data.local.entity.MasterLocationEntity
import com.mail2dev.planfora.data.local.entity.MasterParameterEntity
import com.mail2dev.planfora.data.local.entity.MasterTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MasterDao {
    @Query("SELECT * FROM master_locations ORDER BY name ASC")
    fun getAllLocations(): Flow<List<MasterLocationEntity>>

    @Query("SELECT * FROM master_locations WHERE scope = :scope OR scope = 'GLOBAL' ORDER BY name ASC")
    fun getLocationsByScope(scope: String): Flow<List<MasterLocationEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocation(location: MasterLocationEntity)

    @Delete
    suspend fun deleteLocation(location: MasterLocationEntity)

    @Query("UPDATE master_locations SET name = :newName WHERE name = :oldName AND scope = :scope")
    suspend fun updateLocationNameScoped(oldName: String, newName: String, scope: String)

    @Query("DELETE FROM master_locations WHERE name = :name AND scope = :scope")
    suspend fun deleteLocationByNameScoped(name: String, scope: String)

    @Transaction
    suspend fun renameLocationCascading(oldName: String, newName: String, scope: String) {
        updateLocationNameScoped(oldName, newName, scope)
        if (scope == "ASSET" || scope == "GLOBAL") {
            updateAssetsLocation(oldName, newName)
        }
        if (scope == "SUPPLY" || scope == "GLOBAL") {
            updateSuppliesLocation(oldName, newName)
        }
    }

    @Query("UPDATE plant_assets SET locationNote = :newName WHERE locationNote = :oldName")
    suspend fun updateAssetsLocation(oldName: String, newName: String)

    @Query("UPDATE diy_supplies SET locationNote = :newName WHERE locationNote = :oldName")
    suspend fun updateSuppliesLocation(oldName: String, newName: String)

    @Transaction
    suspend fun deleteLocationCascading(name: String, scope: String) {
        deleteLocationByNameScoped(name, scope)
        if (scope == "ASSET" || scope == "GLOBAL") {
            clearAssetsLocation(name)
        }
        if (scope == "SUPPLY" || scope == "GLOBAL") {
            clearSuppliesLocation(name)
        }
    }

    @Query("UPDATE plant_assets SET locationNote = '' WHERE locationNote = :name")
    suspend fun clearAssetsLocation(name: String)

    @Query("UPDATE diy_supplies SET locationNote = '' WHERE locationNote = :name")
    suspend fun clearSuppliesLocation(name: String)

    @Query("SELECT * FROM master_tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<MasterTagEntity>>

    @Query("SELECT * FROM master_tags WHERE scope = :scope OR scope = 'GLOBAL' ORDER BY name ASC")
    fun getTagsByScope(scope: String): Flow<List<MasterTagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: MasterTagEntity)

    @Delete
    suspend fun deleteTag(tag: MasterTagEntity)

    @Query("UPDATE master_tags SET name = :newTag WHERE name = :oldTag AND scope = :scope")
    suspend fun updateTagNameScoped(oldTag: String, newTag: String, scope: String)

    @Query("DELETE FROM master_tags WHERE name = :name AND scope = :scope")
    suspend fun deleteTagByNameScoped(name: String, scope: String)

    @Transaction
    suspend fun renameTagCascading(oldTag: String, newTag: String, scope: String) {
        updateTagNameScoped(oldTag, newTag, scope)
        if (scope == "ASSET" || scope == "GLOBAL") {
            updateAssetsTags(oldTag, newTag)
            updateLogsTags(oldTag, newTag)
        }
        if (scope == "SUPPLY" || scope == "GLOBAL") {
            updateSuppliesTags(oldTag, newTag)
        }
    }

    @Query("UPDATE plant_assets SET tags = TRIM(REPLACE(',' || tags || ',', ',' || :oldTag || ',', ',' || :newTag || ','), ',') WHERE (',' || tags || ',') LIKE ('%,' || :oldTag || ',%')")
    suspend fun updateAssetsTags(oldTag: String, newTag: String)

    @Query("UPDATE journal_logs SET tags = TRIM(REPLACE(',' || tags || ',', ',' || :oldTag || ',', ',' || :newTag || ','), ',') WHERE (',' || tags || ',') LIKE ('%,' || :oldTag || ',%')")
    suspend fun updateLogsTags(oldTag: String, newTag: String)

    @Query("UPDATE diy_supplies SET tags = TRIM(REPLACE(',' || tags || ',', ',' || :oldTag || ',', ',' || :newTag || ','), ',') WHERE (',' || tags || ',') LIKE ('%,' || :oldTag || ',%')")
    suspend fun updateSuppliesTags(oldTag: String, newTag: String)

    @Transaction
    suspend fun deleteTagCascading(name: String, scope: String) {
        deleteTagByNameScoped(name, scope)
        if (scope == "ASSET" || scope == "GLOBAL") {
            removeAssetsTag(name)
            removeLogsTag(name)
        }
        if (scope == "SUPPLY" || scope == "GLOBAL") {
            removeSuppliesTag(name)
        }
    }

    @Query("UPDATE plant_assets SET tags = TRIM(REPLACE(',' || tags || ',', ',' || :name || ',', ','), ',') WHERE (',' || tags || ',') LIKE ('%,' || :name || ',%')")
    suspend fun removeAssetsTag(name: String)

    @Query("UPDATE journal_logs SET tags = TRIM(REPLACE(',' || tags || ',', ',' || :name || ',', ','), ',') WHERE (',' || tags || ',') LIKE ('%,' || :name || ',%')")
    suspend fun removeLogsTag(name: String)

    @Query("UPDATE diy_supplies SET tags = TRIM(REPLACE(',' || tags || ',', ',' || :name || ',', ','), ',') WHERE (',' || tags || ',') LIKE ('%,' || :name || ',%')")
    suspend fun removeSuppliesTag(name: String)

    // Ingredients
    @Query("SELECT * FROM master_ingredients ORDER BY name ASC")
    fun getAllIngredients(): Flow<List<MasterIngredientEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIngredient(ingredient: MasterIngredientEntity)

    @Query("UPDATE master_ingredients SET name = :newName WHERE name = :oldName")
    suspend fun updateIngredientName(oldName: String, newName: String)

    @Query("DELETE FROM master_ingredients WHERE name = :name")
    suspend fun deleteIngredientByName(name: String)

    @Transaction
    suspend fun renameIngredientCascading(oldName: String, newName: String) {
        updateIngredientName(oldName, newName)
        updateSuppliesActiveIngredient(oldName, newName)
    }

    @Query("UPDATE diy_supplies SET activeIngredient = :newName WHERE activeIngredient = :oldName")
    suspend fun updateSuppliesActiveIngredient(oldName: String, newName: String)

    @Transaction
    suspend fun deleteIngredientCascading(name: String) {
        deleteIngredientByName(name)
        clearSuppliesActiveIngredient(name)
    }

    @Query("UPDATE diy_supplies SET activeIngredient = NULL WHERE activeIngredient = :name")
    suspend fun clearSuppliesActiveIngredient(name: String)

    // Parameters
    @Query("SELECT * FROM master_parameters ORDER BY name ASC")
    fun getAllParameters(): Flow<List<MasterParameterEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertParameter(parameter: MasterParameterEntity)

    @Query("UPDATE master_parameters SET name = :newName WHERE name = :oldName")
    suspend fun updateParameterName(oldName: String, newName: String)

    @Query("DELETE FROM master_parameters WHERE name = :name")
    suspend fun deleteParameterByName(name: String)

    @Transaction
    suspend fun renameParameterCascading(oldName: String, newName: String) {
        updateParameterName(oldName, newName)
        updateLogsParameters(oldName, newName)
    }

    @Query("UPDATE journal_logs SET parameters = REPLACE(parameters, :oldName || ':', :newName || ':')")
    suspend fun updateLogsParameters(oldName: String, newName: String)

    @Transaction
    suspend fun deleteParameterCascading(name: String) {
        deleteParameterByName(name)
        removeLogsParameter(name)
    }

    @Query("UPDATE journal_logs SET parameters = TRIM(REPLACE('|' || parameters || '|', '|' || :name || ':', '|'), '|') WHERE parameters LIKE '%' || :name || ':%'")
    suspend fun removeLogsParameter(name: String)
}
