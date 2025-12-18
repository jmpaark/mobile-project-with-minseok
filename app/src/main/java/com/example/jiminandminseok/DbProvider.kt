package com.example.jiminandminseok

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DbProvider {
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS user_settings (" +
                    "id INTEGER PRIMARY KEY NOT NULL, " +
                    "startDate INTEGER NOT NULL, " +
                    "dailyCigarettes INTEGER NOT NULL, " +
                    "packPrice INTEGER NOT NULL, " +
                    "smokingYears INTEGER NOT NULL, " +
                    "isSetupComplete INTEGER NOT NULL" +
                ")"
            )
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS smoking_records (" +
                    "id TEXT NOT NULL PRIMARY KEY, " +
                    "count INTEGER NOT NULL, " +
                    "memo TEXT, " +
                    "createdAt INTEGER NOT NULL" +
                ")"
            )
        }
    }

    @Volatile
    private var db: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return db ?: synchronized(this) {
            db ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app.db"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
             .build().also { db = it }
        }
    }
}
