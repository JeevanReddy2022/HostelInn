package com.example.hostelmanager.ui.payments

import com.example.hostelmanager.data.Stay
import com.example.hostelmanager.data.repository.PaymentRepository
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.domain.Balance
import com.example.hostelmanager.domain.BillingCycle
import com.example.hostelmanager.ui.BaseHostelViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

data class YetToPayResident(
    val personId: String,
    val name: String,
    val phone: String,
    val profilePhotoPath: String,
    val roomNumber: String,
    val monthlyRent: Long,
    val balancePaise: Long,      // ✅ real money still owed (arrears included)
    val monthsOwed: Int,         // ✅ how many billing cycles are unpaid
    val cycleStart: String,
    val cycleEnd: String,
    val isCheckedOut: Boolean = false
)

class PaymentsViewModel(
    private val paymentRepository: PaymentRepository,
    private val residentRepository: ResidentRepository
) : BaseHostelViewModel() {

    val residents: StateFlow<List<YetToPayResident>> =
        hostelFlow(emptyList<YetToPayResident>()) { hostelId ->
            combine(
                residentRepository.getAllResidentsWithLatestStay(hostelId),
                paymentRepository.getAllPaymentsForHostel(hostelId)
            ) { residents, allPayments ->
                val today = LocalDate.now()
                val paymentsByStay = allPayments.groupBy { it.stayId }
                val result = mutableListOf<YetToPayResident>()

                for (r in residents) {
                    // Daily guests settle per-day on their own screen, not here.
                    if (r.dailyRent > 0) continue

                    val ref: LocalDate = if (r.isActive) {
                        today
                    } else {
                        Balance.parseDateOrNull(r.checkoutDate) ?: continue
                    }

                    // Temporary Stay view so ALL balance math lives in Balance.kt
                    val stay = Stay(
                        id = r.stayId,
                        personId = r.personId,
                        hostelId = hostelId,
                        roomId = "",
                        joinDate = r.joinDate,
                        checkoutDate = r.checkoutDate,
                        monthlyRent = r.monthlyRent,
                        billingAnchorDay = r.billingAnchorDay,
                        isActive = r.isActive
                    )

                    val paid = paymentsByStay[r.stayId] ?: emptyList()
                    val balance = Balance.balancePaise(stay, paid, ref)

                    // ✅ Only people who actually owe money appear here now.
                    if (balance <= 0) continue

                    val anchor = r.billingAnchorDay.coerceIn(1, 31)
                    val cycleStart = BillingCycle.cycleStart(ref, anchor)
                    val cycleEnd = cycleStart.plusMonths(1).minusDays(1)

                    result.add(
                        YetToPayResident(
                            personId = r.personId,
                            name = r.name,
                            phone = r.phone,
                            profilePhotoPath = r.profilePhotoPath,
                            roomNumber = r.roomNumber,
                            monthlyRent = r.monthlyRent,
                            balancePaise = balance,
                            monthsOwed = Balance.cyclesOwed(r.joinDate, ref, anchor),
                            cycleStart = cycleStart.toString(),
                            cycleEnd = cycleEnd.toString(),
                            isCheckedOut = !r.isActive
                        )
                    )
                }
                result.sortedBy { it.name }
            }
        }
}