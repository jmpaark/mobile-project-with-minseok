package com.example.jiminandminseok

import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomRecordRepository(
    private val dao: SmokingRecordDao
) : RecordRepository {
    override suspend fun add(count: Int, memo: String?) {
        val record = SmokingRecordEntity(
            id = UUID.randomUUID().toString(),
            count = count,
            memo = memo,
            createdAt = System.currentTimeMillis()
        )
        dao.insert(record)
    }

    override fun observeAll(): Flow<List<SmokingRecord>> {
        return dao.observeAll().map { entities ->
            entities.map { entity ->
                SmokingRecord(
                    id = entity.id,
                    count = entity.count,
                    memo = entity.memo,
                    createdAt = entity.createdAt
                )
            }
        }
    }
}
