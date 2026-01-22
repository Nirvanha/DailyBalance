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
class ExpenseViewModel @Inject constructor(private val dailyExpenseRepository: DailyExpenseRepository) : ViewModel() {
    private val _amountText = MutableStateFlow("")
    val amountText: StateFlow<String> = _amountText

    private val _category = MutableStateFlow("")
    val category: StateFlow<String> = _category

    private val _origin = MutableStateFlow("")
    val origin: StateFlow<String> = _origin

    private val _isAmountValid = MutableStateFlow(true)
    val isAmountValid: StateFlow<Boolean> = _isAmountValid

    private val _showExpenseError = MutableStateFlow(false)
    val showExpenseError: StateFlow<Boolean> = _showExpenseError

    private val _categoryOptions = MutableStateFlow<List<String>>(emptyList())
    val categoryOptions: StateFlow<List<String>> = _categoryOptions

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val expenses = dailyExpenseRepository.getAll()
            _categoryOptions.value = expenses.map { it.category }.distinct().sorted()
        }
    }

    fun reloadCategories() {
        loadCategories()
    }

    fun setAmountText(text: String) {
        _amountText.value = text
        val parsed = text.toDoubleOrNull()
        _isAmountValid.value = parsed != null && parsed > 0.0
        _showExpenseError.value = false
    }

    fun setCategory(cat: String) {
        _category.value = cat
        _showExpenseError.value = false
    }

    fun setOrigin(orig: String) {
        _origin.value = orig
        _showExpenseError.value = false
    }

    fun resetFields() {
        _amountText.value = ""
        _category.value = ""
        _origin.value = ""
        _showExpenseError.value = false
        _isAmountValid.value = true
    }

    fun registerExpense(): Boolean {
        val amount = _amountText.value.toDoubleOrNull() ?: 0.0
        if (_isAmountValid.value && _category.value.isNotBlank() && _origin.value.isNotBlank()) {
            val expense = DailyExpense(
                amount = amount,
                category = _category.value,
                date = System.currentTimeMillis(),
                note = null,
                origin = _origin.value
            )
            viewModelScope.launch {
                dailyExpenseRepository.insert(expense)
                loadCategories()
            }
            resetFields()
            return true
        } else {
            _showExpenseError.value = true
            return false
        }
    }
}
