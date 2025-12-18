package com.example.jiminandminseok

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smoking_records")
data class SmokingRecordEntity(
    @PrimaryKey val id: String,
    val count: Int,
    val memo: String?,
    val createdAt: Long
)
