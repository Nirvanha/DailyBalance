package com.dailybalance.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybalance.app.data.ActionRecord
import com.dailybalance.app.data.ActionRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordsViewModel @Inject constructor(private val actionRecordRepository: ActionRecordRepository) : ViewModel() {
    private val _records = MutableStateFlow<List<ActionRecord>>(emptyList())
    val records: StateFlow<List<ActionRecord>> = _records

    private val _lastCigaretteTimestamp = MutableStateFlow<Long?>(null)
    val lastCigaretteTimestamp: StateFlow<Long?> = _lastCigaretteTimestamp

    init {
        // Carga inicial para que Home pueda mostrar el banner si ya existe info.
        refreshLastCigarette()
    }

    fun refreshLastCigarette() {
        viewModelScope.launch {
            _lastCigaretteTimestamp.value = actionRecordRepository.getLastTimestampByType("cigarette")
        }
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
