package com.example.hostelmanager.data.repository

import com.example.hostelmanager.data.Expense
import com.example.hostelmanager.data.ExpenseDao
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getAllExpensesForHostel(hostelId: String): Flow<List<Expense>>
    fun getExpensesBetween(hostelId: String, startDate: String, endDate: String): Flow<List<Expense>>
    fun getTotalExpensesForHostel(hostelId: String): Flow<Long>
    fun getExpensesCountForMonth(hostelId: String, monthPattern: String): Flow<Int>
    fun getTotalExpensesForMonth(hostelId: String, monthPattern: String): Flow<Long>
    fun getTotalExpensesForDate(hostelId: String, date: String): Flow<Long>
    fun getTotalExpensesBetween(hostelId: String, startDate: String, endDate: String): Flow<Long>
    suspend fun insertExpense(expense: Expense)
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
}

class ExpenseRepositoryImpl(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {
    override fun getAllExpensesForHostel(hostelId: String): Flow<List<Expense>> = expenseDao.getAllExpensesForHostel(hostelId)
    override fun getExpensesBetween(hostelId: String, startDate: String, endDate: String): Flow<List<Expense>> = expenseDao.getExpensesBetween(hostelId, startDate, endDate)
    override fun getTotalExpensesForHostel(hostelId: String): Flow<Long> = expenseDao.getTotalExpensesForHostel(hostelId)
    override fun getExpensesCountForMonth(hostelId: String, monthPattern: String): Flow<Int> = expenseDao.getExpensesCountForMonth(hostelId, monthPattern)
    override fun getTotalExpensesForMonth(hostelId: String, monthPattern: String): Flow<Long> = expenseDao.getTotalExpensesForMonth(hostelId, monthPattern)
    override fun getTotalExpensesForDate(hostelId: String, date: String): Flow<Long> = expenseDao.getTotalExpensesForDate(hostelId, date)
    override fun getTotalExpensesBetween(hostelId: String, startDate: String, endDate: String): Flow<Long> = expenseDao.getTotalExpensesBetween(hostelId, startDate, endDate)
    override suspend fun insertExpense(expense: Expense) = expenseDao.insertExpense(expense)
    override suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)
    override suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense.id)
}
