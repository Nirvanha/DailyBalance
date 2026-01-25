package com.dailybalance.app.fakes

import com.dailybalance.app.data.ActionRecord
import com.dailybalance.app.data.ActionRecordDao

/** DAO fake minimalista en memoria para tests unitarios. */
class FakeActionRecordDao : ActionRecordDao {
    val records = mutableListOf<ActionRecord>()

    override suspend fun insert(record: ActionRecord) {
        records.add(record)
    }

    override suspend fun getAll(): List<ActionRecord> = records.sortedByDescending { it.timestamp }

    override suspend fun getLastTimestampByType(type: String): Long? =
        records.filter { it.type == type }.maxOfOrNull { it.timestamp }

    override suspend fun countByTypeBetween(type: String, fromTimestamp: Long, toTimestamp: Long): Int =
        records.count { it.type == type && it.timestamp in fromTimestamp..toTimestamp }

    override suspend fun deleteAll() {
        records.clear()
    }
}
