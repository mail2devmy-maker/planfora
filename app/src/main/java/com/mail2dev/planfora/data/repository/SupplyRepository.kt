package com.mail2dev.planfora.data.repository

import com.mail2dev.planfora.data.local.dao.DiySupplyDao
import com.mail2dev.planfora.data.local.entity.DiySupplyEntity
import kotlinx.coroutines.flow.Flow

class SupplyRepository(private val diySupplyDao: DiySupplyDao) {
    
    fun getAllSupplies(): Flow<List<DiySupplyEntity>> = diySupplyDao.getAllSupplies()
    
    suspend fun insertSupply(supply: DiySupplyEntity): Long = diySupplyDao.insertSupply(supply)
    
    suspend fun updateSupply(supply: DiySupplyEntity) = diySupplyDao.updateSupply(supply)
    
    suspend fun deleteSupply(supply: DiySupplyEntity) = diySupplyDao.deleteSupply(supply)
    
    suspend fun generateNextBatchName(prefix: String): String {
        val maxBatchNumber = diySupplyDao.getMaxBatchNumber(prefix) ?: 0
        val nextBatchNumber = maxBatchNumber + 1
        return "$prefix #$nextBatchNumber"
    }

    fun getDistinctActiveIngredients(): Flow<List<String>> = diySupplyDao.getDistinctActiveIngredients()
}
