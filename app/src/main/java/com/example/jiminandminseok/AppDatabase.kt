package com.example.jiminandminseok

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RecordEntity::class, UserSettingsEntity::class, SmokingRecordEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao
    abstract fun settingsDao(): SettingsDao
    abstract fun smokingRecordDao(): SmokingRecordDao
}
