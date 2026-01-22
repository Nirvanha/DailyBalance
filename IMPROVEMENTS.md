# 📊 Guía de Mejoras - DailyBalance

Este documento contiene un análisis completo de las mejoras y refactorizaciones recomendadas para el proyecto DailyBalance. Úsalo como referencia para futuras iteraciones de desarrollo.

---

## 🏗️ Arquitectura Actual

### ✅ Puntos Fuertes

- **Arquitectura MVVM bien definida** con separación clara de responsabilidades
- **Uso de tecnologías modernas**: Jetpack Compose, Room, Hilt, Coroutines, Flow
- **Separación clara en capas**: UI, ViewModel, Data
- **Inyección de dependencias** con Hilt configurado correctamente
- **Base de datos con migraciones** gestionadas correctamente (v1 → v2 → v3)

### ❌ Deuda Técnica

- **Nombre del paquete con error tipográfico**: `MyFristApplication` (debería ser "First")
- **0% de cobertura de tests** (solo templates por defecto)
- **Casi todas las strings están hardcodeadas** en el código (59 instancias)
- **Navegación custom sin type-safety** (basada en strings)

---

## 🔴 Problemas Críticos

### 1. MainViewModel como "God Class"

**Ubicación:** `MainViewModel.kt:1-160`

**Problema:** Tiene 7 responsabilidades diferentes:
- Navegación
- Gestión de mensajes
- Registros de acciones
- Descripción de comidas
- Gastos diarios
- Eventos de exportación
- Validación de campos

**Impacto:** Viola el **Single Responsibility Principle**

**Solución:**
```kotlin
// Dividir en ViewModels específicos:
NavigationViewModel  // Solo navegación
MessageViewModel     // Solo mensajes

// Ya existen: FoodViewModel, ExpenseViewModel, RecordsViewModel
// Eliminar funcionalidad duplicada de MainViewModel
```

---

### 2. Código Duplicado en ViewModels

**Duplicaciones encontradas:**
- `registerAction()` en `MainViewModel.kt:92-102` y `RecordsViewModel.kt:31-40`
- `registerExpense()` en `MainViewModel.kt:120-139` y `ExpenseViewModel.kt:73-93`
- Validación de montos en 2 lugares
- Creación de `SimpleDateFormat` en 4 lugares diferentes

**Solución:** Consolidar en los ViewModels específicos y eliminar de MainViewModel

---

### 3. MainActivity Sobrecargada

**Ubicación:** `MainActivity.kt:34-212` (211 líneas)

**Problemas:**
- Inicializa 6 ViewModels diferentes
- Gestiona launchers de exportación
- Maneja toda la navegación (función `MainApp` con 102 líneas)
- Lógica de exportación de archivos en la Activity (líneas 48-69)

**Solución:**
```kotlin
// Mover lógica de exportación a ViewModels
// Usar Navigation Component para navegación
// Dividir composables grandes en componentes más pequeños
```

---

### 4. Ausencia Total de Manejo de Errores

**Sin try-catch en:**
- Operaciones de base de datos (todos los DAOs)
- Operaciones de archivo (`MainActivity.kt:52-54`)
- Conversiones numéricas (`ExpenseViewModel.kt:74`)
- `SimpleDateFormat.format()` (puede lanzar excepciones)

**Ejemplo problemático:**
```kotlin
// MainActivity.kt:52-54
contentResolver.openOutputStream(uri)?.use { outputStream ->
    outputStream.write(csv.toByteArray()) // ❌ Sin try-catch
}
```

**Solución:**
```kotlin
viewModelScope.launch {
    try {
        val result = repository.insert(expense)
        _uiState.value = UiState.Success(result)
    } catch (e: SQLException) {
        _uiState.value = UiState.Error("Error guardando gasto: ${e.message}")
        Log.e(TAG, "Database error", e)
    } catch (e: Exception) {
        _uiState.value = UiState.Error("Error inesperado")
        Log.e(TAG, "Unexpected error", e)
    }
}
```

---

### 5. ThemeViewModel Sin Inyección de Dependencias

**Ubicación:** `ThemeViewModel.kt:12-13`

**Problema:**
```kotlin
class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = ThemePreferences(application) // ❌ Creación directa
}
```

**Solución:**
```kotlin
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themePreferences: ThemePreferences
) : ViewModel() {
    // ...
}

// En AppModule.kt
@Provides
@Singleton
fun provideThemePreferences(@ApplicationContext context: Context): ThemePreferences {
    return ThemePreferences(context)
}
```

