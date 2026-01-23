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

    suspend fun getLastTimestampByType(type: String): Long? = withContext(Dispatchers.IO) {
        dao.getLastTimestampByType(type)
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        dao.deleteAll()
    }
}
