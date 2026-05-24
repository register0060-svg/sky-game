package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.models.ScoreEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Query("SELECT * FROM scores ORDER BY score DESC LIMIT 100")
    fun getTopScores(): Flow<List<ScoreEntry>>
    
    @Query("SELECT * FROM scores WHERE diffId = :diffId ORDER BY score DESC LIMIT 100")
    fun getTopScoresByDiff(diffId: String): Flow<List<ScoreEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ScoreEntry)
    
    @Query("DELETE FROM scores")
    suspend fun deleteAll()
    
    @Query("DELETE FROM scores WHERE name = :name AND modeId = :modeId AND diffId = :diffId AND score < :newScore")
    suspend fun deleteLowerScores(name: String, modeId: Int, diffId: String, newScore: Int)
}

@Database(entities = [ScoreEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scoreDao(): ScoreDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "skygame_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
