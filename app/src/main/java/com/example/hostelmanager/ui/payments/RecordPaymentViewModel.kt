package com.example.hostelmanager.ui.payments

import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.Payment
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.data.repository.PaymentRepository
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.domain.Balance
import com.example.hostelmanager.ui.BaseHostelViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class RecordPaymentViewModel(
    private val residentRepository: ResidentRepository,
    private val paymentRepository: PaymentRepository
) : BaseHostelViewModel() {

    private val activeResidents: StateFlow<List<ResidentWithRoom>> =
        hostelFlow(emptyList()) { id -> residentRepository.getActiveResidentsByHostel(id) }

    private val pastResidents: StateFlow<List<ResidentWithRoom>> =
        hostelFlow(emptyList()) { id -> residentRepository.getPastResidentsByHostel(id) }

    // ✅ Active + checked-out. A person who still owes money must be selectable.
    val residents: StateFlow<List<ResidentWithRoom>> =
        combine(activeResidents, pastResidents) { active, past ->
            (active + past).sortedBy { it.person.name }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Who has already checked out — used to label the card honestly. */
    val checkedOutPersonIds: StateFlow<Set<String>> =
        pastResidents.map { list -> list.map { it.person.id }.toSet() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    /** Ledger balance for ANY stay, active or closed. Negative = credit. */
    suspend fun balanceForStay(stayId: String): Long {
        if (stayId.isEmpty()) return 0L
        val stay = residentRepository.getStayById(stayId) ?: return 0L
        val payments = paymentRepository.getPaymentsForStayOnce(stayId)
        return Balance.balancePaise(stay, payments, Balance.referenceDateFor(stay, LocalDate.now()))
    }

    suspend fun recordPayment(
        personId: String,
        stayId: String,
        amountPaise: Long,
        mode: String,
        paymentDate: String,
        recordedDate: String,
        note: String
    ): Boolean {
        if (personId.isEmpty() || stayId.isEmpty()) return false
        if (paymentDate.isEmpty() || recordedDate.isEmpty()) return false
        if (amountPaise < 0) return false

        // ✅ Works for closed stays too — no more silent failure after checkout.
        val stay = residentRepository.getStayById(stayId) ?: return false
        if (stay.personId != personId) return false

        val payments = paymentRepository.getPaymentsForStayOnce(stayId)
        val balance = Balance.balancePaise(
            stay, payments, Balance.referenceDateFor(stay, LocalDate.now())
        )

        val waivedPaise = if (amountPaise == 0L) {
            if (note.trim().length < 5) return false
            if (balance <= 0) return false
            balance
        } else {
            0L
        }

        paymentRepository.insertPayment(
            Payment(
                stayId = stay.id,
                personId = personId,
                hostelId = stay.hostelId,
                amountPaise = amountPaise,
                mode = mode,
                paymentDate = paymentDate,
                date = recordedDate,
                note = note.trim(),
                waivedPaise = waivedPaise
            )
        )
        return true
    }
}