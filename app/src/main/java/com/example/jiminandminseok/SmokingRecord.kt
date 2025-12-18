package com.example.jiminandminseok

import java.util.UUID

data class SmokingRecord(
    val id: String = UUID.randomUUID().toString(),
    val count: Int,
    val memo: String?,
    val createdAt: Long
)
