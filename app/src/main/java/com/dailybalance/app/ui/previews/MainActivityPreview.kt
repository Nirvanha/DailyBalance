package com.dailybalance.app.ui.previews

import android.content.res.Configuration
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dailybalance.app.data.ActionRecord
import com.dailybalance.app.data.DailyExpense
import com.dailybalance.app.ui.expense.DailyExpenseScreen
import com.dailybalance.app.ui.food.FoodScreen
import com.dailybalance.app.ui.home.HomeScreen
import com.dailybalance.app.ui.records.ExpenseRecordsScreen
import com.dailybalance.app.ui.records.RecordsScreen

// Preview that reproduces MainApp navigation using simple local state and sample data.
@Composable
fun PreviewMainApp() {
    _root_ide_package_.com.dailybalance.app.ui.theme.ApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize().padding(0.dp), color = MaterialTheme.colorScheme.background) {
            // simple local state to emulate MainViewModel + other viewmodels
            var currentScreen by remember { mutableStateOf("home") }
            var message by remember { mutableStateOf("") }

            var foodDescription by remember { mutableStateOf("") }

            var amountText by remember { mutableStateOf("") }
            var category by remember { mutableStateOf("") }
            var origin by remember { mutableStateOf("") }
            var isAmountValid by remember { mutableStateOf(true) }
            var showExpenseError by remember { mutableStateOf(false) }

            // sample records
            val sampleRecords = listOf(
                ActionRecord(
                    type = "cigarette",
                    timestamp = System.currentTimeMillis() - 1000L * 60 * 60,
                    description = null
                ),
                ActionRecord(
                    type = "comida",
                    timestamp = System.currentTimeMillis() - 1000L * 60 * 30,
                    description = "Ensalada"
                )
            )

            val sampleExpenses = listOf(
                DailyExpense(
                    amount = 5.5,
                    category = "Comida",
                    date = System.currentTimeMillis() - 1000L * 60 * 60 * 24,
                    note = "Bocadillo",
                    origin = "Máquina"
                ),
                DailyExpense(
                    amount = 2.0,
                    category = "Transporte",
                    date = System.currentTimeMillis() - 1000L * 60 * 60 * 2,
                    note = null,
                    origin = "Bus"
                )
            )

            when (currentScreen) {
                "home" -> HomeScreen(
                    lastCigaretteTimestamp = sampleRecords.firstOrNull { it.type == "cigarette" }?.timestamp,
                    todayCigarettesCount = 3,
                    todayBeersCount = 1,
                    onCigaretteClick = {
                        // Simula registrar y quedarse en home
                    },
                    onBeerClick = {
                        // Simula registrar y quedarse en home
                    },
                    onFoodClick = { currentScreen = "food" },
                    onViewRecordsClick = { currentScreen = "records" },
                    onDeleteAllClick = { /* noop for preview */ },
                    onMoneyClick = { currentScreen = "dailyExpense" },
                    onViewExpensesClick = { currentScreen = "expenseRecords" }
                )

                "food" -> FoodScreen(
                    description = foodDescription,
                    onDescriptionChange = { foodDescription = it },
                    onRegistrarClick = {
                        // simula registrar y volver a home
                        foodDescription = ""
                        currentScreen = "home"
                    },
                    onBackClick = {
                        foodDescription = ""
                        currentScreen = "home"
                    }
                )

                "dailyExpense" -> DailyExpenseScreen(
                    amountText = amountText,
                    category = category,
                    origin = origin,
                    isAmountValid = isAmountValid,
                    showExpenseError = showExpenseError,
                    onAmountTextChange = {
                        amountText = it
                        val parsed = it.toDoubleOrNull()
                        isAmountValid = parsed != null && parsed > 0.0
                        showExpenseError = false
                    },
                    onCategoryChange = { category = it; showExpenseError = false },
                    onOriginChange = { origin = it; showExpenseError = false },
                    onRegisterExpenseClick = {
                        val ok = isAmountValid && category.isNotBlank() && origin.isNotBlank()
                        if (ok) {
                            amountText = ""
                            category = ""
                            origin = ""
                            showExpenseError = false
                            currentScreen = "home"
                        } else {
                            showExpenseError = true
                        }
                    },
                    onBackClick = {
                        amountText = ""
                        category = ""
                        origin = ""
                        showExpenseError = false
                        currentScreen = "home"
                    },
                    categoryOptions = listOf("Comida", "Transporte", "Ocio", "Otros")
                )

                "records" -> RecordsScreen(
                    records = sampleRecords,
                    onBackClick = { currentScreen = "home" },
                    onExportClick = { currentScreen = "home" }
                )

                "expenseRecords" -> ExpenseRecordsScreen(
                    expenseRecords = sampleExpenses,
                    onBackClick = { currentScreen = "home" },
                    onExportClick = { currentScreen = "home" }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Preview(name = "Main Preview - Light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
fun PreviewMainApp_Defaults() {
    PreviewMainApp()
}
