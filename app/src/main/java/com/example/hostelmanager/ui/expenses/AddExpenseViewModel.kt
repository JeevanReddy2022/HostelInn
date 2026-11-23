package com.example.hostelmanager.ui.expenses

import androidx.lifecycle.ViewModel
import com.example.hostelmanager.data.Expense
import com.example.hostelmanager.data.repository.ExpenseRepository

class AddExpenseViewModel(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    suspend fun addExpense(
        hostelId: String,
        expenseDate: String,
        enteredDate: String,
        category: String,
        amountPaise: Long,
        note: String,
        receiptImagePath: String?
    ): Boolean {
        if (hostelId.isEmpty() || expenseDate.isEmpty() || enteredDate.isEmpty() ||
            category.isEmpty() || amountPaise <= 0) return false

        expenseRepository.insertExpense(
            Expense(
                hostelId = hostelId,
                expenseDate = expenseDate,
                enteredDate = enteredDate,
                category = category,
                amountPaise = amountPaise,
                note = note,
                receiptImagePath = receiptImagePath
            )
        )
        return true
    }
}
