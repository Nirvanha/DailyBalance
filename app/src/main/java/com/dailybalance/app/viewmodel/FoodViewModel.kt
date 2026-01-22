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
class FoodViewModel @Inject constructor(private val actionRecordRepository: ActionRecordRepository) : ViewModel() {
    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    fun setDescription(desc: String) {
        _description.value = desc
    }

    fun registerFood() {
        val record = ActionRecord(
            type = "comida",
            timestamp = System.currentTimeMillis(),
            description = _description.value
        )
        viewModelScope.launch {
            actionRecordRepository.insert(record)
        }
    }

    fun reset() {
        _description.value = ""
    }
}
