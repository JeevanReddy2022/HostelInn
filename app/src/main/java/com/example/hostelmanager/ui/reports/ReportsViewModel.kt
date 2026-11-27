package com.example.hostelmanager.ui.reports

import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.repository.ExpenseRepository
import com.example.hostelmanager.data.repository.HostelRepository
import com.example.hostelmanager.data.repository.PaymentRepository
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.data.repository.RoomRepository
import com.example.hostelmanager.ui.BaseHostelViewModel
import com.example.hostelmanager.util.toIsoMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

data class ExpenseBreakdown(
    val category: String,
    val totalPaise: Long
)

/** One deliberate write-off: who, when, how much, and why. */
data class WriteOffEntry(
    val paymentId: String,
    val personName: String,
    val roomNumber: String,
    val date: String,
    val waivedPaise: Long,
    val reason: String
)

data class MonthlyReport(
    val totalIncomePaise: Long,
    val totalExpensePaise: Long,
    val netProfitPaise: Long,
    val expenseBreakdown: List<ExpenseBreakdown>,
    // ✅ NEW: money deliberately given up this month
    val writeOffPaise: Long = 0,
    val writeOffCount: Int = 0,
    val writeOffs: List<WriteOffEntry> = emptyList()
)

class ReportsViewModel(
    private val hostelRepository: HostelRepository,
    private val roomRepository: RoomRepository,
    private val residentRepository: ResidentRepository,
    private val paymentRepository: PaymentRepository,
    private val expenseRepository: ExpenseRepository
) : BaseHostelViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now().toIsoMonth())
    val selectedMonth: StateFlow<String> = _selectedMonth

    private val emptyReport = MonthlyReport(0, 0, 0, emptyList())

    val report: StateFlow<MonthlyReport> = combine(
        _hostelId,
        _selectedMonth
    ) { hostelId, month ->
        Pair(hostelId, month)
    }.flatMapLatest { (hostelId, month) ->
        if (hostelId.isEmpty()) {
            flowOf(emptyReport)
        } else {
            combine(
                paymentRepository.getAllPaymentsForHostel(hostelId),
                paymentRepository.getAllPaymentsWithDetailsForHostel(hostelId),
                expenseRepository.getAllExpensesForHostel(hostelId)
            ) { payments, paymentsWithDetails, expenses ->

                val monthPayments = payments.filter { it.paymentDate.startsWith(month) }

                // Money totals come from the plain payments table so a deleted
                // room can never hide income. Names/rooms are only enrichment.
                val totalIncome = monthPayments.sumOf { it.amountPaise }
                val detailsById = paymentsWithDetails.associateBy { it.payment.id }

                val writeOffs = monthPayments
                    .filter { it.waivedPaise > 0 }
                    .map { p ->
                        val details = detailsById[p.id]
                        WriteOffEntry(
                            paymentId = p.id,
                            personName = details?.personName ?: "Resident",
                            roomNumber = details?.roomNumber ?: "—",
                            date = p.paymentDate,
                            waivedPaise = p.waivedPaise,
                            reason = p.note.ifBlank { "No reason recorded" }
                        )
                    }
                    .sortedByDescending { it.waivedPaise }

                val monthExpenses = expenses.filter { it.expenseDate.startsWith(month) }
                val totalExpense = monthExpenses.sumOf { it.amountPaise }

                val breakdown = monthExpenses
                    .groupBy { it.category }
                    .map { (category, items) ->
                        ExpenseBreakdown(category, items.sumOf { it.amountPaise })
                    }
                    .sortedByDescending { it.totalPaise }

                MonthlyReport(
                    totalIncomePaise = totalIncome,
                    totalExpensePaise = totalExpense,
                    netProfitPaise = totalIncome - totalExpense,
                    expenseBreakdown = breakdown,
                    writeOffPaise = writeOffs.sumOf { it.waivedPaise },
                    writeOffCount = writeOffs.size,
                    writeOffs = writeOffs
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyReport)

    fun setMonth(month: String) {
        _selectedMonth.value = month
    }

    fun nextMonth() {
        val current = YearMonth.parse(_selectedMonth.value)
        _selectedMonth.value = current.plusMonths(1).toIsoMonth()
    }

    fun previousMonth() {
        val current = YearMonth.parse(_selectedMonth.value)
        _selectedMonth.value = current.minusMonths(1).toIsoMonth()
    }
}