---

## 🟡 Problemas de Alta Prioridad

### 6. Navegación Sin Type-Safety

**Problema actual:** Strings mágicos

```kotlin
// MainActivity.kt:120-210
when (currentScreen) {
    "home" -> HomeScreen(...)
    "food" -> FoodScreen(...)
    "dailyExpense" -> DailyExpenseScreen(...) // ❌ Propenso a errores
}
```

**Solución Opción 1: Sealed class**
```kotlin
sealed class Screen {
    object Home : Screen()
    object Food : Screen()
    object DailyExpense : Screen()
    object Records : Screen()
}
```

**Solución Opción 2 (Recomendada): Jetpack Navigation Compose**
```kotlin
@Serializable object HomeRoute
@Serializable object FoodRoute
@Serializable object ExpenseRoute

NavHost(navController, startDestination = HomeRoute) {
    composable<HomeRoute> { HomeScreen(navController) }
    composable<FoodRoute> { FoodScreen(navController) }
    composable<ExpenseRoute> { DailyExpenseScreen(navController) }
}
```

**Dependencias necesarias:**
```kotlin
// build.gradle.kts
implementation("androidx.navigation:navigation-compose:2.7.7")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

// En plugins
id("org.jetbrains.kotlin.plugin.serialization") version "2.1.21"
```

---

### 7. 59 Strings Hardcodeadas

**Ejemplos:**
```kotlin
// MainActivity.kt:123
"You smoked a cigarette!" // ❌

// HomeScreen.kt:45
"Cigarette" // ❌

// DailyExpenseScreen.kt:50
"Cantidad" // ❌
```

**Solución:** Crear `res/values/strings.xml` completo

```xml
<resources>
    <string name="app_name">Daily Balance</string>
    
    <!-- Mensajes -->
    <string name="msg_cigarette_registered">You smoked a cigarette!</string>
    <string name="msg_beer_registered">You drank a beer!</string>
    <string name="msg_food_registered">You register food!</string>
    <string name="msg_expense_registered">Gasto diario registrado!</string>
    
    <!-- Botones -->
    <string name="button_cigarette">Cigarette</string>
    <string name="button_beer">Beer</string>
    <string name="button_food">Food</string>
    <string name="button_money">Money</string>
    <string name="button_back">Back</string>
    <string name="button_register">Registrar</string>
    <string name="button_export">Exportar</string>
    
    <!-- Labels -->
    <string name="label_amount">Cantidad</string>
    <string name="label_category">Categoría</string>
    <string name="label_origin">Origen</string>
    <string name="label_note">Nota</string>
    <string name="label_date">Fecha</string>
    <string name="label_description">Descripción</string>
    
    <!-- Títulos -->
    <string name="title_records">Registros</string>
    <string name="title_expenses">Gastos</string>
    <string name="title_daily_expense">Registrar gasto diario</string>
    
    <!-- Errores -->
    <string name="error_invalid_amount">Introduce una cantidad válida mayor que 0</string>
    <string name="error_complete_fields">Por favor, completa todos los campos.</string>
    
    <!-- Orígenes -->
    <string name="origin_nomina">Nomina</string>
    <string name="origin_nocuenta">NoCuenta</string>
    <string name="origin_credito">Credito</string>
    <string name="origin_eci">Eci</string>
    
    <!-- Ordenamiento -->
    <string name="sort_ascending">Ascendente</string>
    <string name="sort_descending">Descendente</string>
    
    <!-- Nombres de archivo -->
    <string name="filename_expenses">gastos.csv</string>
    <string name="filename_records">registros.csv</string>
</resources>
```

**Uso en el código:**
```kotlin
Text(text = stringResource(R.string.button_cigarette))
```

---

### 8. withContext(Dispatchers.IO) Redundante

**Problema:** Room ya ejecuta operaciones suspend en un dispatcher apropiado

```kotlin
// ActionRecordRepository.kt:7-17 ❌
suspend fun insert(record: ActionRecord) = withContext(Dispatchers.IO) {
    dao.insert(record) // Room ya lo hace en IO
}
```

**Solución:**
```kotlin
// ✅ Eliminar withContext
suspend fun insert(record: ActionRecord) {
    dao.insert(record)
}

// Aplicar en:
// - ActionRecordRepository.kt (todas las funciones)
// - DailyExpenseRepository.kt (todas las funciones)
```

