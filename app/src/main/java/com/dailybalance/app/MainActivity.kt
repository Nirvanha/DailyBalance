package com.dailybalance.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.dailybalance.app.ui.theme.ApplicationTheme
import com.dailybalance.app.viewmodel.FoodViewModel
import com.dailybalance.app.viewmodel.ExpenseViewModel
import com.dailybalance.app.viewmodel.RecordsViewModel
import com.dailybalance.app.viewmodel.ExpenseRecordsViewModel
import com.dailybalance.app.viewmodel.ThemeViewModel
import com.dailybalance.app.ui.home.HomeScreen
import com.dailybalance.app.ui.food.FoodScreen
import com.dailybalance.app.ui.expense.DailyExpenseScreen
import com.dailybalance.app.ui.records.RecordsScreen
import com.dailybalance.app.ui.records.ExpenseRecordsScreen
import com.dailybalance.app.ui.records.TodayRecordsScreen
import dagger.hilt.android.AndroidEntryPoint
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.runtime.SideEffect
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState

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

    // Cuando volvemos a home, refrescamos el último cigarro desde BD.
    LaunchedEffect(currentScreen) {
        if (currentScreen == "home") {
            recordsViewModel.refreshHomeStats()
        }
    }

    when (currentScreen) {
        "home" -> HomeScreen(
            lastCigaretteTimestamp = recordsViewModel.lastCigaretteTimestamp.collectAsState().value,
            todayCigarettesCount = recordsViewModel.todayCigarettesCount.collectAsState().value,
            todayBeersCount = recordsViewModel.todayBeersCount.collectAsState().value,
            onCigaretteClick = {
                recordsViewModel.registerAction("cigarette", null)
            },
            onBeerClick = {
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
            },
            onTodayCigarettesClick = {
                recordsViewModel.requestTodayRecordsByType("cigarette")
                mainViewModel.navigateTo("todayRecords/cigarette")
            },
            onTodayBeersClick = {
                recordsViewModel.requestTodayRecordsByType("beer")
                mainViewModel.navigateTo("todayRecords/beer")
            }
        )

        "food" -> FoodScreen(
            description = foodViewModel.description.collectAsState().value,
            onDescriptionChange = { foodViewModel.setDescription(it) },
            onRegistrarClick = {
                foodViewModel.registerFood()
                foodViewModel.reset()
                mainViewModel.navigateTo("home")
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
                    expenseViewModel.reloadCategories() // Recargar categorías tras registrar
                    // Volvemos a home (sin pantalla de mensaje)
                    mainViewModel.navigateTo("home")
                }
            },
            onBackClick = {
                expenseViewModel.resetFields()
                mainViewModel.navigateTo("home")
            },
            categoryOptions = expenseViewModel.categoryOptions.collectAsState().value
        )

        "records" -> RecordsScreen(
            records = recordsViewModel.records.collectAsState().value,
            onBackClick = { mainViewModel.navigateTo("home") },
            onExportClick = {
                mainViewModel.exportRecordsRequested()
            },
            onDeleteRecordConfirm = { record ->
                recordsViewModel.deleteRecord(record)
            }
        )

        "todayRecords/cigarette" -> TodayRecordsScreen(
            type = "cigarette",
            records = recordsViewModel.todayTypeRecords.collectAsState().value,
            onBackClick = { mainViewModel.navigateTo("home") },
            onDeleteTodayClick = {
                recordsViewModel.deleteTodayRecordsByType("cigarette")
            },
            onDeleteRecordConfirm = { record ->
                recordsViewModel.deleteRecord(record)
            },
        )

        "todayRecords/beer" -> TodayRecordsScreen(
            type = "beer",
            records = recordsViewModel.todayTypeRecords.collectAsState().value,
            onBackClick = { mainViewModel.navigateTo("home") },
            onDeleteTodayClick = {
                recordsViewModel.deleteTodayRecordsByType("beer")
            },
            onDeleteRecordConfirm = { record ->
                recordsViewModel.deleteRecord(record)
            },
        )

        "expenseRecords" -> ExpenseRecordsScreen(
            expenseRecords = expenseRecordsViewModel.expenseRecords.collectAsState().value,
            onBackClick = { mainViewModel.navigateTo("home") },
            onExportClick = {
                mainViewModel.exportExpensesRequested()
            },
            onDeleteExpenseConfirm = { expense ->
                expenseRecordsViewModel.deleteExpense(expense)
            }
        )

    }
}
