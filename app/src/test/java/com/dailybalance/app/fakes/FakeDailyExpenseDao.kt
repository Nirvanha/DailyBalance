package com.dailybalance.app.fakes

import com.dailybalance.app.data.DailyExpense
import com.dailybalance.app.data.DailyExpenseDao

/** DAO fake minimalista en memoria para tests unitarios. */
class FakeDailyExpenseDao : DailyExpenseDao {
    val expenses = mutableListOf<DailyExpense>()
    private var nextId = 1

    override suspend fun insert(dailyExpense: DailyExpense): Long {
        val withId = dailyExpense.copy(id = nextId++)
        expenses.add(withId)
        return withId.id.toLong()
    }

    override suspend fun update(dailyExpense: DailyExpense) {
        val index = expenses.indexOfFirst { it.id == dailyExpense.id }
        if (index >= 0) expenses[index] = dailyExpense
    }

    override suspend fun delete(dailyExpense: DailyExpense) {
        expenses.removeAll { it.id == dailyExpense.id }
    }

    override suspend fun getAll(): List<DailyExpense> = expenses.sortedByDescending { it.date }

    override suspend fun getById(id: Int): DailyExpense? = expenses.firstOrNull { it.id == id }
}
