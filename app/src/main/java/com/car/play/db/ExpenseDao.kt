package com.car.play.android.app.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ExpenseDao {
    @Insert suspend fun insert(expense: ExpenseEntity)
    @Update suspend fun update(expense: ExpenseEntity)
    @Delete suspend fun delete(expense: ExpenseEntity)
    @Query("SELECT * FROM expenses ORDER BY date DESC") fun getAllExpenses(): LiveData<List<ExpenseEntity>>
    @Query("SELECT SUM(amount) FROM expenses") fun getTotalExpenses(): LiveData<Double?>
    @Query("SELECT SUM(amount) FROM expenses WHERE category = :category") fun getExpensesByCategory(category: String): LiveData<Double?>
    @Query("SELECT category, SUM(amount) as total FROM expenses GROUP BY category") fun getExpenseSummary(): LiveData<List<ExpenseSummary>>
}

data class ExpenseSummary(val category: String, val total: Double)
