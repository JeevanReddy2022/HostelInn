package com.example.hostelmanager.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.repository.ExpenseRepository
import com.example.hostelmanager.data.repository.HostelRepository
import com.example.hostelmanager.data.repository.PaymentRepository
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.data.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class DashboardStats(
    val hostelName: String,
    val checkInsThisMonth: Int,
    val paymentsThisMonth: Int,
    val rentCollectedThisMonth: Long,
    val activeResidents: Int,
    val totalBeds: Int,
    val availableBeds: Int,
    val expensesLastMonth: Long,
    val expensesToday: Long,
    val expensesThisMonth: Long,
    val incomeLastMonth: Long,
    val incomeToday: Long,
    val incomeThisMonth: Long,
    val profitLastMonth: Long,
    val profitThisMonth: Long,
    val profitYTD: Long
)

private data class HostelOverview(
    val hostelName: String,
    val activeResidents: Int,
    val totalBeds: Int,
    val checkInsThisMonth: Int
)

private data class PaymentSummary(
    val paymentsThisMonth: Int,
    val rentCollectedThisMonth: Long,
    val incomeToday: Long,
    val incomeLastMonth: Long,
    val incomeYTD: Long
)

private data class ExpenseSummary(
    val expensesToday: Long,
    val expensesThisMonth: Long,
    val expensesLastMonth: Long,
    val expensesYTD: Long
)

class DashboardViewModel(
    private val hostelRepository: HostelRepository,
    private val residentRepository: ResidentRepository,
    private val roomRepository: RoomRepository,
    private val paymentRepository: PaymentRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _hostelId = MutableStateFlow("")

    private val today = LocalDate.now()
    private val currentMonth = YearMonth.now()
    private val lastMonth = currentMonth.minusMonths(1)
    private val yearStart = LocalDate.of(currentMonth.year, 1, 1)

    private val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
    private val thisMonthPattern = "${currentMonth.year}-${String.format("%02d", currentMonth.monthValue)}%"
    private val lastMonthPattern = "${lastMonth.year}-${String.format("%02d", lastMonth.monthValue)}%"
    private val yearStartStr = yearStart.format(DateTimeFormatter.ISO_LOCAL_DATE)
    private val lastMonthStartStr = "${lastMonth.year}-${String.format("%02d", lastMonth.monthValue)}-01"
    private val lastMonthEndStr = lastMonth.atEndOfMonth().format(DateTimeFormatter.ISO_LOCAL_DATE)

    val stats: StateFlow<DashboardStats?> = _hostelId.flatMapLatest { hostelId ->
        if (hostelId.isEmpty()) {
            flowOf(null)
        } else {
            val hostelOverviewFlow = combine(
                hostelRepository.getHostelById(hostelId),
                hostelRepository.getActiveResidentsCount(hostelId),
                hostelRepository.getTotalBedsForHostel(hostelId),
                hostelRepository.getCheckInsCount(hostelId, thisMonthPattern)
            ) { hostel, activeResidents, totalBeds, checkIns ->
                HostelOverview(
                    hostelName = hostel?.name ?: "Unknown",
                    activeResidents = activeResidents,
                    totalBeds = totalBeds,
                    checkInsThisMonth = checkIns
                )
            }

            val paymentSummaryFlow = combine(
                paymentRepository.getPaymentsCountForMonth(hostelId, thisMonthPattern),
                paymentRepository.getTotalRentCollected(hostelId, thisMonthPattern),
                paymentRepository.getTotalPaymentsForDate(hostelId, todayStr),
                paymentRepository.getTotalPaymentsBetween(hostelId, lastMonthStartStr, lastMonthEndStr),
                paymentRepository.getTotalPaymentsBetween(hostelId, yearStartStr, todayStr)
            ) { count, rentCollected, todayIncome, lastMonthIncome, ytdIncome ->
                PaymentSummary(
                    paymentsThisMonth = count,
                    rentCollectedThisMonth = rentCollected,
                    incomeToday = todayIncome,
                    incomeLastMonth = lastMonthIncome,
                    incomeYTD = ytdIncome
                )
            }

            val expenseSummaryFlow = combine(
                expenseRepository.getTotalExpensesForDate(hostelId, todayStr),
                expenseRepository.getTotalExpensesForMonth(hostelId, thisMonthPattern),
                expenseRepository.getTotalExpensesForMonth(hostelId, lastMonthPattern),
                expenseRepository.getTotalExpensesBetween(hostelId, yearStartStr, todayStr)
            ) { todayExpenses, thisMonthExpenses, lastMonthExpenses, ytdExpenses ->
                ExpenseSummary(
                    expensesToday = todayExpenses,
                    expensesThisMonth = thisMonthExpenses,
                    expensesLastMonth = lastMonthExpenses,
                    expensesYTD = ytdExpenses
                )
            }

            combine(
                hostelOverviewFlow,
                paymentSummaryFlow,
                expenseSummaryFlow
            ) { overview, payments, expenses ->
                DashboardStats(
                    hostelName = overview.hostelName,
                    checkInsThisMonth = overview.checkInsThisMonth,
                    paymentsThisMonth = payments.paymentsThisMonth,
                    rentCollectedThisMonth = payments.rentCollectedThisMonth,
                    activeResidents = overview.activeResidents,
                    totalBeds = overview.totalBeds,
                    availableBeds = overview.totalBeds - overview.activeResidents,
                    expensesLastMonth = expenses.expensesLastMonth,
                    expensesToday = expenses.expensesToday,
                    expensesThisMonth = expenses.expensesThisMonth,
                    incomeLastMonth = payments.incomeLastMonth,
                    incomeToday = payments.incomeToday,
                    incomeThisMonth = payments.rentCollectedThisMonth,
                    profitLastMonth = payments.incomeLastMonth - expenses.expensesLastMonth,
                    profitThisMonth = payments.rentCollectedThisMonth - expenses.expensesThisMonth,
                    profitYTD = payments.incomeYTD - expenses.expensesYTD
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setHostelId(id: String) {
        _hostelId.value = id
    }
}
