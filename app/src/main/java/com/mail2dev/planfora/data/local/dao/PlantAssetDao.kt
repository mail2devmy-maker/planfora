package com.mail2dev.planfora.data.local.dao

import androidx.room.*
import com.mail2dev.planfora.data.local.entity.PlantAssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantAssetDao {
    @Query("SELECT * FROM plant_assets ORDER BY lastActionDate DESC")
    fun getAllAssets(): Flow<List<PlantAssetEntity>>

    @Query("SELECT * FROM plant_assets")
    suspend fun getAllAssetsOnce(): List<PlantAssetEntity>

    @Query("SELECT * FROM plant_assets WHERE id = :id")
    suspend fun getAssetById(id: Long): PlantAssetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: PlantAssetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<PlantAssetEntity>)

    @Update
    suspend fun updateAsset(asset: PlantAssetEntity): Int

    @Delete
    suspend fun deleteAsset(asset: PlantAssetEntity): Int
}
