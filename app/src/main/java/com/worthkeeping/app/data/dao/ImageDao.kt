package com.worthkeeping.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.worthkeeping.app.data.entities.ImageAsset
import com.worthkeeping.app.scanner.MediaSourceSummary
import com.worthkeeping.app.scanner.MediaSourceType

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(images: List<ImageAsset>)

    @Query("SELECT COUNT(*) FROM image_assets")
    suspend fun getTotalImages(): Int

    @Query("SELECT SUM(sizeBytes) FROM image_assets")
    suspend fun getTotalSizeBytes(): Long

    @Query("SELECT sourceType as type, COUNT(*) as imageCount, SUM(sizeBytes) as sizeBytes FROM image_assets GROUP BY sourceType")
    suspend fun getSourceSummaries(): List<MediaSourceSummary>

    @Query("SELECT * FROM image_assets ORDER BY dateTaken DESC")
    fun getAllImagesFlow(): kotlinx.coroutines.flow.Flow<List<ImageAsset>>

    @Query("SELECT * FROM image_assets ORDER BY dateTaken DESC")
    suspend fun getAllImages(): List<ImageAsset>

    @Query("DELETE FROM image_assets")
    suspend fun clearAll()

    @Query("DELETE FROM image_assets WHERE mediaStoreId IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
