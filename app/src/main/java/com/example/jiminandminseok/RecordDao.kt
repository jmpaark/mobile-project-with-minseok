package com.example.jiminandminseok

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecordDao {

    @Query("SELECT * FROM records ORDER BY createdAt DESC")
    suspend fun getAll(): List<RecordEntity>

    @Query("SELECT * FROM records WHERE createdAt = :createdAt LIMIT 1")
    suspend fun getByCreatedAt(createdAt: Long): RecordEntity?

    @Query("DELETE FROM records WHERE createdAt = :createdAt")
    suspend fun deleteByCreatedAt(createdAt: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RecordEntity)

    // ✅ 금연 시작일 이후 “실제로 핀 개비(count)” 합계
    @Query("SELECT COALESCE(SUM(count), 0) FROM records WHERE createdAt >= :startMillis")
    suspend fun sumSmokedSince(startMillis: Long): Long
}
