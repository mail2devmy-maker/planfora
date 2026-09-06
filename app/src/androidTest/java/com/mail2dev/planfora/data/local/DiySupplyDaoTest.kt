package com.mail2dev.planfora.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mail2dev.planfora.data.local.dao.DiySupplyDao
import com.mail2dev.planfora.data.local.entity.DiySupplyEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiySupplyDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: DiySupplyDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.diySupplyDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertAndGetAll() = runBlocking {
        val supply = DiySupplyEntity(
            name = "Pest Control",
            category = "DIY",
            batchNumber = 1,
            batchCode = "PC #1",
            startDate = 1000L,
            targetMaturityDate = 2000L,
            currentVolume = 500.0,
            originalVolume = 500.0,
            unit = "ml",
            notifyOnMaturity = true,
            activeIngredient = "Neem Oil",
            activePercentage = "100%"
        )
        dao.insertSupply(supply)
        val all = dao.getAllSupplies().first()
        assertEquals(1, all.size)
        assertEquals("Neem Oil", all[0].activeIngredient)
    }

    @Test
    fun testGetMaxBatchNumber() = runBlocking {
        val s1 = DiySupplyEntity(
            name = "FPJ", category = "DIY", batchNumber = 1, batchCode = "FPJ #1",
            startDate = 1000L, targetMaturityDate = 2000L, currentVolume = 500.0,
            originalVolume = 500.0, unit = "ml", notifyOnMaturity = true
        )
        val s2 = DiySupplyEntity(
            name = "FPJ", category = "DIY", batchNumber = 12, batchCode = "FPJ #12",
            startDate = 1000L, targetMaturityDate = 2000L, currentVolume = 500.0,
            originalVolume = 500.0, unit = "ml", notifyOnMaturity = true
        )
        dao.insertSupply(s1)
        dao.insertSupply(s2)
        
        val max = dao.getMaxBatchNumber("FPJ")
        assertEquals(12, max)
    }
}
