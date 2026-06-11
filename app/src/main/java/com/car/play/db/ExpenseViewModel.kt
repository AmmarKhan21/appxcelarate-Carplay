package com.car.play.android.app.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val expenseDao = AppDatabase.getDatabase(application).expenseDao()
    val allExpenses: LiveData<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val totalExpenses: LiveData<Double?> = expenseDao.getTotalExpenses()
    val expenseSummary: LiveData<List<ExpenseSummary>> = expenseDao.getExpenseSummary()

    fun addExpense(title: String, amount: Double, category: String, date: String, notes: String, receiptPath: String) {
        viewModelScope.launch {
            expenseDao.insert(ExpenseEntity(title = title, amount = amount, category = category, date = date, notes = notes, receiptPath = receiptPath))
        }
    }
    fun deleteExpense(expense: ExpenseEntity) { viewModelScope.launch { expenseDao.delete(expense) } }
}