---

### 9. Lógica de Negocio en la Activity

**Problema:** Exportación de archivos en MainActivity

```kotlin
// MainActivity.kt:48-69 ❌
val exportExpensesLauncher = registerForActivityResult(...) { uri: Uri? ->
    if (uri != null) {
        val expenses = expenseRecordsViewModel.expenseRecords.value
        val csv = expenseRecordsViewModel.exportExpensesToCsv(expenses)
        contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(csv.toByteArray())
        }
    }
}
```

**Solución:**

```kotlin
// En ExpenseRecordsViewModel.kt
sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    data class Success(val message: String) : ExportState()
    data class Error(val message: String) : ExportState()
}

private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

fun exportToFile(uri: Uri, contentResolver: ContentResolver) {
    viewModelScope.launch {
        _exportState.value = ExportState.Loading
        try {
            val csv = exportExpensesToCsv(expenseRecords.value)
            contentResolver.openOutputStream(uri)?.use { 
                it.write(csv.toByteArray())
            }
            _exportState.value = ExportState.Success("Gastos exportados correctamente")
        } catch (e: IOException) {
            _exportState.value = ExportState.Error("Error al exportar: ${e.message}")
        } catch (e: Exception) {
            _exportState.value = ExportState.Error("Error inesperado")
        }
    }
}

// En MainActivity.kt
val exportExpensesLauncher = registerForActivityResult(...) { uri: Uri? ->
    uri?.let { expenseRecordsViewModel.exportToFile(it, contentResolver) }
}

// Observar estado de exportación
LaunchedEffect(exportState) {
    when (exportState) {
        is ExportState.Success -> {
            // Mostrar Toast o Snackbar
        }
        is ExportState.Error -> {
            // Mostrar error
        }
        else -> {}
    }
}
```

---

### 10. Composables Demasiado Grandes

**Problemas:**
- `DailyExpenseScreen.kt`: 148 líneas (líneas 24-147)
- `ExpenseRecordsScreen.kt`: 175 líneas (líneas 27-153)
- `MainApp` en MainActivity: 102 líneas (líneas 109-211)

**Solución:** Dividir en componentes más pequeños

```kotlin
// DailyExpenseScreen.kt
@Composable
fun DailyExpenseScreen(...) {
    Column(modifier = Modifier.fillMaxSize()) {
        ExpenseHeader(onBack = onBack)
        ExpenseAmountInput(
            amount = amountText,
            onAmountChange = onAmountChange,
            isValid = isAmountValid
        )
        ExpenseCategoryDropdown(
            category = category,
            categories = categories,
            onCategoryChange = onCategoryChange
        )
        ExpenseOriginDropdown(
            origin = origin,
            onOriginChange = onOriginChange
        )
        ExpenseNoteField(
            note = note,
            onNoteChange = onNoteChange
        )
        ExpenseActionButtons(
            onRegister = onRegister,
            errorMessage = errorMessage
        )
    }
}

// Componentes separados
@Composable 
private fun ExpenseHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Registrar gasto diario", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = onBack) { Text("Volver") }
    }
}

@Composable
private fun ExpenseAmountInput(
    amount: String,
    onAmountChange: (String) -> Unit,
    isValid: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            label = { Text("Cantidad") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = !isValid && amount.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )
        if (!isValid && amount.isNotEmpty()) {
            Text(
                text = "Introduce una cantidad válida mayor que 0",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ExpenseCategoryDropdown(...) { /* implementación */ }

@Composable
private fun ExpenseOriginDropdown(...) { /* implementación */ }

@Composable
private fun ExpenseNoteField(...) { /* implementación */ }

@Composable
private fun ExpenseActionButtons(...) { /* implementación */ }
```

---

## 🟢 Mejoras Recomendadas (Prioridad Media)

### 11. Crear Utilidades Comunes

**utils/DateFormatter.kt**
```kotlin
package com.example.myfristapplication.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {
    private val format = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
    
    fun format(timestamp: Long): String {
        return try {
            format.format(Date(timestamp))
        } catch (e: Exception) {
            "Invalid date"
        }
    }
    
    fun parse(dateString: String): Long? {
        return try {
            format.parse(dateString)?.time
        } catch (e: Exception) {
            null
        }
    }
}
```

