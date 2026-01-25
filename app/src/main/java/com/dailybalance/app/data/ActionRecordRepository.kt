package com.dailybalance.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ActionRecordRepository(private val dao: ActionRecordDao) {
    suspend fun insert(record: ActionRecord) = withContext(Dispatchers.IO) {
        dao.insert(record)
    }

    suspend fun getAll(): List<ActionRecord> = withContext(Dispatchers.IO) {
        dao.getAll()
    }

    suspend fun getByTypeBetween(type: String, fromTimestamp: Long, toTimestamp: Long): List<ActionRecord> = withContext(Dispatchers.IO) {
        dao.getByTypeBetween(type, fromTimestamp, toTimestamp)
    }

    suspend fun getLastTimestampByType(type: String): Long? = withContext(Dispatchers.IO) {
        dao.getLastTimestampByType(type)
    }

    suspend fun countByTypeBetween(type: String, fromTimestamp: Long, toTimestamp: Long): Int = withContext(Dispatchers.IO) {
        dao.countByTypeBetween(type, fromTimestamp, toTimestamp)
    }

    suspend fun deleteByTypeBetween(type: String, fromTimestamp: Long, toTimestamp: Long) = withContext(Dispatchers.IO) {
        dao.deleteByTypeBetween(type, fromTimestamp, toTimestamp)
    }

    suspend fun deleteById(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        dao.deleteAll()
    }
}
