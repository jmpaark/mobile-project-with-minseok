package com.example.jiminandminseok

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SmokingRecordDao {
    @Query("SELECT * FROM smoking_records ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SmokingRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: SmokingRecordEntity)
}
