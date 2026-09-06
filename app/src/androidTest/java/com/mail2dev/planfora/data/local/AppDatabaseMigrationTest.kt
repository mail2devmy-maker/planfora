package com.mail2dev.planfora.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        var db = helper.createDatabase(TEST_DB, 1)
        // Set up v1 data
        db.execSQL("INSERT INTO plant_assets (id, name, category, status, plantedDate, totalLogsCount, lastActionDate) VALUES (1, 'Ficus', 'Trees', 'Optimal', 1000, 0, 1000)")
        db.execSQL("INSERT INTO journal_logs (id, assetId, title, note, timestamp) VALUES (1, 1, 'Initial', 'Note', 1000)")
        db.close()

        // Re-open with v2 and apply migration
        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, AppDatabase.MIGRATION_1_2)

        // Verify journal_logs has new columns
        val cursor = db.query("SELECT * FROM journal_logs LIMIT 1")
        val columnNames = cursor.columnNames.toList()
        assertTrue(columnNames.contains("tags"))
        assertTrue(columnNames.contains("parameters"))
        
        cursor.moveToFirst()
        assertEquals("", cursor.getString(cursor.getColumnIndexOrThrow("tags")))
        assertEquals("", cursor.getString(cursor.getColumnIndexOrThrow("parameters")))
        cursor.close()
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate2To3() {
        var db = helper.createDatabase(TEST_DB, 2)
        // Set up v2 data (which includes journal_logs tags/parameters)
        db.execSQL("INSERT INTO plant_assets (id, name, category, status, plantedDate, totalLogsCount, lastActionDate) VALUES (1, 'Ficus', 'Trees', 'Optimal', 1000, 0, 1000)")
        db.close()

        // Re-open with v3 and apply migration
        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, AppDatabase.MIGRATION_2_3)

        // Verify plant_assets has new columns and dropped status
        val cursor = db.query("SELECT * FROM plant_assets LIMIT 1")
        val columnNames = cursor.columnNames.toList()
        assertTrue(columnNames.contains("tags"))
        assertTrue(columnNames.contains("locationNote"))
        assertTrue(columnNames.contains("acquisitionDate"))
        assertTrue(!columnNames.contains("status"))
        
        cursor.moveToFirst()
        assertEquals("Ficus", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        assertEquals("", cursor.getString(cursor.getColumnIndexOrThrow("tags")))
        cursor.close()
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate3To4() {
        var db = helper.createDatabase(TEST_DB, 3)
        // Set up v3 data
        db.execSQL("INSERT INTO plant_assets (id, name, category, plantedDate, totalLogsCount, lastActionDate, tags, locationNote, acquisitionDate) VALUES (1, 'Ficus', 'Trees', 1000, 0, 1000, '', '', 0)")
        db.execSQL("INSERT INTO journal_logs (id, assetId, title, note, timestamp, tags, parameters) VALUES (1, 1, 'V3 Log', 'Note', 1000, '', '')")
        db.close()

        // Re-open with v4 and apply migration
        db = helper.runMigrationsAndValidate(TEST_DB, 4, true, AppDatabase.MIGRATION_3_4)

        // Verify journal_logs has imageUris
        val cursor = db.query("SELECT * FROM journal_logs LIMIT 1")
        val columnNames = cursor.columnNames.toList()
        assertTrue(columnNames.contains("imageUris"))
        
        cursor.moveToFirst()
        assertEquals("", cursor.getString(cursor.getColumnIndexOrThrow("imageUris")))
        cursor.close()
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate4To5() {
        var db = helper.createDatabase(TEST_DB, 4)
        // Set up v4 data
        db.execSQL("INSERT INTO plant_assets (id, name, category, plantedDate, totalLogsCount, lastActionDate, tags, locationNote, acquisitionDate) VALUES (1, 'Ficus', 'Trees', 1000, 0, 1000, '', '', 0)")
        db.execSQL("INSERT INTO journal_logs (id, assetId, title, note, timestamp, tags, parameters, imageUris, photoPath, audioPath) VALUES (1, 1, 'V4 Log', 'Note', 1000, '', '', '', NULL, 'old/path')")
        db.close()

        // Re-open with v5 and apply migration
        db = helper.runMigrationsAndValidate(TEST_DB, 5, true, AppDatabase.MIGRATION_4_5)

        // Verify journal_logs has new columns and renamed column
        val cursor = db.query("SELECT * FROM journal_logs LIMIT 1")
        val columnNames = cursor.columnNames.toList()
        assertTrue(columnNames.contains("audioFilePath"))
        assertTrue(columnNames.contains("activityType"))
        assertTrue(columnNames.contains("parentLogId"))
        assertTrue(!columnNames.contains("audioPath"))
        
        cursor.moveToFirst()
        assertEquals("old/path", cursor.getString(cursor.getColumnIndexOrThrow("audioFilePath")))
        assertEquals("Observation", cursor.getString(cursor.getColumnIndexOrThrow("activityType")))
        cursor.close()
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate5To6() {
        var db = helper.createDatabase(TEST_DB, 5)
        // Set up v5 data
        db.execSQL("INSERT INTO plant_assets (id, name, category, plantedDate, totalLogsCount, lastActionDate, tags, locationNote, acquisitionDate) VALUES (1, 'Ficus', 'Trees', 1000, 0, 1000, '', '', 0)")
        db.execSQL("INSERT INTO journal_logs (id, assetId, title, note, timestamp, tags, parameters, imageUris, activityType) VALUES (1, 1, 'V5 Log', 'Note', 1000, '', '', '', 'Observation')")
        db.close()

        // Re-open with v6 and apply migration
        db = helper.runMigrationsAndValidate(TEST_DB, 6, true, AppDatabase.MIGRATION_5_6)

        // Verify journal_logs has supplyId and customInputName
        val cursor = db.query("SELECT * FROM journal_logs LIMIT 1")
        val columnNames = cursor.columnNames.toList()
        assertTrue(columnNames.contains("supplyId"))
        assertTrue(columnNames.contains("customInputName"))
        
        cursor.moveToFirst()
        assertEquals("V5 Log", cursor.getString(cursor.getColumnIndexOrThrow("title")))
        cursor.close()
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate6To7() {
        var db = helper.createDatabase(TEST_DB, 6)
        // Set up v6 data
        db.execSQL("INSERT INTO diy_supplies (id, name, category, batchNumber, batchCode, startDate, targetMaturityDate, currentVolume, originalVolume, unit, notifyOnMaturity, isArchived) VALUES (1, 'Neem', 'Pest Control', 1, 'N1', 1000, 1000, 1.0, 1.0, 'L', 0, 0)")
        db.close()

        // Re-open with v7 and apply migration
        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, AppDatabase.MIGRATION_6_7)

        // Verify diy_supplies has A.I. columns
        val cursor = db.query("SELECT * FROM diy_supplies LIMIT 1")
        val columnNames = cursor.columnNames.toList()
        assertTrue(columnNames.contains("activeIngredient"))
        assertTrue(columnNames.contains("activePercentage"))
        
        cursor.moveToFirst()
        assertEquals("Neem", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        cursor.close()
        db.close()
    }
}
