package com.mail2dev.planfora.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mail2dev.planfora.data.local.dao.PlantAssetDao
import com.mail2dev.planfora.data.local.entity.PlantAssetEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlantAssetDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: PlantAssetDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.plantAssetDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertAndFilterByCategory() = runBlocking {
        val tree = PlantAssetEntity(name = "Mango", category = "Trees", plantedDate = 100L, lastActionDate = 100L)
        val plant = PlantAssetEntity(name = "Tomato", category = "Plants", plantedDate = 100L, lastActionDate = 100L)
        
        dao.insertAsset(tree)
        dao.insertAsset(plant)
        
        val all = dao.getAllAssets().first()
        assertEquals(2, all.size)
        
        val trees = all.filter { it.category == "Trees" }
        assertEquals(1, trees.size)
        assertEquals("Mango", trees[0].name)
    }
}
