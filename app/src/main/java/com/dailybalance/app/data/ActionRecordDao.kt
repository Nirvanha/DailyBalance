package com.dailybalance.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ActionRecordDao {
    @Insert
    suspend fun insert(record: ActionRecord)

    @Query("SELECT * FROM action_record ORDER BY timestamp DESC")
    suspend fun getAll(): List<ActionRecord>

    @Query("SELECT timestamp FROM action_record WHERE type = :type ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastTimestampByType(type: String): Long?

    @Query("DELETE FROM action_record")
    suspend fun deleteAll()
}
