package com.example.jiminandminseok

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RecordDao {

    @Insert
    suspend fun insert(record: RecordEntity)

    @Query("SELECT * FROM records ORDER BY createdAt DESC")
    suspend fun getAll(): List<RecordEntity>
}
