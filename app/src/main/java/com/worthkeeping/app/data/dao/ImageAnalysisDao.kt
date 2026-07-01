package com.worthkeeping.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.worthkeeping.app.data.entities.ImageAnalysis
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageAnalysisDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(analyses: List<ImageAnalysis>)

    @Query("SELECT * FROM image_analyses")
    suspend fun getAllAnalyses(): List<ImageAnalysis>

    @Query("SELECT * FROM image_analyses")
    fun getAllAnalysesFlow(): Flow<List<ImageAnalysis>>

    @Query("DELETE FROM image_analyses")
    suspend fun clearAll()

    @Query("DELETE FROM image_analyses WHERE imageId IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
