package com.mail2dev.planfora.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mail2dev.planfora.data.local.dao.CustomFieldDao
import com.mail2dev.planfora.data.local.dao.DiySupplyDao
import com.mail2dev.planfora.data.local.dao.JournalLogDao
import com.mail2dev.planfora.data.local.dao.MasterDao
import com.mail2dev.planfora.data.local.dao.PlantAssetDao
import com.mail2dev.planfora.data.local.entity.*
import androidx.room.TypeConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        DiySupplyEntity::class,
        PlantAssetEntity::class,
        JournalLogEntity::class,
        MasterLocationEntity::class,
        MasterTagEntity::class,
        MasterIngredientEntity::class,
        MasterParameterEntity::class,
        CustomFieldDefinitionEntity::class,
        CustomFieldValueEntity::class
    ],
    version = 27,
    exportSchema = true
)
@TypeConverters(RoomTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diySupplyDao(): DiySupplyDao
    abstract fun plantAssetDao(): PlantAssetDao
    abstract fun journalLogDao(): JournalLogDao
    abstract fun masterDao(): MasterDao
    abstract fun customFieldDao(): CustomFieldDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Drop targetBenefit column by recreating the table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `diy_supplies_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `category` TEXT NOT NULL, 
                        `batchNumber` INTEGER NOT NULL, 
                        `batchCode` TEXT NOT NULL, 
                        `startDate` INTEGER NOT NULL, 
                        `targetMaturityDate` INTEGER NOT NULL, 
                        `currentVolume` REAL NOT NULL, 
                        `originalVolume` REAL NOT NULL, 
                        `unit` TEXT NOT NULL, 
                        `notifyOnMaturity` INTEGER NOT NULL, 
                        `isArchived` INTEGER NOT NULL, 
                        `activeIngredient` TEXT, 
                        `activePercentage` TEXT, 
                        `formType` TEXT NOT NULL DEFAULT 'LIQUID', 
                        `formulationCode` TEXT, 
                        `notes` TEXT NOT NULL DEFAULT '', 
                        `phiDays` INTEGER, 
                        `reiHours` INTEGER, 
                        `stockQuantity` REAL, 
                        `stockUnit` TEXT, 
                        `imageUris` TEXT NOT NULL DEFAULT '', 
                        `audioPath` TEXT, 
                        `locationNote` TEXT NOT NULL DEFAULT '', 
                        `tags` TEXT NOT NULL DEFAULT '', 
                        `subCategory` TEXT, 
                        `containerId` TEXT NOT NULL DEFAULT '', 
                        `materialList` TEXT NOT NULL DEFAULT '', 
                        `displayId` TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO diy_supplies_new (id, name, category, batchNumber, batchCode, startDate, targetMaturityDate, currentVolume, originalVolume, unit, notifyOnMaturity, isArchived, activeIngredient, activePercentage, formType, formulationCode, notes, phiDays, reiHours, stockQuantity, stockUnit, imageUris, audioPath, locationNote, tags, subCategory, containerId, materialList, displayId)
                    SELECT id, name, category, batchNumber, batchCode, startDate, targetMaturityDate, currentVolume, originalVolume, unit, notifyOnMaturity, isArchived, activeIngredient, activePercentage, formType, formulationCode, notes, phiDays, reiHours, stockQuantity, stockUnit, imageUris, audioPath, locationNote, tags, subCategory, containerId, materialList, displayId FROM diy_supplies
                """.trimIndent())

                db.execSQL("DROP TABLE diy_supplies")
                db.execSQL("ALTER TABLE diy_supplies_new RENAME TO diy_supplies")
            }
        }

        val MIGRATION_25_26 = object : Migration(25, 26) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Recreate journal_logs to make assetId nullable
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `journal_logs_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `assetId` INTEGER, 
                        `title` TEXT NOT NULL, 
                        `note` TEXT NOT NULL, 
                        `timestamp` INTEGER NOT NULL, 
                        `photoPath` TEXT, 
                        `audioFilePath` TEXT, 
                        `ecValue` REAL, 
                        `phValue` REAL, 
                        `tags` TEXT NOT NULL DEFAULT '', 
                        `parameters` TEXT NOT NULL DEFAULT '', 
                        `imageUris` TEXT NOT NULL DEFAULT '', 
                        `activityType` TEXT NOT NULL DEFAULT 'Observation', 
                        `parentLogId` INTEGER, 
                        `supplyId` INTEGER, 
                        `customInputName` TEXT, 
                        `batchGroupId` TEXT, 
                        `targetZones` TEXT, 
                        `displayId` TEXT NOT NULL DEFAULT '', 
                        FOREIGN KEY(`assetId`) REFERENCES `plant_assets`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL, 
                        FOREIGN KEY(`parentLogId`) REFERENCES `journal_logs`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL, 
                        FOREIGN KEY(`supplyId`) REFERENCES `diy_supplies`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL 
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO journal_logs_new (id, assetId, title, note, timestamp, photoPath, audioFilePath, ecValue, phValue, tags, parameters, imageUris, activityType, parentLogId, supplyId, customInputName, batchGroupId, targetZones, displayId)
                    SELECT id, assetId, title, note, timestamp, photoPath, audioFilePath, ecValue, phValue, tags, parameters, imageUris, activityType, parentLogId, supplyId, customInputName, batchGroupId, targetZones, displayId FROM journal_logs
                """.trimIndent())

                db.execSQL("DROP TABLE journal_logs")
                db.execSQL("ALTER TABLE journal_logs_new RENAME TO journal_logs")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_journal_logs_assetId` ON `journal_logs` (`assetId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_journal_logs_parentLogId` ON `journal_logs` (`parentLogId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_journal_logs_supplyId` ON `journal_logs` (`supplyId`)")
            }
        }

        val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE diy_supplies ADD COLUMN displayId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE diy_supplies ADD COLUMN targetBenefit TEXT")
            }
        }

        val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE diy_supplies ADD COLUMN containerId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE diy_supplies ADD COLUMN materialList TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE plant_assets ADD COLUMN totalPlants INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE plant_assets ADD COLUMN subLocation TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE journal_logs ADD COLUMN displayId TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Recreate plant_assets to allow NULL for plantedDate and acquisitionDate
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `plant_assets_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `category` TEXT NOT NULL, 
                        `plantedDate` INTEGER, 
                        `totalLogsCount` INTEGER NOT NULL, 
                        `lastActionDate` INTEGER NOT NULL, 
                        `tags` TEXT NOT NULL DEFAULT '', 
                        `locationNote` TEXT NOT NULL DEFAULT '', 
                        `acquisitionDate` INTEGER, 
                        `notes` TEXT NOT NULL DEFAULT '', 
                        `zones` TEXT NOT NULL DEFAULT '', 
                        `imageUris` TEXT NOT NULL DEFAULT '', 
                        `audioPath` TEXT
                    )
                """.trimIndent())
                
                db.execSQL("""
                    INSERT INTO plant_assets_new (id, name, category, plantedDate, totalLogsCount, lastActionDate, tags, locationNote, acquisitionDate, notes, zones, imageUris, audioPath)
                    SELECT id, name, category, plantedDate, totalLogsCount, lastActionDate, tags, locationNote, acquisitionDate, notes, zones, imageUris, audioPath FROM plant_assets
                """.trimIndent())
                
                db.execSQL("DROP TABLE plant_assets")
                db.execSQL("ALTER TABLE plant_assets_new RENAME TO plant_assets")
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE journal_logs ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE journal_logs ADD COLUMN parameters TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new table for plant_assets without 'status' and with new fields
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `plant_assets_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `category` TEXT NOT NULL, 
                        `plantedDate` INTEGER NOT NULL, 
                        `totalLogsCount` INTEGER NOT NULL, 
                        `lastActionDate` INTEGER NOT NULL, 
                        `tags` TEXT NOT NULL DEFAULT '', 
                        `locationNote` TEXT NOT NULL DEFAULT '', 
                        `acquisitionDate` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                
                // Copy data from old table to new table
                database.execSQL("""
                    INSERT INTO plant_assets_new (id, name, category, plantedDate, totalLogsCount, lastActionDate)
                    SELECT id, name, category, plantedDate, totalLogsCount, lastActionDate FROM plant_assets
                """.trimIndent())
                
                // Drop old table
                database.execSQL("DROP TABLE plant_assets")
                
                // Rename new table to original name
                database.execSQL("ALTER TABLE plant_assets_new RENAME TO plant_assets")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE journal_logs ADD COLUMN imageUris TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Recreate journal_logs to rename audioPath to audioFilePath and add parentLogId FK
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `journal_logs_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `assetId` INTEGER NOT NULL, 
                        `title` TEXT NOT NULL, 
                        `note` TEXT NOT NULL, 
                        `timestamp` INTEGER NOT NULL, 
                        `photoPath` TEXT, 
                        `audioFilePath` TEXT, 
                        `ecValue` REAL, 
                        `phValue` REAL, 
                        `tags` TEXT NOT NULL DEFAULT '', 
                        `parameters` TEXT NOT NULL DEFAULT '', 
                        `imageUris` TEXT NOT NULL DEFAULT '', 
                        `activityType` TEXT NOT NULL DEFAULT 'Observation', 
                        `parentLogId` INTEGER, 
                        FOREIGN KEY(`assetId`) REFERENCES `plant_assets`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , 
                        FOREIGN KEY(`parentLogId`) REFERENCES `journal_logs`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL 
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO journal_logs_new (id, assetId, title, note, timestamp, photoPath, audioFilePath, ecValue, phValue, tags, parameters, imageUris)
                    SELECT id, assetId, title, note, timestamp, photoPath, audioPath, ecValue, phValue, tags, parameters, imageUris FROM journal_logs
                """.trimIndent())

                db.execSQL("DROP TABLE journal_logs")
                db.execSQL("ALTER TABLE journal_logs_new RENAME TO journal_logs")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_journal_logs_assetId` ON `journal_logs` (`assetId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_journal_logs_parentLogId` ON `journal_logs` (`parentLogId`)")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Recreate journal_logs to add supplyId and customInputName with FK
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `journal_logs_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `assetId` INTEGER NOT NULL, 
                        `title` TEXT NOT NULL, 
                        `note` TEXT NOT NULL, 
                        `timestamp` INTEGER NOT NULL, 
                        `photoPath` TEXT, 
                        `audioFilePath` TEXT, 
                        `ecValue` REAL, 
                        `phValue` REAL, 
                        `tags` TEXT NOT NULL DEFAULT '', 
                        `parameters` TEXT NOT NULL DEFAULT '', 
                        `imageUris` TEXT NOT NULL DEFAULT '', 
                        `activityType` TEXT NOT NULL DEFAULT 'Observation', 
                        `parentLogId` INTEGER, 
                        `supplyId` INTEGER, 
                        `customInputName` TEXT, 
                        FOREIGN KEY(`assetId`) REFERENCES `plant_assets`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , 
                        FOREIGN KEY(`parentLogId`) REFERENCES `journal_logs`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , 
                        FOREIGN KEY(`supplyId`) REFERENCES `diy_supplies`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL 
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO journal_logs_new (id, assetId, title, note, timestamp, photoPath, audioFilePath, ecValue, phValue, tags, parameters, imageUris, activityType, parentLogId)
                    SELECT id, assetId, title, note, timestamp, photoPath, audioFilePath, ecValue, phValue, tags, parameters, imageUris, activityType, parentLogId FROM journal_logs
                """.trimIndent())

                db.execSQL("DROP TABLE journal_logs")
                db.execSQL("ALTER TABLE journal_logs_new RENAME TO journal_logs")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_journal_logs_assetId` ON `journal_logs` (`assetId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_journal_logs_parentLogId` ON `journal_logs` (`parentLogId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_journal_logs_supplyId` ON `journal_logs` (`supplyId`)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE diy_supplies ADD COLUMN activeIngredient TEXT")
                db.execSQL("ALTER TABLE diy_supplies ADD COLUMN activePercentage TEXT")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `master_locations` (`name` TEXT NOT NULL, PRIMARY KEY(`name`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `master_tags` (`name` TEXT NOT NULL, PRIMARY KEY(`name`))")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE plant_assets ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE diy_supplies ADD COLUMN formType TEXT NOT NULL DEFAULT 'LIQUID'")
                db.execSQL("ALTER TABLE diy_supplies ADD COLUMN formulationCode TEXT")
                db.execSQL("ALTER TABLE diy_supplies ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE diy_supplies ADD COLUMN phiDays INTEGER")
                db.execSQL("ALTER TABLE diy_supplies ADD COLUMN stockQuantity REAL")
                db.execSQL("ALTER TABLE diy_supplies ADD COLUMN stockUnit TEXT")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE journal_logs ADD COLUMN batchGroupId TEXT")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `custom_field_definitions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `category` TEXT NOT NULL, `fieldName` TEXT NOT NULL, `fieldType` TEXT NOT NULL, `radioOptionsJson` TEXT)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `custom_field_values` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `assetId` INTEGER NOT NULL, `fieldDefId` INTEGER NOT NULL, `value` TEXT NOT NULL)")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE plant_assets ADD COLUMN zones TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE journal_logs ADD COLUMN targetZones TEXT")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE plant_assets ADD COLUMN imageUris TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE plant_assets ADD COLUMN audioPath TEXT")
                db.execSQL("ALTER TABLE diy_supplies ADD COLUMN imageUris TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE diy_supplies ADD COLUMN audioPath TEXT")
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE diy_supplies ADD COLUMN locationNote TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE diy_supplies ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Update master_locations
                db.execSQL("CREATE TABLE master_locations_new (name TEXT NOT NULL, scope TEXT NOT NULL DEFAULT 'GLOBAL', PRIMARY KEY(name, scope))")
                db.execSQL("INSERT INTO master_locations_new (name) SELECT name FROM master_locations")
                db.execSQL("DROP TABLE master_locations")
                db.execSQL("ALTER TABLE master_locations_new RENAME TO master_locations")

                // Update master_tags
                db.execSQL("CREATE TABLE master_tags_new (name TEXT NOT NULL, scope TEXT NOT NULL DEFAULT 'GLOBAL', PRIMARY KEY(name, scope))")
                db.execSQL("INSERT INTO master_tags_new (name) SELECT name FROM master_tags")
                db.execSQL("DROP TABLE master_tags")
                db.execSQL("ALTER TABLE master_tags_new RENAME TO master_tags")
            }
        }

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `master_ingredients` (`name` TEXT NOT NULL, PRIMARY KEY(`name`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `master_parameters` (`name` TEXT NOT NULL, `isPreset` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`name`))")
                
                // Seed default parameters
                db.execSQL("INSERT OR IGNORE INTO master_parameters (name, isPreset) VALUES ('pH', 1)")
                db.execSQL("INSERT OR IGNORE INTO master_parameters (name, isPreset) VALUES ('EC', 1)")
                db.execSQL("INSERT OR IGNORE INTO master_parameters (name, isPreset) VALUES ('Temp', 1)")
                db.execSQL("INSERT OR IGNORE INTO master_parameters (name, isPreset) VALUES ('Moisture', 1)")
                db.execSQL("INSERT OR IGNORE INTO master_parameters (name, isPreset) VALUES ('Humidity', 1)")
                db.execSQL("INSERT OR IGNORE INTO master_parameters (name, isPreset) VALUES ('TDS', 1)")

                // Also populate ingredients from existing supplies
                db.execSQL("INSERT OR IGNORE INTO master_ingredients (name) SELECT DISTINCT activeIngredient FROM diy_supplies WHERE activeIngredient IS NOT NULL AND activeIngredient != ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "planfora_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18, MIGRATION_20_21, MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24, MIGRATION_24_25, MIGRATION_25_26, MIGRATION_26_27)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
