package com.worthkeeping.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.worthkeeping.app.data.entities.ReviewDecision
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDecisionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(decision: ReviewDecision)

    @Query("SELECT * FROM review_decisions")
    fun getAllDecisionsFlow(): Flow<List<ReviewDecision>>

    @Query("SELECT * FROM review_decisions WHERE imageId = :imageId")
    suspend fun getDecision(imageId: Long): ReviewDecision?

    @Query("DELETE FROM review_decisions")
    suspend fun clearAll()

    @Query("DELETE FROM review_decisions WHERE imageId IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
