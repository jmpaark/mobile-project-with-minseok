package com.example.jiminandminseok

import android.content.Context
import androidx.room.Room

object DbProvider {
    @Volatile
    private var db: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return db ?: synchronized(this) {
            db ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app.db"
            ).build().also { db = it }
        }
    }
}
