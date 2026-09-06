package com.mail2dev.planfora.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mail2dev.planfora.data.local.dao.JournalLogDao
import com.mail2dev.planfora.data.local.dao.PlantAssetDao
import com.mail2dev.planfora.data.local.entity.JournalLogEntity
import com.mail2dev.planfora.data.local.entity.PlantAssetEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JournalLogDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var logDao: JournalLogDao
    private lateinit var assetDao: PlantAssetDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        logDao = db.journalLogDao()
        assetDao = db.plantAssetDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertLogAndRetrieveByAsset() = runBlocking {
        val assetId = assetDao.insertAsset(
            PlantAssetEntity(name = "Mango", category = "Trees", plantedDate = 100L, lastActionDate = 100L)
        )
        
        val log = JournalLogEntity(
            assetId = assetId,
            title = "First Log",
            note = "Looking good",
            timestamp = 200L,
            photoPath = null,
            audioFilePath = null,
            ecValue = null,
            phValue = null,
            tags = "",
            parameters = "",
            imageUris = "",
            activityType = "Observation",
            parentLogId = null,
            supplyId = null,
            customInputName = null
        )
        logDao.insertLog(log)
        
        val logs = logDao.getLogsForAsset(assetId).first()
        assertEquals(1, logs.size)
        assertEquals("First Log", logs[0].title)
    }
}
