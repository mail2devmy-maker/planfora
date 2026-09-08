package com.mail2dev.planfora.data.repository

import com.mail2dev.planfora.data.local.dao.DiySupplyDao
import com.mail2dev.planfora.data.local.entity.DiySupplyEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SupplyRepositoryTest {

    // Simple Fake DAO
    private val fakeDao = object : DiySupplyDao {
        override fun getAllSupplies(): Flow<List<DiySupplyEntity>> = TODO()
        override suspend fun getAllSuppliesOnce(): List<DiySupplyEntity> = TODO()
        override suspend fun getSupplyById(id: Long): DiySupplyEntity? = TODO()
        override suspend fun insertSupply(supply: DiySupplyEntity): Long = TODO()
        override suspend fun insertSupplies(supplies: List<DiySupplyEntity>) = TODO()
        override suspend fun updateSupply(supply: DiySupplyEntity): Int = TODO()
        override suspend fun deleteSupply(supply: DiySupplyEntity): Int = TODO()
        override suspend fun getMaxBatchNumber(supplyTypeName: String): Int? {
            return if (supplyTypeName == "FPJ") 12 else null
        }
        override fun getDistinctActiveIngredients(): Flow<List<String>> = TODO()
    }

    private val repository = SupplyRepository(fakeDao)

    @Test
    fun testGenerateNextBatchName() = runBlocking {
        val nextFpj = repository.generateNextBatchName("FPJ")
        assertEquals("FPJ #13", nextFpj)

        val nextFfj = repository.generateNextBatchName("FFJ")
        assertEquals("FFJ #1", nextFfj)
    }
}
