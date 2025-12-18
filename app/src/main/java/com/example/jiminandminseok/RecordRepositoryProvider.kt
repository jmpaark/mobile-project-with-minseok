package com.example.jiminandminseok

import android.content.Context

object RecordRepositoryProvider {
    @Volatile
    private var instance: RecordRepository? = null

    fun get(context: Context): RecordRepository {
        return instance ?: synchronized(this) {
            instance ?: buildRepository(context.applicationContext).also { instance = it }
        }
    }

    private fun buildRepository(context: Context): RecordRepository {
        return if (BuildConfig.USE_FIREBASE) {
            FirestoreRecordRepository()
        } else {
            val db = DbProvider.get(context)
            RoomRecordRepository(db.smokingRecordDao())
        }
    }
}