**utils/CsvExporter.kt**
```kotlin
package com.example.myfristapplication.utils

class CsvExporter<T> {
    fun export(
        items: List<T>,
        headers: String,
        rowMapper: (T) -> String
    ): String {
        val builder = StringBuilder()
        builder.append(headers).append("\n")
        items.forEach { item ->
            builder.append(rowMapper(item)).append("\n")
        }
        return builder.toString()
    }
}

// Uso en ViewModels
fun exportExpensesToCsv(expenses: List<DailyExpense>): String {
    val exporter = CsvExporter<DailyExpense>()
    return exporter.export(
        items = expenses,
        headers = "Cantidad,Categoría,Fecha,Origen,Nota",
        rowMapper = { expense ->
            val formattedDate = DateFormatter.format(expense.date)
            "${expense.amount},${expense.category},$formattedDate,${expense.origin ?: ""},${expense.note ?: ""}"
        }
    )
}
```

**utils/AmountValidator.kt**
```kotlin
package com.example.myfristapplication.utils

object AmountValidator {
    fun validate(text: String): ValidationResult {
        val parsed = text.toDoubleOrNull()
        return when {
            parsed == null -> ValidationResult.Invalid("Formato inválido")
            parsed <= 0.0 -> ValidationResult.Invalid("Debe ser mayor que 0")
            else -> ValidationResult.Valid(parsed)
        }
    }
}

sealed class ValidationResult {
    data class Valid(val value: Double) : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}

// Uso en ViewModels
fun onAmountChange(text: String) {
    _amountText.value = text
    when (val result = AmountValidator.validate(text)) {
        is ValidationResult.Valid -> {
            _isAmountValid.value = true
            _validatedAmount.value = result.value
        }
        is ValidationResult.Invalid -> {
            _isAmountValid.value = false
            _errorMessage.value = result.message
        }
    }
}
```

---

### 12. Agregar Sealed Class para UI State

**ui/common/UiState.kt**
```kotlin
package com.example.myfristapplication.ui.common

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

// Extension functions
fun <T> UiState<T>.isLoading() = this is UiState.Loading
fun <T> UiState<T>.isSuccess() = this is UiState.Success
fun <T> UiState<T>.isError() = this is UiState.Error
fun <T> UiState<T>.getDataOrNull() = (this as? UiState.Success)?.data
```

**Uso en ViewModels**
```kotlin
private val _uiState = MutableStateFlow<UiState<List<DailyExpense>>>(UiState.Idle)
val uiState: StateFlow<UiState<List<DailyExpense>>> = _uiState.asStateFlow()

fun loadExpenses() {
    viewModelScope.launch {
        _uiState.value = UiState.Loading
        try {
            val expenses = repository.getAllExpenses()
            _uiState.value = UiState.Success(expenses)
        } catch (e: Exception) {
            _uiState.value = UiState.Error(e.message ?: "Error desconocido")
        }
    }
}
```

**Uso en Composables**
```kotlin
val uiState by viewModel.uiState.collectAsStateWithLifecycle()

when (uiState) {
    is UiState.Idle -> { /* Estado inicial */ }
    is UiState.Loading -> { CircularProgressIndicator() }
    is UiState.Success -> {
        val data = (uiState as UiState.Success).data
        ExpensesList(expenses = data)
    }
    is UiState.Error -> {
        val message = (uiState as UiState.Error).message
        ErrorMessage(message = message)
    }
}
```

---

### 13. Agregar Logging con Timber

**build.gradle.kts**
```kotlin
dependencies {
    implementation("com.jakewharton.timber:timber:5.0.1")
}
```

**MyApplication.kt**
```kotlin
@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // En producción, usar un árbol que envíe logs a un servicio
            // Timber.plant(CrashReportingTree())
        }
        
        Timber.d("Application started")
    }
}
```

**Uso en ViewModels y otras clases**
```kotlin
import timber.log.Timber

class ExpenseViewModel @Inject constructor(...) : ViewModel() {
    
    fun registerExpense(...) {
        Timber.d("Registering expense: amount=$amount, category=$category")
        
        viewModelScope.launch {
            try {
                repository.insert(expense)
                Timber.i("Expense registered successfully: id=${expense.id}")
            } catch (e: SQLException) {
                Timber.e(e, "Database error while registering expense")
                _errorMessage.value = "Error al guardar gasto"
            } catch (e: Exception) {
                Timber.e(e, "Unexpected error")
                _errorMessage.value = "Error inesperado"
            }
        }
    }
}
```

