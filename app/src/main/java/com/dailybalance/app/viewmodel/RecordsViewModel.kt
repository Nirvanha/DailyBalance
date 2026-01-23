package com.dailybalance.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybalance.app.data.ActionRecord
import com.dailybalance.app.data.ActionRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class RecordsViewModel @Inject constructor(private val actionRecordRepository: ActionRecordRepository) : ViewModel() {
    private val _records = MutableStateFlow<List<ActionRecord>>(emptyList())
    val records: StateFlow<List<ActionRecord>> = _records

    private val _lastCigaretteTimestamp = MutableStateFlow<Long?>(null)
    val lastCigaretteTimestamp: StateFlow<Long?> = _lastCigaretteTimestamp

    private val _todayCigarettesCount = MutableStateFlow(0)
    val todayCigarettesCount: StateFlow<Int> = _todayCigarettesCount

    private val _todayBeersCount = MutableStateFlow(0)
    val todayBeersCount: StateFlow<Int> = _todayBeersCount

    init {
        // Carga inicial para que Home pueda mostrar el banner/contadores si ya existe info.
        refreshHomeStats()
    }

    fun refreshHomeStats() {
        refreshLastCigarette()
        refreshTodayCounts()
    }

    fun refreshLastCigarette() {
        viewModelScope.launch {
            _lastCigaretteTimestamp.value = actionRecordRepository.getLastTimestampByType("cigarette")
        }
    }

    fun refreshTodayCounts() {
        val (startOfDay, endOfDay) = todayRangeMillis()
        viewModelScope.launch {
            _todayCigarettesCount.value = actionRecordRepository.countByTypeBetween("cigarette", startOfDay, endOfDay)
            _todayBeersCount.value = actionRecordRepository.countByTypeBetween("beer", startOfDay, endOfDay)
        }
    }

    private fun todayRangeMillis(nowMillis: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val end = start + 24L * 60L * 60L * 1000L - 1L
        return start to end
    }

    fun requestRecords() {
        viewModelScope.launch {
            _records.value = actionRecordRepository.getAll()
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            actionRecordRepository.deleteAll()
            _records.value = emptyList()
            _lastCigaretteTimestamp.value = null
            _todayCigarettesCount.value = 0
            _todayBeersCount.value = 0
        }
    }

    fun registerAction(type: String, description: String?) {
        val record = ActionRecord(
            type = type,
            timestamp = System.currentTimeMillis(),
            description = description
        )
        viewModelScope.launch {
            actionRecordRepository.insert(record)

            if (type == "cigarette") {
                // Actualiza inmediatamente el banner.
                _lastCigaretteTimestamp.value = record.timestamp
                _todayCigarettesCount.value = _todayCigarettesCount.value + 1
            } else if (type == "beer") {
                _todayBeersCount.value = _todayBeersCount.value + 1
            }
        }
    }

    fun exportRecordsToCsv(records: List<ActionRecord>): String {
        val header = "Tipo,Fecha,Descripción"
        val dateFormat = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss", java.util.Locale.getDefault())
        val rows = records.map { record ->
            val formattedDate = dateFormat.format(java.util.Date(record.timestamp))
            val desc = record.description?.replace(",", " ") ?: ""
            "${record.type},$formattedDate,$desc"
        }
        return (listOf(header) + rows).joinToString("\n")
    }
}
