package com.dailybalance.app

import com.dailybalance.app.data.ActionRecord
import com.dailybalance.app.data.ActionRecordRepository
import com.dailybalance.app.data.DailyExpenseRepository
import com.dailybalance.app.fakes.FakeActionRecordDao
import com.dailybalance.app.fakes.FakeDailyExpenseDao
import com.dailybalance.app.testutil.MainDispatcherRule
import com.dailybalance.app.testutil.drain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `setDailyExpenseAmountText valida positivos y resetea error`() = runTest {
        val vm = MainViewModel(
            actionRecordRepository = ActionRecordRepository(FakeActionRecordDao()),
            dailyExpenseRepository = DailyExpenseRepository(FakeDailyExpenseDao())
        )

        vm.setDailyExpenseAmountText("10.5")
        assertTrue(vm.isAmountValid.value)
        assertFalse(vm.showExpenseError.value)

        vm.setDailyExpenseAmountText("0")
        assertFalse(vm.isAmountValid.value)

        vm.setDailyExpenseAmountText("abc")
        assertFalse(vm.isAmountValid.value)
    }

    @Test
    fun `registerExpense valido inserta y vuelve a home`() = runTest {
        val expenseDao = FakeDailyExpenseDao()
        val vm = MainViewModel(
            actionRecordRepository = ActionRecordRepository(FakeActionRecordDao()),
            dailyExpenseRepository = DailyExpenseRepository(expenseDao)
        )

        vm.navigateTo("expense")
        vm.setDailyExpenseAmountText("5")
        vm.setDailyExpenseCategory("Comida")
        vm.setDailyExpenseOrigin("Efectivo")

        vm.registerExpense()
        drain()

        assertEquals("home", vm.currentScreen.value)
        assertEquals("", vm.dailyExpenseAmountText.value)
        assertEquals("", vm.dailyExpenseCategory.value)
        assertEquals("", vm.dailyExpenseOrigin.value)
        assertFalse(vm.showExpenseError.value)

        assertEquals(1, expenseDao.expenses.size)
        val inserted = expenseDao.expenses.single()
        assertEquals(5.0, inserted.amount, 0.0001)
        assertEquals("Comida", inserted.category)
        assertEquals("Efectivo", inserted.origin)
    }

    @Test
    fun `registerExpense invalido muestra error y no inserta`() = runTest {
        val expenseDao = FakeDailyExpenseDao()
        val vm = MainViewModel(
            actionRecordRepository = ActionRecordRepository(FakeActionRecordDao()),
            dailyExpenseRepository = DailyExpenseRepository(expenseDao)
        )

        vm.setDailyExpenseAmountText("0")
        vm.setDailyExpenseCategory("")
        vm.setDailyExpenseOrigin("")

        vm.registerExpense()
        drain()

        assertTrue(vm.showExpenseError.value)
        assertEquals(0, expenseDao.expenses.size)
    }

    @Test
    fun `deleteAllRecords limpia repo y estado`() = runTest {
        val actionDao = FakeActionRecordDao().apply {
            records.add(ActionRecord(type = "beer", timestamp = 1L, description = null))
        }

        val vm = MainViewModel(
            actionRecordRepository = ActionRecordRepository(actionDao),
            dailyExpenseRepository = DailyExpenseRepository(FakeDailyExpenseDao())
        )

        vm.requestRecords()
        drain()
        assertEquals(1, vm.records.value.size)

        vm.deleteAllRecords()
        drain()

        assertEquals(0, actionDao.records.size)
        assertEquals(0, vm.records.value.size)
    }

    @Test
    fun `export events se alternan`() = runTest {
        val vm = MainViewModel(
            actionRecordRepository = ActionRecordRepository(FakeActionRecordDao()),
            dailyExpenseRepository = DailyExpenseRepository(FakeDailyExpenseDao())
        )

        assertFalse(vm.exportExpensesEvent.value)
        vm.exportExpensesRequested()
        assertTrue(vm.exportExpensesEvent.value)
        vm.exportExpensesHandled()
        assertFalse(vm.exportExpensesEvent.value)

        assertFalse(vm.exportRecordsEvent.value)
        vm.exportRecordsRequested()
        assertTrue(vm.exportRecordsEvent.value)
        vm.exportRecordsHandled()
        assertFalse(vm.exportRecordsEvent.value)
    }
}
