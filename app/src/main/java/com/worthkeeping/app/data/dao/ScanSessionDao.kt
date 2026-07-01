package com.worthkeeping.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.worthkeeping.app.data.entities.ScanSession

@Dao
interface ScanSessionDao {
    @Insert
    suspend fun insert(session: ScanSession): Long

    @Update
    suspend fun update(session: ScanSession)

    @Query("SELECT * FROM scan_sessions ORDER BY startedAt DESC LIMIT 1")
    suspend fun getLatestSession(): ScanSession?
}
