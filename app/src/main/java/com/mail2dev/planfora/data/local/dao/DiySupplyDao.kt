package com.mail2dev.planfora.data.local.dao

import androidx.room.*
import com.mail2dev.planfora.data.local.entity.DiySupplyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiySupplyDao {
    @Query("SELECT * FROM diy_supplies ORDER BY startDate DESC")
    fun getAllSupplies(): Flow<List<DiySupplyEntity>>

    @Query("SELECT * FROM diy_supplies")
    suspend fun getAllSuppliesOnce(): List<DiySupplyEntity>

    @Query("SELECT * FROM diy_supplies WHERE id = :id")
    suspend fun getSupplyById(id: Long): DiySupplyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupply(supply: DiySupplyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplies(supplies: List<DiySupplyEntity>)

    @Update
    suspend fun updateSupply(supply: DiySupplyEntity): Int

    @Delete
    suspend fun deleteSupply(supply: DiySupplyEntity): Int

    @Query("SELECT MAX(batchNumber) FROM diy_supplies WHERE name = :supplyTypeName")
    suspend fun getMaxBatchNumber(supplyTypeName: String): Int?

    @Query("SELECT DISTINCT activeIngredient FROM diy_supplies WHERE activeIngredient IS NOT NULL AND activeIngredient != ''")
    fun getDistinctActiveIngredients(): Flow<List<String>>
}
