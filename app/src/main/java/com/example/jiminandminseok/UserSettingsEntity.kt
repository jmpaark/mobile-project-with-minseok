package com.example.jiminandminseok

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val startDate: Long,
    val dailyCigarettes: Int,
    val packPrice: Int,
    val smokingYears: Int,
    val isSetupComplete: Boolean
)
