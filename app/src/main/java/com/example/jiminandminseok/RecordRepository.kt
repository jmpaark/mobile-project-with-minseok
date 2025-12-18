package com.example.jiminandminseok

import kotlinx.coroutines.flow.Flow

interface RecordRepository {
    suspend fun add(count: Int, memo: String?)
    fun observeAll(): Flow<List<SmokingRecord>>
}