**Custom Timber Tree para producción**
```kotlin
class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return // No enviar logs de debug/verbose en producción
        }
        
        // Enviar a servicio de crash reporting (Firebase Crashlytics, etc.)
        // FirebaseCrashlytics.getInstance().log(message)
        if (t != null) {
            // FirebaseCrashlytics.getInstance().recordException(t)
        }
    }
}
```

---

### 14. Mejorar Double-Check Locking en AppDatabase

**AppDatabase.kt** (Actual - incorrecto)
```kotlin
// Líneas 31-42
return INSTANCE ?: synchronized(this) {
    val instance = Room.databaseBuilder(...)
    INSTANCE = instance
    instance
}
```

**Solución correcta:**
```kotlin
return INSTANCE ?: synchronized(this) {
    INSTANCE ?: Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "app_database"
    )
    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
    .build()
    .also { INSTANCE = it }
}
```

**Explicación:** El patrón correcto verifica dos veces si INSTANCE es null antes de crear la instancia, evitando condiciones de carrera.

---

### 15. Usar collectAsStateWithLifecycle

**Problema actual:** `MainActivity.kt:73-75`
```kotlin
val isDark by themeViewModel.isDarkMode.collectAsState()
```

**Solución:**

**build.gradle.kts**
```kotlin
dependencies {
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
}
```

**MainActivity.kt**
```kotlin
import androidx.lifecycle.compose.collectAsStateWithLifecycle

val isDark by themeViewModel.isDarkMode.collectAsStateWithLifecycle()
```

**Ventajas:**
- Automáticamente para la recolección cuando la app está en background
- Previene fugas de memoria
- Respeta el ciclo de vida de Android

---

### 16. Migrar de SystemUiController (Deprecated)

**Problema:** Accompanist SystemUiController está deprecated

**Actual:** `MainActivity.kt:77-84`
```kotlin
val systemUiController = rememberSystemUiController()
SideEffect {
    systemUiController.setStatusBarColor(
        color = if (isDark) Color.Black else Color.White,
        darkIcons = !isDark
    )
}
```

**Solución con WindowCompat:**

**MainActivity.kt**
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Configurar edge-to-edge
    enableEdgeToEdge()
    WindowCompat.setDecorFitsSystemWindows(window, false)
    
    setContent {
        val isDark by themeViewModel.isDarkMode.collectAsStateWithLifecycle()
        
        // Actualizar status bar cuando cambia el tema
        LaunchedEffect(isDark) {
            window.statusBarColor = Color.TRANSPARENT
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
        
        ApplicationTheme(darkTheme = isDark) {
            MainApp(...)
        }
    }
}
```

**Eliminar dependencia:**
```kotlin
// build.gradle.kts - ELIMINAR:
// implementation("com.google.accompanist:accompanist-systemuicontroller:0.36.0")
```

---

## ⚪ Mejoras Adicionales (Baja Prioridad)

### 17. Implementar Tests

#### Tests Unitarios para ViewModels

**test/viewmodel/ExpenseViewModelTest.kt**
```kotlin
@HiltAndroidTest
class ExpenseViewModelTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var viewModel: ExpenseViewModel
    private lateinit var repository: FakeDailyExpenseRepository
    
    @Before
    fun setup() {
        hiltRule.inject()
        repository = FakeDailyExpenseRepository()
        viewModel = ExpenseViewModel(repository)
    }
    
    @Test
    fun `when amount is valid, isAmountValid is true`() = runTest {
        viewModel.onAmountChange("100.50")
        
        assertTrue(viewModel.isAmountValid.value)
    }
    
    @Test
    fun `when amount is invalid, isAmountValid is false`() = runTest {
        viewModel.onAmountChange("-10")
        
        assertFalse(viewModel.isAmountValid.value)
    }
    
    @Test
    fun `when registering expense with valid data, expense is saved`() = runTest {
        viewModel.onAmountChange("100.50")
        viewModel.onCategoryChange("Comida")
        viewModel.onOriginChange("Nomina")
        viewModel.onNoteChange("Test note")
        
        viewModel.registerExpense()
        
        val expenses = repository.getAllExpenses()
        assertEquals(1, expenses.size)
        assertEquals(100.50, expenses[0].amount, 0.01)
        assertEquals("Comida", expenses[0].category)
    }
    
    @Test
    fun `when registering expense with invalid data, error is shown`() = runTest {
        viewModel.onAmountChange("")
        viewModel.onCategoryChange("")
        
        viewModel.registerExpense()
        
        assertNotNull(viewModel.errorMessage.value)
    }
}
```

#### Fake Repository para Tests
```kotlin
class FakeDailyExpenseRepository : DailyExpenseRepository {
    private val expenses = mutableListOf<DailyExpense>()
    
