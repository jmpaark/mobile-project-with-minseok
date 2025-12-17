package com.example.jiminandminseok

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val count: Int,                 // 오늘 피운 담배 개수
    val memo: String?,              // 메모(선택)
    val createdAt: Long = System.currentTimeMillis()
)
