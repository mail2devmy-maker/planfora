package com.mail2dev.planfora.data.local

import com.mail2dev.planfora.data.local.entity.DiySupplyEntity
import com.mail2dev.planfora.data.local.entity.displayName
import org.junit.Assert.assertEquals
import org.junit.Test

class DiySupplyEntityTest {

    @Test
    fun testDisplayNameForStoreProductWithNoBatchCode() {
        val supply = DiySupplyEntity(
            name = "Necide 41",
            category = "Herbicides",
            batchCode = "",
            activeIngredient = "glyphosate-isopropylammonium 41%"
        )
        assertEquals("Necide 41", supply.displayName)
    }

    @Test
    fun testDisplayNameForDiySupplyWithBatchCodeAndName() {
        val supply = DiySupplyEntity(
            name = "Fermented Plant Juice",
            category = "DIY",
            batchCode = "FPJ #1"
        )
        assertEquals("Fermented Plant Juice (FPJ #1)", supply.displayName)
    }

    @Test
    fun testDisplayNameWhenBatchCodeEqualsName() {
        val supply = DiySupplyEntity(
            name = "Organic Compost",
            category = "Fertilizer",
            batchCode = "Organic Compost"
        )
        assertEquals("Organic Compost", supply.displayName)
    }

    @Test
    fun testDisplayNameWhenNameIsEmptyButBatchCodeExists() {
        val supply = DiySupplyEntity(
            name = "",
            category = "DIY",
            batchCode = "JMS #3"
        )
        assertEquals("JMS #3", supply.displayName)
    }

    @Test
    fun testDisplayNameWhenBothEmpty() {
        val supply = DiySupplyEntity(
            name = "",
            category = "DIY",
            batchCode = ""
        )
        assertEquals("Unnamed Supply", supply.displayName)
    }

    @Test
    fun testStockDeductionConversionFromMlToL() {
        val qtyVal = 250.0
        val usedQtyUnit = "mL"
        val stockUnit = "L"
        val deduction = when {
            usedQtyUnit.equals("mL", ignoreCase = true) && stockUnit.equals("L", ignoreCase = true) -> qtyVal / 1000.0
            else -> qtyVal
        }
        assertEquals(0.25, deduction, 0.001)
    }

    @Test
    fun testStockDeductionConversionFromGToKg() {
        val qtyVal = 500.0
        val usedQtyUnit = "g"
        val stockUnit = "kg"
        val deduction = when {
            usedQtyUnit.equals("g", ignoreCase = true) && stockUnit.equals("kg", ignoreCase = true) -> qtyVal / 1000.0
            else -> qtyVal
        }
        assertEquals(0.5, deduction, 0.001)
    }
}