    override suspend fun insert(expense: DailyExpense) {
        expenses.add(expense.copy(id = expenses.size + 1))
    }
    
    override suspend fun getAllExpenses(): List<DailyExpense> = expenses
    
    override suspend fun getExpenseById(id: Int): DailyExpense? {
        return expenses.find { it.id == id }
    }
    
    override suspend fun delete(expense: DailyExpense) {
        expenses.remove(expense)
    }
    
    override suspend fun update(expense: DailyExpense) {
        val index = expenses.indexOfFirst { it.id == expense.id }
        if (index != -1) {
            expenses[index] = expense
        }
    }
}
```

#### Tests de Integración para Repository

**androidTest/data/DailyExpenseRepositoryTest.kt**
```kotlin
@RunWith(AndroidJUnit4::class)
class DailyExpenseRepositoryTest {
    
    private lateinit var database: AppDatabase
    private lateinit var repository: DailyExpenseRepository
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = DailyExpenseRepository(database.dailyExpenseDao())
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun insertExpense_retrievesExpense() = runBlocking {
        val expense = DailyExpense(
            amount = 100.0,
            category = "Test",
            date = System.currentTimeMillis(),
            origin = "Nomina"
        )
        
        repository.insert(expense)
        val expenses = repository.getAllExpenses()
        
        assertEquals(1, expenses.size)
        assertEquals("Test", expenses[0].category)
    }
}
```

#### Tests UI con Compose

**androidTest/ui/HomeScreenTest.kt**
```kotlin
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun homeScreen_displaysAllButtons() {
        composeTestRule.setContent {
            ApplicationTheme {
                HomeScreen(
                    onCigaretteClick = {},
                    onBeerClick = {},
                    onFoodClick = {},
                    onMoneyClick = {},
                    onViewRecordsClick = {},
                    onViewExpensesClick = {},
                    onDeleteAllClick = {}
                )
            }
        }
        
        composeTestRule.onNodeWithText("Cigarette").assertIsDisplayed()
        composeTestRule.onNodeWithText("Beer").assertIsDisplayed()
        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
        composeTestRule.onNodeWithText("Money").assertIsDisplayed()
    }
    
    @Test
    fun homeScreen_clickCigaretteButton_triggersCallback() {
        var clicked = false
        
        composeTestRule.setContent {
            ApplicationTheme {
                HomeScreen(
                    onCigaretteClick = { clicked = true },
                    onBeerClick = {},
                    onFoodClick = {},
                    onMoneyClick = {},
                    onViewRecordsClick = {},
                    onViewExpensesClick = {},
                    onDeleteAllClick = {}
                )
            }
        }
        
        composeTestRule.onNodeWithText("Cigarette").performClick()
        assertTrue(clicked)
    }
}
```

---

### 18. Agregar Encriptación para Datos Sensibles

**build.gradle.kts**
```kotlin
dependencies {
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation("androidx.sqlite:sqlite:2.4.0")
}
```

**AppDatabase.kt**
```kotlin
companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null
    
    // Generar o recuperar passphrase de forma segura
    private fun getPassphrase(context: Context): ByteArray {
        // Usar Android Keystore para almacenar la passphrase de forma segura
        // Ejemplo simplificado - implementar con Android Keystore en producción
        return "your-secure-passphrase".toByteArray()
    }
    
    fun getInstance(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }
    }
    
    private fun buildDatabase(context: Context): AppDatabase {
        val passphrase = getPassphrase(context)
        val factory = SupportFactory(passphrase)
        
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app_database"
        )
        .openHelperFactory(factory)
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
        .build()
    }
}
```

**Implementación segura con Android Keystore:**
```kotlin
object SecureKeystore {
    private const val KEYSTORE_ALIAS = "DailyBalanceKey"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    
    fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        
        return if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
            (keyStore.getEntry(KEYSTORE_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        } else {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
            
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            )
            
            keyGenerator.generateKey()
        }
    }
    
    fun getDatabasePassphrase(): ByteArray {
        // Generar passphrase usando la clave del Keystore
        val key = getOrCreateKey()
        return key.encoded
    }
}
```

---

### 19. Renombrar Paquete

**Proceso para renombrar de `com.example.myfristapplication` a `com.dailybalance.app`:**

1. **En Android Studio:**
   - Click derecho en el paquete base → Refactor → Rename
   - Cambiar a `com.dailybalance.app`
   - Seleccionar "Search in comments and strings"
   - Refactor

2. **Actualizar build.gradle.kts:**
```kotlin
android {
    namespace = "com.dailybalance.app"
    
    defaultConfig {
        applicationId = "com.dailybalance.app"
        // ...
    }
}
```

3. **Actualizar AndroidManifest.xml:**
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.dailybalance.app">
    
    <application
        android:name=".MyApplication"
        ...>
    </application>
</manifest>
```

