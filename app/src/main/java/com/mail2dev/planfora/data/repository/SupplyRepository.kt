package com.mail2dev.planfora.data.repository

import com.mail2dev.planfora.data.local.dao.DiySupplyDao
import com.mail2dev.planfora.data.local.dao.MeasurementToolDao
import com.mail2dev.planfora.data.local.entity.DiySupplyEntity
import com.mail2dev.planfora.data.local.entity.MeasurementToolEntity
import kotlinx.coroutines.flow.Flow

class SupplyRepository(
    private val diySupplyDao: DiySupplyDao,
    private val measurementToolDao: MeasurementToolDao
) {
    
    fun getAllSupplies(): Flow<List<DiySupplyEntity>> = diySupplyDao.getAllSupplies()
    
    suspend fun insertSupply(supply: DiySupplyEntity): Long = diySupplyDao.insertSupply(supply)
    
    suspend fun updateSupply(supply: DiySupplyEntity) = diySupplyDao.updateSupply(supply)
    
    suspend fun deductStock(supplyId: Long, amount: Double) {
        diySupplyDao.getSupplyById(supplyId)?.let { supply ->
            val currentStock = supply.stockQuantity ?: 0.0f
            val newStock = (currentStock - amount.toFloat()).coerceAtLeast(0f)
            diySupplyDao.updateSupply(supply.copy(stockQuantity = newStock))
        }
    }
    
    suspend fun deleteSupply(supply: DiySupplyEntity) = diySupplyDao.deleteSupply(supply)
    
    suspend fun generateNextBatchName(prefix: String): String {
        val maxBatchNumber = diySupplyDao.getMaxBatchNumber(prefix) ?: 0
        val nextBatchNumber = maxBatchNumber + 1
        return "$prefix #$nextBatchNumber"
    }

    fun getDistinctActiveIngredients(): Flow<List<String>> = diySupplyDao.getDistinctActiveIngredients()

    // Measurement Tools
    fun getAllTools(): Flow<List<MeasurementToolEntity>> = measurementToolDao.getAllTools()
    suspend fun insertTool(tool: MeasurementToolEntity) = measurementToolDao.insertTool(tool)
    suspend fun updateTool(tool: MeasurementToolEntity) = measurementToolDao.updateTool(tool)
    suspend fun deleteTool(tool: MeasurementToolEntity) = measurementToolDao.deleteTool(tool)
}
