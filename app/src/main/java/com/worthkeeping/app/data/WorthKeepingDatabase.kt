package com.worthkeeping.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.worthkeeping.app.data.dao.ImageDao
import com.worthkeeping.app.data.dao.ImageAnalysisDao
import com.worthkeeping.app.data.dao.ReviewDecisionDao
import com.worthkeeping.app.data.dao.ScanSessionDao
import com.worthkeeping.app.data.entities.ImageAnalysis
import com.worthkeeping.app.data.entities.ImageAsset
import com.worthkeeping.app.data.entities.ReviewDecision
import com.worthkeeping.app.data.entities.ScanSession

@Database(
    entities = [
        ImageAsset::class,
        ScanSession::class,
        ImageAnalysis::class,
        ReviewDecision::class
    ],
    version = 2,
    exportSchema = false
)
abstract class WorthKeepingDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao
    abstract fun imageAnalysisDao(): ImageAnalysisDao
    abstract fun scanSessionDao(): ScanSessionDao
    abstract fun reviewDecisionDao(): ReviewDecisionDao

    companion object {
        @Volatile
        private var INSTANCE: WorthKeepingDatabase? = null

        fun getDatabase(context: Context): WorthKeepingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorthKeepingDatabase::class.java,
                    "worthkeeping_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