4. **Actualizar schemas de Room:**
   - Mover/renombrar carpeta en `app/schemas/`
   - De: `com.example.myfristapplication.data.AppDatabase/`
   - A: `com.dailybalance.app.data.AppDatabase/`

5. **Sincronizar y limpiar:**
```bash
./gradlew clean
./gradlew build
```

---

### 20. Agregar Documentación de Código

**KDoc para clases y funciones públicas:**

```kotlin
/**
 * ViewModel para gestionar el estado y la lógica de negocio de los gastos diarios.
 * 
 * Maneja la validación de entrada, el registro de gastos y la exportación de datos.
 * 
 * @property repository Repositorio para operaciones CRUD de gastos diarios
 */
@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: DailyExpenseRepository
) : ViewModel() {
    
    /**
     * Registra un nuevo gasto diario en la base de datos.
     * 
     * Valida que todos los campos requeridos estén completos y que el monto sea válido
     * antes de guardar. Si la validación falla, establece un mensaje de error.
     * 
     * @throws SQLException si hay un error al guardar en la base de datos
     */
    fun registerExpense() {
        // implementación
    }
}
```

---

## 📋 Plan de Refactorización Recomendado

### **Fase 1: Fundamentos (1-2 semanas)**

**Prioridad:** 🔴 Crítica

1. ✅ **Agregar manejo de errores global**
   - Implementar try-catch en todas las operaciones de BD
   - Agregar try-catch en operaciones de archivo
   - Crear UiState sealed class
   - Tiempo estimado: 2-3 días

2. ✅ **Mover strings a resources**
   - Crear `strings.xml` completo
   - Refactorizar todas las pantallas para usar `stringResource()`
   - Tiempo estimado: 1-2 días

3. ✅ **Inyectar ThemePreferences con Hilt**
   - Crear provider en AppModule
   - Refactorizar ThemeViewModel
   - Tiempo estimado: 1 día

4. ✅ **Eliminar withContext redundante**
   - Refactorizar ActionRecordRepository
   - Refactorizar DailyExpenseRepository
   - Tiempo estimado: 0.5 días

5. ✅ **Crear utilidades comunes**
   - DateFormatter
   - CsvExporter
   - AmountValidator
   - Tiempo estimado: 1 día

**Total Fase 1:** 5.5-7.5 días

---

### **Fase 2: Arquitectura (2-3 semanas)**

**Prioridad:** 🟡 Alta

6. ✅ **Refactorizar MainViewModel**
   - Crear NavigationViewModel
   - Crear MessageViewModel (o usar eventos)
   - Eliminar código duplicado
   - Tiempo estimado: 3-4 días

7. ✅ **Implementar Navigation Component**
   - Agregar dependencias
   - Crear rutas con sealed class o @Serializable
   - Refactorizar MainActivity
   - Actualizar todas las pantallas
   - Tiempo estimado: 4-5 días

8. ✅ **Mover lógica de exportación a ViewModels**
   - Refactorizar exportación de gastos
   - Refactorizar exportación de registros
   - Implementar estados de exportación
   - Tiempo estimado: 2 días

9. ✅ **Dividir composables grandes**
   - Refactorizar DailyExpenseScreen
   - Refactorizar ExpenseRecordsScreen
   - Refactorizar MainApp
   - Tiempo estimado: 3-4 días

**Total Fase 2:** 12-15 días

---

### **Fase 3: Calidad (1-2 semanas)**

**Prioridad:** 🟢 Media

10. ✅ **Implementar tests unitarios**
    - Tests para todos los ViewModels
    - Crear FakeRepositories
    - Cobertura objetivo: 70%+
    - Tiempo estimado: 4-5 días

11. ✅ **Implementar tests de integración**
    - Tests para Repositories
    - Tests de migraciones de BD
    - Tiempo estimado: 2-3 días

