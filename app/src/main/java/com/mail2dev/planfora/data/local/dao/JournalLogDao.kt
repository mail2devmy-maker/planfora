package com.mail2dev.planfora.data.local.dao

import androidx.room.*
import com.mail2dev.planfora.data.local.entity.JournalLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalLogDao {
    @Query("SELECT * FROM journal_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<JournalLogEntity>>

    @Query("SELECT * FROM journal_logs")
    suspend fun getAllLogsOnce(): List<JournalLogEntity>

    @Query("SELECT * FROM journal_logs WHERE assetId = :assetId ORDER BY timestamp DESC")
    fun getLogsForAsset(assetId: Long): Flow<List<JournalLogEntity>>

    @Query("SELECT * FROM journal_logs WHERE id = :id")
    suspend fun getLogById(id: Long): JournalLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: JournalLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<JournalLogEntity>)

    @Update
    suspend fun updateLog(log: JournalLogEntity): Int

    @Query("SELECT * FROM journal_logs WHERE supplyId = :supplyId ORDER BY timestamp DESC")
    suspend fun getLogsBySupply(supplyId: Long): List<JournalLogEntity>

    @Query("DELETE FROM journal_logs WHERE batchGroupId = :batchGroupId")
    suspend fun deleteBatchLogs(batchGroupId: String)

    @Query("SELECT * FROM journal_logs WHERE batchGroupId = :batchGroupId")
    suspend fun getLogsByBatchGroup(batchGroupId: String): List<JournalLogEntity>

    @Delete
    suspend fun deleteLog(log: JournalLogEntity): Int
}
