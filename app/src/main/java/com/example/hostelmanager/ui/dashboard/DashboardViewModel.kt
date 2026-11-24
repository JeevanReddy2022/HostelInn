package com.example.hostelmanager.ui.dashboard

import com.example.hostelmanager.data.monthLikePattern
import com.example.hostelmanager.data.repository.ExpenseRepository
import com.example.hostelmanager.data.repository.HostelRepository
import com.example.hostelmanager.data.repository.PaymentRepository
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.data.repository.RoomRepository
import com.example.hostelmanager.ui.BaseHostelViewModel
import com.example.hostelmanager.util.toIsoMonth
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
) : BaseHostelViewModel() {

    private val today = LocalDate.now()
    private val currentMonth = YearMonth.now()
    private val lastMonth = currentMonth.minusMonths(1)
    private val yearStart = LocalDate.of(currentMonth.year, 1, 1)

    private val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
    private val thisMonthPattern = currentMonth.monthLikePattern()
    private val lastMonthPattern = lastMonth.monthLikePattern()
    private val yearStartStr = yearStart.format(DateTimeFormatter.ISO_LOCAL_DATE)
    private val lastMonthStartStr = "${lastMonth.toIsoMonth()}-01"
    private val lastMonthEndStr = lastMonth.atEndOfMonth().format(DateTimeFormatter.ISO_LOCAL_DATE)

    val stats: StateFlow<DashboardStats?> = hostelFlow<DashboardStats?>(initialValue = null) { hostelId ->
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
}
