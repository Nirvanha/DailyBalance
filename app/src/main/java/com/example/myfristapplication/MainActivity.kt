package com.example.myfristapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.myfristapplication.ui.theme.ApplicationTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import dagger.hilt.android.AndroidEntryPoint
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.runtime.SideEffect
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect

import com.example.myfristapplication.viewmodel.FoodViewModel
import com.example.myfristapplication.viewmodel.ExpenseViewModel
import com.example.myfristapplication.viewmodel.RecordsViewModel
import com.example.myfristapplication.viewmodel.ExpenseRecordsViewModel
import com.example.myfristapplication.viewmodel.ThemeViewModel

import com.example.myfristapplication.ui.home.HomeScreen
import com.example.myfristapplication.ui.food.FoodScreen
import com.example.myfristapplication.ui.expense.DailyExpenseScreen
import com.example.myfristapplication.ui.records.RecordsScreen
import com.example.myfristapplication.ui.records.ExpenseRecordsScreen
import com.example.myfristapplication.ui.shared.MessageScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewModels are provided by Hilt (ThemeViewModel is AndroidViewModel created by ViewModelProvider)
        val mainViewModel: MainViewModel by viewModels()
        val foodViewModel: FoodViewModel by viewModels()
        val recordsViewModel: RecordsViewModel by viewModels()
        val expenseViewModel: ExpenseViewModel by viewModels()
        val expenseRecordsViewModel: ExpenseRecordsViewModel by viewModels()
        val themeViewModel: ThemeViewModel by viewModels()

        // Launcher para SAF
        var exportExpensesUri: Uri? = null
        val exportExpensesLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
            if (uri != null) {
                val expenses = expenseRecordsViewModel.expenseRecords.value
                val csv = expenseRecordsViewModel.exportExpensesToCsv(expenses)
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csv.toByteArray())
                }
            }
            mainViewModel.exportExpensesHandled()
        }

        // Launcher para exportar registros
        val exportRecordsLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
            if (uri != null) {
                val records = recordsViewModel.records.value
                val csv = recordsViewModel.exportRecordsToCsv(records)
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csv.toByteArray())
                }
            }
            mainViewModel.exportRecordsHandled()
        }

        enableEdgeToEdge()
        setContent {
            val isDark by themeViewModel.isDarkMode.collectAsState()
            val exportExpensesEvent by mainViewModel.exportExpensesEvent.collectAsState()
            val exportRecordsEvent by mainViewModel.exportRecordsEvent.collectAsState()
            ApplicationTheme(darkTheme = isDark) {
                val systemUiController = rememberSystemUiController()
                val backgroundColor = androidx.compose.material3.MaterialTheme.colorScheme.background
                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = backgroundColor,
                        darkIcons = !isDark
                    )
                }
                // Lanzar SAF cuando se solicite exportar
                LaunchedEffect(exportExpensesEvent) {
                    if (exportExpensesEvent) {
                        exportExpensesLauncher.launch("gastos.csv")
                    }
                }
                // Lanzar SAF cuando se solicite exportar registros
                LaunchedEffect(exportRecordsEvent) {
                    if (exportRecordsEvent) {
                        exportRecordsLauncher.launch("registros.csv")
                    }
                }
                MainApp(
                    mainViewModel = mainViewModel,
                    foodViewModel = foodViewModel,
                    expenseViewModel = expenseViewModel,
                    recordsViewModel = recordsViewModel,
                    expenseRecordsViewModel = expenseRecordsViewModel
                )
            }
        }
    }
}

@Composable
fun MainApp(
    mainViewModel: MainViewModel,
    foodViewModel: FoodViewModel,
    expenseViewModel: ExpenseViewModel,
    recordsViewModel: RecordsViewModel,
    expenseRecordsViewModel: ExpenseRecordsViewModel,
) {
    val currentScreen by mainViewModel.currentScreen.collectAsState()
    val message by mainViewModel.message.collectAsState()

    when (currentScreen) {
        "home" -> HomeScreen(
            onCigaretteClick = {
                mainViewModel.setMessage("You smoked a cigarette!")
                mainViewModel.navigateTo("message")
                recordsViewModel.registerAction("cigarette", null)
            },
            onBeerClick = {
                mainViewModel.setMessage("You drank a beer!")
                mainViewModel.navigateTo("message")
                recordsViewModel.registerAction("beer", null)
            },
            onFoodClick = {
                mainViewModel.navigateTo("food")
            },
            onViewRecordsClick = {
                recordsViewModel.requestRecords()
                mainViewModel.navigateTo("records")
            },
            onDeleteAllClick = {
                recordsViewModel.deleteAll()
            },
            onMoneyClick = {
                mainViewModel.navigateTo("dailyExpense")
            },
            onViewExpensesClick = {
                expenseRecordsViewModel.requestExpenseRecords()
                mainViewModel.navigateTo("expenseRecords")
            }
        )

        "food" -> FoodScreen(
            description = foodViewModel.description.collectAsState().value,
            onDescriptionChange = { foodViewModel.setDescription(it) },
            onRegistrarClick = {
                foodViewModel.registerFood()
                mainViewModel.setMessage("You register food!")
                foodViewModel.reset()
                mainViewModel.navigateTo("message")
            },
            onBackClick = {
                foodViewModel.reset()
                mainViewModel.navigateTo("home")
            }
        )

        "dailyExpense" -> DailyExpenseScreen(
            amountText = expenseViewModel.amountText.collectAsState().value,
            category = expenseViewModel.category.collectAsState().value,
            origin = expenseViewModel.origin.collectAsState().value,
            isAmountValid = expenseViewModel.isAmountValid.collectAsState().value,
            showExpenseError = expenseViewModel.showExpenseError.collectAsState().value,
            onAmountTextChange = { expenseViewModel.setAmountText(it) },
            onCategoryChange = { expenseViewModel.setCategory(it) },
            onOriginChange = { expenseViewModel.setOrigin(it) },
            onRegisterExpenseClick = {
                val ok = expenseViewModel.registerExpense()
                if (ok) {
                    mainViewModel.setMessage("Gasto diario registrado!")
                    mainViewModel.navigateTo("message")
                }
            },
            onBackClick = {
                expenseViewModel.resetFields()
                mainViewModel.navigateTo("home")
            }
        )

        "message" -> MessageScreen(
            message = message,
            onBackClick = { mainViewModel.navigateTo("home") }
        )

        "records" -> RecordsScreen(
            records = recordsViewModel.records.collectAsState().value,
            onBackClick = { mainViewModel.navigateTo("home") },
            onExportClick = {
                mainViewModel.exportRecordsRequested()
            }
        )

        "expenseRecords" -> ExpenseRecordsScreen(
            expenseRecords = expenseRecordsViewModel.expenseRecords.collectAsState().value,
            onBackClick = { mainViewModel.navigateTo("home") },
            onExportClick = {
                // Lanzar intent SAF para exportar
                mainViewModel.exportExpensesRequested()
            }
        )
    }
}
