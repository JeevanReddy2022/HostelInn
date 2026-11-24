package com.example.hostelmanager.ui.payments

import com.example.hostelmanager.data.repository.PaymentRepository
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.domain.BillingCycle
import com.example.hostelmanager.ui.BaseHostelViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class YetToPayResident(
    val personId: String,
    val name: String,
    val phone: String,
    val profilePhotoPath: String,
    val roomNumber: String,
    val monthlyRent: Long,
    val cycleStart: String,
    val cycleEnd: String,
    val isCheckedOut: Boolean = false
)

class PaymentsViewModel(
    private val paymentRepository: PaymentRepository,
    private val residentRepository: ResidentRepository
) : BaseHostelViewModel() {

    val residents: StateFlow<List<YetToPayResident>> = hostelFlow(emptyList<YetToPayResident>()) { hostelId ->
        combine(
            residentRepository.getAllResidentsWithLatestStay(hostelId),
            paymentRepository.getAllPaymentsForHostel(hostelId)
        ) { residents, allPayments ->
            val today = LocalDate.now()
            val result = mutableListOf<YetToPayResident>()

            for (resident in residents) {
                val anchorDay = resident.billingAnchorDay.coerceIn(1, 31)

                val cycleStart: LocalDate = if (resident.isActive) {
                    BillingCycle.cycleStart(today, anchorDay)
                } else {
                    val checkoutDate = resident.checkoutDate?.let {
                        LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
                    } ?: continue
                    BillingCycle.cycleStart(checkoutDate, anchorDay)
                }
                val cycleEnd = cycleStart.plusMonths(1).minusDays(1)

                val cycleStartStr = cycleStart.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val cycleEndStr = cycleEnd.format(DateTimeFormatter.ISO_LOCAL_DATE)

                val hasPaymentInCycle = allPayments.any { p ->
                    p.stayId == resident.stayId &&
                            p.paymentDate >= cycleStartStr &&
                            p.paymentDate <= cycleEndStr
                }

                if (!hasPaymentInCycle) {
                    result.add(
                        YetToPayResident(
                            personId = resident.personId,
                            name = resident.name,
                            phone = resident.phone,
                            profilePhotoPath = resident.profilePhotoPath,
                            roomNumber = resident.roomNumber,
                            monthlyRent = resident.monthlyRent,
                            cycleStart = cycleStartStr,
                            cycleEnd = cycleEndStr,
                            isCheckedOut = !resident.isActive
                        )
                    )
                }
            }

            result.sortedBy { it.name }
        }
    }
}