12. ✅ **Implementar UI tests**
    - Tests para pantallas principales
    - Tests de navegación
    - Tiempo estimado: 2-3 días

13. ✅ **Agregar Timber logging**
    - Configurar Timber
    - Agregar logs en puntos clave
    - Tiempo estimado: 1 día

14. ✅ **Mejorar double-check locking en AppDatabase**
    - Aplicar patrón correcto
    - Tiempo estimado: 0.5 días

15. ✅ **Usar collectAsStateWithLifecycle**
    - Refactorizar MainActivity
    - Tiempo estimado: 0.5 días

**Total Fase 3:** 9-12 días

---

### **Fase 4: Mejoras Opcionales (1 semana)**

**Prioridad:** ⚪ Baja

16. ✅ **Renombrar paquete**
    - Refactorizar estructura
    - Actualizar configuraciones
    - Tiempo estimado: 0.5 días

17. ✅ **Agregar encriptación**
    - Implementar SQLCipher
    - Configurar Android Keystore
    - Tiempo estimado: 2 días

18. ✅ **Migrar de SystemUiController**
    - Usar WindowCompat
    - Eliminar dependencia Accompanist
    - Tiempo estimado: 0.5 días

19. ✅ **Agregar documentación de código**
    - KDoc para clases públicas
    - Comentarios en lógica compleja
    - Tiempo estimado: 1-2 días

**Total Fase 4:** 4-5 días

---

## 🎯 Métricas de Calidad

### Estado Actual vs Objetivo

| Métrica | Actual | Objetivo | Fase |
|---------|--------|----------|------|
| **Cobertura de Tests** | 0% | 80%+ | Fase 3 |
| **Strings Hardcodeadas** | 59 | 0 | Fase 1 |
| **Clases > 150 líneas** | 3 | 0 | Fase 2 |
| **Funciones > 50 líneas** | 5 | 0 | Fase 2 |
| **Código Duplicado** | Alto | Bajo | Fase 1-2 |
| **Manejo de Errores** | Ausente | Completo | Fase 1 |
| **Type Safety (Navegación)** | No | Sí | Fase 2 |
| **Inyección de Dependencias** | 83% | 100% | Fase 1 |
| **Logging** | No | Sí | Fase 3 |
| **Documentación** | Mínima | Completa | Fase 4 |

---

## 📈 Seguimiento de Progreso

### Checklist de Implementación

#### 🔴 Crítico
- [ ] Manejo de errores en operaciones de BD
- [ ] Manejo de errores en operaciones de archivo
- [ ] Strings movidos a resources
- [ ] ThemeViewModel con Hilt
- [ ] Eliminar withContext redundante
- [ ] Crear utilidades (DateFormatter, CsvExporter, AmountValidator)

#### 🟡 Alta Prioridad
- [ ] Refactorizar MainViewModel
- [ ] Implementar Navigation Component
- [ ] Type-safe navigation
- [ ] Mover lógica de exportación a ViewModels
- [ ] Dividir DailyExpenseScreen
- [ ] Dividir ExpenseRecordsScreen
- [ ] Dividir MainApp function

#### 🟢 Media Prioridad
- [ ] Tests unitarios para ViewModels
- [ ] Tests de integración para Repositories
- [ ] UI tests para Composables
- [ ] Agregar Timber logging
- [ ] Mejorar double-check locking en AppDatabase
- [ ] Usar collectAsStateWithLifecycle
- [ ] Migrar de SystemUiController a WindowCompat

#### ⚪ Baja Prioridad
- [ ] Renombrar paquete (MyFristApplication → com.dailybalance.app)
- [ ] Agregar encriptación (SQLCipher)
- [ ] Documentación KDoc completa
- [ ] Configurar ProGuard para release

---

## 🔗 Referencias y Recursos

### Documentación Oficial
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-guide.html)

### Mejores Prácticas
- [Guide to app architecture](https://developer.android.com/topic/architecture)
- [Android testing guide](https://developer.android.com/training/testing)
- [Kotlin style guide](https://developer.android.com/kotlin/style-guide)

### Herramientas
- [Timber](https://github.com/JakeWharton/timber)
- [SQLCipher for Android](https://github.com/sqlcipher/android-database-sqlcipher)

---

**Última actualización:** Enero 2026  
**Versión del documento:** 1.0  
**Próxima revisión:** Después de completar Fase 1
