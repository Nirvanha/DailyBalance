package com.dailybalance.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybalance.app.data.DailyExpense
import com.dailybalance.app.data.DailyExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseRecordsViewModel @Inject constructor(private val dailyExpenseRepository: DailyExpenseRepository) : ViewModel() {
    private val _expenseRecords = MutableStateFlow<List<DailyExpense>>(emptyList())
    val expenseRecords: StateFlow<List<DailyExpense>> = _expenseRecords

    fun requestExpenseRecords() {
        viewModelScope.launch {
            _expenseRecords.value = dailyExpenseRepository.getAll()
        }
    }

    fun deleteExpense(expense: DailyExpense) {
        viewModelScope.launch {
            dailyExpenseRepository.delete(expense)
            _expenseRecords.value = dailyExpenseRepository.getAll()
        }
    }

    fun exportExpensesToCsv(expenses: List<DailyExpense>): String {
        val header = "Cantidad,Categoría,Fecha,Origen,Nota"
        val dateFormat = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss", java.util.Locale.getDefault())
        val rows = expenses.map { expense ->
            val formattedDate = dateFormat.format(java.util.Date(expense.date))
            val note = expense.note?.replace(",", " ") ?: ""
            "${expense.amount},${expense.category},$formattedDate,${expense.origin},$note"
        }
        return (listOf(header) + rows).joinToString("\n")
    }
}
