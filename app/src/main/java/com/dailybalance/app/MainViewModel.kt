package com.dailybalance.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailybalance.app.data.ActionRecord
import com.dailybalance.app.data.ActionRecordRepository
import com.dailybalance.app.data.DailyExpense
import com.dailybalance.app.data.DailyExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel principal que gestiona el estado y la lógica de la UI para acciones y gastos diarios.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val actionRecordRepository: ActionRecordRepository,
    private val dailyExpenseRepository: DailyExpenseRepository
) : ViewModel() {
    // Estado de la pantalla actual
    private val _currentScreen = MutableStateFlow("home")
    val currentScreen: StateFlow<String> = _currentScreen

    // Mensaje para la UI
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message

    // Registros de acciones
    private val _records = MutableStateFlow<List<ActionRecord>>(emptyList())
    val records: StateFlow<List<ActionRecord>> = _records

    // Descripción de comida (campo de entrada)
    val foodDescription = MutableStateFlow("")

    // Campos para gastos diarios
    val dailyExpenseAmountText = MutableStateFlow("")
    val dailyExpenseCategory = MutableStateFlow("")
    val dailyExpenseOrigin = MutableStateFlow("")

    // Validación y errores
    private val _isAmountValid = MutableStateFlow(true)
    val isAmountValid: StateFlow<Boolean> = _isAmountValid
    private val _showExpenseError = MutableStateFlow(false)
    val showExpenseError: StateFlow<Boolean> = _showExpenseError

    // Registros de gastos diarios
    private val _expenseRecords = MutableStateFlow<List<DailyExpense>>(emptyList())
    val expenseRecords: StateFlow<List<DailyExpense>> = _expenseRecords

    // Eventos de exportación
    private val _exportExpensesEvent = MutableStateFlow(false)
    val exportExpensesEvent: StateFlow<Boolean> = _exportExpensesEvent
    private val _exportRecordsEvent = MutableStateFlow(false)
    val exportRecordsEvent: StateFlow<Boolean> = _exportRecordsEvent

    // Solicita los gastos diarios al repositorio
    fun requestExpenseRecords() {
        viewModelScope.launch {
            _expenseRecords.value = dailyExpenseRepository.getAll()
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun setMessage(msg: String) {
        _message.value = msg
    }

    // Validación y seteo de campos de gasto diario
    fun setDailyExpenseAmountText(text: String) {
        dailyExpenseAmountText.value = text
        val parsed = text.toDoubleOrNull()
        _isAmountValid.value = parsed != null && parsed > 0.0
        _showExpenseError.value = false
    }

    fun setDailyExpenseCategory(cat: String) {
        dailyExpenseCategory.value = cat
        _showExpenseError.value = false
    }

    fun setDailyExpenseOrigin(origin: String) {
        dailyExpenseOrigin.value = origin
        _showExpenseError.value = false
    }

    // Registra una acción
    fun registerAction(type: String, description: String?) {
        val record = ActionRecord(
            type = type,
            timestamp = System.currentTimeMillis(),
            description = description
        )
        viewModelScope.launch {
            actionRecordRepository.insert(record)
        }
    }

    // Solicita los registros de acciones
    fun requestRecords() {
        viewModelScope.launch {
            _records.value = actionRecordRepository.getAll()
        }
    }

    // Elimina todos los registros de acciones
    fun deleteAllRecords() {
        viewModelScope.launch {
            actionRecordRepository.deleteAll()
            _records.value = emptyList()
        }
    }

    // Registra un gasto diario validando los campos
    fun registerExpense() {
        val amount = dailyExpenseAmountText.value.toDoubleOrNull() ?: 0.0
        if (_isAmountValid.value && dailyExpenseCategory.value.isNotBlank() && dailyExpenseOrigin.value.isNotBlank()) {
            val expense = DailyExpense(
                amount = amount,
                category = dailyExpenseCategory.value,
                date = System.currentTimeMillis(),
                note = null,
                origin = dailyExpenseOrigin.value
            )
            viewModelScope.launch {
                dailyExpenseRepository.insert(expense)
            }
            _message.value = "Gasto diario registrado!"
            resetDailyExpenseFields()
            _currentScreen.value = "message"
        } else {
            _showExpenseError.value = true
        }
    }

    // Limpia los campos de gasto diario
    fun resetDailyExpenseFields() {
        dailyExpenseAmountText.value = ""
        dailyExpenseCategory.value = ""
        dailyExpenseOrigin.value = ""
        _showExpenseError.value = false
    }

    // Limpia el campo de descripción de comida
    fun resetFoodFields() {
        foodDescription.value = ""
    }

    // Eventos de exportación
    fun exportExpensesRequested() { _exportExpensesEvent.value = true }
    fun exportExpensesHandled() { _exportExpensesEvent.value = false }
    fun exportRecordsRequested() { _exportRecordsEvent.value = true }
    fun exportRecordsHandled() { _exportRecordsEvent.value = false }
}
